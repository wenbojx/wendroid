package com.yiluhao.panoplayer;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.yiluhao.utils.IoUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PanoInfoActivity extends Activity {

	private String project_id = "";
	private String projectInfo = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pano_info);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			project_id = extras.getString("id");
		}
		if(project_id != ""){
			display();
		}
		TextView textView= (TextView) this.findViewById(R.id.pano_info_detail);
		textView.setText(projectInfo);
		
	}
	private String display(){
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
		
		if(configStr == "" || configStr == null){
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
			return projectInfo;
		}
		else{
			try {
				JSONObject jsonObject = new JSONObject(configStr);
				projectInfo = jsonObject.getString("info");
				
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return projectInfo;
	}
	
}