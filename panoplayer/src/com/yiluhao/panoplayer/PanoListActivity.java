package com.yiluhao.panoplayer;

import java.io.IOException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.yiluhao.utils.IoUtil;
import com.yiluhao.utils.IoUtil.ImageCallBack;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class PanoListActivity extends ListActivity implements OnScrollListener {
	private List<Map<String, Object>> panoDatas;
	
	private String project_id = "1";
	private View mLoadLayout;
	private ListView mListView;
	private ListViewAdapter mListViewAdapter;
	private TextView loadMoreTip;

	private int pageSize = 15;
	private int mLastItem = 0;
	private int mCount = 0;
	private final Handler mHandler = new Handler();
	private AsyncHttpClient client;
	private String projectInfoUrl = null;
	private IoUtil ioUtil = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pano_list);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			project_id = extras.getString("id");
		}
		mLoadLayout = getLayoutInflater().inflate(R.layout.load_more, null);
		loadMoreTip = (TextView) mLoadLayout.findViewById(R.id.load_more_text);

		/**
		 * 获取ListView组件，并将"加载项"布局添加到ListView组件的Footer中。
		 */
		ioUtil = new IoUtil();
		client = new AsyncHttpClient();
		
		projectInfoUrl = "http://beta1.yiluhao.com/ajax/m/pl/id/"+project_id;
		getPanosData();

	}

	/**
	 * 错误提示
	 */
	private void getWrong(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG)
		.show();
	}
	
	private void startPanoViewerActivity(String id) {
		Intent intent = new Intent(this, PanoPlayerActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("pano_id", id);
		bundle.putString("project_id", project_id);
		intent.putExtras(bundle);

		startActivity(intent);
	}

	/**
	 * 绑定列表item点击事件
	 * 
	 * @author faashi
	 */
	private class MainItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String pano_id = (String) panoDatas.get(position).get("id");
			startPanoViewerActivity(pano_id);
		}
	}

	public void onScroll(AbsListView view, int mFirstVisibleItem,
			int mVisibleItemCount, int mTotalItemCount) {
		mLastItem = mFirstVisibleItem + mVisibleItemCount - 1;
		if (mListViewAdapter.count >= mCount) {
			mListView.removeFooterView(mLoadLayout);
		}
	}

	public void onScrollStateChanged(AbsListView view, int mScrollState) {

		/**
		 * 当ListView滑动到最后一条记录时这时，我们会看到已经被添加到ListView的"加载项"布局， 这时应该加载剩余数据。
		 */
		if (mLastItem == mListViewAdapter.count
				&& mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if (mListViewAdapter.count <= mCount) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						int listDisplayTotal = mListViewAdapter.count
								+ pageSize;
						if (listDisplayTotal > mCount) {
							mListViewAdapter.count = mCount;
							mListView.removeFooterView(mLoadLayout);
						} else {
							mListViewAdapter.count += pageSize;
						}
						mListViewAdapter.notifyDataSetChanged();
						mListView.setSelection(mLastItem);
					}
				}, 1000);
			}
		}
	}

	/**
	 * 获取项目文件数据
	 * 
	 * @return
	 */
	private void getPanosData() {

		String response = null;
		if( ioUtil.FileExists(project_id, projectInfoUrl)){
			Log.v("infoCached=", "cached");
			response = ioUtil.ReadStringFromSD(project_id, projectInfoUrl);
	        ExtractProjectDatas(response);
	        LoadListView();
	        return ;
		}
		
		//AsyncHttpClient client = new AsyncHttpClient();
		client.get(projectInfoUrl, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	if(response =="" || response == null){
		    		return ;
		    	}
		    	ioUtil.SaveStringToSD(project_id, projectInfoUrl, response);
		        ExtractProjectDatas(response);
		        LoadListView();
		        return ;
		    }
		    public void onFailure(Throwable error, String content){
		    	getWrong("获取项目信息失败");
		    }
		});
		
		return ;
		
	}
	
	private void ExtractProjectDatas(String content){
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		mCount = 0;
		if (content == "" || content == null) {
			getWrong("获取项目信息失败，请检查您的网络设置");
			pageSize = 0;
			return ;
		}
		try {
			JSONObject jsonObject = new JSONObject(content);
			JSONArray jsonArray = jsonObject.getJSONArray("panos");
			mCount = jsonArray.length();
			if (pageSize > mCount) {
				pageSize = mCount;
			}
			for (int i = 0; i < mCount; i++) {
				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
				map = new HashMap<String, Object>();
				map.put("id", jsonObject2.getString("id"));
				map.put("title", jsonObject2.getString("title"));
				map.put("created", jsonObject2.getString("created"));
				map.put("thumb", jsonObject2.getString("thumb"));
				list.add(map);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		panoDatas = list;
		return ;
	}

	private void LoadListView(){
		mListView = getListView();
		mListView.addFooterView(mLoadLayout);
		/**
		 * 组ListView组件设置Adapter,并设置滑动监听事件。
		 */
		mListViewAdapter = new ListViewAdapter(this);
		setListAdapter(mListViewAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(new MainItemClickListener());
	}
	
	class ListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		int count = pageSize;

		public int getCount() {
			return count;
		}

		public ListViewAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.panolist_item, null);
				holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (panoDatas.isEmpty()) {
				return convertView;
			}

			String thumbFileName = panoDatas.get(position).get("thumb").toString();
			
			holder.thumb.setTag(thumbFileName);
			Bitmap thumb = null;
			
			if(ioUtil.FileExists(project_id, thumbFileName)){
				Log.v("picCached=", "cached");
				thumb = ioUtil.ReadBitmapFromSD(project_id, thumbFileName);
			}
			else{
				//AsyncHttpClient client = new AsyncHttpClient();
				String[] allowedContentTypes = new String[] { "image/png", "image/jpeg", "image/gif" };
				client.get(thumbFileName, new BinaryHttpResponseHandler(allowedContentTypes, thumbFileName) {
				    @Override
				    public void onSuccess(byte[] fileData, String thumbFileName) {
				    	if(fileData.length<1 || thumbFileName == null || thumbFileName == ""){
				    		return ;
				    	}
				    	//position = 1;
				    	Bitmap thumbPic = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, null);  
				    	ioUtil.SavePicToSD(project_id, thumbFileName, thumbPic);
				    	
				    	ImageView imageViewByTag = (ImageView) mListView
								.findViewWithTag(thumbFileName);
						if (imageViewByTag != null) {
							imageViewByTag.setImageBitmap(thumbPic);
						}
						
				    }
				    public void onFailure(Throwable error, String content){
				    	//getWrong("获取地图文件失败");
				    }
				});
			}

			
			if (thumb == null) {
				holder.thumb.setImageResource(R.raw.loading);
			} else {
				holder.thumb.setImageBitmap(thumb);
			}

			holder.title.setText((String) panoDatas.get(position).get("title"));
			String created = getString(R.string.photo_time) + "\n"
					+ (String) panoDatas.get(position).get("created");
			holder.info.setText(created);

			return convertView;
		}
	}

	/**
	 * 列表页数据组装
	 * 
	 * @author faashi
	 * 
	 */
	public final class ViewHolder {
		public ImageView thumb;
		public TextView title;
		public TextView info;
	}
}
