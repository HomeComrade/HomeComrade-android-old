package com.shiznatix.mediacomrade.android.adapters;

import java.util.ArrayList;
import java.util.List;

import com.shiznatix.mediacomrade.android.entities.Server;
import com.shiznatix.mediacomrade.android.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ServerCellAdapter extends ArrayAdapter<Server> {
	private final Context mContext;
	private List<Server> mServers = new ArrayList<Server>();
	
	public ServerCellAdapter(Context context, int resourceId, List<Server> servers) {
		super(context, resourceId, servers);
		
		mServers = servers;
		mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = View.inflate(mContext, R.layout.cell_server, null);
		
		Server server = mServers.get(position);
		
		TextView serverUrlTextView = (TextView)rowView.findViewById(R.id.serverUrlTextView);
		serverUrlTextView.setText(server.url);
		
		return rowView;
	}
}
