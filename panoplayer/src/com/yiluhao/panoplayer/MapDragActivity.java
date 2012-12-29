package com.yiluhao.panoplayer;


import android.app.Activity;


import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class MapDragActivity extends Activity implements OnGestureListener{
	
	private MapView myView;
    private GestureDetector mGestureDetector;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGestureDetector = new GestureDetector(this);
		DisplayMetrics display = this.getApplication().getResources().getDisplayMetrics();

		RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(display.widthPixels/2, LayoutParams.WRAP_CONTENT);
	    param2.addRule(RelativeLayout.ALIGN_LEFT);
	    param2.addRule(RelativeLayout.ALIGN_PARENT_TOP);

		Bitmap imageMap = BitmapFactory
				.decodeResource(getResources(), R.raw.panorama);

		myView = new MapView(this, imageMap, display.widthPixels,
				display.heightPixels);
		myView.setClickable(true);
		myView.setOnTouchListener(touchListener);
		
		GestureOverlayView total = new GestureOverlayView(this);
		total.setGestureVisible(false);
		total.setLayoutParams(param1);
		total.addView(myView);
		setContentView(total);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private OnTouchListener touchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return mGestureDetector.onTouchEvent(event);
		}
		
	};
	
	static Bitmap bufferImageMap;

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	
	
	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {
		myView.handleScroll(distanceX, distanceY);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}