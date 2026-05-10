package com.yaoxin.appbase.net;

import java.io.IOException;

/**
 * Created by will
 * on 2018/6/15.
 */
public class NetServerException extends IOException{
    private static final long serialVersionUID = 1L;
    private int errCode;
    /** 例如图形验证码错误时（704）服务端返回的新图片地址 */
    private String extraPayload;

    public NetServerException(String message) {
        super(message);
    }

    public NetServerException(String message, int errCode) {
        super(message);
        this.errCode = errCode;
    }

    public NetServerException(String message, int errCode, String extraPayload) {
        super(message);
        this.errCode = errCode;
        this.extraPayload = extraPayload;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getExtraPayload() {
        return extraPayload;
    }
}
