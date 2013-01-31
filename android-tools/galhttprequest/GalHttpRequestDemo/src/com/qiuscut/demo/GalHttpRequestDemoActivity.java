package com.qiuscut.demo;

import com.qiuscut.demo.RequestActivity.RequestType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class GalHttpRequestDemoActivity extends Activity {
	ListView listView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupViews();
	}

	private void setupViews() {
		setContentView(R.layout.main);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(new ItemAdaper(this));
		initListener();
	}

	private void initListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String item = (String) parent.getAdapter().getItem(position);
				int type = 0;
				if ("同步请求InputStream".equalsIgnoreCase(item)) {
					type = RequestType.SYNC_REQUESTIS;
				} else if ("同步请求String".equalsIgnoreCase(item)) {
					type = RequestType.SYNC_REQUESTSTRING;
				} else if ("同步请求Bitmap".equalsIgnoreCase(item)) {
					type = RequestType.SYNC_REQUESTBITMAP;
				} else if ("异步请求InputStream".equalsIgnoreCase(item)) {
					type = RequestType.ASYN_REQUESTIS;
				} else if ("异步请求String".equalsIgnoreCase(item)) {
					type = RequestType.ASYN_REQUESTSTRING;
				} else if ("异步请求Bitmap".equalsIgnoreCase(item)) {
					type = RequestType.ASYN_REQUESTBITMAP;
				} else if ("组装http参数".equalsIgnoreCase(item)) {
					type = RequestType.ASYN_EASYPARAMS;
				} else if ("Post内容".equalsIgnoreCase(item)) {
					type = RequestType.ASYN_EASYPOST;
				}
				Intent intent = new Intent(GalHttpRequestDemoActivity.this, RequestActivity.class);
				intent.putExtra("type", type);
				startActivity(intent);
			}
		});
	}
}