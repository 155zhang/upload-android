package com.chinanetcenter.wcs.android.entity;

import java.util.ArrayList;

public class SliceCache {

	private String fileHash;
	private ArrayList<String> blockContext;
	private ArrayList<Integer> blockUploadedIndex;
	
	public String getFileHash() {
		if (null == fileHash) {
			return "";
		}
		return fileHash;
	}
	
	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}
	
	public ArrayList<String> getBlockContext() {
		if (null == blockContext) {
			return new ArrayList<String>();
		}
		return blockContext;
	}
	
	public void setBlockContext(ArrayList<String> blockContext) {
		this.blockContext = blockContext;
	}
	
	public ArrayList<Integer> getBlockUploadedIndex() {
		if (null == blockUploadedIndex) {
			return new ArrayList<Integer>();
		}
		return blockUploadedIndex;
	}
	
	public void setBlockUploadedIndex(ArrayList<Integer> blockUploadedIndex) {
		this.blockUploadedIndex = blockUploadedIndex;
	}
	
	
	@Override
	public String toString() {
		String cacheString = fileHash;
		cacheString += "; context ";
		for (String context : getBlockContext()) {
			cacheString += ("\t" + context);
		}
		cacheString += ";";
		cacheString += "; index ";
		for (Integer index : getBlockUploadedIndex()) {
			cacheString += ("\t" + index);
		}
		cacheString += ";";
		return cacheString;
	}
	
}
