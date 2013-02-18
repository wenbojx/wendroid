package com.ece594bb.geopan;

import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapsActivity extends MapActivity implements OnClickListener, LocationListener
{
    
    private MapView mapView;
    private Button buttonGoBack;
    private DataHelper dh;

	private LocationManager lm;
	private static double currentLat = 0.0;
	private static double currentLon = 0.0;
    
    //private static final int latitudeE6 = 34413522;
    //private static final int longitudeE6 = -119841405;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        this.dh = new DataHelper(this);
        
        lm= (LocationManager) this.getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        
        buttonGoBack = (Button)findViewById(R.id.ButtonBack);
        buttonGoBack.setOnClickListener(this);
        
        mapView = (MapView) findViewById(R.id.map_view);       
        mapView.setBuiltInZoomControls(true);
        

        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.map_icon);
        CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable,this);

        itemizedOverlay.clear();

        int i = 0;
        List<String> pictures = this.dh.selectAll();//this.dh.selectCoordinates();//this.dh.selectAll();
        if(pictures.size() == 0)
        {
        		Toast.makeText(MapsActivity.this, "No Pictures!", Toast.LENGTH_SHORT).show();
        		Intent mainMenu = new Intent(this, GeoPan.class);
    			startActivityForResult(mainMenu, 0);
        }
        		
        GeoPoint point = null;
        OverlayItem overlayitem = null;
        
        GeoPoint currentLocation = new GeoPoint((int) (currentLat * 1E6), (int) (currentLon * 1E6));
        
        double lat = 0.0;
        double lon = 0.0;
        String pictureLocation = null;
        
        for (String picture : pictures) 
        {
        		if(i % 4 == 1)
        			lat = Double.parseDouble(picture);
        		
        		else if(i % 4 == 2)
        			lon = Double.parseDouble(picture);
        		
        		else if(i % 4 == 3)
        		{
        			pictureLocation = picture;
        			
        			point = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
            		
        		 	overlayitem = new OverlayItem(point, pictureLocation,null);
        		 	
        		 	itemizedOverlay.addOverlay(overlayitem);
        		}
        		        		
        		i++;
        }
        
        //GeoPoint point = new GeoPoint(30443769,-91158458);
        //OverlayItem overlayitem = new OverlayItem(point, "Laissez les bon temps rouler!", "I'm in Louisiana!");

        //GeoPoint point2 = new GeoPoint(17385812,78480667);
        //OverlayItem overlayitem2 = new OverlayItem(point2, "Namashkaar!", "I'm in Hyderabad, India!");

        //itemizedoverlay.addOverlay(overlayitem);
        //itemizedoverlay.addOverlay(overlayitem2);

        	mapOverlays.add(itemizedOverlay);

        
        MapController mapController = mapView.getController();
        if(currentLat != 0.0 && currentLon != 0.0)
        {
        		mapController.animateTo(currentLocation);
        		mapController.setZoom(4);
        }
    }
    
    
    public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    		Intent mainMenu = new Intent(this, GeoPan.class);
			startActivityForResult(mainMenu, 0);
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    @Override
	public void onClick(View src) 
    {
	    	switch(src.getId())
		{
			case R.id.ButtonBack:				
				Intent i = new Intent(this, GeoPan.class);
				startActivityForResult(i, 0);				
				break;
		}
		
	}

    @Override
    protected boolean isRouteDisplayed() 
    {
        return false;
    }
    
    @Override
	public void onLocationChanged(Location location) 
	{
		if (location != null) 
		{
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();
		}
		Log.d("LocationManager", "Accuracy = " + location.getAccuracy());
		//if(currentLat != 0.0 && currentLon != 0.0)
		if(location.getAccuracy() < 100)
		{
			lm.removeUpdates(this);
	    		lm = null;
		}
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// TODO Auto-generated method stub
		
	}

	
    
}

