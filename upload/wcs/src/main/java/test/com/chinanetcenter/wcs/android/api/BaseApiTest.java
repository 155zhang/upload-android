package test.com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.utils.DateUtil;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BaseApiTest extends InstrumentationTestCase {

	static final String UPLOAD_TOKEN_URL = "http://172.16.0.239:81/getUploadToken";
	static final String SCOPE_TOKEN_URL = "http://172.16.0.239:81/getScopeToken";
	static final String DOWNLOAD_TOKEN_URL = "http://172.16.0.239:81/getDownloadToken";
	static final String DELETE_TOKEN_URL = "http://172.16.0.239:81/getDeleteToken";
	static final String STAT_TOKEN_URL = "http://172.16.0.239:81/getStatToken";
	
	static final String TEST_AK = "c4190a6cc75e2192d79e76b7f4e5161e16292883";
	static final String CALLBACK_URL = "http://callback-test.wcs.biz.matocloud.com:8088/callbackUrl";
	static final String TEST_BUCKET = "combinetest";
	static final long TEST_EXPIRED = DateUtil.parseDate("2099-01-01 00:00:00", DateUtil.COMMON_PATTERN).getTime();

	static final String TAG = "CNCLog";

	protected byte[] getResponseData(HttpEntity entity) throws IOException {
		byte[] responseBody = null;
		if (entity != null) {
			InputStream instream = entity.getContent();
			if (instream != null) {
				long contentLength = entity.getContentLength();
				if (contentLength > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
				}
				int bufferSize = 4096;
				try {
					ByteArrayBuffer buffer = new ByteArrayBuffer(bufferSize);
					try {
						byte[] tmp = new byte[bufferSize];
						int l, count = 0;
						// do not send messages if request has been cancelled
						while ((l = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
							count += l;
							buffer.append(tmp, 0, l);
						}
					} finally {
						instream.close();
					}
					responseBody = buffer.toByteArray();
				} catch (OutOfMemoryError e) {
					System.gc();
					throw new IOException("File too large to fit into available memory");
				}
			}
		}
		return responseBody;
	}
	
	protected String getDownloadToken(String ak, long expired, String downloadUrl){
		HttpClient httpClient = new DefaultHttpClient();
		StringBuffer sb = new StringBuffer(DOWNLOAD_TOKEN_URL);
		sb.append("?ak=");
		sb.append(ak);
		sb.append("&downloadUrl=");
		sb.append(EncodeUtils.urlsafeEncode(downloadUrl));
		sb.append("&expire=");
		sb.append(expired);
		HttpGet httpGet = new HttpGet(sb.toString());
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			byte[] responseData = getResponseData(response.getEntity());
			String responseString = StringUtils.stringFrom(responseData);
			Log.d(TAG, "get download token status code : " + statusCode + "; responseString : " + responseString);
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

	protected String getCommonToken(String tokenUrlString, String ak, String bucket, String fileKey, long expired) {
		HttpClient httpClient = new DefaultHttpClient();
		StringBuffer sb = new StringBuffer(tokenUrlString);
		sb.append("?ak=");
		sb.append(ak);
		sb.append("&bucket=");
		sb.append(bucket);
		sb.append("&key=");
		sb.append(EncodeUtils.urlsafeEncode(fileKey));
		sb.append("&expire=");
		sb.append(expired);
		Log.d(TAG, "get string : " + sb.toString());
		HttpGet httpGet = new HttpGet(sb.toString());
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			byte[] responseData = getResponseData(response.getEntity());
			String responseString = StringUtils.stringFrom(responseData);
			Log.d(TAG, "get common token status code : " + statusCode + "; responseString : " + responseString);
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
	
	protected void copyAssets() {
	    AssetManager assetManager = getInstrumentation().getContext().getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open(filename);
	          File outFile = new File(getInstrumentation().getContext().getFilesDir(), filename);
	          out = new FileOutputStream(outFile);
	          if(outFile.length() <= 0){
	        	  copyFile(in, out);
	          }
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }     
	        finally {
	            if (in != null) {
	                try {
	                    in.close();
	                } catch (IOException e) {
	                    // NOOP
	                }
	            }
	            if (out != null) {
	                try {
	                    out.close();
	                } catch (IOException e) {
	                    // NOOP
	                }
	            }
	        }  
	    }
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

}
