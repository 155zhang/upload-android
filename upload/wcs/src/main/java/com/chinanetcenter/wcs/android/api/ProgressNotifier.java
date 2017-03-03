package com.chinanetcenter.wcs.android.api;

import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;

public class ProgressNotifier {

    private SliceUploaderListener mUploaderListener;

    private long mTotal;

    private long mWritten;

    public ProgressNotifier(long total, SliceUploaderListener uploaderListener) {
        mUploaderListener = uploaderListener;
        mTotal = total;
    }

    public void decreaseProgress(long decrease) {
        mWritten -= decrease;
    }

    public void increaseProgressAndNotify(long written) {
        mWritten += written;
        mUploaderListener.onProgress(mWritten, mTotal);
    }

}
