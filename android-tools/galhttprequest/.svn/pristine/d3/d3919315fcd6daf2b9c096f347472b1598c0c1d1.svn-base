package com.qiuscut.demo;

import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.galhttprequest.GalHttpRequest;
import com.galhttprequest.GalHttpRequest.GalHttpLoadImageCallBack;
import com.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.galhttprequest.GalHttpRequest.GalHttpRequestListener;

/**
 * @Title: RequestActivity.java
 * @Package com.qiuscut.demo
 * @author 林秋明
 * @date 2012-4-23 下午10:20:44
 * @version V1.0
 */
public class RequestActivity extends Activity {
//	static final String PATH_INPUTSTREAM = "http://192.168.1.220:9090/KaKaZhuan/card/getAllCardAndCollection.do";
	static final String PATH_INPUTSTREAM = "http://qiuming.sinaapp.com/";
	static final String PATH_STRING = "http://qiuming.sinaapp.com/";
	static final String PATH_BITMAP = "http://tp3.sinaimg.cn/1859125850/180/5628821209/1";
	static final String PATH_WITHPARAMS = "http://qiuming.sinaapp.com/";
	static final String PATH_POSTCONTENT = "http://qiuming.sinaapp.com/";
	int type;
	EditText textView;
	ImageView imageView;
	TextView title;
	Handler handler;

	public static class RequestType {
		public static final int SYNC_REQUESTIS = 1;
		public static final int SYNC_REQUESTSTRING = 2;
		public static final int SYNC_REQUESTBITMAP = 3;
		public static final int ASYN_REQUESTIS = 4;
		public static final int ASYN_REQUESTSTRING = 5;
		public static final int ASYN_REQUESTBITMAP = 6;
		public static final int ASYN_EASYPARAMS = 7;
		public static final int ASYN_EASYPOST = 8;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		type = getIntent().getIntExtra("type", 1);
		handler = new Handler();
		setupViews();
		startRequest(type);
	}

	private void setupViews() {
		setContentView(R.layout.activity_request);
		textView = (EditText) findViewById(R.id.textView);
		imageView = (ImageView) findViewById(R.id.imageView);
		title = (TextView) findViewById(R.id.requestcontent);
	}

