package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.Config;
import com.chinanetcenter.wcs.android.api.ImageManager;
import com.chinanetcenter.wcs.android.entity.ImageInfo;
import com.chinanetcenter.wcs.android.entity.ImageOption;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.listener.ImageInfoListener;
import com.chinanetcenter.wcs.android.listener.ImageScaleListener;

import org.apache.http.Header;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ImageManagerTest extends BaseApiTest {

	public void testImageInfoNullParams() throws InterruptedException {
		Log.i(TAG, "testImageInfoNullParams");
		imageInfoTemplate(getInstrumentation().getContext(), null, null, null, -1);
	}

	public void testImageInfoInvalidateToken() throws InterruptedException {
		Log.i(TAG, "testImageInfoInvalidateToken");
		imageInfoTemplate(getInstrumentation().getContext(), "fdsafdsa", TEST_BUCKET, "hahaha.jpg", TEST_EXPIRED);
	}

	public void testImageInfoNormal() throws InterruptedException {
		Log.i(TAG, "testImageInfoNormal");
		String fileKey = "hahaha.png";
		String downloadUrl = getDownloadUrl(TEST_BUCKET, fileKey);
		String token = getDownloadToken(TEST_AK, TEST_EXPIRED, downloadUrl);
		imageInfoTemplate(getInstrumentation().getContext(), token, TEST_BUCKET, fileKey, TEST_EXPIRED);
	}

	public void testImageInfoInvalidteExpired() throws InterruptedException {
		Log.i(TAG, "testImageInfoInvalidteExpired");
		String fileKey = "hahaha.png";
		String downloadUrl = getDownloadUrl(TEST_BUCKET, fileKey);
		String token = getDownloadToken(TEST_AK, TEST_EXPIRED, downloadUrl);
		imageInfoTemplate(getInstrumentation().getContext(), token, TEST_BUCKET, fileKey, TEST_EXPIRED + 3000);
	}

	public void testScaleImageNormal() throws InterruptedException {
		Log.i(TAG, "testScaleImageNormal");
		String fileKey = "hahaha.png";
		String downloadUrl = getDownloadUrl(TEST_BUCKET, fileKey);
		String token = getDownloadToken(TEST_AK, TEST_EXPIRED, downloadUrl);
		ImageOption option = new ImageOption();
		// String token = getCommonToken(tokenUrlString, ak, bucket, fileKey,
		// expired)
		scaleImageTemplate(getInstrumentation().getContext(), token, TEST_BUCKET, fileKey, TEST_EXPIRED, option);
	}

	private void imageInfoTemplate(final Context context, final String token, final String bucket, final String fileKey, final long expired) throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		final String downUrl = getDownloadUrl(bucket, fileKey);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				ImageManager.fetchImageInfo(context, token, expired, downUrl, new ImageInfoListener() {

					@Override
					public void onSuccess(int statusCode, ImageInfo imageInfo) {
						Log.d(TAG, "fetched image info : " + imageInfo);
						signal.countDown();
					}

					@Override
					public void onFailure(int statusCode, OperationMessage operationMessage) {
						Log.e(TAG, "fetch image info failured : " + operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	private void scaleImageTemplate(final Context context, final String token, final String bucket, final String fileKey, final long expired, final ImageOption option)
			throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		final String downloadUrl = getDownloadUrl(bucket, fileKey);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				ImageManager.scaleImage(context, token, expired, downloadUrl, option, new ImageScaleListener() {

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						assertNotNull(responseBody);
						assertTrue(responseBody.length > 0);
						Log.d(TAG, "scale image success : " + responseBody.length);
						signal.countDown();
					}

					@Override
					public void onFailure(int statusCode, OperationMessage operationMessage) {
						Log.e(TAG, "scale image failured : " + operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	private String getDownloadUrl(String bucket, String fileKey) {
		StringBuilder url = new StringBuilder();
		if (Config.GET_URL.startsWith("http://")) {
			url.append("http://").append(bucket).append(".").append(Config.GET_URL.substring("http://".length()));
		} else {
			url.append(bucket).append(".").append(Config.GET_URL);
		}
		url.append("/").append(fileKey);
		return url.toString();
	}

}
