package com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.entity.ImageOption;
import com.chinanetcenter.wcs.android.http.SimpleRequestParams;
import com.chinanetcenter.wcs.android.listener.ImageInfoListener;
import com.chinanetcenter.wcs.android.listener.ImageScaleListener;

import android.content.Context;
import android.text.TextUtils;

public class ImageManager extends BaseApi {

	/**
	 * 获取图片的详细信息
	 * @param imageInfoToken 获取图片信息所需要的token
	 * @param expiredTime 过期时间，必须与token中的过期时间一致
	 * @param downloadUrl 下载的url，生成规则请查看文档
	 * @param imageInfoListener 图片信息的接口回调
	 */
	public static void fetchImageInfo(Context context, String imageInfoToken, long expiredTime, String downloadUrl, ImageInfoListener imageInfoListener) {
//		StringBuilder url = new StringBuilder();
//		if (Config.GET_URL.startsWith("http://")) {
//			url.append("http://").append(bucketName).append(".").append(Config.GET_URL.substring("http://".length()));
//		} else {
//			url.append(bucketName).append(".").append(Config.GET_URL);
//		}
//		url.append("/").append(fileKey);
		SimpleRequestParams params = new SimpleRequestParams();
		params.put("e", String.valueOf(expiredTime));
		params.put("token", imageInfoToken);
		params.put("op", "imageInfo");
		getAsyncClient(context).get(downloadUrl, params, imageInfoListener);
	}

	/**
	 * 压缩图片并下载压缩后的图片，下载的内容会在{@link com.chinanetcenter.wcs.android.listener.ImageScaleListener#onSuccess(int, org.apache.http.Header[], byte[])}中返回，
	 * 更多详细信息请查看{@link com.chinanetcenter.wcs.android.listener.ImageScaleListener}
	 * @param scaleImageToken 压缩下载图片所需要的token
	 * @param expiredTime 过期时间，必须与token中的过期时间一致
	 * @param downloadUrl 下载的url，生成规则请查看文档
	 * @param option 图片压缩策略，更多信息请查看{@link com.chinanetcenter.wcs.android.entity.ImageOption}
	 * @param scaleListener 压缩并下载图片的接口回调
	 */
	public static void scaleImage(Context context, String scaleImageToken, long expiredTime, String downloadUrl, ImageOption option, ImageScaleListener scaleListener) {
//		StringBuilder url = new StringBuilder();
//		if (Config.GET_URL.startsWith("http://")) {
//			url.append("http://").append(bucketName).append(".").append(Config.GET_URL.substring("http://".length()));
//		} else {
//			url.append(bucketName).append(".").append(Config.GET_URL);
//		}
//		url.append("/").append(fileKey);
		SimpleRequestParams params = new SimpleRequestParams();
		params.put("e", String.valueOf(expiredTime));
		params.put("token", scaleImageToken);
		params.put("op", "imageView2");
		params.put("mode", option.getMode().getValue());
		if (!TextUtils.isEmpty(option.getHeight())) {
			params.put("height", option.getHeight());
		}
		if (!TextUtils.isEmpty(option.getWidth())) {
			params.put("width", option.getWidth());
		}
		if (!TextUtils.isEmpty(option.getQuality())) {
			params.put("quality", option.getQuality());
		}
		if (!TextUtils.isEmpty(option.getFormat())) {
			params.put("format", option.getFormat());
		}
		getAsyncClient(context).get(downloadUrl, params, scaleListener);
	}

}
