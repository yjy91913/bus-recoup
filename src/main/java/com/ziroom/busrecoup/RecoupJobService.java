package com.ziroom.busrecoup;

import com.google.common.base.Preconditions;
import com.ziroom.busrecoup.internal.DateUtils;
import com.ziroom.busrecoup.internal.RecoupJobDao;
import com.ziroom.busrecoup.internal.RecoupJobEntity;
import com.ziroom.busrecoup.internal.SpringContextAware;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecoupJobService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoupJobService.class);
    //默认重试总次数
    public static final int DEFAULT_RetryTotalTimes = 3;
    @Autowired
    private SpringContextAware springContextAware;
    @Autowired
    private RecoupJobDao recoupJobDao;

    /**
     * 根据业务编码判断是否已经存在一个补偿作业
     *
     * @param busCode - 唯一编号由业务保证补偿作业的唯一性(如果需要唯一)
     * @Return true or false
     */
    public boolean exists(String busCode) {
        Preconditions.checkArgument(StringUtils.isNotBlank(busCode), "busCode must be not empty");
        return recoupJobDao.findOneByBusCode(busCode) != null;
    }

    /**
     * 提供在补偿作业执行完毕之前根据busCode修改相关作业信息的可能
     * 注意：该接口只修改startTime、effectTime、retryTotalTimes
     *
     * @param busCode
     * @param startTime       - 作业开始时间,大于0则修改
     * @param effectTime      - 作业截止时间,大于0则修改
     * @param retryTotalTimes - 作业总次数,大于0则修改
     */
    public void updateRecoupJobInfo(String busCode, long startTime, long effectTime, int retryTotalTimes) {
        Preconditions.checkArgument(StringUtils.isNotBlank(busCode), "busCode must be not empty");
        RecoupJobEntity recoupJobEntity = recoupJobDao.findOneByBusCode(busCode);
        if (recoupJobEntity == null) {
            throw new RuntimeException("busCode[" + busCode + "]无法找到对应的补偿作业");
        }
        if (startTime > 0L) {
            recoupJobDao.updateStartTimeById(recoupJobEntity.getId(), startTime);
        }
        if (effectTime > 0L) {
            recoupJobDao.updateEffectTimeById(recoupJobEntity.getId(), effectTime);
        }
        if (retryTotalTimes > 0) {
            recoupJobDao.updateRetryTotalTimesById(recoupJobEntity.getId(), retryTotalTimes);
        }
    }

    /**
     * 将一个辅助业务流程落地，待合适的时机以补偿作业的方式执行。
     * 建议将辅助业务和主业务在一个事物中保存,这样才可保证完整业务流程的最终一致。
     *
     * @param recoupJob - 补偿作业VO,该作业封装一个业务辅助流程执行所需要的数据
     */
    public void saveRecoupJob(com.ziroom.busrecoup.RecoupJob recoupJob) {
        Preconditions.checkNotNull(recoupJob);
        Preconditions.checkArgument(StringUtils.isNotBlank(recoupJob.getJobName()), "jobName must be not empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(recoupJob.getJobClass()), "jobClass must be not empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(recoupJob.getJobDesc()), "jobDesc must be not empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(recoupJob.getJobJsonParam()), "jobJsonParam must be not empty");

        //当前时间大于等于开始时间，立即执行
        boolean immediatelyExecute = DateUtils.formatNow2Long() >= recoupJob.getStartTime();
        if (immediatelyExecute) {
            IRecoup recoupBean = springContextAware.getIRecoupBean(recoupJob.getJobClass());
            if (recoupBean != null) {//插入补偿任务和真正执行补偿任务的作业可能不在一个模块
                try {
                    recoupBean.recoup(recoupJob.getJobJsonParam(), recoupJob.getBusCode());
                    return;//执行成功立即返回
                } catch (Exception e) {
                    LOGGER.error("[通用补偿作业异常]RecoupJobService.saveRecoupJob.recoup error", e);
                }
            }
        }

        RecoupJobEntity recoupJobEntity = new RecoupJobEntity();
        recoupJobEntity.setJobName(recoupJob.getJobName());
        recoupJobEntity.setJobClass(recoupJob.getJobClass());
        recoupJobEntity.setJobDesc(recoupJob.getJobDesc());
        recoupJobEntity.setJobJsonParam(recoupJob.getJobJsonParam());
        recoupJobEntity.setAsync(recoupJob.isAsyncExecute() ? 1 : 0);//1异步执行 0同步执行

        //如果重试总次数和重试截止时间都没有设置，使用默认总次数3次
        if (recoupJob.getRetryTotalTimes() <= 0 && recoupJob.getEffectTime() <= 0L) {
            recoupJobEntity.setRetryTotalTimes(DEFAULT_RetryTotalTimes);
            recoupJobEntity.setEffectTime(0L);
        } else {
            recoupJobEntity.setRetryTotalTimes(recoupJob.getRetryTotalTimes());
            recoupJobEntity.setEffectTime(recoupJob.getEffectTime());
        }

        recoupJobEntity.setFailReason("无");
        recoupJobEntity.setStartTime(recoupJob.getStartTime());
        recoupJobEntity.setCreateTime(DateUtils.formatNow2Long());
        recoupJobEntity.setCompleteTime(0L);
        recoupJobEntity.setRetryCurTimes(0);
        recoupJobEntity.setJobStatus(JobStatusEnum.EXECUTE.getCode());
        recoupJobEntity.setBusCode(recoupJob.getBusCode());
        recoupJobDao.insert(recoupJobEntity);
    }
}
