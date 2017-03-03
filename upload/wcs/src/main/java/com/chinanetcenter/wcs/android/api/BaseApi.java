package com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.LogRecorder;
import com.chinanetcenter.wcs.android.http.AsyncHttpClient;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

public class BaseApi {

	static final String FORM_TOKEN = "token";

	private static AsyncHttpClient sAsyncHttpClient;

	private static final Object mObject = new Object();

	static synchronized AsyncHttpClient getAsyncClient(Context context) {
		synchronized (mObject) {
			if (null == sAsyncHttpClient) {
				sAsyncHttpClient = new AsyncHttpClient();
			}
			LogRecorder.getInstance().setup(context);
		}
		return sAsyncHttpClient;
	}

	static boolean isNetworkReachable() {
		return true;
	}
	
	public static JSONObject parseWCSUploadResponse(String responseString){
		WCSLogUtil.d("parsing upload response : " + responseString);
		
		JSONObject responseJsonObject = null;
		try {
			responseJsonObject = new JSONObject(responseString);
		} catch (JSONException e) {
			WCSLogUtil.d("Try serializing as json failured, response may encoded.");
		}

		if(null == responseJsonObject){ 
			responseJsonObject = new JSONObject();
			if (!TextUtils.isEmpty(responseString)) {
				String response = EncodeUtils.urlsafeDecodeString(responseString);
				WCSLogUtil.d("response string : " + response);
				String[] params = response.split("&");
				for (String param : params) {
					int index = param.indexOf("=");
					if (index > 0 ) {
						try {
							responseJsonObject.put(param.substring(0, index), param.substring(index + 1));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return responseJsonObject;
	}
	
}
