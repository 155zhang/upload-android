package com.example.administrator.upload.http;

/**
 * Created by ZHANGQIANG
 * <p>
 * on 2017/3/2.
 */
public class ErrorResponse {

    /**
     * status : 406
     * message : File Already Exist
     */

    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
