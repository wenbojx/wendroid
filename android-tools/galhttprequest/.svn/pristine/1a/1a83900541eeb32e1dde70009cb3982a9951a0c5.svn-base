package com.galhttprequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
public class GalDownLoadTask extends AsyncTask<GalDownloadParams, Integer, Void> {
	public static final int bufferSize = 512 * 1024;
	private static HashMap<String, GalDownLoadTask> runningMap = new HashMap<String, GalDownLoadTask>();

	Context mContext;
	GalDownloadParams downloadParams = null;
	GalDownLoadTaskListener listener;
	/** 保存的绝对路径+文件名 */
	String filePath;
	/** 显示的文件名 */
	String titleName;
	int notify_id;
	boolean isFailed;
	boolean isCancel = false;
	File saveFile;
	int filesize;

	int errCode = 0;
	int speed;
	NotificationManager mNotificationManager;

	public GalDownLoadTask(Context context, GalDownLoadTaskListener listener) {
		mContext = context;
		this.listener = listener;
	}

	@Override
	protected Void doInBackground(GalDownloadParams... params) {
		downloadParams = params[0];

		URL downloadURL = downloadParams.getDownLoadUrl();
		mContext = downloadParams.mContext;
		notify_id = downloadParams.notify_id;

		filePath = downloadParams.getFileName();
		titleName = downloadParams.getTitleName();

		File file = null;
		file = new File(filePath);

		if (runningMap.containsKey(filePath)) {
			cancel(true);
			return null;
		}
		runningMap.put(filePath, this);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		// 先删除久的内容
		saveFile = file;

		if (saveFile.exists() && this.listener != null
				&& this.listener.onLoadFileExisting(mContext, downloadParams)) {
			return null;
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) downloadURL
					.openConnection();
			GALURL galurl = GalDBHelper.getInstance(mContext).getURL(
					downloadURL.toString());
			if (galurl != null) {
				if (!GalStringUtil.isEmpty(galurl.getLastModified())) {
					conn.setRequestProperty("If-Modified-Since",
							galurl.getLastModified());
				}
				if (!GalStringUtil.isEmpty(galurl.getEtag())) {
					conn.setRequestProperty("If-None-Match", galurl.getEtag());
				}
			}
			int statucode = conn.getResponseCode();
			switch (statucode) {
			case HttpURLConnection.HTTP_OK: {
				if (saveFile.exists()) {
					saveFile.delete();
					saveFile.createNewFile();
				}
				bis = new BufferedInputStream(conn.getInputStream(), bufferSize);
				bos = new BufferedOutputStream(new FileOutputStream(saveFile),
						bufferSize);
				int totalSize = conn.getContentLength();
				filesize = totalSize;
				readFromInputStream(bis, bos);
				break;
			}
			case HttpURLConnection.HTTP_NOT_MODIFIED:{
				// 从缓存读取数据
				File cacheFile = new File(galurl.getLocalData());
				conn.disconnect();
				bis = new BufferedInputStream(new FileInputStream(cacheFile),
						bufferSize);
				bos = new BufferedOutputStream(new FileOutputStream(saveFile),
						bufferSize);
				int totalSize = (int) cacheFile.length();
				filesize = totalSize;
				readFromInputStream(bis, bos);
				break;
			}
			default:
				isFailed = true;
				errCode = statucode;
				break;
			}
		} catch (Exception e) {
			LogUtil.e(new Throwable().getStackTrace()[0].toString()
					+ " Exception ", e);
			isFailed = true;
		} finally {
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}

			} catch (IOException e) {
			}
		}

		return null;
	}

	private void readFromInputStream(BufferedInputStream bis,
			BufferedOutputStream bos) throws IOException {
		byte[] buf = new byte[bufferSize];
		int progress = 0;
		int finishedSize = 0;
		int readLen = -1;
		long time = System.currentTimeMillis();
		int lencount = 0;
		while ((readLen = bis.read(buf)) != -1 && !isCancel) {
			bos.write(buf, 0, readLen);
			finishedSize += readLen;
			lencount += readLen;
			// 计算新进度
			int newProgress = (int) (((double) finishedSize / filesize) * 100);
			long curTime = System.currentTimeMillis();
			if (newProgress - progress > 0) {
				if (curTime - time > 1000) {
					speed = (int) (((lencount * 1000) >> 10) / (curTime - time));
					lencount = 0;
					time = curTime;
				}
				publishProgress(newProgress);
			}
			progress = newProgress;
		}
		if (isCancel && finishedSize != filesize) {
		} else {
			publishProgress(100);// 下载完成
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (runningMap != null) {
			runningMap.remove(filePath);
		}
		if (isFailed) {
			if (this.listener != null) {
				this.listener.onLoadFailed(mContext, downloadParams, errCode);
			}
			return;
		}
		if (isCancel) {
			if (this.listener != null) {
				this.listener.onLoadCancel(mContext, downloadParams);
			}
			return;
		}
		if (this.listener != null) {
			this.listener.onLoadFinish(mContext, downloadParams);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		int progress = values[0];
		// float size = ((int) ((filesize >> 10) * 10.0f / 1024)) / 10.0f;
		// CharSequence contentTitle = titleName + " [" + size + "M]";
		// CharSequence contentText = "正在下载，已完成  " + progress + "%," + speed
		// + "k/s";
		if (this.listener != null) {
			this.listener.onLoadProgress(mContext, downloadParams, progress,
					filesize,speed);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onCancelled() {
		if (runningMap != null) {
			runningMap.remove(filePath);
		}
		if (this.listener != null) {
			this.listener.onLoadCancel(mContext, downloadParams);
		}
		super.onCancelled();
	}

	// 是否正在下载，fileName不包含路径名
	public static boolean isDownLoadingFile(String fileName) {
		return runningMap.containsKey(fileName);
	}

	public static void cancelDownload(String filename) {
		try {
			if (isDownLoadingFile(filename)) {
				runningMap.get(filename).isCancel = true;
			}
		} catch (Exception e) {
			LogUtil.e(new Throwable().getStackTrace()[0].toString()
					+ " Exception ", e);
		}
	}

	public interface GalDownLoadTaskListener {
		/** 如果下载不再重新下载则返回true,在子线程调用 */
		public boolean onLoadFileExisting(Context context, GalDownloadParams params);

		public void onLoadProgress(Context context, GalDownloadParams params,
				int progress, long allsize,int kbpersecond);

		public void onLoadFinish(Context context, GalDownloadParams params);

		public void onLoadFailed(Context context, GalDownloadParams params, int err);

		public void onLoadCancel(Context context, GalDownloadParams params);
	}

}
