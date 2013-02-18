package com.ece594bb.geopan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraActivity extends Activity implements LocationListener, OnClickListener{
	/** Called when the activity is first created. */
	
	private Button mBtnNewPanorama;
	private Button mBtnGeneratePanorama;
	private Button mBtnTakePicture;

	private DataHelper dh;
	private double lat, lon;
	private LocationManager mLocationManager;

	private int		mImageWidth;
	private int		mImageHeight;
	private int		mImageNumber=0;
	private long 	mProjectTimeStamp;
	private String	mProjectName="";
    private String	mProjectPathString;
    private File	mProjectPathFile;
    private File	mTempImageFile=null;
    private File	mTempPath=null;	// !!! Rename
	private boolean	mIsProcessing=false;
	
    private static final String	TEMP_DIRECTORY_NAME="/temp_images/";
    private static final int	CAMERA_PIC_REQUEST = 1337;  
    private static final int	DIALOG_NEW_PROJECT = 1;
    
	private ImageView 	mImageView;
	private TextView 	mTextOutput; 
	private TextView	mTextImageCount;
	private TextView	mTextPostMessage;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);		
		this.dh = new DataHelper(this);
		
        mTextImageCount = (TextView) findViewById(R.id.txtImageCount);
        mTextPostMessage = (TextView) findViewById(R.id.txtPostMessage);

        mImageView = (ImageView) findViewById(R.id.imgPicture);

        // Create temp directory for storing all non-panoramic images
        mTempPath = new File(getExternalCacheDir().toString() + TEMP_DIRECTORY_NAME);
        mTempPath.mkdirs();
		
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        mTextOutput = (TextView) this.findViewById(R.id.out_text);        
        
        mBtnNewPanorama = (Button)findViewById(R.id.btnNewPanorama);
        mBtnGeneratePanorama = (Button)findViewById(R.id.btnGeneratePanorama);
        mBtnTakePicture = (Button)findViewById(R.id.btnTakePicture);
        
        mBtnNewPanorama.setOnClickListener(this);
        mBtnGeneratePanorama.setOnClickListener(this);
        mBtnTakePicture.setOnClickListener(this);

        initNewProject();
        
        // For Debug Purposes
        mTextOutput.setVisibility(0);
        mTextPostMessage.setVisibility(0);
	}

	private void initNewProject()
	{
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        mProjectTimeStamp = System.currentTimeMillis ();
    	mProjectName = String.valueOf(mProjectTimeStamp);

    	mBtnGeneratePanorama.setEnabled(false);
    	mBtnGeneratePanorama.setText("Generate Panorama \n(Need >3 Images)");
    	mIsProcessing = false;

    	mBtnTakePicture.setEnabled(true);

    	mImageNumber=0;
        mImageWidth = -1;
    	mImageHeight = -1;
    	
        // Create Project Directory
        mProjectPathString = Environment.getExternalStorageDirectory().toString() + "/geopan/" + mProjectName + "/";
        mProjectPathFile = new File(mProjectPathString);
        mProjectPathFile.mkdirs();

        mTextImageCount.setText("Image Number = " + Integer.toString(mImageNumber));
    	//mImageView.;	// Reset ImageView? !!!
        mImageView.setImageResource(R.drawable.icon);

        //mTextOutput.setText("Project Name = " + mProjectName);
	}
	
