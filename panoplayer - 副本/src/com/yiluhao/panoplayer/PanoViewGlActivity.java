package com.yiluhao.panoplayer;

import java.io.IOException;

import java.util.Random;


import javax.microedition.khronos.opengles.GL10;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.panoramagl.PLCubicPanorama;
import com.panoramagl.PLIPanorama;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.PLTexture;
import com.panoramagl.PLView;
import com.panoramagl.PLViewEventListener;
import com.panoramagl.enumeration.PLCubeFaceOrientation;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;
import com.yiluhao.utils.GetUrlInfo;
import com.yiluhao.utils.IoUtil;

public class PanoViewGlActivity extends PLView{
	/**constants*/
	
	private static final int kHotspotIdMin = 1;
	private static final int kHotspotIdMax = 1000;
	
	/**member variables*/
	
	private Random random = new Random();
	private String pano_id = "";
	private String project_id = "";
	private JSONObject panoInfo = null;
	private ProgressBar progress;
	private ProgressDialog dialog=null;
	/**init methods*/
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pano_view);
        
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			pano_id = extras.getString("pano_id");
			project_id = extras.getString("project_id");
		}
    	Log.v("ids=",  pano_id+"-"+project_id);
    	
    	
		loadPanorama();
    	
        this.setListener(new PLViewEventListener()
        {
        	@Override
    		public void onDidClickHotspot(PLIView pView, PLHotspot hotspot, CGPoint screenPoint, PLPosition scene3DPoint)
        	{
        		Toast.makeText(pView.getActivity(), String.format("You select the hotspot with ID %d", hotspot.getIdentifier()), Toast.LENGTH_SHORT).show();
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
    /**
     * This event is fired when OpenGL renderer was created
     * @param gl Current OpenGL context
     */
    @Override
	protected void onGLContextCreated(GL10 gl)
	{
    	super.onGLContextCreated(gl);
    	//Add layout
    	View mainView = this.getLayoutInflater().inflate(R.layout.pano_view, null);
        this.addContentView(mainView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        //Zoom controls
        ZoomControls zoomControls = (ZoomControls)mainView.findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new OnClickListener()
        {	
			@Override
			public void onClick(View view)
			{
				getCamera().zoomIn(true);
			}
		});
        zoomControls.setOnZoomOutClickListener(new OnClickListener()
        {	
			@Override
			public void onClick(View view)
			{
				getCamera().zoomOut(true);
			}
		});
	}
/**load methods*/
    
    /**
     * Load panorama image manually
     * @param index Spinner position where 0 = spherical, 1 = spherical2, 2 = cubic, 3 = cylindrical
     */
    private void loadPanorama()
    {
    	GL10 gl = this.getCurrentGL();
    	PLIPanorama panorama_1 = null;
    	//Lock panoramic view
    	this.setBlocked(true);
        
        PLCubicPanorama cubicPanorama_1 = new PLCubicPanorama();
        
        cubicPanorama_1.setImage (gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationFront);
        cubicPanorama_1.setImage(gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationBack);
        cubicPanorama_1.setImage(gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationLeft);
        cubicPanorama_1.setImage(gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationRight);
        cubicPanorama_1.setImage(gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationUp);
        cubicPanorama_1.setImage(gl, PLImage.imageWithBitmap (PLUtils.getBitmap(this, R.raw.back), false), PLCubeFaceOrientation.PLCubeFaceOrientationDown);
    	panorama_1 = cubicPanorama_1;
    	this.reset();
    	 this.setPanorama(panorama_1);
    	 this.setBlocked(false);
    	 clear();
    	 
    	 PLIPanorama panorama = null;
    	 this.setBlocked(true);
    	 PLCubicPanorama cubicPanorama = new PLCubicPanorama();
    	 
    	getPanoDetail();
    	String front_url="",back_url="",left_url="",right_url="",up_url="",down_url="";
    	try{
    		front_url = panoInfo.getString("s_f");
        	back_url = panoInfo.getString("s_b");
        	left_url = panoInfo.getString("s_l");
        	right_url = panoInfo.getString("s_r");
        	up_url = panoInfo.getString("s_u");
        	down_url = panoInfo.getString("s_d");
    		JSONArray hotspotInfo = panoInfo.getJSONArray("hotspot");
	    } catch (JSONException e) {
			throw new RuntimeException(e);
		}
    	IoUtil ioutil = new IoUtil();
		Bitmap bfront=null , bback=null, bleft=null, bright=null, bup=null, bdown=null;
		String front = "/"+project_id+"/"+pano_id+"/s_f.jpg";
		String back = "/"+project_id+"/"+pano_id+"/s_b.jpg";
		String left = "/"+project_id+"/"+pano_id+"/s_l.jpg";
		String right = "/"+project_id+"/"+pano_id+"/s_r.jpg";
		String up = "/"+project_id+"/"+pano_id+"/s_u.jpg";
		String down = "/"+project_id+"/"+pano_id+"/s_d.jpg";
		try{
			bfront = ioutil.ReadPicFromSD(front, front_url);
			bback = ioutil.ReadPicFromSD(back, back_url);
			bleft = ioutil.ReadPicFromSD(left, left_url);
			bright = ioutil.ReadPicFromSD(right, right_url);
			bup = ioutil.ReadPicFromSD(up, up_url);
			bdown = ioutil.ReadPicFromSD(down, down_url);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//Log.v("face", front+"++"+back+"++"+left+"++"+right+"++"+top+"++"+bottom);
        cubicPanorama.setImage (gl, PLImage.imageWithBitmap (bfront, false), PLCubeFaceOrientation.PLCubeFaceOrientationFront);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bback, false), PLCubeFaceOrientation.PLCubeFaceOrientationBack);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bleft, false), PLCubeFaceOrientation.PLCubeFaceOrientationLeft);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bright, false), PLCubeFaceOrientation.PLCubeFaceOrientationRight);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bup, false), PLCubeFaceOrientation.PLCubeFaceOrientationUp);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bdown, false), PLCubeFaceOrientation.PLCubeFaceOrientationDown);
        	panorama = cubicPanorama;
        	initializeValues();
       	 this.setPanorama(panorama);
       	 this.setBlocked(false);
            
        //Add a hotspot
        //panorama.addHotspot(new PLHotspot((kHotspotIdMin + Math.abs(random.nextInt()) % ((kHotspotIdMax + 1) - kHotspotIdMin)), PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.hotspot), false), 0.0f, 0.0f, 0.08f, 0.08f));
        //Load panorama
       // this.reset();
       
        
        showProgressBar();
        //loadNewPano();
        
    }
    private void loadNewPano(){
    	//context = this.getActivity();
    	
    	GL10 gl = this.getCurrentGL();
    	PLIPanorama panorama = null;
    	
    	//this.setBlocked(true);
    	
    	PLCubicPanorama cubicPanorama = new PLCubicPanorama();
    	getPanoDetail();
    	String front_url="",back_url="",left_url="",right_url="",up_url="",down_url="";
    	try{
    		front_url = panoInfo.getString("s_f");
        	back_url = panoInfo.getString("s_b");
        	left_url = panoInfo.getString("s_l");
        	right_url = panoInfo.getString("s_r");
        	up_url = panoInfo.getString("s_u");
        	down_url = panoInfo.getString("s_d");
    		JSONArray hotspotInfo = panoInfo.getJSONArray("hotspot");
	    } catch (JSONException e) {
			throw new RuntimeException(e);
		}
    	IoUtil ioutil = new IoUtil();
		Bitmap bfront=null , bback=null, bleft=null, bright=null, bup=null, bdown=null;
		String front = "/"+project_id+"/"+pano_id+"/s_f.jpg";
		String back = "/"+project_id+"/"+pano_id+"/s_b.jpg";
		String left = "/"+project_id+"/"+pano_id+"/s_l.jpg";
		String right = "/"+project_id+"/"+pano_id+"/s_r.jpg";
		String up = "/"+project_id+"/"+pano_id+"/s_u.jpg";
		String down = "/"+project_id+"/"+pano_id+"/s_d.jpg";
		try{
			bfront = ioutil.ReadPicFromSD(front, front_url);
			bback = ioutil.ReadPicFromSD(back, back_url);
			bleft = ioutil.ReadPicFromSD(left, left_url);
			bright = ioutil.ReadPicFromSD(right, right_url);
			bup = ioutil.ReadPicFromSD(up, up_url);
			bdown = ioutil.ReadPicFromSD(down, down_url);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//Log.v("face", front+"++"+back+"++"+left+"++"+right+"++"+top+"++"+bottom);
        	cubicPanorama.setImage (gl, PLImage.imageWithBitmap (bfront, false), PLCubeFaceOrientation.PLCubeFaceOrientationFront);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bback, false), PLCubeFaceOrientation.PLCubeFaceOrientationBack);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bleft, false), PLCubeFaceOrientation.PLCubeFaceOrientationLeft);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bright, false), PLCubeFaceOrientation.PLCubeFaceOrientationRight);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bup, false), PLCubeFaceOrientation.PLCubeFaceOrientationUp);
        	cubicPanorama.setImage(gl, PLImage.imageWithBitmap (bdown, false), PLCubeFaceOrientation.PLCubeFaceOrientationDown);
            panorama = cubicPanorama;
            
        //Add a hotspot
        panorama.addHotspot(new PLHotspot((kHotspotIdMin + Math.abs(random.nextInt()) % ((kHotspotIdMax + 1) - kHotspotIdMin)), PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.hotspot), false), 0.0f, 0.0f, 0.08f, 0.08f));
        //Load panorama
       // this.reset();
        this.setPanorama(panorama);
        //随手指自动转动
        setScrollingEnabled(true);
        setMinDistanceToEnableDrawing(3);
        
        setAnimationInterval(0.05f);
        setAnimationFrameInterval(2);
        
    }
    
    

}
