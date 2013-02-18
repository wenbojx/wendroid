package com.ece594bb.geopan;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class GeoPan extends Activity implements OnClickListener
{
	private static Button buttonMap;
	private static Button buttonCamera;
	private DataHelper dh;
	
	private static final int MENU_ID_LOAD_DB = 0;
    private static final int MENU_ID_DELETE_DB = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        buttonMap = (Button)findViewById(R.id.ButtonMap);
        buttonMap.setOnClickListener(this); 
        
        buttonCamera = (Button)findViewById(R.id.ButtonCamera);
        buttonCamera.setOnClickListener(this); 
        
        this.dh = new DataHelper(this);
    }
    
    public void onClick(View src) 
	{
		
		switch(src.getId())
		{
			case R.id.ButtonMap:	
				List<String> pictures = this.dh.selectAll();//this.dh.selectCoordinates();
		        if(pictures.size() == 0)
		        {
		        		Toast.makeText(GeoPan.this, "No Pictures!", Toast.LENGTH_SHORT).show();
		        }
		        else
		        {
		        		Intent map = new Intent(this, MapsActivity.class);
					startActivityForResult(map, 0);
		        }
				break;
				
			case R.id.ButtonCamera:
				Intent cam = new Intent(this, CameraActivity.class);
				startActivityForResult(cam, 0);				
				break;
		}
		
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    
    /*public void onPause()
	{
		super.onDestroy();
		this.finish();// close the application
	}*/
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        menu.add(Menu.NONE, MENU_ID_LOAD_DB, 0, "Load Demo");
        menu.add(Menu.NONE, MENU_ID_DELETE_DB, 1, "Delete Demo");
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case MENU_ID_LOAD_DB:
            		try
                {
                		this.dh.copyDataBase();
                }
                catch (IOException ioe) 
                {        	 
             		throw new Error("Unable to copy database");
                	}
                break;

            case MENU_ID_DELETE_DB:
                this.dh.deleteAll();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}