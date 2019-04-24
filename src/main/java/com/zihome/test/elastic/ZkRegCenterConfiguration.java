package com.zihome.test.elastic;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:zhangbo
 * @Date:2018/9/6 20:00
 */
@Configuration
public class ZkRegCenterConfiguration {

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter springZookeeperRegistryCenter(
            @Value("${elastic.job.serverLists}") String serverLists,
            @Value("${elastic.job.namespace}") String namespace) {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(
                serverLists, namespace);
        return new ZookeeperRegistryCenter(zookeeperConfiguration);
    }
}
