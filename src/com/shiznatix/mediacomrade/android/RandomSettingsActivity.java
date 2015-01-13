package com.shiznatix.mediacomrade.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shiznatix.mediacomrade.android.adapters.RandomSettingCellAdapter;
import com.shiznatix.mediacomrade.android.entities.RandomShow;
import com.shiznatix.mediacomrade.android.entities.RandomShowCategory;
import com.shiznatix.mediacomrade.android.tables.RandomShowsTable;
import com.shiznatix.mediacomrade.android.R;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class RandomSettingsActivity extends ExpandableListActivity {
	static private final String LOG_TAG = "mc_RandomSettingsActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_random_settings);
		
		List<RandomShowCategory> showCategories = new ArrayList<RandomShowCategory>();
		
		try {
			String showsData = getIntent().getExtras().getString("showsData");
			
			if (null == showsData) {
				Log.d(LOG_TAG, "Missing showsData for Random Settings Activity");
				finish();
			}
			
			JSONObject jsonObject = new JSONObject(showsData);
			JSONArray categories = jsonObject.names();
			RandomShowsTable randomShowsTable = new RandomShowsTable(this);
			
			Log.d(LOG_TAG, "show categories: "+categories);
			
			for (int i = 0; i < categories.length(); i++) {
				String categoryName = categories.getString(i);
				JSONArray showsJsonArray = jsonObject.getJSONArray(categoryName);
				List<RandomShow> shows = new ArrayList<RandomShow>();
				
				//add the shows to the array
				for (int y = 0; y < showsJsonArray.length(); y++) {
					String showTitle = showsJsonArray.getString(y);
					
					RandomShow randomShow = randomShowsTable.getShowByTitle(showTitle);
					
					if (null == randomShow) {
						shows.add(new RandomShow(-1, showTitle));
					}
					else {
						shows.add(randomShow);
					}
				}
				
				Collections.sort(shows, new Comparator<RandomShow>() {
					public int compare(RandomShow s1, RandomShow s2) {
						return s1.title.compareToIgnoreCase(s2.title);
					}
				});
				
				//add the shows to the category / map
				showCategories.add(new RandomShowCategory(categoryName, shows));
			}
		}
		catch (JSONException e) {
			Log.d(LOG_TAG, "Invalid JSON for showsData");
			finish();
		}
		
		Collections.sort(showCategories, new Comparator<RandomShowCategory>() {
			public int compare(RandomShowCategory c1, RandomShowCategory c2) {
				return c1.title.compareToIgnoreCase(c2.title);
			}
		});
		
		RandomSettingCellAdapter listAdapter = new RandomSettingCellAdapter(this, showCategories);
		setListAdapter(listAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.random_settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.actionDisconnect:
				Intent intent = new Intent(this, ConnectionsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		String showTitle = ((TextView)view.findViewById(R.id.randomSettingShow)).getText().toString();
		
		RandomShowsTable randomShowsTable = new RandomShowsTable(this);
		RandomShow randomShow = randomShowsTable.getShowByTitle(showTitle);
		
		TextView randomSettingShow = (TextView)view.findViewById(R.id.randomSettingShow);
		ImageView checkboxImage = (ImageView)view.findViewById(R.id.randomSettingShowCheck);
		
		if (null == randomShow) {
			randomShowsTable.addShow(showTitle);
			checkboxImage.setImageResource(android.R.drawable.checkbox_on_background);
			randomSettingShow.setTextColor(Color.BLACK);
		}
		else {
			randomShowsTable.deleteShow(randomShow);
			checkboxImage.setImageResource(android.R.drawable.checkbox_off_background);
			randomSettingShow.setTextColor(Color.GRAY);
		}
		
		return true;
	}
}
