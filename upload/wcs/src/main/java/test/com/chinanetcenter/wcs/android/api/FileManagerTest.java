package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.api.FileManager;
import com.chinanetcenter.wcs.android.entity.FileInfo;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.listener.DeleteFileListener;
import com.chinanetcenter.wcs.android.listener.FileInfoListener;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FileManagerTest extends BaseApiTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFileInfoNormal() throws InterruptedException {
		Log.i(TAG, "testFileInfoNormal");
		String fileKey = "testFile";
		String token = getCommonToken(STAT_TOKEN_URL, TEST_AK, TEST_BUCKET, fileKey, TEST_EXPIRED);
		fileInfoTemplate(getInstrumentation().getContext(), token, TEST_BUCKET, fileKey);
	}

	public void testFileInfoNullParams() throws InterruptedException {
		Log.i(TAG, "testFileInfoNullParams");
		fileInfoTemplate(getInstrumentation().getContext(), null, null, null);
	}

	public void testFileInfoInvalidateToken() throws InterruptedException {
		Log.i(TAG, "testFileInfoInvalidateToken");
		String fileKey = "testFile";
		String token = "fdsajkfldskl";
		fileInfoTemplate(getInstrumentation().getContext(), token, TEST_BUCKET, fileKey);
	}

	private void fileInfoTemplate(final Context context, final String fileInfoToken, final String bucketName, final String fileKey) throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				FileManager.fetchFileInfo(context, fileInfoToken, bucketName, fileKey, new FileInfoListener() {

					@Override
					public void onSuccess(int statusCode, FileInfo fileInfo) {
						Log.d(TAG, "fetchd file info : " + fileInfo);
						signal.countDown();
					}

					@Override
					public void onFailure(int statusCode, OperationMessage operationMessage) {
						Log.e(TAG, "fetch file info failed : " + operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

	public void testDeleteInvalidateToken() throws InterruptedException {
		Log.i(TAG, "testDeleteInvalidateToken");
		String fileKey = "testFile";
		String deleteToken = "fdsjaklfdsjkl";
		deleteTemplate(getInstrumentation().getContext(), deleteToken, TEST_BUCKET, fileKey);
	}

	public void testDeleteNullParams() throws InterruptedException {
		Log.i(TAG, "testDeleteNullParams");
		deleteTemplate(getInstrumentation().getContext(), null, null, null);
	}

	public void testXDeleteNormal() throws InterruptedException {
		Log.i(TAG, "testXDeleteNormal");
		String fileKey = "testFile";
		String deleteToken = getCommonToken(DELETE_TOKEN_URL, TEST_AK, TEST_BUCKET, fileKey, TEST_EXPIRED);
		deleteTemplate(getInstrumentation().getContext(), deleteToken, TEST_BUCKET, fileKey);
	}

	private void deleteTemplate(final Context context, final String deleteToken, final String bucket, final String fileKey) throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		getInstrumentation().runOnMainSync(new Runnable() {

			@Override
			public void run() {
				FileManager.delete(context, deleteToken, bucket, fileKey, new DeleteFileListener() {

					@Override
					public void onSuccess(int status, OperationMessage operationMessage) {
						Log.d(TAG, "success message : " + operationMessage);
						assertNotNull(operationMessage);
						signal.countDown();
					}

					@Override
					public void onFailure(int status, OperationMessage operationMessage) {
						Log.e(TAG, "failured message : " + operationMessage);
						assertNotNull(operationMessage);
						signal.countDown();
					}
				});
			}
		});
		signal.await(30 * 1000, TimeUnit.SECONDS);
	}

}
