package com.zihome.test.controller;

import com.ziroom.busrecoup.RecoupJob;
import com.ziroom.busrecoup.RecoupJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * description:
 *
 * @Author Yangjy
 * @Date 2019/4/24
 * @Version 1.0
 */
@Controller
public class IndexController {

    @Autowired
    private RecoupJobService recoupJobService;

    @RequestMapping("/test")
    public String test(){
        RecoupJob build = RecoupJob.builder()
                .jobJsonParam("11")
                .jobClass("com.zihome.test.elastic.job.TestShiYingJob")
                .jobDesc("test111")
                .jobName("test测试")
                .isAsyncExecute(true)
                .startTime(20190424214038L)
                .retryTotalTimes(3).build();
        recoupJobService.saveRecoupJob(build);
        return "ok";
    }

}