//	public void onPause(){	
//		// clean up application global
//		// super.onStop();
//		mLocationManager.removeUpdates(this);
//		mLocationManager = null;
//
//		super.onDestroy();
//		this.finish();// close the application
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	    		Intent mainMenu = new Intent(this, GeoPan.class);
			startActivityForResult(mainMenu, 0);
	    }
	    return super.onKeyDown(keyCode, event);
	}

	
	public void onClick(View src){
		switch(src.getId()){
			case R.id.btnNewPanorama:
				showDialog(DIALOG_NEW_PROJECT);
			break;
			
			case R.id.btnTakePicture:
		        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		        mTempImageFile = new File(Environment.getExternalStorageDirectory(), "tempimage.jpg");
		        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempImageFile));
		        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
			break;

			case R.id.btnGeneratePanorama:
				if (!mIsProcessing){
					mIsProcessing = generatePanorama();
			    	mBtnTakePicture.setEnabled(false);
				}else{
					downloadPanorama();
				}
				break;
		}
	}
	
	private boolean generatePanorama(){
		try{
	    	HttpClient client = new DefaultHttpClient();
	
	        // Add Text Fields
	        HttpPost post = new HttpPost("http://dragonox.cs.ucsb.edu/Mosaic3D/process.php");
	        List<NameValuePair> params = new ArrayList<NameValuePair>(6);
	        params.add(new BasicNameValuePair("project", mProjectName));
	        params.add(new BasicNameValuePair("width", Integer.toString(mImageWidth)));		//"320"));  
	        params.add(new BasicNameValuePair("height", Integer.toString(mImageHeight)));	//"240"));  
	        params.add(new BasicNameValuePair("eFocal", "24"));  
	        params.add(new BasicNameValuePair("loop", "0"));  
	        params.add(new BasicNameValuePair("email", "yoav@umail.ucsb.edu"));  
	
	        post.setEntity(new UrlEncodedFormEntity(params));
	        
	        // Post Request  
	        HttpResponse response;
			response = client.execute(post);
				
	        HttpEntity resEntity = response.getEntity();  
	        if (resEntity != null) {    
	        	//mTextPostMessage.setText(resEntity.toString());
	        }

	        // Change Button Functionality
			mBtnGeneratePanorama.setText("Download Panorama");
			
			return true;
    	} catch (Exception e) {
		    	e.printStackTrace();
		    	//mTextPostMessage.setText(e.toString());
		}
    	return false;
	}

	private void downloadPanorama(){
        String imageURL = "mosaic.jpg";
        String fileName = "mosaic.jpg";

        File file = new File(mProjectPathFile, fileName);
        try {
                URL url = new URL("http://dragonox.cs.ucsb.edu/Mosaic3D/uploads/" + mProjectName + "/" + imageURL);
                URLConnection ucon = url.openConnection();
 
                //Define InputStreams to read from the URLConnection.
                InputStream inStream = ucon.getInputStream();
                BufferedInputStream buffInStream = new BufferedInputStream(inStream);
 
                // Read bytes to the Buffer until there is nothing more to read(-1).
                ByteArrayBuffer byteBuffer = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = buffInStream.read()) != -1) {
                	byteBuffer.append((byte) current);
                }
 
                // Convert the Bytes read to a String.
                FileOutputStream fileOutStream = new FileOutputStream(file);
                fileOutStream.write(byteBuffer.toByteArray());
                fileOutStream.close();
                //mTextPostMessage.setText("Download Complete");
				// Insert into Database
                this.dh.insert(String.valueOf(mProjectTimeStamp),String.valueOf(lat), String.valueOf(lon),"/sdcard/geopan/"+String.valueOf(mProjectTimeStamp)+"/mosaic.jpg");
            	mBtnGeneratePanorama.setEnabled(false);
            	mBtnGeneratePanorama.setText("Processing Complete. Enjoy!");
        } catch (IOException e) {
        	//mTextPostMessage.setText("Error: " + e.toString());
        	//mTextPostMessage.setText("Processing Still In Progress!");
        	downloadStatus();
        }
    	mImageView.setImageURI(Uri.fromFile(file));
	}

	private int downloadStatus(){
        String	statusText = ""; 
        int		status=0;
        try {
                URL url = new URL("http://dragonox.cs.ucsb.edu/Mosaic3D/uploads/" + mProjectName + "/" + "status.txt");
                URLConnection ucon = url.openConnection();
 
                //Define InputStreams to read from the URLConnection.
                InputStream inStream = ucon.getInputStream();
                BufferedInputStream buffInStream = new BufferedInputStream(inStream);
                int current = 0;
                while ((current = buffInStream.read()) != -1) {
                	statusText = statusText + (char) current; //byteBuffer.append((byte) current);
                }

                if (statusText.contains("-1")){
                	// Processing Failed
                	//mTextPostMessage.setText("Processing Failed. Please try to create a new Panorama.");
                	mBtnGeneratePanorama.setEnabled(false);
                	mBtnGeneratePanorama.setText("Processing Failed");
                	return -1;
                }
                
                //mTextPostMessage.setText("Status = " + statusText + "% Complete");
                //return status;
        } catch (IOException e) {
        	//mTextPostMessage.setText("Error: " + e.toString());
        	//mTextPostMessage.setText("Status Not Received!");
        }
		
		return 0;	
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_NEW_PROJECT:
            return new AlertDialog.Builder(this)
//                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.new_project_dialog_title)
                .setMessage(R.string.new_project_dialog_msg)
                .setPositiveButton(R.string.new_project_dialog_ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        initNewProject();
                    }
                })
                .setNegativeButton(R.string.new_project_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    // Do Nothing
                    }
                })
                .create();
        }
        return null;
	}


	// Update GPS Location
	@Override
	public void onLocationChanged(Location location){
		if (location != null){
            lat = location.getLatitude();
            lon = location.getLongitude();
		}
//		if(location.getAccuracy() < 50){
//			mLocationManager.removeUpdates(this);
//	    	mLocationManager = null;
//		}
	}

	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intentData) {
		if (requestCode == CAMERA_PIC_REQUEST) {
			if (resultCode == RESULT_OK) {
				
				// Scale the picture to 640x480
				Bitmap tempBitmap = BitmapFactory.decodeFile(mTempImageFile.getAbsolutePath());
	        	// Filtered? false?
    			Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 640, 480, false); 
    			mImageWidth = scaledBitmap.getWidth();  
	        	mImageHeight = scaledBitmap.getHeight();
	        	tempBitmap.recycle();//		!!! recycle?
	            
	        	// Show last picture taken
	        	mImageView.setImageBitmap(scaledBitmap);
	        	
	        	// Set new image name and increment mImageNumber!
	        	mImageNumber++;
	            mTextImageCount.setText("Image Number = " + Integer.toString(mImageNumber));

	        	// Enable Panorama Generation if Enough Images Exist 
	        	if (mImageNumber > 2) {
		        	mBtnGeneratePanorama.setEnabled(true);
		        	mBtnGeneratePanorama.setText("Generate Panorama");
	        	}

	            // Prepare New File
	        	String newImageName = ("img" + Integer.toString(mImageNumber) + ".jpg");
	        	File newImageFile = null;
	        	
	    		// Store image just taken in file
				try {
					// getExternalCacheDir() - Change Dirs? !!!
					newImageFile = new File(Environment.getExternalStorageDirectory(), newImageName);
					FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
				    BufferedOutputStream buffOutStream = new BufferedOutputStream(fileOutputStream);
				    // Set compression to maximum quality
				    scaledBitmap.compress(CompressFormat.JPEG, 100, buffOutStream);
				    // Clean Memory
				    buffOutStream.flush();
				    buffOutStream.close();
				    fileOutputStream.flush();
				    fileOutputStream.close();
				    //thumbnail.recycle();	// !!! This will cause exception!!
				    
				} catch (FileNotFoundException e) {
				    Log.d("FileNotFoundException", e.getMessage());
				} catch (IOException e) {
				    Log.d("IOException", e.getMessage());
				}
	    		
				// Clear Bitmap Cache
				//scaledBitmap.recycle();
				
				// Upload File to Web Server
				try{
			    	HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost("http://dragonox.cs.ucsb.edu/Mosaic3D/clientupload.php");
					FileBody bin = new FileBody(newImageFile);
					MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
					reqEntity.addPart("project", new StringBody(mProjectName));
					reqEntity.addPart(newImageFile.getName(), bin);
					post.setEntity(reqEntity);  
					
					// Post Request  
					HttpResponse response;
					response = client.execute(post);
						
					HttpEntity resEntity = response.getEntity();  
					if (resEntity != null) {    
						//mTextPostMessage.setText(resEntity.toString());
					}
				} catch (Exception e) {
				    	e.printStackTrace();
				    	//mTextPostMessage.setText(e.toString());
				}
    		}
        }
    }

    
	@Override
	public void onProviderDisabled(String provider){
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider){
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras){
		// TODO Auto-generated method stub
	}

}