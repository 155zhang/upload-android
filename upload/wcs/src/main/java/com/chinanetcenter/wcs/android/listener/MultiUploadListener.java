package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.api.BaseApi;
import com.chinanetcenter.wcs.android.entity.MultiOperationMessage;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.utils.StringUtils;

import org.apache.http.Header;
import org.json.JSONObject;

import android.util.Log;

import java.util.ArrayList;

public abstract class MultiUploadListener extends AsyncHttpResponseHandler {

	@Override
	public final void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		parseResponse(statusCode, responseBody);

	}

	@Override
	public final void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
		if (null != error && null != error.getLocalizedMessage()) {
			Log.e("CNCLog", error.getLocalizedMessage());
		}
		parseResponse(statusCode, responseBody);
	}

	/**
	 * @deprecated 请使用 {@link #onFinished(int, JSONObject)}，该接口在配置上传回调时无法正常使用。
	 * @param statusCode
	 *            http的状态码
	 * @param successNum
	 *            成功上传的条数
	 * @param failNum
	 *            上传失败的条数
	 * @param operationMessages
	 *            具体的信息
	 */
	public void onFinished(int statusCode, int successNum, int failNum, ArrayList<MultiOperationMessage> operationMessages) {

	}

	/**
	 * 上传结果回调
	 * 
	 * @param statusCode
	 *            http的状态码
	 * @param jsonObject
	 *            上传返回的结果，没有返回结果则为空的JSONObject。
	 */
	public abstract void onFinished(int statusCode, JSONObject jsonObject);

	private void parseResponse(int statusCode, byte[] responseBody) {
		JSONObject reponseJSON = BaseApi.parseWCSUploadResponse(StringUtils.stringFrom(responseBody));
		onFinished(statusCode, reponseJSON);
	}

}
