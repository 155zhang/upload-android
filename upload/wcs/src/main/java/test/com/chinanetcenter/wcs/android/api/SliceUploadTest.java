package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SliceUploadTest extends BaseApiTest {

	private static final String SLICE_UPLOAD_TEST_FILE = "test.ipa";
//	private static final String SLICE_UPLOAD_TEST_FILE = "restore.ipsw";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSuperHugh() throws InterruptedException {
		Log.i(TAG, "$testSuperHugh");
//		final String uploadToken = getToken(TEST_AK, TEST_BUCKET, "restore.ipsw", TEST_EXPIRED, null, null);
		final String uploadToken = "db17ab5d18c137f786b67c490187317a0738f94a:OTgyMGMxZjA5NmZlMmZjYmZmNDcyM2RhYzVhMGFmNzQ4ZDg3OTkxNw==:eyJzY29wZSI6Ind1eWlrdW46cmVzdG9yZS5pcHN3IiwiZGVhZGxpbmUiOiI0MDcwODgwMDAwMDAwIiwib3ZlcndyaXRlIjoxLCJmc2l6ZUxpbWl0IjowfQ==";
//		final String filePath = Environment.getExternalStorageDirectory() + File.separator + "restore.ipsw";
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + "restore.ipsw";
		Log.i(TAG, "test normal path " + filePath);
		final File file = new File(filePath);
		Log.i(TAG, "file exists " + file.exists() + " can read " + file.canRead());
		template(getInstrumentation().getContext(), uploadToken, file, null);
	}
	
	private void testNormal() throws InterruptedException {
		Log.i(TAG, "$testNormal");
		final String uploadToken = getToken(TEST_AK, TEST_BUCKET, "哈哈IBYU^*(kl.ipa", TEST_EXPIRED, null, null);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
		Log.i(TAG, "telst normal path " + filePath);
		final File file = new File(filePath);
		template(getInstrumentation().getContext(), uploadToken, file, null);
	}

//	public void testUsingCallbackBody() throws InterruptedException {
//		Log.i(TAG, "$testUsingCallbackBody");
//		final String uploadToken = getToken(TEST_AK, TEST_BUCKET, "testCallback", TEST_EXPIRED, CALLBACK_URL, "location=$(x:location)&price=$(x:price)");
//		final HashMap<String, String> callbackBody = new HashMap<String, String>();
//		callbackBody.put("x:location", "123456.001001");
//		callbackBody.put("x:price", "12321");
//		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
//		final File file = new File(filePath);
//		template(getInstrumentation().getContext(), uploadToken, file, callbackBody);
//	}

	private void testInvalidateToekn() throws InterruptedException {
		Log.i(TAG, "testInvalidateToekn");
		final String uploadToken = "fdsajifdsaj";
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
		final File file = new File(filePath);
		template(getInstrumentation().getContext(), uploadToken, file, null);
	}

	private void testInvalidateToekn2() throws InterruptedException {
		Log.i(TAG, "testInvalidateToekn2");
		final String uploadToken = "fdsajifdsaj:fhdsjka:fdsajkl";
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
		final File file = new File(filePath);
		template(getInstrumentation().getContext(), uploadToken, file, null);
	}

	private void testNoContext() throws InterruptedException {
		Log.i(TAG, "testNoContext");
		final String uploadToken = getToken(TEST_AK, TEST_BUCKET, "宝宝树~!1@#$%^&*.ipa", TEST_EXPIRED, null, null);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
		final File file = new File(filePath);
		template(null, uploadToken, file, null);
	}

	private void testNullToken() throws InterruptedException {
		Log.i(TAG, "testNullToken");
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + SLICE_UPLOAD_TEST_FILE;
		final File file = new File(filePath);
		template(getInstrumentation().getContext(), null, file, null);
	}

	private void testNullFile() throws InterruptedException {
		Log.i(TAG, "testNullFile");
		final String uploadToken = getToken(TEST_AK, TEST_BUCKET, "宝宝树~!1@#$%^&*.ipa", TEST_EXPIRED, null, null);
		template(getInstrumentation().getContext(), uploadToken, null, null);
	}

	private void template(final Context context, final String uploadToken, final File file, final HashMap<String, String> callbackBody)
			throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				long start = System.currentTimeMillis();
				FileUploader.sliceUpload(context, uploadToken, file, callbackBody, new SliceUploaderListener() {

					@Override
					public void onSliceUploadSucceed(JSONObject responseJSON) {
						Log.d(TAG, "responseJSON : " + responseJSON);
						signal.countDown();
					}

					@Override
					public void onSliceUploadFailured(HashSet<String> errorMessages) {
						for (String string : errorMessages) {
							Log.e(TAG, "errorMessage : " + string);
						}
						signal.countDown();
					}

					@Override
					public void onProgress(long uploaded, long total) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s, percent %s", uploaded, total, (double)((float)uploaded / total) ));
					}
				});
				long end = System.currentTimeMillis();
				Log.i(TAG, "cost : " + (end - start));
			}
		});
//		Thread.sleep(3000);
//		FileUploader.cancelRequests(context);
//		getInstrumentation().runOnMainSync(new Runnable() {
//
//			@Override
//			public void run() {
//				FileUploader.sliceUpload(context, uploadToken, file, callbackBody, new SliceUploaderListener() {
//
//					@Override
//					public void onSliceUploadSucceed(JSONObject responseJSON) {
//						Log.d(TAG, "responseJSON : " + responseJSON);
//						signal.countDown();
//					}
//
//					@Override
//					public void onSliceUploadFailured(HashSet<String> errorMessages) {
//						for (String string : errorMessages) {
//							Log.e(TAG, "errorMessage : " + string);
//						}
//						signal.countDown();
//					}
//
//					@Override
//					public void onProgress(long uploaded, long total) {
//						Log.d(TAG, String.format("bytes written : %s, total size : %s, percent %s", uploaded, total, (double)((float)uploaded / total) ));
//					}
//				});
//			}
//		});
		signal.await(100 * 1000, TimeUnit.SECONDS);
	}
	
	private String getToken(String ak, String bucket, String key, long expired, String callbackUrl, String callbackBody) {
		HttpClient httpClient = new DefaultHttpClient();
		StringBuffer sb = new StringBuffer(UPLOAD_TOKEN_URL);
		sb.append("?ak=");
		sb.append(ak);
		sb.append("&bucket=");
		sb.append(bucket);
		sb.append("&key=");
		sb.append(EncodeUtils.urlsafeEncode(key));
		sb.append("&expire=");
		sb.append(expired);
		sb.append("&overwrite=");
		sb.append("1");
		if (!TextUtils.isEmpty(callbackUrl)) {
			sb.append("&callBackUrl=");
			sb.append(callbackUrl);
		}
		if (!TextUtils.isEmpty(callbackBody)) {
			sb.append("&callBody=");
			sb.append(EncodeUtils.urlsafeEncode(callbackBody));
		}
		Log.d(TAG, "get string : " + sb.toString());
		HttpGet httpGet = new HttpGet(sb.toString());
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			byte[] responseData = getResponseData(response.getEntity());
			String responseString = StringUtils.stringFrom(responseData);
			Log.d(TAG, "status code : " + statusCode + "; responseString : " + responseString);
			assertTrue(statusCode == 200);
			if (statusCode == 200) {
				return responseString;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
