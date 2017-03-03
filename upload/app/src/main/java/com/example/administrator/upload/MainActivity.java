package com.example.administrator.upload;


import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beiing.flikerprogressbar.FlikerProgressBar;
import com.blankj.utilcode.utils.EncodeUtils;
import com.blankj.utilcode.utils.StringUtils;
import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;
import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;
import com.example.administrator.upload.http.Constant;
import com.example.administrator.upload.http.ErrorResponse;
import com.example.administrator.upload.http.FilterSubscriber;
import com.example.administrator.upload.http.RetrofitHelper;
import com.example.administrator.upload.http.SuccessResponse;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bartwell.exfilepicker.ExFilePickerActivity;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Bind(R.id.tv_token)
    Button mTvUpload;
    @Bind(R.id.tv_path)
    TextView mTvPath;
    @Bind(R.id.bt_select)
    Button mBtSelect;
    @Bind(R.id.round_flikerbar)
    FlikerProgressBar mProgressBar;

    private MainActivity mContext;
    private File mFile;
    private String mFileName;
    private long mFileLength;
    private static final int EX_FILE_PICKER_RESULT = 0;
    private Gson mGson;
    private SuccessResponse mSuccessResponse;
    private String mPlayUrl;
    private ClipboardManager mClipboardManager;
    private ErrorResponse mErrorResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        mGson = new Gson();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }


    @OnClick({R.id.tv_token, R.id.bt_select})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_token:
                if (StringUtils.isEmpty(mTvPath.getText())) {
                    ToastUtil.showToast(mContext, "请选择文件");
                    return;
                }

                if (mFile != null)

                {
                    mFileLength = mFile.length() / (1024 * 1024);
                }

                makeUpload(false, "上传中...");

                getToken();

                break;
            case R.id.bt_select:
                Intent intent = new Intent(getApplicationContext(), ExFilePickerActivity.class);
                startActivityForResult(intent, EX_FILE_PICKER_RESULT);
                break;
        }
    }


    /**
     * 获取token
     */
    private void getToken() {
        RetrofitHelper.getInstance()
                .getToken(Constant.bucket, EncodeUtils.base64Encode2String(mFileName.getBytes()), Constant.overwrite, Constant.expire, Constant.ak)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FilterSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e(TAG, "get token error...");
                        makeUpload(true, "上传失败,再次上传...");
                    }

                    @Override
                    public void onNext(String httpResponse) {
                        Log.d(TAG, httpResponse);
                        if (null != httpResponse) {
                            if (null != mFile) {
                                Log.d(TAG, "get token success");
                                if (mFileLength > 500) { //大于500M 分片上传
                                    sliceUpload(mFile, httpResponse);
                                } else {
                                    simpleUpload(mFile, httpResponse);
                                }

                            } else {
                                Log.e(TAG, " filename  is null");
                                makeUpload(true, "上传失败,再次上传...");
                            }
                        } else {
                            Log.e(TAG, " response token is null");
                            makeUpload(true, "上传失败,再次上传...");
                        }
                    }
                });
    }

    /**
     * 普通上传
     */
    private void simpleUpload(File srcFile, String token) {
        FileUploader.upload(
                mContext,
                token,
                srcFile,
                null,
                new FileUploaderListener() {
                    @Override
                    public void onSuccess(int status, JSONObject responseJson) {
                        uploadSuccess("simpleUpload responseJson : " + responseJson.toString(), responseJson.toString());
                    }

                    @Override
                    public void onFailure(OperationMessage operationMessage) {
                        Log.e(TAG, "simpleUpload errorMessage : " + operationMessage.toString());
                        makeUpload(true, "上传失败,再次上传...");
                        mProgressBar.reset();
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mErrorResponse = mGson.fromJson(operationMessage.toString(),ErrorResponse.class);
                        ToastUtil.showToast(mContext,mErrorResponse.getMessage());
                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                        uploading(bytesWritten, totalSize);
                    }
                });
    }

    /**
     * 切片上传
     *
     * @param srcFile
     * @param token
     */
    public void sliceUpload(File srcFile, String token) {
        FileUploader.sliceUpload(mContext, token, srcFile, null, new SliceUploaderListener() {
            @Override
            public void onSliceUploadSucceed(JSONObject responseJSON) {
                uploadSuccess("sliceUpload responseJson : " + responseJSON.toString(), responseJSON.toString());
            }

            @Override
            public void onSliceUploadFailured(HashSet<String> errorMessages) {
                for (String string : errorMessages) {
                    Log.e(TAG, "sliceUpload errorMessage : " + string);
                    ToastUtil.showToast(mContext, string);
                }
                makeUpload(true, "上传失败,再次上传...");
                mProgressBar.reset();
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onProgress(long uploaded, long total) {
                uploading(uploaded, total);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTvUpload.setText("开始上传");
        mProgressBar.reset();
        mProgressBar.setVisibility(View.INVISIBLE);
        if (requestCode == EX_FILE_PICKER_RESULT) {
            if (data != null) {
                ExFilePickerParcelObject object = (ExFilePickerParcelObject) data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                if (object.count > 0) {
                    // Here is object contains selected files names and path
                    mTvPath.setText("文件名称 : " + object.names.get(0));
                    mFile = new File(object.path + object.names.get(0));
                    mFileName = object.names.get(0);
                }
            }
        }
    }

    /**
     *设置上传
     * @param clickable
     * @param text
     */
    private void makeUpload(boolean clickable, String text) {
        mTvUpload.setClickable(clickable);
        mTvUpload.setText(text);
        mBtSelect.setClickable(clickable);
    }

    /**
     * 上传中
     *
     * @param bytesWritten
     * @param totalSize
     */
    private void uploading(long bytesWritten, long totalSize) {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(100 * bytesWritten / totalSize);
        if (totalSize > 0) {
            Log.d(TAG, " progress: " + (100 * bytesWritten / totalSize) + "    %");
            makeUpload(false, bytesWritten / (1024 * 1024) + "M /" + totalSize / (1024 * 1024) + "M");
        } else {
            Log.e(TAG, " progress:  -1 ");
            makeUpload(true, "上传失败,再次上传...");
        }
    }


    /**
     * 上传成功
     *
     * @param msg
     * @param json
     */
    private void uploadSuccess(String msg, String json) {
        Log.d(TAG, msg);
        makeUpload(true, "上传成功,选择其他文件继续上传");
        mTvPath.setText("");
        mSuccessResponse = mGson.fromJson(json, SuccessResponse.class);
        mPlayUrl = mSuccessResponse.getUrl();
        mClipboardManager.setText(mPlayUrl);
        ToastUtil.showToast(mContext, "文件地址已复制到粘贴板,可在浏览器中打开 :" + mPlayUrl);
        mProgressBar.finishLoad();
    }

}
