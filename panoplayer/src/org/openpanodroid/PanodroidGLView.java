/* 
 * Copyright 2012 Frank Dürr
 * 
 * This file is part of OpenPanodroid.
 *
 * OpenPanodroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenPanodroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenPanodroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openpanodroid;

import java.util.Date;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.openpanodroid.panoutils.android.CubicPanoNative;


import junit.framework.Assert;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

public class PanodroidGLView extends GLSurfaceView {
	private static final String LOG_TAG = PanodroidGLView.class.getSimpleName();
    
	private static final long KINETIC_INTERVAL = 200; // [ms]
	private static final int REST_THRESHOLD = 5; // [pixel]
	
	private PanodroidVortexRenderer renderer;
	private boolean AnimateFlag = true;
	private long LastTime;
	private float Ymove = 0f;
	private int AnimateWaitTime = 5000;
    
    private Stack<EventInfo> motionEvents;
    
    class EventInfo {
    	public float x;
    	public float y;
    	public long time;
    	
    	public EventInfo(float x, float y, long time) {
    		this.x = x;
    		this.y = y;
    		this.time = time;
    	}
    }
    
    
    private Handler mHandler = new Handler(){  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case 1:  
            	float distX = -0.5f;
        		float distY = 0.0f;
        		//Log.v("moeY", "aaa"+Ymove);
        		if(Ymove>0){
        			distY = -0.3f;
        		}
        		else if(Ymove<0){
        			distY = 0.3f;
        		}
        		
        		Ymove += distY;
        		rotate(distX, distY);
                break;  
            }  
        };  
    };  
    public void StartAnimate(){
    	Timer timer = new Timer();
        timer.schedule(new MyTask(), AnimateWaitTime, 10);
    }
    private class MyTask extends TimerTask {
        public void run() {  
            Message message = new Message();  
            message.what = 1;  
            Date currentDate = new Date();
    		long currentTime = currentDate.getTime();
            if(AnimateFlag && (currentTime-LastTime)>AnimateWaitTime ){
            	mHandler.sendMessage(message); 
            }
        }     
    }  
    public void StopAnimate(){
    	AnimateFlag = false;
    }
    public void ReAnimate(){
    	AnimateFlag = true;
    }
    
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		
		switch (event.getAction()) {
    	case MotionEvent.ACTION_DOWN :
    		stopKineticRotation();
    		motionEvents = new Stack<EventInfo>();
    		motionEvents.push(new EventInfo(event.getX(), event.getY(), event.getEventTime()));
    		StopAnimate();
    		return true;
    	case MotionEvent.ACTION_MOVE :
    		Assert.assertTrue(motionEvents != null);
    		
    		if (motionEvents.size() > 0) {
    			EventInfo lastEvent = motionEvents.lastElement(); 
        		
        		float distX = event.getX()-lastEvent.x;
        		float distY = event.getY()-lastEvent.y;
        		Ymove += distY;
        		rotate(distX, distY);
        		
    		}
    		Log.v("aaa", "aa--"+Ymove);
    		motionEvents.push(new EventInfo(event.getX(), event.getY(), event.getEventTime()));
    		
    		return true;
    	case MotionEvent.ACTION_UP :
    		Assert.assertTrue(motionEvents != null);
    		
    		motionEvents.push(new EventInfo(event.getX(), event.getY(), event.getEventTime()));
    		
    		startKineticRotation();
    		
    		Date currentDate = new Date();
    		LastTime = currentDate.getTime();
    		ReAnimate();
    		
    		return true;
    	case MotionEvent.ACTION_CANCEL :
    		motionEvents = null;
    		return true;
    	}
   
    	return false;
	}	
	
	private void stopKineticRotation() {
		renderer.stopKineticRotation();
	}
	
	private void startKineticRotation() {
		Assert.assertTrue(motionEvents != null);
		
		if (motionEvents.size() < 2) {
			return;
		}
		
		EventInfo event1 = motionEvents.pop();
		long tEnd = event1.time;
		float directionX = 0.0f;
		float directionY = 0.0f;
		EventInfo event2 = motionEvents.pop();
		long tStart = tEnd;
		
		while (event2 != null && tEnd-event2.time < KINETIC_INTERVAL) {
			tStart = event2.time;
			directionX += event1.x-event2.x;
			directionY += event1.y-event2.y;
			
			event1 = event2;
			if (motionEvents.size() > 0) {
				event2 = motionEvents.pop();
			} else {
				event2 = null;
			}
		}
		
		float dist = (float) Math.sqrt(directionX*directionX + directionY*directionY);
		if (dist <= REST_THRESHOLD) {
			return;
		}
		
		// The pointer was moved by more than REST_THRESHOLD pixels in the last
		// KINETIC_INTERVAL seconds (or less). --> We have a kinetic scroll event.

		float deltaT = (tEnd-tStart)/1000.0f;
		if (deltaT == 0.0f) {
			return;
		}
		
		int surfaceWidth = renderer.getSurfaceWidth();
		int surfaceHeight = renderer.getSurfaceHeight();
		
		float hFovDeg = renderer.getHFovDeg();
		float vFovDeg = renderer.getVFovDeg();
		
		float deltaLongitute = directionX/surfaceWidth*hFovDeg;
		float rotationSpeedLongitude = deltaLongitute/deltaT;
		
		float deltaLatitude = directionY/surfaceHeight*vFovDeg;
		float rotationSpeedLatitude = deltaLatitude/deltaT;
		
		if (rotationSpeedLongitude == 0.0f && rotationSpeedLatitude == 0.0f) {
			return;
		}
		//Log.v("aaa", "aa"+Ymove);
		Ymove += -1.0f*rotationSpeedLongitude;
		renderer.startKineticRotation(-1.0f*rotationSpeedLatitude, -1.0f*rotationSpeedLongitude);
	}
	
	public void rotate(float deltaX, float deltaY) {
		int surfaceWidth = renderer.getSurfaceWidth();
		int surfaceHeight = renderer.getSurfaceHeight();
		float aspect = (float) surfaceWidth/(float) surfaceHeight;
		float rotationLatitudeDeg = renderer.getRotationLatitudeDeg();
		float rotationLongitudeDeg = renderer.getRotationLongitudeDeg();
		float hFovDeg = renderer.getHFovDeg();
		
		float deltaLongitute = deltaX/surfaceWidth*hFovDeg;
		rotationLongitudeDeg -= deltaLongitute;	
		
		float fovYDeg = hFovDeg/aspect;
		float deltaLatitude = deltaY/surfaceHeight*fovYDeg;
		rotationLatitudeDeg -= deltaLatitude;
	
		renderer.setRotation(rotationLatitudeDeg, rotationLongitudeDeg);
	}
	
    public PanodroidGLView(Activity activity, CubicPanoNative pano) {
        super(activity);
    	
        renderer = new PanodroidVortexRenderer(this, pano);
        setRenderer(renderer);
    }
    
}
