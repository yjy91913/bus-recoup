package com.ziroom.busrecoup;

/**
 * 无需继续补偿异常
 * 执行补偿逻辑过程中业务发现某些不具备继续补偿的条件可抛出此异常主动终止补偿
 *
 * @Author Yangjy
 * @Date 2018/9/3
 */
public class NotNeedContinueRecoupException extends RuntimeException {

    public NotNeedContinueRecoupException(String message) {
        super(message);
    }

    public NotNeedContinueRecoupException(String message, Throwable cause) {
        super(message, cause);
    }
}
