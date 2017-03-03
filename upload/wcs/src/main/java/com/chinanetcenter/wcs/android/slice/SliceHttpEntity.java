package com.chinanetcenter.wcs.android.slice;

import com.chinanetcenter.wcs.android.http.ResponseHandlerInterface;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SliceHttpEntity extends AbstractHttpEntity {

    private ResponseHandlerInterface mResponseHandlerInterface;

    private Slice mSlice;

    public SliceHttpEntity(Slice slice, ResponseHandlerInterface responseHandlerInterface) {
        mSlice = slice;
        mResponseHandlerInterface = responseHandlerInterface;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return mSlice.toByteArray().length;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return null;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        int progressSize = 64 * 1024;
        int offset = 0;
        final byte[] sliceData = mSlice.toByteArray();
        int sliceSize = sliceData.length;
        while (offset < sliceSize - 1) {
            int uploadSize = Math.min(progressSize, sliceSize - offset);
            outputStream.write(sliceData, offset, uploadSize);
            outputStream.flush();
            mResponseHandlerInterface.sendProgressMessage(uploadSize, sliceSize);
            offset += uploadSize;
        }
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
