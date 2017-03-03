package com.chinanetcenter.wcs.android.api;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.chinanetcenter.wcs.android.Config;
import com.chinanetcenter.wcs.android.LogRecorder;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.entity.SliceCache;
import com.chinanetcenter.wcs.android.entity.SliceCacheManager;
import com.chinanetcenter.wcs.android.entity.SliceResponse;
import com.chinanetcenter.wcs.android.http.AsyncHttpResponseHandler;
import com.chinanetcenter.wcs.android.http.SimpleRequestParams;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;
import com.chinanetcenter.wcs.android.listener.FileUploaderStringListener;
import com.chinanetcenter.wcs.android.listener.SliceUploaderBase64Listener;
import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;
import com.chinanetcenter.wcs.android.slice.Block;
import com.chinanetcenter.wcs.android.slice.Slice;
import com.chinanetcenter.wcs.android.slice.SliceHttpEntity;
import com.chinanetcenter.wcs.android.utils.Crc32;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.FileUtil;
import com.chinanetcenter.wcs.android.utils.StringUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class FileUploader extends BaseApi {

	private static final String FORM_FILE = "file";

	private static final String FORM_FILE_DESC = "desc";

	public static final String SLICE_UPLOAD_MESSAGE_FORMAT = "upload file failed at index `%s` with error message `%s`";

	private FileUploader() {
	}

	public static void setUploadUrl(String uploadUrl) {
		Config.PUT_URL = uploadUrl;
	}

	/**
	 * 取消上传中的任务
	 * 
	 * @param context
	 *            当前任务关联的context，必须与上传时使用到的是同一个实例。
	 */
	public static void cancelRequests(Context context) {
		getAsyncClient(context).cancelRequests(context, true);
	}

	/**
	 * 根据tag、context取消上传中的任务
	 * 
	 * @param context
	 *            当前任务关联的context，必须与上传时使用到的是同一个实例。
	 * @param tag
	 *            上传时设置tag（目前只有分片可以设置。）
	 */
	public static void cancelRequests(Context context, String tag) {
		getAsyncClient(context).cancelRequests(context, tag, true);
	}

	/**
	 * 上传文件到网宿云存储
	 * 
	 * @param context
	 *            当前上下文
	 * @param token
	 *            上传文件所需要的凭证
	 * @param fileUri
	 *            要上传的文件的URI
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param uploaderListener
	 *            上传的回调
	 */
	public static void upload(Context context, String token, Uri fileUri, HashMap<String, String> callbackBody, FileUploaderListener uploaderListener) {
		upload(context, token, FileUtil.getFile(context, fileUri), callbackBody, uploaderListener);
	}

	/**
	 * 上传文件到网宿云存储
	 * 
	 * @param token
	 *            上传文件所需要的凭证
	 * @param filePath
	 *            要上传文件的路径
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param uploaderListener
	 *            上传的回调
	 */
	public static void upload(Context context, String token, String filePath, HashMap<String, String> callbackBody, FileUploaderListener uploaderListener) {
		if (null == filePath || filePath.trim().equals("")) {
			uploaderListener.onFailure(new OperationMessage(-1, "file no exists : " + filePath));
			return;
		}
		upload(context, token, new File(filePath), callbackBody, uploaderListener);
	}

	/**
	 * 上传文件到网宿云存储
	 * 
	 * @param context
	 *            当前上下文
	 * @param token
	 *            上传文件所需要的凭证
	 * @param fileName
	 *            文件名
	 * @param inputStream
	 *            上传的文件的流
	 * @param callbackBody
	 *            自定义参数，没有则传空
	 * @param uploaderListener
	 *            上传的回调
	 */
	public static void upload(Context context, String token, String fileName, InputStream inputStream, HashMap<String, String> callbackBody,
			FileUploaderListener uploaderListener) {
		if (null == token || token.trim().equals("")) {
			uploaderListener.onFailure(new OperationMessage(-1, "token invalidate : " + token));
			return;
		}

		if (null == fileName || fileName.trim().equals("")) {
			uploaderListener.onFailure(new OperationMessage(-1, "file name empty."));
			return;
		}

		SimpleRequestParams params = new SimpleRequestParams(callbackBody);
		params.put(FORM_TOKEN, token);
		params.put(FORM_FILE, inputStream, fileName);
		String uploadUrlString = Config.PUT_URL + "/file/upload";
		dump(context, token, uploadUrlString, 0, fileName);
		getAsyncClient(context).post(context, uploadUrlString, params, uploaderListener);
	}

	/**
	 * 上传文件到网宿云存储
	 * 
	 * @param token
	 *            上传文件所需要的凭证
	 * @param file
	 *            要上传的文件
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param uploaderListener
	 *            上传的回调
	 */
	public static void upload(Context context, String token, File file, HashMap<String, String> callbackBody, FileUploaderListener uploaderListener) {
		upload(context, token, file, callbackBody, (FileUploaderStringListener) uploaderListener);
	}

	public static void upload(Context context, String token, File file, HashMap<String, String> callbackBody, FileUploaderStringListener uploaderListener) {
		if (null == token || token.trim().equals("")) {
			uploaderListener.onFailure(new OperationMessage(-1, "token invalidate : " + token));
			return;
		}
		if (!file.canRead()) {
			uploaderListener.onFailure(new OperationMessage(-1, "file access denied."));
			return;
		}

		try {
			SimpleRequestParams params = new SimpleRequestParams(callbackBody);
			params.put(FORM_TOKEN, token);
			params.put(FORM_FILE, file);
			params.put(FORM_FILE_DESC, file.getName());
			String uploadUrlString = Config.PUT_URL + "/file/upload";
			dump(context, token, uploadUrlString, file.length(), file.getName());
			getAsyncClient(context).post(context, uploadUrlString, params, uploaderListener);
		} catch (FileNotFoundException e) {
			WCSLogUtil.e("file not found while upload.");
			OperationMessage responseMsg = new OperationMessage();
			responseMsg.setStatus(LocalResultCode.FILE_NOT_FOUND.code);
			responseMsg.setMessage(LocalResultCode.FILE_NOT_FOUND.errorMsg);
			uploaderListener.onFailure(responseMsg);
			return;
		}
	}

	/**
	 * 切片上传到网宿云存储，当一个Block上传完成之后，下次重新上传则只上传其他的Block
	 * 
	 * @param context
	 *            应用当前的上下文
	 * @param uploadToken
	 *            上传的文件所需要的token，一般由服务器端生成，具体请查看文档
	 * @param file
	 *            需要上传的文件
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param sliceUploaderListener
	 *            分片上传的回调，回调成功返回值为base64Encoded的字符串。
	 */
	public static void sliceUpload(final Context context, final String uploadToken, final File file, final HashMap<String, String> callbackBody,
			final SliceUploaderBase64Listener sliceUploaderListener) {
		sliceUpload(context, uploadToken, file, callbackBody, (SliceUploaderListener) sliceUploaderListener);
	}

	/**
	 * 切片上传到网宿云存储，当一个Block上传完成之后，下次重新上传则只上传其他的Block
	 * 
	 * @param tag
	 *            设置本次分片的tag（取消上传时可根据tag取消任务。）
	 * @param context
	 *            应用当前的上下文
	 * @param uploadToken
	 *            上传的文件所需要的token，一般由服务器端生成，具体请查看文档
	 * @param file
	 *            需要上传的文件
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param sliceUploaderListener
	 *            分片上传的回调，回调成功返回值为base64Encoded的字符串。
	 */
	public static void sliceUpload(final String tag, final Context context, final String uploadToken, final File file,
			final HashMap<String, String> callbackBody, final SliceUploaderBase64Listener sliceUploaderListener) {
		sliceUpload(tag, context, uploadToken, file, callbackBody, (SliceUploaderListener) sliceUploaderListener);
	}

	/**
	 * 切片上传到网宿云存储，当一个Block上传完成之后，下次重新上传则只上传其他的Block
	 * 
	 * @param context
	 *            应用当前的上下文
	 * @param uploadToken
	 *            上传的文件所需要的token，一般由服务器端生成，具体请查看文档
	 * @param file
	 *            需要上传的文件
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param sliceUploaderListener
	 *            分片上传的回调
	 */
	public static void sliceUpload(final Context context, final String uploadToken, final File file, final HashMap<String, String> callbackBody,
			final SliceUploaderListener sliceUploaderListener) {
		sliceUpload(null, context, uploadToken, file, callbackBody, sliceUploaderListener);
	}

	/**
	 * 切片上传到网宿云存储，当一个Block上传完成之后，下次重新上传则只上传其他的Block
	 * 
	 * @param tag
	 *            设置本次分片的tag（取消上传时可根据tag取消任务。）
	 * @param context
	 *            应用当前的上下文
	 * @param uploadToken
	 *            上传的文件所需要的token，一般由服务器端生成，具体请查看文档
	 * @param file
	 *            需要上传的文件
	 * @param callbackBody
	 *            自定义参数及callbackBody，没有则传空
	 * @param sliceUploaderListener
	 *            分片上传的回调
	 */
	public static void sliceUpload(final String tag, final Context context, final String uploadToken, final File file,
			final HashMap<String, String> callbackBody, final SliceUploaderListener sliceUploaderListener) {
		if (null == file || !file.exists()) {
			if (null != sliceUploaderListener) {
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add(String.format(SLICE_UPLOAD_MESSAGE_FORMAT, -1, "file no exists"));
				sliceUploaderListener.onSliceUploadFailured(hashSet);
			}
			return;
		}
		if (!file.canRead()) {
			if (null != sliceUploaderListener) {
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add(String.format(SLICE_UPLOAD_MESSAGE_FORMAT, -1, "access file denied."));
				sliceUploaderListener.onSliceUploadFailured(hashSet);
			}
			return;
		}
		if (null == context || TextUtils.isEmpty(uploadToken) || TextUtils.isEmpty(getUploadScope(uploadToken))) {
			if (null != sliceUploaderListener) {
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add(String.format(SLICE_UPLOAD_MESSAGE_FORMAT, -1, "param invalidate"));
				sliceUploaderListener.onSliceUploadFailured(hashSet);
			}
			return;
		}
		Block[] blocks = Block.blocks(file);
		if (null == blocks || blocks.length <= 0) {
			if (null != sliceUploaderListener) {
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add(String.format(SLICE_UPLOAD_MESSAGE_FORMAT, -1, "read file failured."));
				sliceUploaderListener.onSliceUploadFailured(hashSet);
			}
			return;
		}
		// String fileHash = WetagUtil.getEtagHash(file) + ":" +
		// getUploadScope(uploadToken);
		String fileHash = file.getName() + ":" + getUploadScope(uploadToken);
		final SliceCache sliceCache = getSliceCache(uploadToken, fileHash, blocks);
		WCSLogUtil.i("get slice cache " + sliceCache);
		long allPersistentSize = getUploadedSize(blocks, sliceCache);

		WCSLogUtil.d(fileHash + "" + " persistent size from cache " + allPersistentSize);

		final int blockCount = blocks.length;
		final long fileSize = blocks[0].getOriginalFileSize();
		final ProgressNotifier progressNotifier = new ProgressNotifier(fileSize, sliceUploaderListener);
		progressNotifier.increaseProgressAndNotify(allPersistentSize);
		if (allPersistentSize >= fileSize) {
			WCSLogUtil.d("all file uploaded, merge directly");
			mergeBlock(tag, context, uploadToken, fileSize, sliceCache, convertListToString(sliceCache.getBlockContext()), callbackBody, sliceUploaderListener);
			return;
		}
		final int[] success = new int[] { 0 };
		final int[] failed = new int[] { 0 };
		final HashSet<String> failedMessages = new HashSet<String>();

		for (int i = 0; i < blocks.length; i++) {
			WCSLogUtil.d("block : " + blocks[i].toString());
			uploadBlock(context, uploadToken, blocks[i], i, sliceCache, tag, progressNotifier, new UploadBlockListener() {
				@Override
				public void onBlockUploaded(int blockIndex, String blockContext) {
					success[0]++;
					if (success[0] == blockCount) {
						mergeBlock(tag, context, uploadToken, fileSize, sliceCache, convertListToString(sliceCache.getBlockContext()), callbackBody,
								sliceUploaderListener);
					} else if (success[0] + failed[0] == blockCount) {
						if (null != sliceUploaderListener) {
							sliceUploaderListener.onSliceUploadFailured(failedMessages);
						}
					}
				}

				@Override
				public void onBlockUploadFailured(int blockIndex, OperationMessage operationMessage) {
					String failedMessage = String.format(SLICE_UPLOAD_MESSAGE_FORMAT, blockIndex, operationMessage.getMessage());
					failedMessages.add(failedMessage);
					failed[0]++;
					if (success[0] + failed[0] == blockCount) {
						if (null != sliceUploaderListener) {
							sliceUploaderListener.onSliceUploadFailured(failedMessages);
						}
					}
				}
			});
		}
	}

	private static long getUploadedSize(Block[] blocks, SliceCache sliceCache) {
		long allUploadedSize = 0;
		for (int i = 0; i < sliceCache.getBlockUploadedIndex().size(); i++) {
			Integer persistentIndex = sliceCache.getBlockUploadedIndex().get(i);
			int uploadingIndexValue = persistentIndex == null ? 0 : persistentIndex.intValue();
			blocks[i].setIndex(uploadingIndexValue);
			// 持久化的是nextIndex，计算当前需要减一
			WCSLogUtil.d("uploaded index " + uploadingIndexValue + " from " + i);
			allUploadedSize += uploadingIndexValue * Block.SLICE_SIZE;
		}
		return allUploadedSize;
	}

	private static SliceCache getSliceCache(String uploadToken, String fileHash, Block[] blocks) {
		SliceCache sliceCache = SliceCacheManager.getInstance().getSliceCache(fileHash);
		if (null == sliceCache || sliceCache.getBlockUploadedIndex().size() != blocks.length) {
			sliceCache = new SliceCache();
			sliceCache.setFileHash(fileHash);
			sliceCache.setBlockContext(new ArrayList<String>());
			sliceCache.setBlockUploadedIndex(new ArrayList<Integer>());
			for (int i = 0; i < blocks.length; i++) {
				sliceCache.getBlockUploadedIndex().add(0);
				sliceCache.getBlockContext().add("");
			}
			SliceCacheManager.getInstance().addSliceCache(sliceCache);
		}
		return sliceCache;
	}

	private static void uploadBlock(final Context context, final String uploadToken, final Block block, final int blockIndex, final SliceCache sliceCache,
			final String tag, final ProgressNotifier progressNotifier, final UploadBlockListener uploadBlockListener) {
		AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				SliceResponse sliceResponse = SliceResponse.fromJsonString(StringUtils.stringFrom(responseBody));
				uploadNextSlice(tag, responseBody, blockIndex, block, context, uploadToken, sliceCache, progressNotifier, uploadBlockListener);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if (null != error && null != error.getLocalizedMessage()) {
					Log.e("CNCLog", error.getLocalizedMessage());
				}
				String responseString = StringUtils.stringFrom(responseBody);
				WCSLogUtil.d("block index failured : " + blockIndex + ", onFailure : " + responseString + "; error : " + error.getLocalizedMessage());
				uploadBlockListener.onBlockUploadFailured(blockIndex, OperationMessage.fromJsonString(responseString));
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				progressNotifier.increaseProgressAndNotify(bytesWritten);
				WCSLogUtil.d(String.format(Locale.CHINA, "block index : %s ,written : %s, totalSize : %s", blockIndex, bytesWritten, totalSize));
			}
		};
		int currentIndex = block.getIndex();
		Slice slice = block.moveToNext();
		if (null != slice && currentIndex == 0) {
			SliceHttpEntity sliceHttpEntity = new SliceHttpEntity(slice, httpResponseHandler);
			String initBlockUrl = Config.PUT_URL + "/mkblk/" + block.size() + "/" + blockIndex;
			Header[] headers = new Header[] { new BasicHeader("Authorization", uploadToken) };
			dump(context, uploadToken, initBlockUrl, slice.size(), block.getOriginalFileName());
			getAsyncClient(context).post(context, initBlockUrl, headers, sliceHttpEntity, null, httpResponseHandler, tag);
		} else if (null != slice && currentIndex != 0) {
			uploadSlice(tag, context, uploadToken, block, blockIndex, slice, sliceCache, sliceCache.getBlockContext().get(blockIndex), progressNotifier,
					uploadBlockListener);
		} else if (null == slice) {
			uploadBlockListener.onBlockUploaded(blockIndex, sliceCache.getBlockContext().get(blockIndex));
		}
	}

	private static void uploadNextSlice(String tag, byte[] responseBody, int blockIndex, Block block, Context context, String uploadToken,
			SliceCache sliceCache, ProgressNotifier progressNotifier, UploadBlockListener uploadBlockListener) {
		SliceResponse sliceResponse = SliceResponse.fromJsonString(StringUtils.stringFrom(responseBody));
		WCSLogUtil.d("block index : " + blockIndex + "; uploadSlice slice response : " + sliceResponse);
		final Slice lastSlice = block.lastSlice();
		if (Crc32.calc(lastSlice.toByteArray()) == sliceResponse.crc32) {
			sliceCache.getBlockContext().set(blockIndex, sliceResponse.context);
			sliceCache.getBlockUploadedIndex().set(blockIndex, block.getIndex());
			Slice nextSlice = block.moveToNext();
			if (null != nextSlice) {
				uploadSlice(tag, context, uploadToken, block, blockIndex, nextSlice, sliceCache, sliceResponse.context, progressNotifier, uploadBlockListener);
			} else {
				WCSLogUtil.d("get empty slice while upload next slice");
				uploadBlockListener.onBlockUploaded(blockIndex, sliceResponse.context);
			}
		} else {
			progressNotifier.decreaseProgress(lastSlice.toByteArray().length);
			uploadSlice(tag, context, uploadToken, block, blockIndex, lastSlice, sliceCache, sliceResponse.context, progressNotifier, uploadBlockListener);
		}
		SliceCacheManager.getInstance().dumpAll();
	}

	private static void uploadSlice(final String tag, final Context context, final String uploadToken, final Block block, final int blockIndex,
			final Slice slice, final SliceCache sliceCache, String blockContext, final ProgressNotifier progressNotifier,
			final UploadBlockListener uploadBlockListener) {
		AsyncHttpResponseHandler uploadSliceResponseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				uploadNextSlice(tag, responseBody, blockIndex, block, context, uploadToken, sliceCache, progressNotifier, uploadBlockListener);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				String responseString = StringUtils.stringFrom(responseBody);
				WCSLogUtil.d("onFailure : " + responseString + "; error : " + error.getLocalizedMessage());
				uploadBlockListener.onBlockUploadFailured(blockIndex, OperationMessage.fromJsonString(responseString));
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				progressNotifier.increaseProgressAndNotify(bytesWritten);
				WCSLogUtil.d(String.format(Locale.CHINA, "block index : %s ,written : %s, totalSize : %s", blockIndex, bytesWritten, totalSize));
			}
		};
		SliceHttpEntity sliceHttpEntity = new SliceHttpEntity(slice, uploadSliceResponseHandler);
		String uploadSliceUrl = Config.PUT_URL + "/bput/" + blockContext + "/" + slice.getOffset();
		Header[] headers = new Header[] { new BasicHeader("Authorization", uploadToken) };
		dump(context, uploadToken, uploadSliceUrl, slice.size(), block.getOriginalFileName());
		getAsyncClient(context).post(context, uploadSliceUrl, headers, sliceHttpEntity, null, uploadSliceResponseHandler, tag);
	}

	private static String convertListToString(ArrayList<String> contexts) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < contexts.size(); i++) {
			sb.append(contexts.get(i));
			if (i + 1 < contexts.size()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private static void mergeBlock(final String tag, final Context context, String uploadToken, long fileSize, final SliceCache sliceCache, String contextList,
			HashMap<String, String> customParams, final SliceUploaderListener sliceUploaderListener) {
		WCSLogUtil.d("context list : " + contextList);
		StringEntity stringEntity = null;
		try {
			stringEntity = new StringEntity(contextList);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String mergeUrlString = Config.PUT_URL + "/mkfile/" + fileSize;
		StringBuffer mergeUrlStringBuffer = new StringBuffer(Config.PUT_URL);
		mergeUrlStringBuffer.append("/mkfile/");
		mergeUrlStringBuffer.append(fileSize);
		if (null != customParams && customParams.size() > 0) {
			for (String key : customParams.keySet()) {
				String value = customParams.get(key);
				if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
					mergeUrlStringBuffer.append("/");
					mergeUrlStringBuffer.append(key);
					mergeUrlStringBuffer.append("/");
					mergeUrlStringBuffer.append(EncodeUtils.urlsafeEncode(value));
				}
			}
		}
		Header[] headers = new Header[] { new BasicHeader("Authorization", uploadToken) };
		AsyncHttpResponseHandler mergeBlockHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				SliceCacheManager.getInstance().removeSliceCache(sliceCache);
				if (null != sliceUploaderListener) {
					sliceUploaderListener.onSliceUploadSucceed(BaseApi.parseWCSUploadResponse(StringUtils.stringFrom(responseBody)));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				String responseString = StringUtils.stringFrom(responseBody);
				WCSLogUtil.d("merge block failured : " + responseString);
				OperationMessage operationMessage = OperationMessage.fromJsonString(responseString);
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add(String.format(SLICE_UPLOAD_MESSAGE_FORMAT, -1, operationMessage.getMessage()));
				if (null != sliceUploaderListener) {
					sliceUploaderListener.onSliceUploadFailured(hashSet);
				}
			}
		};
		dump(context, uploadToken, mergeUrlString, fileSize, "unknown");
		getAsyncClient(context).post(context, mergeUrlString, headers, stringEntity, null, mergeBlockHandler, tag);
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

	private static void dump(Context context, String token, String urlString, long length, String fileName) {
		String userAgent = HttpProtocolParams.getUserAgent(getAsyncClient(context).getHttpClient().getParams());
		long timestamp = System.currentTimeMillis();
		String string2dump = String.format(
				"### url : %s,\r\n ### time : %s,\r\n ### token : %s,\r\n ### fileName : %s,\r\n ### length : %s,\r\n ### userAgent : %s\r\n", urlString,
				timestamp, token, fileName, length, userAgent);
		LogRecorder.getInstance().dumpLog(string2dump);
	}

	private interface UploadBlockListener {

		public void onBlockUploaded(int blockIndex, String context);

		public void onBlockUploadFailured(int blockIndex, OperationMessage operationMessage);

	}

}
