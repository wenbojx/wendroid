package com.ece594bb.geopan;


import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.ece594bb.geopan.SimpleZoomListener.ControlType;


public class PictureViewer extends Activity implements OnClickListener
{	
    private static final int MENU_ID_ZOOM = 0;
    private static final int MENU_ID_PAN = 1;
    private static final int MENU_ID_RESET = 2;    
    private static final int MENU_ID_DELETE = 3;
    private static final int MENU_ID_INFO = 4;

    private ImageZoomView mZoomView;

    private ZoomState mZoomState;

    /** On touch listener for zoom view */
    private SimpleZoomListener mZoomListener;

	private static String picturePath;
	private static Bitmap panoramic;
	private DataHelper dh;
	private static final String delimiter = "[/]+";
	private static String filename = "";
	private static String[] tokens;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.picture_view);
			
		Bundle b = getIntent().getExtras();
        if(b == null)
        {
        		panoramic = BitmapFactory.decodeResource(getResources(), R.drawable.picture_not_found);
        }
		
        else
        {
        		picturePath = b.getString("PICTURE LOCATION");
        		
        		File f = new File(picturePath);
        		if(f.exists()) 
        		{        		
	        		BitmapFactory.Options options = new BitmapFactory.Options();
	      		  options.inSampleSize = 2;
	      		  panoramic = BitmapFactory.decodeFile(picturePath, options);
        		}
        		else
        			panoramic = BitmapFactory.decodeResource(getResources(), R.drawable.picture_not_found);
         }
        
        tokens = picturePath.split(delimiter);        
        filename = tokens[3];
        filename = filename.replace(".jpg", "");
        
        this.dh = new DataHelper(this);
        
        mZoomState = new ZoomState();
        mZoomListener = new SimpleZoomListener();
        mZoomListener.setZoomState(mZoomState);
        
        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mZoomView.setZoomState(mZoomState);
        mZoomView.setImage(panoramic); 
        mZoomView.setOnTouchListener(mZoomListener);

        resetZoomState();
	}

	public void onClick(View src) 
	{
		/*switch (src.getId()) 
		{
			case R.id.buttonPick:
				Intent i = new Intent(this, HelloAndroid.class);
				i.putExtra("PICTURE INDEX", pictureId);
				startActivity(i);
	
			break;
		}*/
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {	    		
	    		Intent map = new Intent(this, MapsActivity.class);
			startActivityForResult(map, 0);
			return true;
	    	
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        panoramic.recycle();
        mZoomView.setOnTouchListener(null);
        mZoomState.deleteObservers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        menu.add(Menu.NONE, MENU_ID_ZOOM, 0, "Zoom");
        menu.add(Menu.NONE, MENU_ID_PAN, 1, "Pan");
        menu.add(Menu.NONE, MENU_ID_RESET, 2, "Reset");
        menu.add(Menu.NONE, MENU_ID_DELETE, 3, "Delete");
        menu.add(Menu.NONE, MENU_ID_INFO, 4, "Info");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_ZOOM:
                mZoomListener.setControlType(ControlType.ZOOM);
                break;

            case MENU_ID_PAN:
                mZoomListener.setControlType(ControlType.PAN);
                break;

            case MENU_ID_RESET:
                resetZoomState();
                break;
                
            case MENU_ID_DELETE:
            		new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Picture")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {
                    		deletePicture();
                    		return;
                    }
                })
                .setNegativeButton("No", null)
                .show();       
        			break;
            		
            case MENU_ID_INFO:
            		Date timestamp = new Date(Long.parseLong(filename.trim()));
            		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm aaa");
            		sdf.format(timestamp);
            		Toast.makeText(PictureViewer.this, "Date Taken: " + String.valueOf(sdf.format(timestamp)), Toast.LENGTH_LONG).show();
            		break;
            		
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void deletePicture()
    {
		this.dh.deletePicture(filename); 
		Intent map = new Intent(this, MapsActivity.class);
		startActivityForResult(map, 0);
		return;
    }

    /**
     * Reset zoom state and notify observers
     */
    private void resetZoomState() 
    {
        mZoomState.setPanX(0.5f);
        mZoomState.setPanY(0.5f);
        mZoomState.setZoom(1f);
        mZoomState.notifyObservers();
    }

	
}