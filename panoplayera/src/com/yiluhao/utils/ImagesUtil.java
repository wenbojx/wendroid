package com.yiluhao.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImagesUtil {
	
	public Bitmap translateScale (Bitmap pic) {
		Bitmap bitmap = null;
		int width = pic.getWidth();  
		int height = pic.getHeight();
		Matrix matrix = new Matrix();  
		matrix.postScale(1, -1); /*垂直翻转*/ 
		bitmap = Bitmap.createBitmap(pic, 0, 0, width, height, matrix, true);  
		return bitmap;
	}
	public Bitmap translateRotate (Bitmap pic) {
		Bitmap bitmap = null;
		int width = pic.getWidth();  
		int height = pic.getHeight();
		Matrix matrix = new Matrix();  
		matrix.postRotate(180); /*旋转180*/ 
		bitmap = Bitmap.createBitmap(pic, 0, 0, width, height, matrix, true);  
		return bitmap;
	}
}
