package com.yiluhao.panoplayer;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.openpanodroid.BitmapUtilities;
import org.openpanodroid.GlobalConstants;
import org.openpanodroid.PanodroidGLView;
import org.openpanodroid.Pipe;
import org.openpanodroid.UIUtilities;
import org.openpanodroid.panoutils.android.CubicPanoNative;
import org.openpanodroid.panoutils.android.CubicPanoNative.TextureFaces;


import com.yiluhao.utils.GetUrlInfo;
import com.yiluhao.utils.ImagesUtil;
import com.yiluhao.utils.IoUtil;

import junit.framework.Assert;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class PanoViewBackActivity extends Activity {
	
	private static int IMG_QUALITY = 85;
	
	private static final String LOG_TAG = PanoViewActivity.class.getSimpleName();
	
	private CubicPanoNative cubicPano = null;
	private PanodroidGLView glView = null;
	private String pano_id = "";
	private String project_id = "";
	private JSONObject panoInfo = null;
	private Bitmap pano = null;
	
	private BitmapDownloadTask panoDownloadTask = null;
	private PanoConversionTask panoConversionTask = null;
	
	private class ClickListenerErrorDialog implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// After an (fatal) error dialog, the activity will be dismissed.
			finish();
		}
	}
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.pano_view);
			
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				pano_id = extras.getString("pano_id");
				project_id = extras.getString("project_id");
			}
	    	Log.v("ids=",  pano_id+"-"+project_id);
	    	getPanoDetail();
    		getPanoFace();
    		//setupOpenGLView();
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
	    private String GetFacePath(Integer face){
	    	String path = "";
	    	switch (face) {
		    	case 1:
		    		path = "/"+project_id+"/"+pano_id+"/s_f.jpg";
		    		break;
		    	case 2:
		    		path = "/"+project_id+"/"+pano_id+"/s_b.jpg";
		    		break;
		    	case 3:
		    		path = "/"+project_id+"/"+pano_id+"/s_l.jpg";
		    		break;
		    	case 4:
		    		path = "/"+project_id+"/"+pano_id+"/s_r.jpg";
		    		break;
		    	case 5:
		    		path = "/"+project_id+"/"+pano_id+"/s_u.jpg";
		    		break;
		    	case 6:
		    		path = "/"+project_id+"/"+pano_id+"/s_d.jpg";
		    		break;
	    	};
	    	
	    	return path;
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
			String front = GetFacePath (1);
			String back = GetFacePath (2);
			String left = GetFacePath (3);
			String right = GetFacePath (4);
			String up = GetFacePath (5);
			String down = GetFacePath (6);
			IoUtil ioutil = new IoUtil();
			
			if (!ioutil.FileExists(front) || !ioutil.FileExists(back) || !ioutil.FileExists(left) || !ioutil.FileExists(right) || !ioutil.FileExists(up) || !ioutil.FileExists(down)){
				GetUrlInfo urlinfo = new GetUrlInfo();
				Uri panoUri = urlinfo.GetPanoPath (project_id, pano_id);
				panoDownloadTask = new BitmapDownloadTask();
				
		    	panoDownloadTask.execute(panoUri);
			}
			else {
				try{
					ImagesUtil imgutil = new ImagesUtil();
					
					bfront = ioutil.ReadPicFromSD(front, front_url);
					//bfront  = imgutil.translateScale(bfront);
					bback = ioutil.ReadPicFromSD(back, back_url);
					//bback  = imgutil.translateScale(bback);
					bleft = ioutil.ReadPicFromSD(left, left_url);
					//bleft  = imgutil.translateScale(bleft);
					bright = ioutil.ReadPicFromSD(right, right_url);
					//bright  = imgutil.translateScale(bright);
					
					bup = ioutil.ReadPicFromSD(up, up_url);
					//bup  = imgutil.translateScale(bup);
					//bup  = imgutil.translateRotate(bup);
					
					bdown = ioutil.ReadPicFromSD(down, down_url);
					//bdown  = imgutil.translateRotate(bdown);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				cubicPano = new CubicPanoNative(bfront, bback, bup, bdown, bleft, bright);
				setupOpenGLView();
			}
			//Log.v("aaa", "bbbbb");
			//
	    }
	    
	    @Override
	    protected void onDestroy() {
	    	Log.i(LOG_TAG, "Destroyed");

	    	if (panoDownloadTask != null) {
	    		// An AsyncTask will continue, also if the activity has been already destroyed.
	    		// Therefore, we signal it that the activity was destroyed. The AsyncTask
	    		// will cancel itself at the earliest possible time and avoid any further action.
	    		// (In particular, UI actions would be dangerous since the main activity is gone.)
	    		panoDownloadTask.destroy();
	    	}
	    	
/*	    	if (panoConversionTask != null) {
	    		panoConversionTask.destroy();
	    	}*/
	    	
	    	// We might have used a lot of memory.
	    	// Explicitly free it now.
	    	
	    	if (cubicPano != null ) {
	    		cubicPano.getFace(TextureFaces.front).recycle();
	    		cubicPano.getFace(TextureFaces.back).recycle();
	    		cubicPano.getFace(TextureFaces.top).recycle();
	    		cubicPano.getFace(TextureFaces.bottom).recycle();
	    		cubicPano.getFace(TextureFaces.left).recycle();
	    		cubicPano.getFace(TextureFaces.right).recycle();
	    		cubicPano = null;
	    		System.gc();
	    	}
	    	
	    	super.onDestroy();
	    }
	 private void setupOpenGLView() {
	    	Assert.assertTrue(cubicPano != null);
	    	glView = new PanodroidGLView(this, cubicPano);
	        setContentView(glView);
	    }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 public static int getOptimalFaceSize(int screenSize, int equirectImgSize, double hfov) {
			// Maximum possible size with this equirectangular image.
			int maxFaceSize = (int) (0.25*equirectImgSize * 90.0/hfov + 0.5);
			
			// Optimal face size for this screen size.
			int optimalFaceSize = (int) (90.0/hfov * screenSize + 0.5);
			
			return (optimalFaceSize < maxFaceSize ? optimalFaceSize : maxFaceSize);
		}
		
		public static int getOptimalEquirectSize(int screenSize, double hfov) {
			int optimalEquirectSize = (int) (360.0/hfov * screenSize + 0.5);
			return optimalEquirectSize;
		}

		/**
		 * 解析图片
		 * @author faashi
		 *
		 */
		private class BitmapDecoderThread extends Thread {
			public Bitmap bitmap;
			public String errorMsg;
			
			private InputStream is;
			
			BitmapDecoderThread(InputStream is) {
				bitmap = null;
				this.is = is;
			}
			
			@Override
			public void run() {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				options.inScaled = false;
				BitmapUtilities.setHiddenNativeAllocField(options);
				
				//ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				//int largeMem = activityManager.getLargeMemoryClass();
				//int regularMem = activityManager.getMemoryClass();
				
				try {
					bitmap = BitmapFactory.decodeStream(is, null, options);
				} catch (OutOfMemoryError e) {
					Log.e(LOG_TAG, "Failed to decode image: " + e.getMessage());
					//errorMsg = getString(R.string.cancel);
				} catch (Exception e) {
					Log.e(LOG_TAG, "Failed to decode image: " + e.getMessage());
				} finally {
					try {
						is.close();
					} catch (IOException e) {}
					if (bitmap == null && errorMsg == null) {
						Log.e(LOG_TAG, "Failed to decode image");
						//errorMsg = getString(R.string.imageDecodeFailed);
					}
				}
			}
		}
		
		/**
		 * 下载图片
		 * @author faashi
		 *
		 */
		private class BitmapDownloadTask extends AsyncTask<Uri, Integer, Bitmap> {

			private final static int BUFFER_SIZE = 5000;
			
			private InputStream downloadStream = null;
			private BitmapDecoderThread bitmapDecoder = null;
			private ProgressDialog waitDialog = null;
			private boolean destroyed = false;
			
			@Override
			protected void onPreExecute() {
				waitDialog = new ProgressDialog(PanoViewBackActivity.this);
				
		    	waitDialog.setMessage(getString(R.string.loadingPanoImage));
		    	waitDialog.setCancelable(false);
		    	waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		    	waitDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface dialog, int id) {
		    			cancel(true);
		    			if (downloadStream != null) {
		    				// Force download to end.
		    				try {
								downloadStream.close();
							} catch (IOException e) {}
		    			}
		    		}
		    	});
		    	waitDialog.show();
			}
			
			@Override
			protected Bitmap doInBackground(Uri... params) {
				
				Assert.assertTrue(params.length > 0);
				Uri uri = params[0];
				int contentLength = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				URLConnection connection;
				URL url;
				
				try {
					url = new URL(uri.toString());
				} catch (MalformedURLException ex) {		
					url = null;
					
				}
				
				Pipe pipe = new Pipe(BUFFER_SIZE);
				OutputStream pipeOutput = pipe.getOutputStream();
				InputStream pipeInput = pipe.getInputStream();	
				
				Bitmap bitmap = null;
				
				try {
					if (url != null) {
						// We try to open an URL connection since this gives us a content length
						// (in contrast to the generic way of opening an URI).
						connection = url.openConnection();
						downloadStream = new BufferedInputStream(connection.getInputStream());
						contentLength = connection.getContentLength();
					} else {
						// Try generic way to open URI.
						downloadStream = getContentResolver().openInputStream(uri);				
					}
					
					if (contentLength > 0) {
						waitDialog.setMax(contentLength);	
					} else {
						waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					}
					
					bitmapDecoder = new BitmapDecoderThread(pipeInput);
					bitmapDecoder.start();
					
					int currentLength = 0;
					int readCnt;
					
					while (!isCancelled() && (readCnt = downloadStream.read(buffer)) != -1) {
						pipeOutput.write(buffer, 0, readCnt);
						currentLength += readCnt;
						
						if (contentLength > 0) {
							publishProgress(currentLength);
						}
					}
				} catch (Exception e) {
					Log.e("laod image", "Failed to load image: " + e.getMessage());
				} finally {
					if (pipeOutput != null) {
						try {
							pipeOutput.close();
						} catch (IOException e) {}
					}
				}
				
				if (bitmapDecoder != null) {
					try {
						bitmapDecoder.join();
						bitmap = bitmapDecoder.bitmap;
					} catch (InterruptedException e) {
						Log.e(LOG_TAG, "Download taks interrupted: " + e.getMessage());
					}
				}
				
				return bitmap;
			}
			
			@Override
			protected void onProgressUpdate(Integer... progress) {
				Assert.assertTrue(progress.length > 0);
				int p = progress[0];
				waitDialog.setProgress(p);
		    }
			
			synchronized boolean isDestroyed() {
				return destroyed;
			}
			
			synchronized void destroy() {
				destroyed = true;
				cancel(true);
			}
			
			@Override
			protected void onCancelled () {
				if (isDestroyed()) {
					return;
				}
				
				waitDialog.dismiss();
				finish();
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				if (isDestroyed()) {
					return;
				}
				
				waitDialog.dismiss();
				if (result == null) {
					String msg = getString(R.string.loadingPanoFailed);
					if (bitmapDecoder != null && bitmapDecoder.errorMsg != null) {
						msg += " (" + bitmapDecoder.errorMsg + ")";
					}
					//UIUtilities.showAlert(PanoViewerActivity.this, null, msg, new ClickListenerErrorDialog());
				} else if (result.getWidth() != 2*result.getHeight()) {
					String msg = getString(R.string.invalidPanoImage);
					UIUtilities.showAlert(PanoViewBackActivity.this, null, msg, new ClickListenerErrorDialog());
				} else {
					pano = result;
					convertCubicPano();
				}
			}
		}
		private int toPowerOfTwo(int number) {
	    	int n_2 = 1;
	    	
	    	while (n_2 < number) {
	    		n_2 *= 2;
	    	}
	    	
	    	return n_2;
	    }
		/**
		 * 转图
		 */
		private void convertCubicPano() {
	    	Assert.assertTrue(pano != null);
	    	
	    	Log.i(LOG_TAG, "Converting panorama ...");
	    	
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	String str = prefs.getString("textureSize", "");
	    	int maxTextureSize = GlobalConstants.DEFAULT_MAX_TEXTURE_SIZE;
	    	if (!str.equals("")) {
	    		try {
	    			maxTextureSize = Integer.parseInt(str);
	    		} catch (NumberFormatException ex) {
	    			maxTextureSize = GlobalConstants.DEFAULT_MAX_TEXTURE_SIZE;
	    		}
	    	}
	    	
	    	// On the one hand, we don't want to waste memory for textures whose resolution 
	    	// is too large for the device. On the other hand, we want to have a resolution
	    	// that is high enough to give us good quality on any device. However, we don't
			// know the resolution of the GLView a priori, and it could be resized later.
			// Therefore, we use the display size to calculate the optimal texture size.
	    	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	    	int width = display.getWidth();
	    	int height = display.getHeight();
	    	int maxDisplaySize = width > height ? width : height;
	    	
	    	int optimalTextureSize = getOptimalFaceSize(maxDisplaySize, pano.getWidth(), GlobalConstants.DEFAULT_FOV_DEG);
	    	int textureSize = toPowerOfTwo(optimalTextureSize);
	    	textureSize = textureSize <= maxTextureSize ? textureSize : maxTextureSize;
	    	
	    	Log.i(LOG_TAG, "Texture size: " + textureSize + " (optimal size was " + optimalTextureSize + ")");
	    
	    	panoConversionTask = new PanoConversionTask(textureSize);
	    	panoConversionTask.execute(pano);
	    }
		private class PanoConversionTask extends AsyncTask<Bitmap, Integer, CubicPanoNative> {
			
			private ProgressDialog waitDialog = null;
			private int textureSize;
			private boolean destroyed = false;
			
			public PanoConversionTask(int textureSize) {
				this.textureSize = textureSize;
			}
			
			@Override
			protected void onPreExecute() {
				waitDialog = new ProgressDialog(PanoViewBackActivity.this);
		    	waitDialog.setMessage(getString(R.string.convertingPanoImage));
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
			protected void SaveFacePic (Bitmap bitmap, Integer face){
				String front_path = GetFacePath (face);
				//保存图片
				IoUtil ioutil = new IoUtil();
				try{
					ioutil.WritePicToSD(bitmap, front_path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			protected CubicPanoNative doInBackground(Bitmap... params) {
				Bitmap bmp;
				if (isCancelled()) {
					return null;
				}
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.front, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap front = createPurgableBitmap(bmp);
				SaveFacePic (front, 1);
				
				bmp.recycle();
				publishProgress(1);
				
				if (isCancelled()) {
					return null;
				}
				
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.back, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap back = createPurgableBitmap(bmp);
				SaveFacePic (back, 2);
				bmp.recycle();
				publishProgress(2);

				if (isCancelled()) {
					return null;
				}
				
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.top, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap top = createPurgableBitmap(bmp);
				SaveFacePic (top, 5);
				bmp.recycle();
				publishProgress(3);
				
				if (isCancelled()) {
					return null;
				}
				
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.bottom, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap bottom = createPurgableBitmap(bmp);
				SaveFacePic (bottom, 6);
				bmp.recycle();
				publishProgress(4);
				
				if (isCancelled()) {
					return null;
				}
				
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.right, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap right = createPurgableBitmap(bmp);
				SaveFacePic (right, 4);
				bmp.recycle();
				publishProgress(5);
				
				if (isCancelled()) {
					return null;
				}
				
				bmp = CubicPanoNative.getCubeSide(pano, TextureFaces.left, textureSize);
				if (bmp == null) {
					return null;
				}
				Bitmap left = createPurgableBitmap(bmp);
				SaveFacePic (left, 3);
				bmp.recycle();
				publishProgress(6);
				
				CubicPanoNative cubic = new CubicPanoNative(front, back, top, bottom, left, right);
				
				return cubic;
			}
			
			private Bitmap createPurgableBitmap(Bitmap original) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				original.compress(Bitmap.CompressFormat.JPEG, IMG_QUALITY, os);
				byte[] imgDataCompressed = os.toByteArray();

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				// When we run out of memory, the decompressed bitmap can be purged.
				// If it is re-accessed, the byte array will be decompressed again.
				// This allows us to handle larger or more bitmaps.
				options.inPurgeable = true;
				// Original byte array will not be altered anymore. --> Can make a shallow reference.
				options.inInputShareable = true; 
				Bitmap compressedBitmap = BitmapFactory.decodeByteArray(imgDataCompressed, 0, imgDataCompressed.length, options);
				
				return compressedBitmap;
			}
			
			synchronized boolean isDestroyed() {
				return destroyed;
			}
			
			synchronized void destroy() {
				destroyed = true;
				cancel(true);
			}
			
			@Override
			protected void onCancelled () {
				if (isDestroyed()) {
					return;
				}
				
				waitDialog.dismiss();
				pano.recycle();
				finish();
			}
			
			@Override
			protected void onPostExecute(CubicPanoNative result) {
				if (isDestroyed()) {
					return;
				}
				
				waitDialog.dismiss();
				pano.recycle();
				pano = null;
				
				if (result == null) {
					UIUtilities.showAlert(PanoViewBackActivity.this, null, getString(R.string.convertingPanoImage), new ClickListenerErrorDialog());
				} else {
					cubicPano = result;
					setupOpenGLView();
					panoDisplaySetupFinished();
				}
			}
			
			@Override
			protected void onProgressUpdate(Integer... progress) {
				Assert.assertTrue(progress.length > 0);
				int p = progress[0];
				waitDialog.setProgress(p);
		    }
		}
		protected void panoDisplaySetupFinished() {
		}
}
