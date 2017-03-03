package com.chinanetcenter.wcs.android.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class OperationMessage {

	private boolean succeeded;
	private int status;
	private String message;

	public static OperationMessage fromJsonString(String jsonString) {
		OperationMessage errorMessage = new OperationMessage();
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				JSONObject jsonObject = new JSONObject(jsonString);
				errorMessage.status = jsonObject.optInt("code", 500);
				errorMessage.message = jsonObject.optString("message", "服务器内部错误");
			} catch (JSONException e) {
				Log.e("CNCLog", "json error : " + jsonString);
			}
		}
		return errorMessage;
	}

	public OperationMessage() {
	}

	public OperationMessage(int status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * 操作状态码
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 操作结果对应的信息
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.putOpt("status", status);
			jsonObject.putOpt("message", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

}
