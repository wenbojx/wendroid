 package com.galhttprequest;   

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.galhttprequest.GalDownLoadTask.GalDownLoadTaskListener;

/**   
 * @Title: SimpleDownLoadTaskListener.java    
 * @Package galhttprequest   
 * @Description: TODO(添加描述)   
 * @author 林秋明   
 * @date 2012-3-28 下午4:13:00   
 * @version V1.0   
 */
public class SimpleDownLoadTaskListener implements GalDownLoadTaskListener {
	Context mContext;
	Notification notification;
	PendingIntent loadingIntent;
	NotificationManager mNotificationManager;
	
	public SimpleDownLoadTaskListener(Context context) {
		this.mContext = context;
		initNotification();
	}
	private void initNotification() {
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		long when = System.currentTimeMillis();

		loadingIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(), 0);
		notification = new Notification(R.drawable.stat_sys_download, "开始下载", when);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
	}
	@Override
	public boolean onLoadFileExisting(Context context, GalDownloadParams params) {
		return true;
	}

	@Override
	public void onLoadProgress(Context context, GalDownloadParams params,
			int progress, long allsize,int kbpersecond) {
		float size = ((int)((allsize>>10)*10.0f/1024))/10.0f;
		CharSequence contentTitle = params.getTitleName() +" ["+size+"M]";

		CharSequence contentText = "正在下载，已完成  " + progress + "%,"+kbpersecond+"k/s";
		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				loadingIntent);
		mNotificationManager.notify(params.notify_id, notification);
	}

	@Override
	public void onLoadFinish(Context context, GalDownloadParams params) {
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.icon =  R.drawable.stat_sys_download_done;
		CharSequence contentText = "下载完成";
		notification.setLatestEventInfo(context, params.getTitleName(), contentText,
				loadingIntent);
		mNotificationManager.notify(params.notify_id, notification);
	}

	@Override
	public void onLoadFailed(Context context, GalDownloadParams params, int err) {
		CharSequence contentTitle = params.getTitleName();
		CharSequence contentText = params.getTitleName()+" 下载失败";
		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				loadingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(params.notify_id, notification);
	}

	@Override
	public void onLoadCancel(Context context, GalDownloadParams params) {
		CharSequence contentTitle = params.getTitleName();
		CharSequence contentText = "已取消下载 "+params.getTitleName();
		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				loadingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(params.notify_id, notification);
	}

}
