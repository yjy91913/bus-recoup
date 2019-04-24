package com.zihome.test.elastic;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.zihome.test.elastic.job.DeviceRespHandleJob;
import com.ziroom.busrecoup.RecoupJob;
import com.ziroom.busrecoup.elasticjob.RecoupSimpleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author:zhangbo
 * @Date:2018/9/7 11:40
 */
@Configuration
public class JobConfiguration {

    @Resource
    private ZookeeperRegistryCenter regCenterConfiguration;

    @Resource
    private DeviceRespHandleJob deviceRespHandleJob;

    @Autowired
    private RecoupSimpleJob recoupSimpleJob;

    @Bean(initMethod = "init")
    public SpringJobScheduler deviceRespHandleJobScheduler(){

        // 定义作业核心配置
        JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(deviceRespHandleJob.getClass().getName(),
                deviceRespHandleJob.corn,deviceRespHandleJob.shardingCount).build();
        // 定义SIMPLE类型配置
        SimpleJobConfiguration jobConfiguration = new SimpleJobConfiguration(coreConfiguration,deviceRespHandleJob.getClass().getCanonicalName());
        // 定义Lite作业根配置
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobConfiguration).overwrite(true).build();
        return new SpringJobScheduler(deviceRespHandleJob,regCenterConfiguration,liteJobConfiguration);
    }

   /*@Bean(name = "busRecoupJob", initMethod = "init")
    public SpringJobScheduler busRecoupJob() {
        SimpleJobConfigurationDto job = new SimpleJobConfigurationDto(
                "busRecoupJob", SimpleJob.class, 10, "0 0/10 * * * ?");
        return new SpringJobScheduler(regCenter, job);
    }*/


    @Bean(initMethod = "init")
    public SpringJobScheduler recoupJobScheduler(){
        // 定义作业核心配置
        JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(RecoupSimpleJob.class.getName(),
                deviceRespHandleJob.corn,deviceRespHandleJob.shardingCount).build();
        // 定义SIMPLE类型配置
        SimpleJobConfiguration jobConfiguration = new SimpleJobConfiguration(coreConfiguration,RecoupSimpleJob.class.getCanonicalName());
        // 定义Lite作业根配置
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobConfiguration).overwrite(true).build();
        return new SpringJobScheduler(recoupSimpleJob,regCenterConfiguration,liteJobConfiguration);
    }

}
