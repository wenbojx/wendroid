package com.yiluhao.panoplayer;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*SimpleAdapter adapter = new SimpleAdapter(this,getProjectData(),R.layout.panolist_item,
                new String[]{"title","info","img"},
                new int[]{R.id.title,R.id.info,R.id.img});
        setListAdapter(adapter);*/
		mData = getProjectData();
		MyAdapter adapter = new MyAdapter(this);
		setListAdapter(adapter);
		
		 ListView lv = getListView();
		 lv.setOnItemClickListener(new MainItemClickListener());
	}
	
	private void startPanoViewerActivity(String id) {
		Log.i(LOG_TAG, "id"+ id);
		Intent intent = new Intent(this, MapDragActivity.class);
		//intent.putExtra(FlickrPanoViewerActivity.FLICKR_PANO_INFO, (Parcelable) selectedImageInfo);
		
		startActivity(intent); 
	}
	
	private class MainItemClickListener implements OnItemClickListener{
	        /**
	         * @param parent 浠ｈ〃褰撳墠鐨刧ridview
	         * @param 浠ｈ〃鐐瑰嚮鐨刬tem
	         * @param 褰撳墠鐐瑰嚮鐨刬tem鍦ㄩ�閰嶄腑鐨勪綅缃�
	         * @param id The row id of the item that was clicked.
	         */
	  public void onItemClick(AdapterView<?> parent, View view, int position,
	    long id) {
		   // TODO Auto-generated method stub
		   //Log.i(TAG, "position"+ position);
		  String pano_id = (String) mData.get(position).get("id");
		  //showmsg(pano_id);
		  Log.i(LOG_TAG, "id"+ pano_id);
		  startPanoViewerActivity(pano_id);
	  }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private List<Map<String, Object>> getProjectData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "101");
        map.put("title", "G1");
        map.put("info", "google 1");
        map.put("thumb", R.raw.hxy);
        list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("id", "102");
        map.put("title", "G2");
        map.put("info", "google 2");
        map.put("thumb", R.raw.xh);
        list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("id", "103");
        map.put("title", "G3");
        map.put("info", "google 3");
        map.put("thumb", R.raw.xx);
        list.add(map);
         
        return list;
    }
	
	public void showmsg(String msg){
		new AlertDialog.Builder(this)
		.setTitle("鎴戠殑listview")
		.setMessage(msg)
		.setPositiveButton("纭畾", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
		
	}
	
	public final class ViewHolder{
		public ImageView thumb;
		public TextView title;
		public TextView info;
	}
	
	public class MyAdapter extends BaseAdapter{

		private LayoutInflater mInflater;
		
		
		public MyAdapter(Context context){
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
				
				holder=new ViewHolder();  
				
				convertView = mInflater.inflate(R.layout.panolist_item, null);
				holder.thumb = (ImageView)convertView.findViewById(R.id.thumb);
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				convertView.setTag(holder);
				
			}else {
				
				holder = (ViewHolder)convertView.getTag();
			}
			
			
			holder.thumb.setBackgroundResource((Integer)mData.get(position).get("thumb"));
			holder.title.setText((String)mData.get(position).get("title"));
			holder.title.setTextColor(Color.BLACK);
			holder.info.setText((String)mData.get(position).get("info"));
			holder.info.setTextColor(Color.BLACK);

			return convertView;
		}
		
	}
}
