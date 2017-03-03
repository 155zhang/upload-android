package com.chinanetcenter.wcs.android.slice;

import org.apache.http.util.ByteArrayBuffer;

public class Slice {

	private byte[] mData;
	private ByteArrayBuffer mArrayBuffer;
	private long mOffset;

	Slice(long offset, ByteArrayBuffer arrayBuffer) {
		mOffset = offset;
		mArrayBuffer = arrayBuffer;
	}

	Slice(long offset, byte[] data) {
		mOffset = offset;
		mData = data;
	}

	public long size() {
		if (mArrayBuffer != null) {
			return mArrayBuffer.length();
		} else if (mData != null) {
			return mData.length;
		}
		return 0;
	}

	public byte[] toByteArray() {
		if (null != mArrayBuffer) {
			return mArrayBuffer.buffer();
		} else if (null != mData) {
			return mData;
		}
		return new byte[0];
	}

	public long getOffset() {
		return mOffset;
	}

}
