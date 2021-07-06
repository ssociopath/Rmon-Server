package com.bobooi.watch.common.utils.misc;

/**
 * 常量类
 * @author bobo
 * @date 2021/6/22
 */

public class Constant {
    public static final int ACCOUNT_MAX_LEN =20;
    public static final int PASSWORD_MAX_LEN = 8;

    public static final String WS_CONNECT = "CONNECT";
    public static final String WS_IMAGE = "IMAGE";
    public static final String WS_TASK = "TASK";
    public static final String WS_RES = "RES";
    public static final String WS_CMD = "CMD";

    /**
     * 消息类型，即请求内容类型
     */
    public static final byte HEART = 1;
    public static final byte LOGIN = 2;
    public static final byte LOGOUT = 3;
    public static final byte IMAGE = 4;
    public static final byte TASK = 5;
    public static final byte DATA_UPDATE = 6;
    public static final byte DATA_DELETE = 7;
    public static final byte RES_UPDATE = 8;
    public static final byte CMD = 9;

    public static final byte DF = -1;
    public static final byte MF = -2;

    public static final byte RESPONSE_SUCCEED = 20;
    public static final byte RESPONSE_FAIL = 21;
}
