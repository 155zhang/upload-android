package com.example.administrator.upload.http;

import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2017/2/10.
 */
public interface  APIService {

    /**
     * 获取token
     * @param bucket
     * @param key
     * @param overwrite
     * @param expire
     * @param ak
     * @return
     */
    @GET("getUploadToken")
    @Headers("Content-Type:text/html")
    Observable<String>  getToken(@Query("bucket") String bucket, @Query("key") String key, @Query("overwrite") String overwrite,@Query("expire") String expire, @Query("ak") String ak);

}
