package com.ece594bb.geopan;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public CustomItemizedOverlay(Drawable defaultMarker, Context context) 
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	public void addOverlay(OverlayItem overlay) 
	{
		mOverlays.add(overlay);
		populate();
	}
	
	public void removeOverlay(OverlayItem overlay) {
        mOverlays.remove(overlay);
        populate();
    }
	
	 public void clear() 
	 {
        mOverlays.clear();
        populate();
	 }

	@Override
	protected OverlayItem createItem(int i) 
	{
		return mOverlays.get(i);
	}

	@Override
	public int size() 
	{
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) 
	{
		OverlayItem item = mOverlays.get(index);
		
		//this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri))); 
		
		//Intent myIntent = new Intent(this, GeoPan.class);
		//myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//this.startActivity(myIntent, 0); 
		
		//Intent i = new Intent(this, GeoPan.class);
		Intent viewPicture = new Intent(mContext, com.ece594bb.geopan.PictureViewer.class);
		viewPicture.putExtra("PICTURE LOCATION", item.getTitle());
		mContext.startActivity(viewPicture);

		//startActivityForResult(i, 0);
		//AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		//dialog.setTitle(item.getTitle());
		//dialog.setMessage(item.getSnippet());
		//dialog.show();
		//Intent mainMenu = new Intent(this, GeoPan.class);
		//startActivityForResult(mainMenu, 0);
		return true;
	}
}

