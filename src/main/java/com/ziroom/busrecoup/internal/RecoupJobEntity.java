package com.ziroom.busrecoup.internal;

import com.google.common.base.MoreObjects;

/**
 * 通用业务补偿job实体
 * Created by zhoutao on 2016/7/19.
 */
public class RecoupJobEntity {
    private long id;
    //执行补偿业务逻辑的类
    private String jobClass;
    //补偿job的名称
    private String jobName;
    //补偿job的描述
    private String jobDesc;
    //jobClass的业务参数json串
    private String jobJsonParam;
    //补偿job状态 @see JobStatusEnum
    private String jobStatus;
    //补偿job执行开始时间,绝对时间如20160720121212
    private long startTime;
    //补偿job执行完成时间,绝对时间如20160720121212
    private long completeTime;
    //补偿job创建时间,绝对时间如20160720121212
    private long createTime;
    //补偿截止时间,绝对时间如20160720121212，超过该时间没成功放弃补偿
    private long effectTime;
    //当前重试次数
    private int retryCurTimes;
    //重试总次数,超过该次数还没有补偿成功自动放弃补偿
    private int retryTotalTimes;
    //执行失败原因
    private String failReason;
    //是否开启异步执行,0同步执行 1异步执行
    private int async;
    //设置业务编码，需要业务保证唯一
    private String busCode;
    //执行失败报警标志0 未报警 1已报警
    private int failAlarmFlag;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(long effectTime) {
        this.effectTime = effectTime;
    }

    public int getRetryCurTimes() {
        return retryCurTimes;
    }

    public void setRetryCurTimes(int retryCurTimes) {
        this.retryCurTimes = retryCurTimes;
    }

    public int getRetryTotalTimes() {
        return retryTotalTimes;
    }

    public void setRetryTotalTimes(int retryTotalTimes) {
        this.retryTotalTimes = retryTotalTimes;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public int getAsync() {
        return async;
    }

    public void setAsync(int async) {
        this.async = async;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public int getFailAlarmFlag() {
        return failAlarmFlag;
    }

    public void setFailAlarmFlag(int failAlarmFlag) {
        this.failAlarmFlag = failAlarmFlag;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("jobName", jobName)
                .add("jobStatus", jobStatus)
                .add("startTime", startTime)
                .add("completeTime", completeTime)
                .add("createTime", createTime)
                .add("effectTime", effectTime)
                .add("retryCurTimes", retryCurTimes)
                .add("retryTotalTimes", retryTotalTimes)
                .add("busCode", async)
                .toString();
    }
}
