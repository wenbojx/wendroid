package com.yiluhao.utils;

import java.lang.reflect.Field;

import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtilities {
	public static final String LOG_TAG = BitmapUtilities.class.getSimpleName();
	
	public static void setHiddenNativeAllocField(BitmapFactory.Options options) {
        Class bitmapFactoryOptionsClass = android.graphics.BitmapFactory.Options.class;
        try {
			Field inAllocNativeField = bitmapFactoryOptionsClass.getField("inNativeAlloc");
			inAllocNativeField.setBoolean(options, true);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Could not set inNativeAlloc flag: " + e.getMessage());
		} catch (NoSuchFieldException e) {
			Log.e(LOG_TAG, "Could not set inNativeAlloc flag: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.e(LOG_TAG, "Could not set inNativeAlloc flag: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(LOG_TAG, "Could not set inNativeAlloc flag: " + e.getMessage());
		}
	}
}
