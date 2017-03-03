
package com.chinanetcenter.wcs.android.listener;

import android.util.Log;

import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.utils.StringUtils;

import org.apache.http.Header;

public abstract class ImageScaleListener extends AsyncHttpResponseHandler {

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
		onFailure(statusCode, OperationMessage.fromJsonString(StringUtils.stringFrom(responseBody)));
	}

	public abstract void onFailure(int statusCode, OperationMessage operationMessage);

}
