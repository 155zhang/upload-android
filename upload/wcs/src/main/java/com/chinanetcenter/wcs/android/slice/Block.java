package com.chinanetcenter.wcs.android.slice;

import android.util.Log;

import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Locale;

public class Block {

	public static final int SLICE_SIZE = 256 * 1024;
	private static final int MAX_BLOCK_COUNT = 100;
	private static final long DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024;
	private static long sDefaultBlockSize = DEFAULT_BLOCK_SIZE;

	private RandomAccessFile mRandomAccessFile;
	private long mStart;
	private long mSize;
	private int mSliceIndex;
	private long mOriginalFileSize;
	private String mFileName;
	private ByteArrayBuffer mByteArrayBuffer;

	Block(RandomAccessFile randomAccessFile, String fileName, long start, long blockSize) throws IOException {
		mRandomAccessFile = randomAccessFile;
		mOriginalFileSize = randomAccessFile.length();
		mFileName = fileName;
		mStart = start;
		mSize = blockSize;
	}

	public static Block[] blocks(File file) {
		return blocks(file, DEFAULT_BLOCK_SIZE);
	}

	public static Block[] blocks(File file, long defaultBlockSize) {
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			Log.e("CNCLog", "file not found : " + file);
			return null;
		}

		long fileSize = 0;
		try {
			fileSize = randomAccessFile.length();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fileSize == 0) {
			try {
				randomAccessFile.close();
			} catch (IOException e) {
			}
			return null;
		}

		sDefaultBlockSize = calculateBlockSize(fileSize);
		int blockCount = (int) ((fileSize + sDefaultBlockSize - 1) / sDefaultBlockSize);
		if (blockCount > 120) {
			sDefaultBlockSize = sDefaultBlockSize * 2;
		}
		blockCount = (int) ((fileSize + sDefaultBlockSize - 1) / sDefaultBlockSize);
		
		WCSLogUtil.d(String.format(Locale.CHINA, "file size : %s, block count : %s", fileSize, blockCount));
		Block[] blocks = new Block[blockCount];
		for (int i = 0; i < blockCount; i++) {
			long blockSize = sDefaultBlockSize;
			if (i + 1 == blockCount) {
				long remain = fileSize % sDefaultBlockSize;
				blockSize = remain == 0 ? sDefaultBlockSize : remain;
			}
			try {
				blocks[i] = new Block(randomAccessFile, file.getName(), i * sDefaultBlockSize, blockSize);
				blocks[i].mByteArrayBuffer = new ByteArrayBuffer(SLICE_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return blocks;
	}

	public void clear() {
		try {
			mRandomAccessFile.close();
		} catch (IOException e) {
		}
	}
	
	public Slice moveToNext() {
		return getSlice(mSliceIndex++);
	}

	public void setIndex(int index) {
		mSliceIndex = index;
	}

	public int getIndex() {
		return mSliceIndex;
	}

	public Slice moveToIndex(int index) {
		mSliceIndex = index;
		return moveToNext();
	}

	public Slice lastSlice() {
		return getSlice(mSliceIndex - 1);
	}

	// 因为精度的原因，算出来的blockCount最多可能达到10.
	private static long calculateBlockSize(long fileSize) {
		long blockSize = DEFAULT_BLOCK_SIZE;
		if (fileSize > 4 * MAX_BLOCK_COUNT * 1024 * 1024) {
			long atomCount = fileSize / DEFAULT_BLOCK_SIZE / MAX_BLOCK_COUNT;
			blockSize = atomCount * DEFAULT_BLOCK_SIZE;
		}
		
		return blockSize;
	}

	private Slice getSlice(int index) {
		long offset = mStart + index * SLICE_SIZE;
		if (index * SLICE_SIZE >= mSize) {
			return null;
		}
		int sliceSize = SLICE_SIZE;
		if ((offset + SLICE_SIZE) > (mStart + mSize)) {
			sliceSize = (int) (mSize % SLICE_SIZE);
		}
		byte[] sliceData = mByteArrayBuffer.buffer();
		Arrays.fill(sliceData, (byte) 0);
		if (sliceSize < SLICE_SIZE) {
			sliceData = new byte[sliceSize];
		}

		try {
			mRandomAccessFile.seek(offset);
			mRandomAccessFile.read(sliceData, 0, sliceSize);
			WCSLogUtil.d("offset : " + offset + "; slice size : " + sliceSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (sliceSize < SLICE_SIZE) {
			return new Slice(index * SLICE_SIZE, sliceData);
		} else {
			return new Slice(index * SLICE_SIZE, mByteArrayBuffer);
		}
	}

	public long size() {
		return mSize;
	}

	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("start", mStart);
			jsonObject.put("size", mSize);
			jsonObject.put("slice index", mSliceIndex);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			return jsonObject.toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "Block<>";
	}

	public long getOriginalFileSize() {
		return mOriginalFileSize;
	}

	public String getOriginalFileName() {
		return mFileName;
	}

}
