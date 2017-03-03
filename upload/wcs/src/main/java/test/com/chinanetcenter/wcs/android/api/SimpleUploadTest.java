package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleUploadTest extends BaseApiTest {

	private static final String FILE_NAME = "PhotoTable.apk";
	
	protected void setUp() throws Exception {
		super.setUp();
		copyAssets();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void testNullParams() throws InterruptedException {
		Log.i("CNCLog", "testNullParams");
		final String token = null;
		final String filePath = null;
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						Log.d(TAG, "response JSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						Log.d(TAG, "operationMessage : " + operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	private void testChinese() throws InterruptedException {
		Log.i("CNCLog", "testChinese");
		final String token = getToken(TEST_AK, TEST_BUCKET, "测试@#￥（*……%￥", TEST_EXPIRED, null, null);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, null,  new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						Log.d(TAG, "reponse JSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	private void testUsingCallbackUrl() throws InterruptedException {
		Log.i("CNCLog", "testUsingCallbackUrl");
		final String token = getToken(TEST_AK, TEST_BUCKET, "testFileUsingCallback", TEST_EXPIRED, CALLBACK_URL, null);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						assertNotNull(responseJson);
						Log.d(TAG, "responseJSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						assertNotNull(operationMessage);
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}
	
	private void testUsingCallbackBody() throws InterruptedException {
		Log.i("CNCLog", "testUsingCallbackBody");
		String callbackBodyString = "location=$(x:location)&price=$(x:price)";
		final String token = getToken(TEST_AK, TEST_BUCKET, "testFile", TEST_EXPIRED, CALLBACK_URL, callbackBodyString);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final HashMap<String, String> callbackBody = new HashMap<String, String>();
		callbackBody.put("x:location", "123456.001001");
		callbackBody.put("x:price", "12321");
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, callbackBody, new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						assertNotNull(responseJson);
						Log.d(TAG, "responseJSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						assertNotNull(operationMessage);
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}
	
	public void testNormalStream() throws FileNotFoundException, InterruptedException {
		Log.i("CNCLog", "testNormalStream");
		final String token = "86622e227a50d49d858c2494a935bc2e4ac543a7:YzI1ZmQ3YmVjZmQ3ZGQzOGVkZDdiNGEyNzQ0MTNmY2U3YTk0MDk5NA==:eyJzY29wZSI6ImltYWdlcyIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsInJldHVybkJvZHkiOiJidWNrZXQ9JChidWNrZXQpJmZzaXplPSQoZnNpemUpJmhhc2g9JChoYXNoKSZrZXk9JChrZXkpIiwib3ZlcndyaXRlIjoxLCJmc2l6ZUxpbWl0IjowfQ==";
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final InputStream input = new FileInputStream(filePath);
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				
				FileUploader.upload(getInstrumentation().getContext(), token, null, input, null, new FileUploaderListener() {
					
					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						assertNotNull(responseJson);
						Log.d(TAG, "responseJSON : " + responseJson);
						signal.countDown();
					}
					
					@Override
					public void onFailure(OperationMessage operationMessage) {
						assertNotNull(operationMessage);
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}
					
					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}
	
	private void testNormal() throws InterruptedException {
		Log.i("CNCLog", "testNormal");
		final String token = getToken(TEST_AK, TEST_BUCKET, "testFile", TEST_EXPIRED, null, null);
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						assertNotNull(responseJson);
						Log.d(TAG, "responseJSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						assertNotNull(operationMessage);
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	private void testInvalidateToken() throws InterruptedException {
		Log.i("CNCLog", "testInvalidateToken");
		final String token = "fdsafdsa";
		final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
		final String noExistsFilePath = "fdsafdsa";

		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {

					@Override
					public void onSuccess(int status, JSONObject responseJson) {
						assertNotNull(responseJson);
						Log.e(TAG, "responseJSON : " + responseJson);
						signal.countDown();
					}

					@Override
					public void onFailure(OperationMessage operationMessage) {
						assertNotNull(operationMessage);
						Log.e(TAG, "operation message : " + operationMessage);
						signal.countDown();
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
					}
				});
			}
		});

		signal.await(30 * 1000, TimeUnit.SECONDS);
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
		if (!TextUtils.isEmpty(callbackUrl)) {
			sb.append("&callBackUrl=");
			sb.append(callbackUrl);
		}
		if(!TextUtils.isEmpty(callbackBody)){
			sb.append("&callBody=");
			sb.append(EncodeUtils.urlsafeEncode(callbackBody));
		}
		sb.append("&overwrite=");
		sb.append("1");
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
