package com.yiluhao.haoplayer;

import java.util.Stack;

import android.view.MotionEvent;

import com.panoramagl.PLIPanorama;
import com.panoramagl.PLView;

public class PlayerView extends PLView{
	private boolean AnimateFlag = true;
	private long LastTime;
	private float Ymove = 0f;
	private int AnimateWaitTime = 5000;

	@Override
	public void clear()
	{
	    PLIPanorama panorama = this.getPanorama();
	    if(panorama != null)
	    {
	        synchronized(this)
	        {
	        	panorama.clearPanorama(this.getCurrentGL());
	        }
	    }
	}
	@Override
	protected void onDestroy() 
	{
		this.hideProgressBar();
		super.onDestroy();
	}
    public void StopAnimate(){
    	AnimateFlag = false;
    }
    public void ReAnimate(){
    	AnimateFlag = true;
    }
    

}
