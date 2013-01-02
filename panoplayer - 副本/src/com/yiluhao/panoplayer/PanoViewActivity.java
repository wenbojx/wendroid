package com.yiluhao.panoplayer;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.yiluhao.utils.CubicPanoNative;
import com.yiluhao.utils.ImagesUtil;
import com.yiluhao.utils.IoUtil;
import com.yiluhao.utils.PanodroidGLView;

import junit.framework.Assert;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;

public class PanoViewActivity extends Activity {
	
	private ProgressBar progress;
	private ProgressDialog dialog=null;
	private CubicPanoNative cubicPano = null;
	private PanodroidGLView glView = null;
	private String pano_id = "";
	private String project_id = "";
	private JSONObject panoInfo = null;
	private List<Map<String, Object>> mData;
	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				pano_id = extras.getString("pano_id");
				project_id = extras.getString("project_id");
			}
	    	Log.v("ids=",  pano_id+"-"+project_id);
	    	LoadDatas();
	    }
	 private void LoadDatas(){
		 dialog = ProgressDialog.show(this, "",
	                "加载数据，请稍等 …", true, true);
	        //启动一个后台线程
	        handler.post(new Runnable(){
	            @Override
	            public void run() {
	                 //这里下载数据
	                	//mData = getPanosData();
	            		getPanoDetail();
	            		getPanoFace();
	            		setupOpenGLView();
	                    Message msg=new Message();
	                    msg.what=1;
	                    handler.sendMessage(msg);
	            }
	        });  
	 }
	 /**
	     * 获取全景图信息
	     */
	    private void getPanoDetail() {
			String configStr = "";
			String fileName = "/"+project_id+"/"+pano_id+"/"+"pano.cfg";
			Integer type = 3;
			String id = pano_id;
			IoUtil ioutil = new IoUtil();
			try{
				configStr = ioutil.ReadStringFromSD(fileName, type, id);
			}catch (IOException e){
				e.printStackTrace();
			}
			
			try {
				panoInfo = new JSONObject(configStr)
						.getJSONObject("pano");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
	    }

	    private void getPanoFace(){
	    	String front_url="",back_url="",left_url="",right_url="",up_url="",down_url="";
	    	try{
	    		front_url = panoInfo.getString("s_f");
	        	back_url = panoInfo.getString("s_b");
	        	left_url = panoInfo.getString("s_l");
	        	right_url = panoInfo.getString("s_r");
	        	up_url = panoInfo.getString("s_u");
	        	down_url = panoInfo.getString("s_d");
	    		//JSONArray hotspotInfo = panoInfo.getJSONArray("hotspot");
		    } catch (JSONException e) {
				throw new RuntimeException(e);
			}
	    	
			Bitmap bfront=null , bback=null, bleft=null, bright=null, bup=null, bdown=null;
			String front = "/"+project_id+"/"+pano_id+"/s_f.jpg";
			String back = "/"+project_id+"/"+pano_id+"/s_b.jpg";
			String left = "/"+project_id+"/"+pano_id+"/s_l.jpg";
			String right = "/"+project_id+"/"+pano_id+"/s_r.jpg";
			String up = "/"+project_id+"/"+pano_id+"/s_u.jpg";
			String down = "/"+project_id+"/"+pano_id+"/s_d.jpg";
			try{
				IoUtil ioutil = new IoUtil();
				ImagesUtil imgutil = new ImagesUtil();
				bfront = ioutil.ReadPicFromSD(front, front_url);
				bfront  = imgutil.translateScale(bfront);
				bback = ioutil.ReadPicFromSD(back, back_url);
				bback  = imgutil.translateScale(bback);
				bleft = ioutil.ReadPicFromSD(left, left_url);
				bleft  = imgutil.translateScale(bleft);
				bright = ioutil.ReadPicFromSD(right, right_url);
				bright  = imgutil.translateScale(bright);
				
				bup = ioutil.ReadPicFromSD(up, up_url);
				bup  = imgutil.translateScale(bup);
				bup  = imgutil.translateRotate(bup);
				
				bdown = ioutil.ReadPicFromSD(down, down_url);
				bdown  = imgutil.translateRotate(bdown);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Log.v("aaa", "bbbbb");
			cubicPano = new CubicPanoNative(bfront, bback, bup, bdown, bleft, bright);
	    }
	 private Handler handler=new Handler(){
	        @Override
	        public void handleMessage(Message msg){
	            switch(msg.what){
	            case 1:
	                dialog.dismiss();
	                break;
	            }
	        }
	    };
	 private void setupOpenGLView() {
	    	Assert.assertTrue(cubicPano != null);
	    	glView = new PanodroidGLView(this, cubicPano);
	        setContentView(glView);
	    }
}
