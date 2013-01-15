package com.yiluhao.panoplayer;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openpanodroid.panoutils.android.CubicPanoNative;

import com.panoramagl.PLCubicPanorama;
import com.panoramagl.PLIPanorama;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLSpherical2Panorama;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.PLView;
import com.panoramagl.PLViewEventListener;
import com.panoramagl.enumeration.PLCubeFaceOrientation;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;
import com.yiluhao.panoplayer.PanoViewActivity.LoadFaceAsyncTask;
import com.yiluhao.utils.ImagesUtil;
import com.yiluhao.utils.IoUtil;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.view.View.OnClickListener;

public class PanoPlayerActivity extends PLView {

	private String pano_id = "";
	private String project_id = "";
	private JSONObject panoInfo = null;
	private JSONArray hotspots = null;
	private Bitmap bfront = null, bback = null, bleft = null, bright = null,
			bup = null, bdown = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			pano_id = extras.getString("pano_id");
			project_id = extras.getString("project_id");
		}
		Log.v("ids=", pano_id + "-" + project_id);
		displayPano();
		// this.loadPanorama(2);

	}

	private void displayPano() {
		boolean hasData = getPanoDetail();
		if (hasData) {
			LoadFaceAsyncTask asyncTask = new LoadFaceAsyncTask();
			asyncTask.execute();
		}
	}

	private void setHotspotListener() {
		this.setListener(new PLViewEventListener() {
			@Override
			public void onDidClickHotspot(PLIView pView, PLHotspot hotspot,
					CGPoint screenPoint, PLPosition scene3DPoint) {
				// Toast.makeText(pView.getActivity(),
				// String.format("You select the hotspot with ID %d",
				// hotspot.getIdentifier()), Toast.LENGTH_SHORT).show();
				int id = 0;
				String linkId = "0";
				String pano_id_back = pano_id;
				for (int i = 0; i < hotspots.length(); i++) {
					JSONObject jsonObject2 = (JSONObject) hotspots.opt(i);
					try {
						id = jsonObject2.getInt("id");
						linkId = jsonObject2.getString("link_scene_id");
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}
					if (id == hotspot.getIdentifier()) {
						pano_id = linkId;
					}
				}
				if (linkId != "0" || pano_id_back != pano_id) {
					startPanoViewerActivity(linkId, project_id);
				}
			}
		});
	}

	private void startPanoViewerActivity(String sid, String pid) {
		Intent intent = new Intent(this, PanoPlayerActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("pano_id", sid);
		bundle.putString("project_id", pid);
		intent.putExtras(bundle);

		startActivity(intent);
	}

	/**
	 * 获取全景图信息
	 */
	private boolean getPanoDetail() {
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
		if (configStr == "" || configStr == null) {
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
			// return list;
			return false;
		} else {
			try {
				panoInfo = new JSONObject(configStr).getJSONObject("pano");
				hotspots = new JSONObject(configStr).getJSONArray("hotspots");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
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

	private void showErrorTip() {
		Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
	}

	private Bitmap getDefaultPic() {
		return PLUtils.getBitmap(this, R.raw.player);
	}

	public class LoadFaceAsyncTask extends AsyncTask<Integer, Integer, String> {

		private ProgressDialog waitDialog = null;

		@Override
		protected void onPreExecute() {
			waitDialog = new ProgressDialog(PanoPlayerActivity.this);
			waitDialog.setMessage(getString(R.string.loading_face));
			waitDialog.setCancelable(false);
			waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			waitDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							cancel(true);
						}
					});
			waitDialog.setMax(6);
			waitDialog.show();
		}

		/**
		 * 这里的Integer参数对应AsyncTask中的第一个参数 这里的String返回值对应AsyncTask的第三个参数
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
				// ImagesUtil imgutil = new ImagesUtil();

				bfront = ioutil.ReadPicFromSD(front, front_url);
				// bfront = imgutil.translateScale(bfront);
				publishProgress(1);
				if (bfront == null) {
					showErrorTip();
					bfront = getDefaultPic();
				}

				bback = ioutil.ReadPicFromSD(back, back_url);
				// bback = imgutil.translateScale(bback);
				publishProgress(2);
				if (bback == null) {
					showErrorTip();
					bback = getDefaultPic();
				}

				bleft = ioutil.ReadPicFromSD(left, left_url);
				// bleft = imgutil.translateScale(bleft);
				publishProgress(3);
				if (bleft == null) {
					showErrorTip();
					bleft = getDefaultPic();
				}

				bright = ioutil.ReadPicFromSD(right, right_url);
				// bright = imgutil.translateScale(bright);
				publishProgress(4);
				if (bright == null) {
					showErrorTip();
					bright = getDefaultPic();
				}

				bup = ioutil.ReadPicFromSD(up, up_url);
				// bup = imgutil.translateScale(bup);
				// bup = imgutil.translateRotate(bup);
				publishProgress(5);
				if (bup == null) {
					showErrorTip();
					bup = getDefaultPic();
				}

				bdown = ioutil.ReadPicFromSD(down, down_url);
				// bdown = imgutil.translateRotate(bdown);
				publishProgress(6);
				if (bdown == null) {
					showErrorTip();
					bdown = getDefaultPic();
				}

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
			loadPanorama(2);
			setHotspotListener();
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

	private void loadPanorama(int index) {
		GL10 gl = this.getCurrentGL();
		PLIPanorama panorama = null;
		// Lock panoramic view
		this.setBlocked(true);
		// Spherical panorama example (supports up 1024x512 texture)
		/*
		 * if(index == 0) { panorama = new PLSphericalPanorama();
		 * ((PLSphericalPanorama)panorama).setImage(gl,
		 * PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.pano_sphere2),
		 * false)); } //Spherical2 panorama example (only support 2048x1024
		 * texture) else if(index == 1) { panorama = new PLSpherical2Panorama();
		 * ((PLSpherical2Panorama)panorama).setImage(gl,
		 * PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.pano_sphere2),
		 * false)); }
		 */
		// Cubic panorama example (supports up 1024x1024 texture per face)
		if (index == 2) {
			PLCubicPanorama cubicPanorama = new PLCubicPanorama();
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bfront, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationFront);
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bback, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationBack);
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bleft, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationLeft);
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bright, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationRight);
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bup, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationUp);
			cubicPanorama.setImage(gl, PLImage.imageWithBitmap(bdown, false),
					PLCubeFaceOrientation.PLCubeFaceOrientationDown);
			panorama = cubicPanorama;
		}
		int id = 0;
		float pan = 0f;
		float tilt = 0f;
		// 屏幕宽高
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		float hotspot = 0.05f;
		int maxWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
		// Log.v("Width", Integer.toString(maxWidth));
		if (maxWidth < 1000) {
			hotspot = 0.08f;
		}
		// Add a hotspot
		for (int i = 0; i < hotspots.length(); i++) {
			JSONObject jsonObject2 = (JSONObject) hotspots.opt(i);
			try {
				id = jsonObject2.getInt("id");
				pan = jsonObject2.getInt("pan");
				tilt = jsonObject2.getInt("tilt");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			panorama.addHotspot(new PLHotspot(id, PLImage.imageWithBitmap(
					PLUtils.getBitmap(this, R.raw.hotspot), false), tilt, pan,
					hotspot, hotspot));
		}
		// Load panorama
		this.reset();
		this.setPanorama(panorama);
		// Unlock panoramic view
		this.setBlocked(false);
	}

	@Override
	protected void onGLContextCreated(GL10 gl) {
		super.onGLContextCreated(gl);

		// Add layout
		View mainView = this.getLayoutInflater().inflate(R.layout.pano_player,
				null);
		this.addContentView(mainView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		ImageButton exitButton = (ImageButton) findViewById(R.id.exit_btn);
		exitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				startPanoViewerActivity();
			}
		});
		
		// Zoom controls
		ImageButton zoomIn = (ImageButton) mainView
				.findViewById(R.id.zoom_in_btn);
		zoomIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getCamera().zoomIn(true);
			}
		});
		ImageButton zoomout = (ImageButton) mainView
				.findViewById(R.id.zoom_out_btn);
		zoomout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getCamera().zoomOut(true);
			}
		});
	}
	private void startPanoViewerActivity() {
		// Log.i(LOG_TAG, "id" + id);
		Intent intent = new Intent(this, PanoTabActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("id", project_id);
		intent.putExtras(bundle);

		startActivity(intent);
	}

}
