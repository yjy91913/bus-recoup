package com.zihome.test.elastic.job;


import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 设备超时任务
 * </p>
 *
 * @author Yangjy
 * @since 2019-04-02
 */
@Service
@Slf4j
public class DeviceRespHandleJob implements SimpleJob {

    public final String corn = "*/5 * * * * ?";
    public final int shardingCount = 10;


    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("分片:"+shardingContext.getShardingItem());
        //System.out.println(111111);
    }

}
