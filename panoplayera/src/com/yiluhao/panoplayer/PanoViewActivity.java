package com.yiluhao.panoplayer;

import java.io.IOException;


import org.json.JSONException;
import org.json.JSONObject;
import org.openpanodroid.PanodroidGLView;
import org.openpanodroid.panoutils.android.CubicPanoNative;
import org.openpanodroid.panoutils.android.CubicPanoNative.TextureFaces;

import com.yiluhao.utils.ImagesUtil;
import com.yiluhao.utils.IoUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class PanoViewActivity extends Activity {


	private static final String LOG_TAG = PanoViewActivity.class
			.getSimpleName();

	private CubicPanoNative cubicPano = null;
	private PanodroidGLView glView = null;
	private String pano_id = "";
	private String project_id = "";
	private JSONObject panoInfo = null;
	int  moveX=0, moveY=0;
	private Bitmap bfront = null, bback = null, bleft = null, bright = null, bup = null, bdown = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.pano_view);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			pano_id = extras.getString("pano_id");
			project_id = extras.getString("project_id");
		}
		Log.v("ids=", pano_id + "-" + project_id);
		getPanoDetail();
		LoadFaceAsyncTask asyncTask = new LoadFaceAsyncTask();  
        asyncTask.execute();
        
	}
	private void setupOpenGLView() {
		glView = new PanodroidGLView(this, cubicPano);
		setContentView(glView);
		glView.StartAnimate();
		/*View mapView = this.getLayoutInflater().inflate(R.layout.pano_map, null);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(  
		        FrameLayout.LayoutParams.WRAP_CONTENT,  
		        FrameLayout.LayoutParams.WRAP_CONTENT); 
		params.bottomMargin = 0;
		params.rightMargin = 0;
		params.gravity = Gravity.BOTTOM | Gravity.RIGHT; 
        this.addContentView(mapView, params);*/
	}
	@Override
	protected void onDestroy() {
		Log.i(LOG_TAG, "Destroyed");

		if (cubicPano != null) {
			cubicPano.getFace(TextureFaces.front).recycle();
			cubicPano.getFace(TextureFaces.back).recycle();
			cubicPano.getFace(TextureFaces.top).recycle();
			cubicPano.getFace(TextureFaces.bottom).recycle();
			cubicPano.getFace(TextureFaces.left).recycle();
			cubicPano.getFace(TextureFaces.right).recycle();
			cubicPano = null;
			System.gc();
		}
		if(bfront != null){
			bfront.recycle();
			bfront = null;
			bback.recycle();
			bback = null;
			bleft.recycle();
			bleft = null;
			bright.recycle();
			bright = null;
			bup.recycle();
			bup = null;
			bdown.recycle();
			bdown = null;
			System.gc();
		}
		super.onDestroy();
	}
	
	/**
	 * 获取全景图信息
	 */
	private void getPanoDetail() {
		String configStr = "";
		String fileName = "/" + project_id + "/" + pano_id + "/" + "pano.cfg";
		Integer type = 3;
		String id = pano_id;
		IoUtil ioutil = new IoUtil();
		try {
			configStr = ioutil.ReadStringFromSD(fileName, type, id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			panoInfo = new JSONObject(configStr).getJSONObject("pano");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private String GetFacePath(Integer face) {
		String path = "";
		switch (face) {
		case 1:
			path = "/" + project_id + "/" + pano_id + "/s_f.jpg";
			break;
		case 2:
			path = "/" + project_id + "/" + pano_id + "/s_b.jpg";
			break;
		case 3:
			path = "/" + project_id + "/" + pano_id + "/s_l.jpg";
			break;
		case 4:
			path = "/" + project_id + "/" + pano_id + "/s_r.jpg";
			break;
		case 5:
			path = "/" + project_id + "/" + pano_id + "/s_u.jpg";
			break;
		case 6:
			path = "/" + project_id + "/" + pano_id + "/s_d.jpg";
			break;
		}
		;

		return path;
	}

	public class LoadFaceAsyncTask extends AsyncTask<Integer, Integer, String> {  
		
		private ProgressDialog waitDialog = null;
		@Override
		protected void onPreExecute() {
			waitDialog = new ProgressDialog(PanoViewActivity.this);
	    	waitDialog.setMessage(getString(R.string.loading_face));
	    	waitDialog.setCancelable(false);
	    	waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    	waitDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    			cancel(true);
	    		}
	    	});
	    	waitDialog.setMax(6);
	    	waitDialog.show();
		} 
	    /**  
	     * 这里的Integer参数对应AsyncTask中的第一个参数   
	     * 这里的String返回值对应AsyncTask的第三个参数  
	     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改  
	     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作  
	     */  
	    @Override  
	    protected String doInBackground(Integer... params) {  
	    	String status = "ok";
	    	String front_url = "", back_url = "", left_url = "", right_url = "", up_url = "", down_url = "";
			try {
				front_url = panoInfo.getString("s_f");
				back_url = panoInfo.getString("s_b");
				left_url = panoInfo.getString("s_l");
				right_url = panoInfo.getString("s_r");
				up_url = panoInfo.getString("s_u");
				down_url = panoInfo.getString("s_d");
				// JSONArray hotspotInfo = panoInfo.getJSONArray("hotspot");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			String front = GetFacePath(1);
			String back = GetFacePath(2);
			String left = GetFacePath(3);
			String right = GetFacePath(4);
			String up = GetFacePath(5);
			String down = GetFacePath(6);
	    	
			IoUtil ioutil = new IoUtil();
			try {
				ImagesUtil imgutil = new ImagesUtil();
				
				bfront = ioutil.ReadPicFromSD(front, front_url);
				bfront = imgutil.translateScale(bfront);
				publishProgress(1);
				
				bback = ioutil.ReadPicFromSD(back, back_url);
				bback = imgutil.translateScale(bback);
				publishProgress(2);
				
				bleft = ioutil.ReadPicFromSD(left, left_url);
				bleft = imgutil.translateScale(bleft);
				publishProgress(3);
				
				bright = ioutil.ReadPicFromSD(right, right_url);
				bright = imgutil.translateScale(bright);
				publishProgress(4);

				bup = ioutil.ReadPicFromSD(up, up_url);
				bup = imgutil.translateScale(bup);
				bup = imgutil.translateRotate(bup);
				publishProgress(5);

				bdown = ioutil.ReadPicFromSD(down, down_url);
				bdown = imgutil.translateRotate(bdown);
				publishProgress(6);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	    	return status;
	    }  

	    /**  
	     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）  
	     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置  
	     */  
	    @Override  
	    protected void onPostExecute(String result) {  
	    	waitDialog.dismiss();
	    	cubicPano = new CubicPanoNative(bfront, bback, bup, bdown, bleft,
					bright);
			setupOpenGLView();
	    }  

	    /**  
	     * 这里的Intege参数对应AsyncTask中的第二个参数  
	     * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行  
	     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作  
	     */  
	    @Override  
	    protected void onProgressUpdate(Integer... progress) {  
	    	int p = progress[0];
			waitDialog.setProgress(p);
	    }  

	}  
}
