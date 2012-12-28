package com.yiluhao.utils;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class GetUrlInfo {
	private String projects = "http://192.168.2.102/html/json.php";
	private String panos = "http://192.168.2.102/html/json.php?panos=";
	private String pano = "http://192.168.2.102/html/json.php?pano=";
	private String panoPicPath = "http://beta1.yiluhao.com/html/Panorama.jpg";
	
	// 获取网络全景图片
	public Bitmap GetPanoPic(String projec_id, String pano_id){
		String urlPath = panoPicPath;
		Bitmap bitmap = getWebPic(urlPath);
		return bitmap;
	}
	/**
	 * 获取网络全景图地址
	 */
	public Uri GetPanoPath(String projec_id, String pano_id){
		String path=  panoPicPath;
		Uri panoUri = null;
		try {
			URL url = new URL(path);
		    panoUri = Uri.parse(url.toString());
		} catch (MalformedURLException e) {
			Log.e("Invalid", "Invalid URL");
		}
		return panoUri;
	}
    /**
     * 获取配置文件信息
     */
	public String GetConfigInfo(Integer type, String id){
		String content = "";
		if(type == 1)
		{
			content = GetProjectList();
		}
		else if (type == 2)
		{
			content = GetPanoList(id);
		}
		else if (type == 3)
		{
			content = GetPanoDetail(id);
		}
		return content;
	}
	/**
	 * 获取项目列表
	 */
	public String GetProjectList() {
		return FetchWebString(projects);
	}
	/**
	 * 获取项目场景列表
	 */
	public String GetPanoList(String id){
		panos = panos+id;
		return FetchWebString(panos);
	}
	/**
	 * 获取全景信息
	 */
	public String GetPanoDetail (String id){
		pano = pano+id;
		return FetchWebString(pano);
	}
	/**
	 * 获取远程图片
	 */
	public Bitmap getWebPic (String imageUrl){
		Bitmap mBitmap = getWebData(imageUrl);
		return mBitmap;
	}
	/**
	 * 获取远程数据
	 */
	public  Bitmap getWebData (String weburl) {
		Bitmap mBitmap = null;
		try {
			URL url = new URL(weburl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Log.v("Path", weburl);
			InputStream in = conn.getInputStream();
			mBitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mBitmap;
	}
	/**
	 * 获取远程文本文件
	 * @param path
	 * @return
	 */
	public String FetchWebString(String path) {
		Log.v("Path", path);
		String content = "";
		StringBuilder response = new StringBuilder();
		try {
			URL url = new URL(path);
			HttpURLConnection httpconn = (HttpURLConnection) url
					.openConnection();
			if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader input = new BufferedReader(
						new InputStreamReader(httpconn.getInputStream()), 8192);
				String strLine = null;
				while ((strLine = input.readLine()) != null) {
					response.append(strLine);
				}
				input.close();
			}
			content = response.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	
	/*
	 * 
	 * 参考
	 */
	public Bitmap getImgFromSd(String fileName ){
    	Log.v("EagleTag",fileName);
    	Bitmap mBitmap = null;
    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    	{
    		//String filePath=Environment.getExternalStorageDirectory().getPath();
    	}
    	String file_folder = Environment.getExternalStorageDirectory()+"/yiluhao";
    	File f = new File(file_folder);
    	//可读
    	if(f.canRead())
    	Log.v("EagleTag","very bad");
    	//可写
    	if(f.canWrite())
    	Log.v("EagleTag","very good");
    	
    	String file_path = file_folder+"/"+fileName;
    	Log.v("EagleTag",file_path);
    	File mfile=new File(file_path);
    	
        if (mfile.exists()) {//若该文件存在
        	Log.v("EagleTag",file_path+"存在");
        	mBitmap = BitmapFactory.decodeFile(file_path);
        }
    	
    	return mBitmap;
    }
    public Bitmap getBitmap(String imageUrl) {
		Bitmap mBitmap = null;
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream is = conn.getInputStream();
			mBitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mBitmap;
	}
}
