package com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.Config;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.http.SimpleRequestParams;
import com.chinanetcenter.wcs.android.listener.DeleteFileListener;
import com.chinanetcenter.wcs.android.listener.FileInfoListener;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;

public class FileManager extends BaseApi {

	/**
	 * 删除在云空间中的文件
	 * @param deleteToken 删除图片所需要的token
	 * @param bucketName 云空间名
	 * @param fileKey 在云空间中的文件名
	 * @param deleteFileListener 删除之后的回调
	 */
	public static void delete(Context context, String deleteToken, String bucketName, String fileKey, DeleteFileListener deleteFileListener) {
		if(null == deleteToken || deleteToken.trim().length() == 0){
			if(null != deleteFileListener){
				deleteFileListener.onFailure(-1, new OperationMessage(-1, "deleteToken invalidate"));
			}
			return;
		}
		if(null == bucketName || bucketName.trim().length() == 0){
			if(null != deleteFileListener){
				deleteFileListener.onFailure(-1, new OperationMessage(-1, "bucketName invalidate"));
			}
			return;
		}
		if(null == fileKey || fileKey.trim().length() == 0){
			if(null != deleteFileListener){
				deleteFileListener.onFailure(-1, new OperationMessage(-1, "fileKey invalidate"));
			}
			return;
		}
		
		String entry = bucketName + ":" + fileKey;
		String encodedEntryURI = EncodeUtils.urlsafeEncodeString(entry.getBytes());
		String url = Config.MGR_URL + "/fileManageCmd/delete/" + encodedEntryURI;
		Header[] headers = new Header[] {
			new BasicHeader("Authorization", deleteToken)
		};
		getAsyncClient(context).post(null, url, headers, new SimpleRequestParams(), null, deleteFileListener);
	}

	/**
	 * 获取文件信息
	 * @param fileInfoToken 获取文件信息所对应的token
	 * @param bucketName 云空间名
	 * @param fileKey 在云空间中的文件名
	 * @param fileInfoListener 获取文件信息的回调
	 */
	public static void fetchFileInfo(Context context, String fileInfoToken, String bucketName, String fileKey, FileInfoListener fileInfoListener) {
		if(null == fileInfoToken || fileInfoToken.trim().length() == 0){
			if(null != fileInfoListener){
				fileInfoListener.onFailure(-1, new OperationMessage(-1, "fileInfoToken invalidate"));
			}
			return;
		}
		if(null == bucketName || bucketName.trim().length() == 0){
			if(null != fileInfoListener){
				fileInfoListener.onFailure(-1, new OperationMessage(-1, "bucketName invalidate"));
			}
			return;
		}
		if(null == fileKey || fileKey.trim().length() == 0){
			if(null != fileInfoListener){
				fileInfoListener.onFailure(-1, new OperationMessage(-1, "fileKey invalidate"));
			}
			return;
		}
		String entry = bucketName + ":" + fileKey;
		String encodedEntryURI = EncodeUtils.urlsafeEncodeString(entry.getBytes());
		String url = Config.MGR_URL + "/fileManageCmd/stat/" + encodedEntryURI;
		Header[] headers = new Header[] {
			new BasicHeader("Authorization", fileInfoToken)
		};
		getAsyncClient(context).post(null, url, headers, new SimpleRequestParams(), null, fileInfoListener);
	}

}
