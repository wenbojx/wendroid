package com.yiluhao.panoplayer;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yiluhao.utils.IoUtil;
import com.yiluhao.utils.IoUtil.ImageCallBack;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private List<Map<String, Object>> mData;
	private MyAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 列表展示
		adapter = new MyAdapter(this);
		DrawListView();
	}
	private void DrawListView(){
		mData = getProjectData();
        setListAdapter(adapter);
		// 绑定列表item点击事件
		ListView lv = getListView();
		lv.setOnItemClickListener(new MainItemClickListener());
	}

	private void startPanoViewerActivity(String id) {
		Log.i(LOG_TAG, "id" + id);
		Intent intent = new Intent(this, PanoListActivity.class);
		
		Bundle bundle = new Bundle();  
		bundle.putString("id", id);  
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
			String pano_id = (String) mData.get(position).get("id");
			Log.i(LOG_TAG, "id" + pano_id);
			startPanoViewerActivity(pano_id);
		}
	}

	/**
	 * 获取项目文件数据
	 * 
	 * @return
	 */
	private List<Map<String, Object>> getProjectData() {
		String configStr = "";
		String fileName = "/projects.cfg";
		Integer type = 1;
		String id = "0";
		IoUtil ioutil = new IoUtil();
		try{
			configStr = ioutil.ReadStringFromSD(fileName, type, id);
		}catch (IOException e){
			e.printStackTrace();
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			JSONObject jsonObject = new JSONObject(configStr);
			JSONArray jsonArray = jsonObject.getJSONArray("project");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
				map = new HashMap<String, Object>();
				map.put("id", jsonObject2.getString("id"));
				map.put("title", jsonObject2.getString("title"));
				map.put("info", jsonObject2.getString("info"));
				map.put("count", jsonObject2.getString("count"));
				Log.v("PROJECT",
						jsonObject2.getString("title")
								+ jsonObject2.getString("thumb"));
				map.put("thumb", jsonObject2.getString("thumb"));
				list.add(map);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return list;
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

	/**
	 * 列表页数据适配器
	 * 
	 * @author faashi
	 * 
	 */
	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.mainlist_item, null);
				holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			IoUtil ioutil = new IoUtil();
			String fileName = "/"+mData.get(position).get("id")+"/thumb.jpg";
			Bitmap thumb = null;
			try{
				thumb = ioutil.ReadPicFromSDThread(fileName, mData.get(position).get("thumb").toString(), holder.thumb, new ImageCallBack()  
                {  
                    @Override  
                    public void imageLoad(ImageView imageView, Bitmap bitmap)  
                    {  
                        imageView.setImageBitmap(bitmap);  
                    }  
                }
				);  
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(thumb == null){
				holder.thumb.setImageResource(R.raw.loading);
			} else{
				holder.thumb.setImageBitmap(thumb);
			}
			holder.title.setText((String) mData.get(position).get("title"));
			holder.title.setTextColor(Color.BLACK);
			String info = "含场景" + (String) mData.get(position).get("count")
					+ "个," + (String) mData.get(position).get("info");
			holder.info.setText(info);
			holder.info.setTextColor(Color.BLACK);

			return convertView;
		}

	}
}
