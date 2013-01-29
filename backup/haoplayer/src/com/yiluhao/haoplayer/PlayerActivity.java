package com.yiluhao.haoplayer;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.panoramagl.PLIPanorama;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLSpherical2Panorama;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.PLView;
import com.panoramagl.PLViewEventListener;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.view.View.OnClickListener;

public class PlayerActivity extends PLView{
	/**constants*/
	
	private static final int kHotspotIdMin = 1;
	private static final int kHotspotIdMax = 1000;
	
	/**member variables*/
	
	private Random random = new Random();
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.loadPanorama(0);
        this.setListener(new PLViewEventListener()
        {
        	@Override
    		public void onDidClickHotspot(PLIView pView, PLHotspot hotspot, CGPoint screenPoint, PLPosition scene3DPoint)
        	{
        		Toast.makeText(pView.getActivity(), String.format("You select the hotspot with ID %d", hotspot.getIdentifier()), Toast.LENGTH_SHORT).show();
        	}
		});
    }
	private void loadPanorama(int index)
    {
    	GL10 gl = this.getCurrentGL();
    	PLIPanorama panorama = null;
    	//Lock panoramic view
    	this.setBlocked(true);
    	//Spherical panorama example (supports up 1024x512 texture)
        if(index == 0)
        {
            panorama = new PLSphericalPanorama();
            ((PLSphericalPanorama)panorama).setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.pano_sphere), false));
        }
        //Spherical2 panorama example (only support 2048x1024 texture)
        else if(index == 1)
        {
        	panorama = new PLSpherical2Panorama();
            ((PLSpherical2Panorama)panorama).setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.pano_sphere2), false));
        }
        //Add a hotspot
        panorama.addHotspot(new PLHotspot((kHotspotIdMin + Math.abs(random.nextInt()) % ((kHotspotIdMax + 1) - kHotspotIdMin)), PLImage.imageWithBitmap(PLUtils.getBitmap(this, R.raw.hotspot), false), 0.0f, 0.0f, 0.08f, 0.08f));
        //Load panorama
        this.reset();
        this.setPanorama(panorama);
        //Unlock panoramic view
        this.setBlocked(false);
    }
	@Override
	protected void onGLContextCreated(GL10 gl)
	{
    	super.onGLContextCreated(gl);
    	
    	//Add layout
    	View mainView = this.getLayoutInflater().inflate(R.layout.player, null);
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
}
