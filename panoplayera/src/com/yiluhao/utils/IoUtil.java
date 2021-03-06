package com.yiluhao.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.EncodingUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class IoUtil {
	public boolean saveFile = true;
	
	private final static String ALBUM_PATH = Environment
			.getExternalStorageDirectory() + "/yiluhao"; // Sd卡目录

	public boolean file_exit(String fileName) {
		boolean exit = false;
		String path = ALBUM_PATH + fileName;
		File file = new File(path);
		if (file.exists()) {
			exit = true;
		}
		return exit;
	}

	/**
	 * 自动创建不存在的目录
	 */
	final private void AutoMkdir(String path) throws IOException {
		String[] s = path.split("/");
		String mkPath = ALBUM_PATH;
		File dirFile = null;
		dirFile = new File(mkPath);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		for (int i = 1; i < s.length - 1; i++) {
			mkPath += "/" + s[i];
			dirFile = new File(mkPath);
			if (!dirFile.exists()) {
				dirFile.mkdir();
				Log.v("mkdir-", mkPath);
			}
		}
	}

	// 写文本数据
	final public void WriteStringToSD(String fileName, String writestr)
			throws IOException {
		if (writestr != "") {
			try {
				AutoMkdir(fileName);
				String path = ALBUM_PATH + fileName;
				FileOutputStream fout = new FileOutputStream(path);
				byte[] bytes = writestr.getBytes();
				fout.write(bytes);
				fout.close();
			} catch (IOException e) {
				Log.v("CONFIGURL", "write file ko");
				e.printStackTrace();
			}
		}

	}

	// 读文本数据
	/**
	 * @param fileName
	 *            文件名
	 * @param type
	 *            类型1项目列表 2全景列表 3 全景信息 4地图
	 * @return
	 * @throws IOException
	 */
	public String ReadStringFromSD(String fileName, Integer type, String id)
			throws IOException {
		String content = "";
		try {
			String path = ALBUM_PATH + fileName;
			File file = new File(path);
			if (!file.exists()) {// 若该文件不存在 去网络上取
				GetUrlInfo urlInfo = new GetUrlInfo();
				content = urlInfo.GetConfigInfo(type, id);
				WriteStringToSD(fileName, content);
				Log.v("CONFIGURL", "read from url");
			} else {
				Log.v("path", path);
				FileInputStream fin = new FileInputStream(path);
				int length = fin.available();
				byte[] buffer = new byte[length];
				fin.read(buffer);
				content = EncodingUtils.getString(buffer, "UTF-8");
				fin.close();
				Log.v("CONFIGURL", "read from local");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	/**
	 * 从网络获取配置文件并保存
	 */
	public String ReadStringFromWeb(String fileName, Integer type, String id)
			throws IOException {
		String content = "";
		
		try {
				GetUrlInfo urlInfo = new GetUrlInfo();
				content = urlInfo.GetConfigInfo(type, id);
				Log.v("content", content+"-----------");
				WriteStringToSD(fileName, content);
				Log.v("CONFIGURL", "read from url");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * 读取文件地址
	 */
	public String GetFilePath(String fileName) {
		String path = ALBUM_PATH + fileName;
		return path;
	}

	/**
	 * 读取文件String
	 */
	public InputStream GetFileStream(String path) {
		InputStream inStream = null;
		if(path == ""  || path == null){
			return inStream;
		}
		try {
			inStream = new FileInputStream(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inStream;
	}

	// 写图片数据
	public void WritePicToSD(Bitmap bm, String fileName) throws IOException {
		Log.v("Filename", fileName);
		if(bm !=null){
			try {
				AutoMkdir(fileName);
				File file = new File(ALBUM_PATH + fileName);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));
				bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @return
	 */
	public boolean FileExists(String fileName) {
		boolean flag = false;
		String path = ALBUM_PATH + fileName;
		File file = new File(path);
		if (file.exists()) {// 若该文件存在
			flag = true;
		}
		return flag;
	}

	/**
	 * 读取地图
	 */
	public void ReadMapFromSDThread(final String fileName,
			final String urlPath, final MapCallBack MapCallBack)
			throws IOException {
		Bitmap bitmap = null;
		String path = ALBUM_PATH + fileName;
		File file = new File(path);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				MapCallBack.LoadMap((Bitmap) msg.obj);
			}
		};
		
		if (file.exists()) {// 若该文件存在
			bitmap = BitmapFactory.decodeFile(path);
			// bitmap.
			Log.v("CONFIGURL", "pic get  from local" + path);
			Message msg = handler.obtainMessage(0, bitmap);
			handler.sendMessage(msg);
			
		} else {
			new Thread() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					GetUrlInfo urlInfo = new GetUrlInfo();
					bitmap = urlInfo.getWebPic(urlPath);

					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);

					try {
						AutoMkdir(fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						WritePicToSD(bitmap, fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Log.v("CONFIGURL", "pic get  from url" + urlPath);
				}
			}.start();
		}
	}

	/**
	 * 读图片文件
	 */
	public Bitmap ReadPicFromSDThread(final String fileName,
			final String urlPath, final ImageView imageView,
			final ImageCallBack imageCallBack) throws IOException {
		Bitmap bitmap = null;
		String path = ALBUM_PATH + fileName;
		File file = new File(path);
		if (file.exists()) {// 若该文件存在
			bitmap = BitmapFactory.decodeFile(path);
			// bitmap.
			Log.v("CONFIGURL", "pic get  from local" + path);
			return bitmap;
		} else {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					imageCallBack.imageLoad(fileName, (Bitmap) msg.obj);
				}
			};
			new Thread() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					GetUrlInfo urlInfo = new GetUrlInfo();
					bitmap = urlInfo.getWebPic(urlPath);

					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);

					try {
						AutoMkdir(fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(bitmap !=null ){
						try {
							WritePicToSD(bitmap, fileName);
						} catch (IOException e) {
							//Log.v("CONFIGURL", "write file ko");
							e.printStackTrace();
						}
					}
					Log.v("CONFIGURL", "pic get  from url" + urlPath);
				}
			}.start();
		}
		return null;
	}

	// 读图片文件
	public Bitmap ReadPicFromSD(String fileName, String urlPath)
			throws IOException {
		Bitmap bitmap = null;
		String path = ALBUM_PATH + fileName;
		File file = new File(path);
		if (file.exists()) {// 若该文件存在
			bitmap = BitmapFactory.decodeFile(path);
			// bitmap.
			//Log.v("CONFIGURL", "pic get  from local" + path);

		} else {
			GetUrlInfo urlInfo = new GetUrlInfo();
			bitmap = urlInfo.getWebPic(urlPath);

			try {
				AutoMkdir(fileName);
			} catch (IOException e) {
				//Log.v("CONFIGURL", "mkdir ko");
				e.printStackTrace();
			}
			try {
				WritePicToSD(bitmap, fileName);
			} catch (IOException e) {
				//Log.v("CONFIGURL", "write file ko");
				e.printStackTrace();
			}
			Log.v("CONFIGURL", "pic get  from url" + urlPath);
		}
		return bitmap;
	}

	/**
	 * 回调接口
	 * 
	 */
	public interface ImageCallBack {
		public void imageLoad(String fileName, Bitmap bitmap);
	}
	public interface MapCallBack {
		public void LoadMap(Bitmap bitmap);
	}
}
