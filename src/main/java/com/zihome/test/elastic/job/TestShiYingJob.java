package com.zihome.test.elastic.job;

import com.ziroom.busrecoup.IRecoup;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @Author Yangjy
 * @Date 2019/4/24
 * @Version 1.0
 */
@Component
public class TestShiYingJob implements IRecoup {
    @Override
    public void recoup(String jobJsonParam, String busCode) throws Exception {
        System.out.println(jobJsonParam+"after****************************************");
        System.out.println(1/0);
    }

    @Override
    public void afterRecoup(String jobStatus, String jobJsonParam, String busCode, String failReason) throws Exception {
        System.out.println("after****************************************");
    }
}
