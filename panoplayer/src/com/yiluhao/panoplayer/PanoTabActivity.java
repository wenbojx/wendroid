package com.yiluhao.panoplayer;


import android.app.TabActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;
public class PanoTabActivity extends TabActivity implements OnCheckedChangeListener{
	private RadioGroup mainTab;
	private TabHost tabhost;
	private Intent iHome;
	private Intent iInfor;
	private Intent iMap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String project_id = "1";
		if (extras != null) {
			project_id = extras.getString("id");
		}
		Log.v("PROJECT", project_id+"tab");
		
        setContentView(R.layout.pano_tab);
        mainTab=(RadioGroup)findViewById(R.id.pano_tab);
        mainTab.setOnCheckedChangeListener(this);
        tabhost = getTabHost();
        
        iHome = new Intent(this, PanoListActivity.class);
        Bundle bundle_home = new Bundle();  
        bundle_home.putString("id", project_id);  
		iHome.putExtras(bundle_home);
        tabhost.addTab(tabhost.newTabSpec("iHome")
        		.setIndicator(getResources().getString(R.string.pano_home), getResources().getDrawable(R.drawable.icon_1_n))
        		.setContent(iHome));
        
		iInfor = new Intent(this, PanoInfoActivity.class);
		Bundle bundle_infor = new Bundle();  
		bundle_infor.putString("id", project_id);  
		iInfor.putExtras(bundle_infor);
		tabhost.addTab(tabhost.newTabSpec("iInfor")
	        	.setIndicator(getResources().getString(R.string.pano_info), getResources().getDrawable(R.drawable.icon_2_n))
	        	.setContent(iInfor));
		
		iMap = new Intent(this, ProjectMapActivity.class);
		Bundle bundle_map = new Bundle();  
		bundle_map.putString("id", project_id);  
		iMap.putExtras(bundle_map);
		tabhost.addTab(tabhost.newTabSpec("iMap")
	        	.setIndicator(getResources().getString(R.string.pano_map), getResources().getDrawable(R.drawable.icon_2_n))
	        	.setContent(iMap));
    }
   

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId){

		case R.id.pano_home_tab:
			this.tabhost.setCurrentTabByTag("iHome");
			break;
		case R.id.pano_info_tab:
			this.tabhost.setCurrentTabByTag("iInfor");
			break;
		case R.id.pano_map_tab:
			this.tabhost.setCurrentTabByTag("iMap");
			break;
		}
	}
}