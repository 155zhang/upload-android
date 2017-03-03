package com.chinanetcenter.wcs.android.utils;

import java.io.File;

public class Patch {

    public static native int patch(String apkPath, String destPath, String diffPath);

}
