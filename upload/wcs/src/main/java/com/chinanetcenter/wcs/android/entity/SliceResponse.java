package com.chinanetcenter.wcs.android.entity;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SliceResponse {

    public long offset;

    public String context;

    public long crc32;

    public String md5;

    public static SliceResponse fromJsonString(String jsonString) {
        SliceResponse sliceResponse = new SliceResponse();
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                sliceResponse.offset = jsonObject.optLong("offset", 0);
                sliceResponse.context = jsonObject.optString("ctx", "0");
                sliceResponse.crc32 = jsonObject.optLong("crc32", 0);
                sliceResponse.md5 = jsonObject.optString("checksum", "0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return sliceResponse;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("offset", offset);
            jsonObject.putOpt("context", context);
            jsonObject.putOpt("crc32", crc32);
            jsonObject.putOpt("md5", md5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
