package com.yiluhao.panoplayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openpanodroid.panoutils.android.CubicPanoNative.TextureFaces;

import com.yiluhao.utils.DrawHotspots;
import com.yiluhao.utils.IoUtil;
import com.yiluhao.utils.IoUtil.MapCallBack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * 图片浏览、缩放、拖动、自动居中
 */
public class ProjectMapActivity extends Activity {

	ImageMap mImageMap;
	private String project_id = "";
	List hostspotsList = null;
	Bitmap mapPic = null;
	InputStream stream = null;
	private ProgressDialog progressDialog;  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			project_id = extras.getString("id");
		}
		Log.v("PROJECT", project_id + "map");

		// find the image map in the view
		String fileName = "/" + project_id + "/" + "map.xml";
		Integer type = 4;
		String id = project_id;
		IoUtil ioutil = new IoUtil();
		try {
			ioutil.ReadStringFromSD(fileName, type, id);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String path = ioutil.GetFilePath(fileName);

		stream = ioutil.GetFileStream(path);
		Log.v("Stream", stream.toString());

		String mapName = "/" + project_id + "/map.jpg";
		String map_url = "";
		
		if (!ioutil.file_exit(mapName)) {
			String configStr = "";
			String project_file = "/" + project_id + "/" + "panos.cfg";
			type = 2;
			id = project_id;
			try {
				configStr = ioutil.ReadStringFromSD(project_file, type, id);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (configStr != "") {
				try {
					JSONObject jsonObject = new JSONObject(configStr);
					map_url = jsonObject.getString("map");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			} else {
				Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG)
						.show();
			}
		}
		progressDialog = ProgressDialog.show(ProjectMapActivity.this, getString(R.string.loading), getString(R.string.loading_map), true, false);  
		try {
			ioutil.ReadMapFromSDThread(mapName, map_url,
					new MapCallBack() {
						@Override
						public void LoadMap(Bitmap bitmap) {
							progressDialog.dismiss();  
							mapPic = bitmap;
							DrawMap();
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		// mapPic = BitmapFactory.decodeResource(getResources(), R.raw.usamap);
	}
	
	@Override
	protected void onDestroy() {
		if (mapPic != null) {
			mapPic.recycle();
			mapPic = null;
			System.gc();
		}
		super.onDestroy();
	}
	private void DrawMap(){
		setContentView(R.layout.project_map);
		mImageMap = (ImageMap) findViewById(R.id.map);
		mImageMap.SetMapHotspots(stream);
		mImageMap.loadMap("map");
		hostspotsList = mImageMap.GetCoords();
		DrawHotspots newMap = new DrawHotspots();
		Bitmap markImgG = BitmapFactory.decodeResource(getResources(),
				R.raw.marker_green);
		mapPic = newMap.Draw(mapPic, hostspotsList, markImgG);
		markImgG.recycle();
		System.gc();
		
		mImageMap.setImageBitmap(mapPic);
		
		// add a click handler to react when areas are tapped
		mImageMap
				.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
					@Override
					public void onImageMapClicked(int id) {
						Log.v("Click_id", " : -------"+id);
						mImageMap.showBubble(id);
					}

					@Override
					public void onBubbleClicked(int id, String link) {
						if (link != null) {
							startPanoViewerActivity(link);
						}
					}
				});
	}

	private void startPanoViewerActivity(String id) {
		Intent intent = new Intent(this, PanoViewActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("pano_id", id);
		bundle.putString("project_id", project_id);
		intent.putExtras(bundle);

		startActivity(intent);
	}

}
