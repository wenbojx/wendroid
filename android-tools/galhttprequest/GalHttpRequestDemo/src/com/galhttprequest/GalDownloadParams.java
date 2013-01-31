package com.galhttprequest;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;


public class GalDownloadParams {
	/****下载路径**/
	private URL downLoadUrl;
	
	/****在状态栏的标题**/
	private String titleName;
	
	/**保存文件名*/
	String fileName;

	Context mContext;
	
	int notify_id=0;

	
	

	public URL getDownLoadUrl() {
		return downLoadUrl;
	}

	public GalDownloadParams(URL downLoadUrl, String titleName,String fileName, Context mContext,
			int notifyId) {
		super();
		this.downLoadUrl = downLoadUrl;
		this.setTitleName(titleName);
		this.mContext = mContext;
		this.fileName = fileName;
		notify_id = notifyId;
	}
	public GalDownloadParams(String downLoadUrl, String titleName,String fileName, Context mContext,
			int notifyId) {
		super();
		try {
			this.downLoadUrl = new URL(downLoadUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.setTitleName(titleName);
		this.mContext = mContext;
		this.fileName = fileName;
		notify_id = notifyId;
	}

	public void setDownLoadUrl(URL downLoadUrl) {
		this.downLoadUrl = downLoadUrl;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}
	
	
}
