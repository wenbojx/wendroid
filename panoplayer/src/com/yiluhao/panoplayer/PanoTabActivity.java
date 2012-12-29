package com.yiluhao.panoplayer;


import android.app.TabActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
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
        setContentView(R.layout.pano_tab);
        mainTab=(RadioGroup)findViewById(R.id.pano_tab);
        mainTab.setOnCheckedChangeListener(this);
        tabhost = getTabHost();
        
        iHome = new Intent(this, PanoListActivity.class);
        tabhost.addTab(tabhost.newTabSpec("iHome")
        		.setIndicator(getResources().getString(R.string.main_home), getResources().getDrawable(R.drawable.icon_1_n))
        		.setContent(iHome));
        
		iInfor = new Intent(this, AboutActivity.class);
		tabhost.addTab(tabhost.newTabSpec("iInfor")
	        	.setIndicator(getResources().getString(R.string.main_news), getResources().getDrawable(R.drawable.icon_2_n))
	        	.setContent(iInfor));
		iMap = new Intent(this, ProjectMapActivity.class);
		tabhost.addTab(tabhost.newTabSpec("iMap")
	        	.setIndicator(getResources().getString(R.string.main_news), getResources().getDrawable(R.drawable.icon_2_n))
	        	.setContent(iMap));
		
    }
   

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId){
		case R.id.main_home_tab:
			//this.tabhost.setCurrentTabByTag("iHome");
			Intent intent = new Intent(this, MainTabActivity.class);  
	        startActivity(intent);
			break;
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