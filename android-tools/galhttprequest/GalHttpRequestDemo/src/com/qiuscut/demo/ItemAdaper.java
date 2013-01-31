 package com.qiuscut.demo;   

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**   
 * @Title: ItemAdaper.java    
 * @Package com.qiuscut.demo   
 * @Description: TODO(添加描述)   
 * @author 林秋明   
 * @date 2012-4-23 下午8:47:17   
 * @version V1.0   
 */
public class ItemAdaper extends BaseAdapter {
	Context mContext;
	ArrayList<String> demoList  = new ArrayList<String>();
	public ItemAdaper(Context context) {
		this.mContext =context;
		demoList.add("同步请求InputStream");
		demoList.add("同步请求String");
		demoList.add("同步请求Bitmap");
		demoList.add("异步请求InputStream");
		demoList.add("异步请求String");
		demoList.add("异步请求Bitmap");
		demoList.add("组装http参数");
		demoList.add("Post内容");
	}
	
	@Override
	public int getCount() {
		return demoList.size();
	}

	@Override
	public Object getItem(int position) {
		return demoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		String itemText= (String) getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.listviewitem, null);
			holder.item = (TextView) convertView.findViewById(R.id.textView1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.item.setText(itemText);
		return convertView;
	}

	public class ViewHolder{
		TextView item;
	}
}
