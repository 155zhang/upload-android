
package com.chinanetcenter.wcs.android.listener;

import android.util.Log;

import com.chinanetcenter.wcs.android.entity.ImageInfo;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.utils.StringUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.apache.http.Header;

public abstract class ImageInfoListener extends AsyncHttpResponseHandler {

	/**
	 * @deprecated Use
	 *             {@link #onSuccess(int, com.chinanetcenter.wcs.android.entity.ImageInfo)}
	 *             instead.
	 */
	@Deprecated
	@Override
	public final void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		String rawResponse = StringUtils.stringFrom(responseBody);
		WCSLogUtil.d("image info : " + rawResponse);
		onSuccess(statusCode, ImageInfo.fromJsonString(rawResponse));
	}

	/**
	 * @deprecated Use
	 *             {@link #onFailure(int, com.chinanetcenter.wcs.android.entity.OperationMessage)}
	 *             instead.
	 */
	@Deprecated
	@Override
	public final void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        if(null != error && null != error.getLocalizedMessage()){
        	Log.e("CNCLog", error.getLocalizedMessage());
        }
		String rawResponse = StringUtils.stringFrom(responseBody);
		WCSLogUtil.d("fetch image info failured : " + rawResponse + "; error : "
				+ error.getLocalizedMessage());
		onFailure(statusCode, OperationMessage.fromJsonString(rawResponse));
	}

	/**
	 * @param statusCode
	 * @param imageInfo
	 */
	public abstract void onSuccess(int statusCode, ImageInfo imageInfo);

	public abstract void onFailure(int statusCode, OperationMessage operationMessage);

}