	/**
	 * @Title: startRequest
	 * @Description:TODO(这里用一句话描述这个方法的作用)
	 * @param @param type 传入参数名字
	 * @return void 返回类型
	 * @date 2012-4-23 下午11:37:16
	 * @throw
	 */
	private void startRequest(int type) {
		GalHttpRequest request;
		textView.setVisibility(View.GONE);
		imageView.setVisibility(View.GONE);
		switch (type) {
		case RequestType.SYNC_REQUESTIS: {
			// 同步请求InputStream
			title.setText("同步请求InputStream");
			request = GalHttpRequest.requestWithURL(this, PATH_INPUTSTREAM);

			// 如果不检测缓存，则设置：
			// request.setCacheEnable(false);
			// 必须在调用startXXX()函数之前设置

			// 返回的缓存已经是ufferedInputStream类型
			InputStream is = request.startSynchronous();
			textView.setVisibility(View.VISIBLE);
			if (is!=null) {
				textView.setText(is.toString());
			}
			break;
		}
		case RequestType.SYNC_REQUESTSTRING: {
			// 同步请求String
			title.setText("同步请求String");
			request = GalHttpRequest.requestWithURL(this, PATH_STRING);
			// 根据服务器返回的状态读取内容，如果服务器内容没有改变，则直接读取缓存内容，如果服务器内容已经修改，则从服务器拉取数据
			// 并刷新缓存内容
			String string = request.startSyncRequestString();
			textView.setText(string);
			textView.setVisibility(View.VISIBLE);
			break;
		}
		case RequestType.SYNC_REQUESTBITMAP: {
			// 同步请求Bitmap
			title.setText("同步请求Bitmap");
			Header header = new BasicHeader("Accept-Language", "zh-cn,zh;q=0.5");
			// 支持添加自定义的Http Header请求
			request = GalHttpRequest.requestWithURL(this, PATH_BITMAP,
					new Header[] { header });
			// 请求Bitmap，由于图片基本上不改变，因此如果存在缓存，则直接从缓存读取
			Bitmap bitmap = request.startSyncRequestBitmap();
			imageView.setImageBitmap(bitmap);
			imageView.setVisibility(View.VISIBLE);
			break;
		}
		case RequestType.ASYN_REQUESTIS: {
			// 异步请求InputStream
			title.setText("异步请求InputStream");
			request = GalHttpRequest.requestWithURL(this, PATH_INPUTSTREAM);
			// 必须先设置回调函数，否则调用异步请求无效
			request.setListener(new GalHttpRequestListener() {
				@Override
				public void loadFinished(final InputStream is, boolean fromcache) {
					//注意，由于返回的是InputStream，一般情况都需要长时间操作，所以，回调函数是在子线程调用
					//因此使用handler
					handler.post(new Runnable() {
						@Override
						public void run() {
							textView.setText(is.toString());
							textView.setVisibility(View.VISIBLE);
						}
					});
				}
				@Override
				// 请求失败时，有可能可以从缓存里面读取数据返回
				public void loadFailed(final HttpResponse respone,
						InputStream cacheInputStream) {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							textView.setText(respone.toString());
							textView.setVisibility(View.VISIBLE);
						}
					});
				}
			});
			request.startAsynchronous();
			break;
		}
		case RequestType.ASYN_REQUESTSTRING: {
			title.setText("异步请求String");
			// 异步请求String
			request = GalHttpRequest.requestWithURL(this, PATH_STRING);
			//第一次调用startAsynRequestString或者startAsynRequestBitmap必须在主线程调用
			//因为只有在主线程中调用才可以初始化GalHttprequest内部的全局句柄Handler
			request.startAsynRequestString(new GalHttpLoadTextCallBack() {
				@Override
				public void textLoaded(String text) {
					//该部分允许于UI线程
					textView.setText(text);
					textView.setVisibility(View.VISIBLE);
				}
			});
			break;
		}
		case RequestType.ASYN_REQUESTBITMAP: {
			title.setText("异步请求Bitmap");
			// 异步请求Bitmap
			request = GalHttpRequest.requestWithURL(this, PATH_BITMAP);
			request.startAsynRequestBitmap(new GalHttpLoadImageCallBack() {
				@Override
				public void imageLoaded(Bitmap bitmap) {
					imageView.setImageBitmap(bitmap);
					imageView.setVisibility(View.VISIBLE);
				}
			});
			break;
		}
		case RequestType.ASYN_EASYPARAMS: {
			// 异步组装参数
			title.setText("组装http参数");
			//交给GalHttprequest自动组装url中的参数
			NameValuePair feedPair = new BasicNameValuePair("p","51");
			request = GalHttpRequest.requestWithURL(this, PATH_WITHPARAMS,feedPair);
			request.startAsynRequestString(new GalHttpLoadTextCallBack() {
				@Override
				public void textLoaded(String text) {
					//该部分允许于UI线程
					textView.setText(text);
					textView.setVisibility(View.VISIBLE);
				}
			});
			break;
		}
		case RequestType.ASYN_EASYPOST: {
			// 异步post 数据给服务器
			title.setText("异步post 数据给服务器");
			//交给GalHttprequest自动组装url中的参数
			request = GalHttpRequest.requestWithURL(this, PATH_POSTCONTENT);
			//设置post内容
			request.setPostValueForKey("name", "qiuscut");
			request.startAsynRequestString(new GalHttpLoadTextCallBack() {
				@Override
				public void textLoaded(String text) {
					//该部分允许于UI线程
					textView.setText("在这里post应该是无效的，因为当前url不支持post");
					textView.setVisibility(View.VISIBLE);
				}
			});
			break;
		}
		default:
			finish();
			return;
		}
	}
}
