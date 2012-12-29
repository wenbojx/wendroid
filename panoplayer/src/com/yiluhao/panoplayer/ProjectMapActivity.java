package com.yiluhao.panoplayer;

import android.app.Activity;

import android.os.Bundle;

/**
 * 图片浏览、缩放、拖动、自动居中
 */
public class ProjectMapActivity extends Activity {

	ImageMap mImageMap;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_map);
        
        // find the image map in the view
        mImageMap = (ImageMap)findViewById(R.id.map);
        
        // add a click handler to react when areas are tapped
        mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
			@Override
			public void onImageMapClicked(int id) {
				// when the area is tapped, show the name in a 
				// text bubble
				mImageMap.showBubble(id);
			}

			@Override
			public void onBubbleClicked(int id) {
				// react to info bubble for area being tapped
			}
		});
    }

}
