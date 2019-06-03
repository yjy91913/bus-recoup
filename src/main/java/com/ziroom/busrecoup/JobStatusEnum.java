package com.ziroom.busrecoup;

/**
 * @Author by Yangjy
 * @Date 2018/6/30  11:04
 * 补偿任务执行状态枚举
 */
public enum JobStatusEnum {

    EXECUTE("execute", "待执行"),
    EXECUTING("executing", "执行中"),
    SUCCESSED("successed", "执行成功"),
    FAILED("failed", "执行失败"),
    PAUSED("paused", "暂停");

    private final String code;
    private final String codeName;

    JobStatusEnum(String code, String codeName){
        this.code = code;
        this.codeName = codeName;
    }

    public String getCode() {
        return code;
    }

    public String getCodeName() {
        return codeName;
    }
}
