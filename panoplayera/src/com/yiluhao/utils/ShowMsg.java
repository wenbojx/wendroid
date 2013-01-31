package com.yiluhao.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class ShowMsg extends Activity {
	public void AlertMsg(String msg) {
		new AlertDialog.Builder(this)
				.setTitle("listview")
				.setMessage(msg)
				.setPositiveButton("sdf",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();

	}
}
