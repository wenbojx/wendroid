package com.yiluhao.utils;

import java.util.List;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.util.Log;

public class DrawHotspots extends Activity{
	private int hotspotWidth = 15;
	private int hotspotHeight = 20;

	public Bitmap Draw (Bitmap map, List list, Bitmap markImgG){
		if(list.size()<1){
			return map;
		}
		//Bitmap mapHotspot = map;
		String[] ss =null;
		//Bitmap markImg = BitmapFactory.decodeResource(getResources(), R.raw.);
		Bitmap newb = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);  
		canvas.drawBitmap(map, 0, 0, null);// 在 0，0坐标开始画入原图片src  

		for (int i =0; i<list.size(); i++){
			ss=(String[]) list.get(i);
			String a[] = ss[1].split(",");
			
			int positionLeft =   Integer.parseInt(a[0]) + hotspotWidth;
			int positionTop =  Integer.parseInt(a[1]) + hotspotHeight;
			Log.v("position", "left:"+positionLeft+" top:"+positionTop);
			canvas.drawBitmap(markImgG, positionLeft, positionTop, null);
		}
		//存储新合成的图片
		canvas.save(Canvas.ALL_SAVE_FLAG);  
		canvas.restore();  
		//canvas.restore(); 
		//watermark.recycle();  
       // watermark = null;  
		
        
		return newb;
	}
}
