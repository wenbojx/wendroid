package com.yiluhao.panoplayer;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.yiluhao.utils.DrawHotspots;
import com.yiluhao.utils.IoUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.loopj.android.http.*;

/**
 * 图片浏览、缩放、拖动、自动居中
 */
public class ProjectMapActivity extends Activity {

	ImageMap mImageMap;
	private String project_id = "";
	private IoUtil ioUtil = null;
	private String mapInfoUrl = null;
	
	private String mapUrl = null;
	private String mapInfo = null;
	
	private ProgressDialog progressDialog;  
	private AsyncHttpClient client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			project_id = extras.getString("id");
			mapInfoUrl = "http://beta1.yiluhao.com/ajax/m/pm/id/"+project_id;
		}
		ioUtil = new IoUtil();
		client = new AsyncHttpClient();
		//progressDialog = ProgressDialog.show(ProjectMapActivity.this, getString(R.string.loading), getString(R.string.loading_map), true, false);
		progressDialog = new ProgressDialog(ProjectMapActivity.this);
		progressDialog.setMessage(getString(R.string.loading_map));
		progressDialog.setCancelable(true);
		//progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
				getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						cancel();
					}
				});
		progressDialog.show();
		
		LoadMapDatas();
	}
	/**
	 * 错误提示
	 */
	private void getWrong(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG)
		.show();
	}
	/**
	 * 取消下载
	 */
	private void cancel(){
		//cancelRequests
		client.cancelRequests(ProjectMapActivity.this, true);
		ioUtil.DelFile(project_id, mapUrl);
		ioUtil.DelFile(project_id, mapInfoUrl);
	}
	/**
	 * 加载地图信息
	 */
	private void LoadMapDatas(){

		String response = null;
		if( ioUtil.FileExists(project_id, mapInfoUrl)){
			Log.v("infoCached=", "cached");
			response = ioUtil.ReadStringFromSD(project_id, mapInfoUrl);
			mapInfo = response;
	        mapUrl = ExtractMapUrl(response);
	        LoadMapPic();
	        return ;
		}
		
		//AsyncHttpClient client = new AsyncHttpClient();
		client.get(mapInfoUrl, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	if(response =="" || response == null){
		    		return ;
		    	}
		    	ioUtil.SaveStringToSD(project_id, mapInfoUrl, response);
		    	mapInfo = response;
		        mapUrl = ExtractMapUrl(response);
		        LoadMapPic();
		    }
		    public void onFailure(Throwable error, String content){
		    	getWrong("获取地图配置信息失败");
		    }
		});
		
		return ;
	}
	private void LoadMapPic(){
		if(mapUrl == null || mapUrl == ""){
			return ;
		}
		if(ioUtil.FileExists(project_id, mapUrl)){
			Log.v("picCached=", "cached");
			Bitmap mapPicture = ioUtil.ReadBitmapFromSD(project_id, mapUrl);
			StartPaintMap(mapPicture);
			return ;
		}
		//AsyncHttpClient client = new AsyncHttpClient();
		String[] allowedContentTypes = new String[] { "image/png", "image/jpeg", "image/gif" };
		client.get(mapUrl, new BinaryHttpResponseHandler(allowedContentTypes) {
		    @Override
		    public void onSuccess(byte[] fileData) {
		    	if(fileData.length<1){
		    		return ;
		    	}
		    	Bitmap mapPicture = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, null);  
		    	ioUtil.SavePicToSD(project_id, mapUrl, mapPicture);
		    	StartPaintMap(mapPicture);
		    }
		    public void onFailure(Throwable error, String content){
		    	getWrong("获取地图文件失败");
		    }
		});
		
		
	}
	/**
	 * 解析地图图片信息
	 */
	private String ExtractMapUrl(String content){
		String mapUrl = null;
		if(content == "" || content ==null){
			return mapUrl;
		}
		try {
			JSONObject jsonObject = new JSONObject(content);
			mapUrl = jsonObject.getString("map");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return mapUrl;
	}
	/**
	 * 绘制地图
	 */
	private void StartPaintMap(Bitmap mapPicture){
		if(mapPicture == null ){
			return ;
		}
		ProjectMapActivity.this.setContentView(R.layout.project_map);
		//setContentView(R.layout.project_map);
		mImageMap = (ImageMap) findViewById(R.id.map);
		//mImageMap.SetMapHotspots(stream);
		if(mapInfo == null || mapInfo == ""){
			return ;
		}

		mImageMap.loadMap(mapInfo);
		
		ArrayList hostspotsList = mImageMap.GetCoords();
		DrawHotspots newMap = new DrawHotspots();
		Bitmap markImgG = BitmapFactory.decodeResource(getResources(),
				R.raw.marker_green);
		mapPicture = newMap.Draw(mapPicture, hostspotsList, markImgG);
		markImgG.recycle();
		System.gc();

		mImageMap.setImageBitmap(mapPicture);
		
		// add a click handler to react when areas are tapped
		mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
					@Override
					public void onImageMapClicked(int id) {
						//Log.v("Click_id", " : -------"+id);
						mImageMap.showBubble(id);
					}

					@Override
					public void onBubbleClicked(int id, String link) {
						if (link != null) {
							startPanoViewerActivity(link);
						}
					}
				});
		progressDialog.dismiss();
				
	}
	
	private void startPanoViewerActivity(String id) {
		Intent intent = new Intent(this, PanoPlayerActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("pano_id", id);
		bundle.putString("project_id", project_id);
		intent.putExtras(bundle);

		startActivity(intent);
	}
	
}
