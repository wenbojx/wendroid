package com.yiluhao.utils;

import java.io.IOException;


import org.json.JSONException;
import org.json.JSONObject;
import org.openpanodroid.panoutils.android.CubicPanoNative;


import android.app.Activity;
import android.graphics.Bitmap;

public class GetPanoFace extends Activity{

	private CubicPanoNative panoFace( JSONObject panoInfo, String project_id, String pano_id ){
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
		CubicPanoNative cubicPano = new CubicPanoNative(bfront, bback, bup, bdown, bleft, bright);
		return cubicPano;
    }
}
