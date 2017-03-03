package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.Config;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.lang.reflect.Field;

public class Validate extends BaseApiTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAsset() {
		copyAssets();
	}

	private static String getUploadScope(String uploadToken) {
		String[] uploadTokenArray = uploadToken.split(":");
		if (uploadTokenArray.length != 3) {
			return "";
		}
		String policyJsonString = EncodeUtils.urlsafeDecodeString(uploadTokenArray[2]);
		String scope = "";
		try {
			JSONObject jsonObject = new JSONObject(policyJsonString);
			scope = jsonObject.optString("scope", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return scope;
	}
	
	public void xtestEnvironment() throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		Field debuggingField = WCSLogUtil.class.getDeclaredField("DEBUGGING");
		debuggingField.setAccessible(true);
		boolean debugging = debuggingField.getBoolean(null);
		assertFalse(debugging);
		assertEquals(Config.PUT_URL, "http://up.wcsapi.biz.matocloud.com:8090");
		assertEquals(Config.MGR_URL, "http://mgr.wcsapi.biz.matocloud.com");
	}

}
