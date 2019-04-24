package com.ziroom.busrecoup.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.ziroom.busrecoup.IRecoup;
import com.ziroom.busrecoup.JobStatusEnum;
import com.ziroom.busrecoup.NotNeedContinueRecoupException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务执行器
 * Created by zhoutao on 2016/7/25.
 *
 * @lastModify 无视数据库中的同步异步标志，统一走异步执行 2017/2/5
 */
@Component
public class RecoupJobExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoupJobExecuter.class);
    @Autowired
    private com.ziroom.busrecoup.internal.SpringContextAware springContextAware;

    //异步执行补偿任务的线程池 - 暂时8倍cpu核心数
    private ExecutorService asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);
    //异步执行中的补偿任务记录
    private final HashMap<Long/*jobId*/, Boolean> asyncExecutingJobIdMap = Maps.newHashMap();
    //重试次数降级阀值，避免成为僵尸任务占用其他任务的执行线程
    private final int retryTimesThreshold = 5;

    @Autowired
    private com.ziroom.busrecoup.internal.RecoupJobDao recoupJobDao;

    public RecoupJobExecuter() {
    }

    @PostConstruct
    void afterPropertiesSet() {
        //异步任务执行统计
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) asyncExecutorService;
                StringBuilder sb = new StringBuilder("AsyncJobStatisticTask异步补偿任务执行情况统计:");
                sb.append("活跃任务数:").append(pool.getActiveCount());
                sb.append(",当前积压任务数:").append(pool.getQueue().size());
                sb.append(",核心线程数:").append(pool.getCorePoolSize());
                sb.append(",当前线程数:").append(pool.getPoolSize());
                sb.append(",最大线程数:").append(pool.getMaximumPoolSize());
                sb.append(",历史最大线程数:").append(pool.getLargestPoolSize());
                sb.append(",已完成任务数:").append(pool.getCompletedTaskCount());
                sb.append("任务总数:").append(pool.getTaskCount());
                LOGGER.info(sb.toString());
            }
        }, 5000L, 30L, TimeUnit.MINUTES);
    }

    /**
     * @param recoupJobId - 补偿作业id
     */
    public void execute(long recoupJobId) {
        if (addJobIdToExecutingSet(recoupJobId)) {
            asyncExecutorService.execute(new Worker(recoupJobId));
        }
    }

    private void removeJobIdFromExecutingSet(long recoupJobId) {
        synchronized (asyncExecutingJobIdMap) {
            asyncExecutingJobIdMap.remove(recoupJobId);
        }
    }

    private boolean addJobIdToExecutingSet(long recoupJobId) {
        synchronized (asyncExecutingJobIdMap) {
            if (asyncExecutingJobIdMap.containsKey(recoupJobId)) {
                return false;
            }
            asyncExecutingJobIdMap.put(recoupJobId, Boolean.TRUE);
            return true;
        }
    }

    /**
     * 执行补偿逻辑worker
     */
    private final class Worker implements Runnable {
        long recoupJobId;

        public Worker(long recoupJobId) {
            this.recoupJobId = recoupJobId;
        }

        @Override
        public void run() {
            boolean needCallback; //是否需要回调业务
            com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity = null;
            IRecoup recoupBean = null;
            int n = 0; //记录单次循环内重试次数
            while (true) {
                //准备执行一次补偿任务
                long recoupStart = System.currentTimeMillis();
                StringBuilder logSB = new StringBuilder();
                logSB.append("[通用补偿作业 - 单个任务开始执行]One RecoupJobEntity Start").append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                boolean success = false;
                String notNeedContinueRecoupExceptionMessage = "";
                String exceptionMessage = "";

                //开始执行一次补偿业务
                try {
                    recoupJobEntity = recoupJobDao.findSimpleOneById(recoupJobId);
                    if (recoupJobEntity == null) {
                        throw new NotNeedContinueRecoupException("无法从数据库中获取该任务");
                    }

                    //已执行完毕直接退出
                    if (JobStatusEnum.SUCCESSED.getCode().equals(recoupJobEntity.getJobStatus())
                            || JobStatusEnum.FAILED.getCode().equals(recoupJobEntity.getJobStatus())) {
                        removeJobIdFromExecutingSet(recoupJobEntity.getId());
                        return;
                    }

                    logSB.append(recoupJobEntity.toString()).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);

                    //获取执行补偿业务逻辑的类,必须实现IRecoup
                    recoupBean = springContextAware.getIRecoupBean(recoupJobEntity.getJobClass());
                    if (recoupBean == null) {
//                        throw new NotNeedContinueRecoupException("无法获取IRecoup对象");
                        //2017/2/23 由于多个系统的定时任务可能连一个数据库，所以任务真正执行的时候如果取不到直接退出执行
                        removeJobIdFromExecutingSet(recoupJobEntity.getId());
                        return;
                    }

                    recoupBean.recoup(recoupJobEntity.getJobJsonParam(), recoupJobEntity.getBusCode());
                    success = true;
                    logSB.append("执行成功").append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                } catch (NotNeedContinueRecoupException e) {
                    notNeedContinueRecoupExceptionMessage = "NotNeedContinueRecoupException业务主动放弃补偿，异常：" + e.getMessage();
                    logSB.append("执行失败：NotNeedContinueRecoupException").append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                } catch (Throwable e) {
                    LOGGER.error("[通用补偿作业异常]作业执行过程中recoupBean.recoup异常[" + recoupJobEntity.toString() + "]", e);
                    exceptionMessage = Throwables.getStackTraceAsString(Throwables.getRootCause(e));
                    if (exceptionMessage.length() > 3000) {
                        exceptionMessage = exceptionMessage.substring(0, 3000);
                    }
                    logSB.append("执行失败：" + e.getMessage()).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                }

                //结束执行一次补偿业务，回写状态
                try {
                    if (success) {
                        recoupJobDao.recordSuccessStatus(recoupJobEntity);
                    } else {
                        long effectTime = recoupJobEntity.getEffectTime();
                        int retryCurTimes = recoupJobEntity.getRetryCurTimes();
                        int retryTotalTimes = recoupJobEntity.getRetryTotalTimes();

                        if (StringUtils.isNotBlank(notNeedContinueRecoupExceptionMessage)) {//业务主动中断
                            recoupJobDao.recordFailStatus(recoupJobEntity, notNeedContinueRecoupExceptionMessage);
                        } else if (retryTotalTimes > 0 && (retryCurTimes + 1) >= retryTotalTimes) {//执行重试次数限制
                            recoupJobDao.recordFailStatus(recoupJobEntity, "RetryTotalTimes[超过总次数放弃补偿,异常:" + exceptionMessage + "]");
                        } else if (effectTime > 0L && com.ziroom.busrecoup.internal.DateUtils.formatNow2Long() >= effectTime) {//执行截止时间限制
                            recoupJobDao.recordFailStatus(recoupJobEntity, "EffectTime[超过截止日期放弃补偿,异常:" + exceptionMessage + "]");
                        } else {//重试次数更新
                            recoupJobDao.recordExecutingStatus(recoupJobEntity, "ExecutingException[执行过程中异常:" + exceptionMessage + "]");
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error("[通用补偿作业异常]作业执行后回写状态异常[" + recoupJobEntity.toString() + "]", e);
                    logSB.append("回写状态失败:" + e.getMessage()).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                } finally {
                    logSB.append("耗时:" + (System.currentTimeMillis() - recoupStart)).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                    LOGGER.info(logSB.toString());
                }

                //判断是否需要继续执行补偿业务
                String jobStatus = recoupJobEntity.getJobStatus();
                if (JobStatusEnum.SUCCESSED.getCode().equals(jobStatus)
                        || JobStatusEnum.FAILED.getCode().equals(jobStatus)) {//执行完毕
                    removeJobIdFromExecutingSet(recoupJobEntity.getId());
                    needCallback = true;
                    break;
                } else if ((++n) > retryTimesThreshold) {//当前循环内超过重试超过指定次数退出循环等待下一次调度，避免成为僵尸任务占用其他任务的执行线程
                    removeJobIdFromExecutingSet(recoupJobEntity.getId());
                    needCallback = false;
                    break;
                } else {//还需要继续执行
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);//考虑网络闪断情况等待50毫秒
                    } catch (InterruptedException e1) {
                    }
                }
            }

            //回调业务告知结果【要么成功要么失败】
            if (needCallback) {
                StringBuilder logSB = new StringBuilder();
                long start = System.currentTimeMillis();
                logSB.append("[通用补偿作业 - 单个任务执行回调]").append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                logSB.append(recoupJobEntity.toString()).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                try {
                    recoupBean.afterRecoup(recoupJobEntity.getJobStatus(), recoupJobEntity.getJobJsonParam(), recoupJobEntity.getBusCode(), recoupJobEntity.getFailReason());
                } catch (Throwable e) {
                    LOGGER.error("[通用补偿作业异常]补偿完成后回调afterRecoup异常[" + recoupJobEntity + "]", e);
                    logSB.append(",Error:" + e.getMessage());
                } finally {
                    logSB.append(",耗时:" + (System.currentTimeMillis() - start)).append(com.ziroom.busrecoup.internal.SymbolConstant.BAR);
                    LOGGER.info(logSB.toString());
                }
            }
        }
    }
}
