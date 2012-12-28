package com.yiluhao.panoplayer;

import java.io.File;
import java.io.IOException;

import com.yiluhao.utils.IoUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class FileTestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_test);
		
		IoUtil fileIO = new IoUtil();
		String filename = "project.cfg";
		String projectStr = "";
		File mfile = new File(filename);
		/*if (mfile.exists()) {// 若该文件存在
			try {
				projectStr = fileIO.readFile(filename);
				Log.v("etag", "read success" + projectStr);
			} catch (IOException e) {
				Log.v("etag", "read file error");
			}
		} else {
			try {
				fileIO.writeFile(filename, projectStr);
				Log.v("etag", "save success");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		
	}
}
