package com.example.administrator.upload.http;

import com.google.gson.JsonSyntaxException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import rx.Subscriber;

/**
 * Created by Administrator on 2017/2/14.
 *
 * Retrofit中的异常
 */
public abstract class FilterSubscriber<T> extends Subscriber<T> {
   protected String error;
    @Override
    public abstract void onCompleted();
    @Override
    public void onError(Throwable e) {
        if (e instanceof TimeoutException || e instanceof SocketTimeoutException
                || e instanceof ConnectException){
            error = "网络不给力";
        }else if (e instanceof JsonSyntaxException){
            error = "Json格式出错了";
        }else {
            error = e.getMessage();
        }
    }
}
