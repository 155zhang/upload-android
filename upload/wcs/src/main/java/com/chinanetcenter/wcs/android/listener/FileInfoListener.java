package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.entity.FileInfo;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.utils.StringUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.apache.http.Header;

import android.util.Log;

public abstract class FileInfoListener extends AsyncHttpResponseHandler {

	@Override
	public final void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		String rawResponse = StringUtils.stringFrom(responseBody);
		onSuccess(statusCode, FileInfo.fromJsonString(rawResponse));
	}

	@Override
	public final void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
		String rawResponse = StringUtils.stringFrom(responseBody);
        if(null != error && null != error.getLocalizedMessage()){
        	Log.e("CNCLog", error.getLocalizedMessage());
        }
		WCSLogUtil.e("fetch file info failured : " + rawResponse);
		onFailure(statusCode, OperationMessage.fromJsonString(rawResponse));
	}

	public abstract void onSuccess(int statusCode, FileInfo fileInfo);

	public abstract void onFailure(int statusCode, OperationMessage operationMessage);

}
