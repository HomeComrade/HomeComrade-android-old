package com.shiznatix.mediacomrade.android.adapters;

import java.util.List;

import com.shiznatix.mediacomrade.android.entities.RandomShow;
import com.shiznatix.mediacomrade.android.entities.RandomShowCategory;
import com.shiznatix.mediacomrade.android.tables.RandomShowsTable;
import com.shiznatix.mediacomrade.android.R;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RandomSettingCellAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private List<RandomShowCategory> mShowCategories;
	
	public RandomSettingCellAdapter(Context context, List<RandomShowCategory> showCategories) {
		mContext = context;
		mShowCategories = showCategories;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mShowCategories.get(groupPosition).shows.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
		View view;
		
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.cell_random_settings_show, null);
		}
		else {
			view = convertView;
		}
		
		RandomShow randomShow = mShowCategories.get(groupPosition).shows.get(childPosition);
		
		TextView randomSettingShow = (TextView)view.findViewById(R.id.randomSettingShow);
		randomSettingShow.setText(randomShow.title);
		
		ImageView randomSettingShowCheck = (ImageView)view.findViewById(R.id.randomSettingShowCheck);
		RandomShowsTable randomShowsTable = new RandomShowsTable(mContext);
		
		if (null == randomShowsTable.getShowByTitle(randomShow.title)) {
			randomSettingShowCheck.setImageResource(android.R.drawable.checkbox_off_background);
			randomSettingShow.setTextColor(Color.GRAY);
		}
		else {
			randomSettingShowCheck.setImageResource(android.R.drawable.checkbox_on_background);
			randomSettingShow.setTextColor(Color.BLACK);
		}
		
		return view;
	}

	@Override
	public int getChildrenCount(int position) {
		return mShowCategories.get(position).shows.size();
	}

	@Override
	public Object getGroup(int position) {
		return mShowCategories.get(position).title;
	}

	@Override
	public int getGroupCount() {
		return mShowCategories.size();
	}

	@Override
	public long getGroupId(int position) {
		return position;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view;
		
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.cell_random_settings_category, null);
		}
		else {
			view = convertView;
		}
		
		RandomShowCategory randomShowCategory = mShowCategories.get(groupPosition);
		
		TextView textLabel = (TextView)view.findViewById(R.id.randomSettingCategory);
		textLabel.setText(randomShowCategory.title);
		
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
