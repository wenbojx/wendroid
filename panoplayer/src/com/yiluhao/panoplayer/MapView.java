package com.yiluhao.panoplayer;


import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class MapView extends View {
	
	
	// 控制地图移动速度
	int scrollRate = 50;
	
	boolean scrollHorizontalEnabled = true;
	boolean scrollVerticalEnabled = true;
	Bitmap imageMap, bufferImageMap;
	
	int maxWidth;
	int maxHeight;
	int imgMapWidth;
	int imgMapHeight;
	
	private float scrollX, scrollY;

	Paint paint = new Paint();

	public MapView(Context context) {
		super(context);
	}

	public MapView(Context context, Bitmap imgMap,
			int width, int height) {
		super(context);
		this.imageMap = imgMap;
		
		if (bufferImageMap==null) {
			bufferImageMap = Bitmap.createBitmap(imgMap);
		}
		calculateSize(width, height);
	}

	public MapView(Context context, Bitmap imgMap,
			int width, int height, boolean scrollHorizontal,
			boolean scrollVertical) {
		super(context);
		this.imageMap = imgMap;
		this.scrollHorizontalEnabled = scrollHorizontal;
		this.scrollVerticalEnabled = scrollVertical;

		if (bufferImageMap==null) {
			bufferImageMap = Bitmap.createBitmap(imgMap);
		}
		calculateSize(width, height);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	protected void calculateSize(int width, int height) {

		scrollX = 0;
		scrollY = 0;
		// map size
		imgMapWidth = imageMap.getWidth();
		imgMapHeight = imageMap.getHeight();

		// window size
		maxWidth = Math.min(imgMapWidth, width);
		maxHeight = Math.min(imgMapHeight, height);

		// layout size
		setLayoutParams(new LayoutParams(imgMapWidth, imgMapHeight));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(bufferImageMap, 0, 0, paint);
	}
	

	protected void handleScroll(float distX, float distY) {
		
		int maxScrollX = Math.max(imgMapWidth - maxWidth, 0);
		int maxScrollY = Math.max(imgMapHeight - maxHeight, 0);

		// X-Axis
		if (scrollHorizontalEnabled) {
			if (distX > 6.0) {
				if (scrollX < maxScrollX - scrollRate) {
					scrollX += scrollRate;
				} else {
					scrollX = maxScrollX;
				}
				
			} else if (distX < -6.0) {
				if (scrollX >= scrollRate) {
					scrollX -= scrollRate;
				} else {
					scrollX = 0;
				}
				
			}
		}

		// Y-AXIS
		if (scrollVerticalEnabled) {
			if (distY > 6.0) {
				if (scrollY < maxScrollY - scrollRate) {
					scrollY += scrollRate;
				} else {
					scrollY = maxScrollY;
				}
				
			} else if (distY < -6.0) {
				if (scrollY >= scrollRate) {
					scrollY -= scrollRate;
				} else {
					scrollY = 0;
				}
				
			}
		}

		// Swap image
		swapImage();
		invalidate();
	}

	protected void swapImage() {
		bufferImageMap = Bitmap.createBitmap(imageMap, (int)scrollX, (int)scrollY,
				maxWidth, maxHeight);
	}
	
}
