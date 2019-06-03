package com.ziroom.busrecoup;

/**
 * 补偿业务逻辑接口
 * Created by Yangjy on 2018/7/19.
 */
public interface IRecoup {

    /**
     * 执行业务辅助流程逻辑
     *
     * @param jobJsonParam - 补偿作业运行时json参数由业务在创建补偿作业时设置，执行补偿作业时解析
     * @param busCode      - 唯一编号由业务保证补偿作业的唯一性(如果需要唯一)
     * @Throws Exception
     */
    void recoup(String jobJsonParam, String busCode) throws Exception;

    /**
     * 业务辅助流程完成后回调接口,用于失败告警或者清理工作
     * 注意：afterRecoup只会在补偿最终完成回调一次
     *
     * @param jobStatus    - 作业执行成功或失败状态,@see JobStatusEnum,successed or failed
     * @param jobJsonParam - 补偿作业运行时json参数由业务在创建补偿作业时设置，执行补偿作业时解析
     * @param busCode      - 唯一编号由业务保证补偿作业的唯一性(如果需要唯一)
     * @param failReason   - 失败原因
     * @Throws Exception
     */
    void afterRecoup(String jobStatus, String jobJsonParam, String busCode, String failReason) throws Exception;
}
