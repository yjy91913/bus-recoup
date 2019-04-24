package com.ziroom.busrecoup;

import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 通用业务补偿job对象针对外部访问使用
 * Created by zhoutao on 2016/7/19.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoupJob {
    //执行补偿业务逻辑的类
    private String jobClass;
    //补偿job的名称
    private String jobName;
    //补偿job的描述
    private String jobDesc;
    //jobClass的业务参数json串
    private String jobJsonParam;
    //设置业务编码，需要业务保证唯一
    private String busCode;
    //补偿job执行开始时间,绝对时间如20160720121212
    private long startTime;
    //补偿截止时间,绝对时间如20160720121212，超过该时间没成功放弃补偿
    private long effectTime;
    //重试总次数,超过该次数还没有补偿成功自动放弃补偿
    private int retryTotalTimes;
    //是否开启异步执行
    private boolean isAsyncExecute;

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getJobJsonParam() {
        return jobJsonParam;
    }

    public void setJobJsonParam(String jobJsonParam) {
        this.jobJsonParam = jobJsonParam;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(long effectTime) {
        this.effectTime = effectTime;
    }

    public int getRetryTotalTimes() {
        return retryTotalTimes;
    }

    public void setRetryTotalTimes(int retryTotalTimes) {
        this.retryTotalTimes = retryTotalTimes;
    }

    public boolean isAsyncExecute() {
        return isAsyncExecute;
    }

    public void setAsyncExecute(boolean asyncExecute) {
        isAsyncExecute = asyncExecute;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jobClass", jobClass)
                .add("jobName", jobName)
                .add("jobDesc", jobDesc)
                .add("busCode", busCode)
                .add("jobJsonParam", jobJsonParam)
                .add("startTime", startTime)
                .add("effectTime", effectTime)
                .add("retryTotalTimes", retryTotalTimes)
                .add("isAsyncExecute", isAsyncExecute)
                .toString();
    }
}
