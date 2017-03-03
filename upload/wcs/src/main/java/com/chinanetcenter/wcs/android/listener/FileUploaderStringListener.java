package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.utils.StringUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.apache.http.Header;

import android.util.Log;

/**
 *
 * 普通文件上传的回调，回调中返回值为未base64Encoded的字符串。
 *
 */
public abstract class FileUploaderStringListener extends AsyncHttpResponseHandler {

	@Override
	public final void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		onSuccess(statusCode, StringUtils.stringFrom(responseBody));
	}

	/**
	 * @deprecated Use
	 *             {@link #onFailure(com.chinanetcenter.wcs.android.entity.OperationMessage)}
	 *             instead.
	 */
	@Deprecated
	@Override
	public final void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
		String responseString = StringUtils.stringFrom(responseBody);
		if (null != error && null != error.getLocalizedMessage()) {
			Log.e("CNCLog", error.getLocalizedMessage());
		}
		WCSLogUtil.d(String.format("file upload failed : %s # %s ", statusCode, responseString));
		onFailure(OperationMessage.fromJsonString(responseString));
	}

	/**
	 * 文件上传成功之后回调
	 * 
	 * @param status
	 * @param responseJson
	 */
	public abstract void onSuccess(int status, String responseString);

	/**
	 * 文件上传失败之后的回调
	 * 
	 * @param operationMessage
	 *            操作对应的信息
	 */
	public abstract void onFailure(OperationMessage operationMessage);

	/**
	 * 上传进度
	 * 
	 * @param bytesWritten
	 *            已经上传的进度
	 * @param totalSize
	 *            文件总大小
	 */
	@Override
	public void onProgress(int bytesWritten, int totalSize) {
	}

}
