package com.yiluhao.panoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView textView= (TextView) this.findViewById(R.id.about_info_detail);
		textView.setText(R.string.about_info);
	}
	
}
