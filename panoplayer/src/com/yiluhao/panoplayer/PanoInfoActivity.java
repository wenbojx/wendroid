package com.yiluhao.panoplayer;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.yiluhao.utils.IoUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class PanoInfoActivity extends Activity {

	private String project_id = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			project_id = extras.getString("id");
		}
		String projectInfo = "";
		setContentView(R.layout.pano_info);
		TextView textView= (TextView) this.findViewById(R.id.pano_info_detail);
		
		String configStr = "";
		String fileName = "/"+project_id+"/"+"panos.cfg";
		Integer type = 2;
		String id = project_id;
		IoUtil ioutil = new IoUtil();
		try{
			configStr = ioutil.ReadStringFromSD(fileName, type, id);
		}catch (IOException e){
			e.printStackTrace();
		}

		if(configStr == ""){
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
		}
		try {
			JSONObject jsonObject = new JSONObject(configStr);
			projectInfo = jsonObject.getString("info");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		
		textView.setText(projectInfo);
	}
	
}