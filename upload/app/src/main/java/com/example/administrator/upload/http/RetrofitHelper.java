package com.example.administrator.upload.http;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;


public class RetrofitHelper {

    private static RetrofitHelper mRetrofitHelper;
    private  APIService services;

    public RetrofitHelper() {
        Retrofit mLoginRetrofit = new Retrofit.Builder()
                .baseUrl("http://118.193.167.237:24001/")
                .addConverterFactory(StringConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        services = mLoginRetrofit.create(APIService.class);
    }

    public static RetrofitHelper getInstance() {
        if (mRetrofitHelper == null) {
            synchronized (RetrofitHelper.class) {
                if (mRetrofitHelper == null) {
                    mRetrofitHelper = new RetrofitHelper();
                }
            }
        }
        return mRetrofitHelper;
    }

    /**
     * get token
     * @param bucket
     * @param key
     * @param overwrite
     * @param expire
     * @param ak
     * @return
     */
    public Observable<String> getToken(String bucket, String key,String overwrite,String expire,String ak) {
        if (services == null) {
            new RetrofitHelper();
        }
        return services.getToken(bucket,key,overwrite,expire,ak);
    }
}