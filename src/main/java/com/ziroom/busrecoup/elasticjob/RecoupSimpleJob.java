package com.ziroom.busrecoup.elasticjob;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.google.common.collect.Lists;
import com.ziroom.busrecoup.internal.RecoupJobDao;
import com.ziroom.busrecoup.internal.RecoupJobExecuter;
import com.ziroom.busrecoup.internal.SymbolConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务补偿JOB入口,扩展自Elastic-job,并依赖spring
 * 框架内部使用，外部不需要基础继承
 * Created by Yangjy on 2018/6/27.
 */
@Component
public final class RecoupSimpleJob implements SimpleJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoupSimpleJob.class);
    @Autowired
    private RecoupJobDao recoupJobDao;
    @Autowired
    private RecoupJobExecuter recoupJobExecuter;

    private void processOne(long recoupJobId) {
        recoupJobExecuter.execute(recoupJobId);
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        StringBuilder logSB = new StringBuilder();
        long start = System.currentTimeMillis();

        logSB.append("[通用补偿作业入口]RecoupJob Start").append(SymbolConstant.BAR);
        logSB.append("ShardingContext:" + shardingContext.toString()).append(SymbolConstant.BAR);

        //得到分片项
        int shardingItem = shardingContext.getShardingItem();

        //List<Integer> shardingItems = context.getShardingItems();
        /*if (shardingItems == null || shardingItems.isEmpty()) {
            logSB.append("该服务器未分配到分片项，放弃执行");
            LOGGER.warn(logSB.toString());
            return;
        }*/

        //得到待处理的所有job Id
        List<Long> toBeExecutedIdList = recoupJobDao.getAllToBeExecutedIdList();
        if (toBeExecutedIdList == null || toBeExecutedIdList.isEmpty()) {
            logSB.append("未找到待执行或执行中的作业，放弃执行");
            LOGGER.info(logSB.toString());
            return;
        }

        //处理分片项和实际数据的对应关系
        List<Long> realToBeExecutedIdList = Lists.newArrayList();
        int shardingTotalCount = shardingContext.getShardingTotalCount();//分片总数
        for (Long id : toBeExecutedIdList) {
            int divisor = id.intValue() % shardingTotalCount;
            if(divisor == shardingItem){
                realToBeExecutedIdList.add(id);
            }
        }

        //本次需要处理的补偿job id
        logSB.append("Execute job count:" + realToBeExecutedIdList.size()).append(SymbolConstant.BAR);
        for (Long id : realToBeExecutedIdList) {
            this.processOne(id);
        }

        logSB.append("Recoup Job End").append(SymbolConstant.BAR);
        logSB.append("Time:" + (System.currentTimeMillis() - start)).append(SymbolConstant.BAR);
        LOGGER.info(logSB.toString());
    }
}
