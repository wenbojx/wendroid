package com.yiluhao.panoplayer;


import android.app.TabActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;
public class MainTabActivity extends TabActivity implements OnCheckedChangeListener{
	private RadioGroup mainTab;
	private TabHost tabhost;
	private Intent iMain;
	private Intent iHome;
	private Intent iAbout;
	//private Intent iExit;
	private boolean flashPage = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_tab);
        mainTab=(RadioGroup)findViewById(R.id.main_tab);
        mainTab.setOnCheckedChangeListener(this);
        tabhost = getTabHost();
        
        iHome = new Intent(this, MainActivity.class);
        tabhost.addTab(tabhost.newTabSpec("iHome")
        		.setIndicator(getResources().getString(R.string.main_home), getResources().getDrawable(R.drawable.icon_main_home))
        		.setContent(iHome));
        
		iAbout = new Intent(this, AboutActivity.class);
		tabhost.addTab(tabhost.newTabSpec("iAbout")
	        	.setIndicator(getResources().getString(R.string.main_about), getResources().getDrawable(R.drawable.icon_main_about))
	        	.setContent(iAbout));
		/*iExit = new Intent(this, AboutActivity.class);
		tabhost.addTab(tabhost.newTabSpec("iExit")
	        	.setIndicator(getResources().getString(R.string.main_exit), getResources().getDrawable(R.drawable.icon_2_n))
	        	.setContent(iExit));*/
		
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId){
		case R.id.main_home_tab:
			this.tabhost.setCurrentTabByTag("iHome");
			break;
		case R.id.main_about_tab:
			this.tabhost.setCurrentTabByTag("iAbout");
			break;
		/*case R.id.main_exit_tab:
			this.finish();
			Intent intent = new Intent(this, WelcomeActivity.class);
			Bundle bundle = new Bundle();  
			String exit = "1";
			bundle.putString("exit", exit);  
			intent.putExtras(bundle);  
			startActivity(intent);
			break;*/
		}
	}
}