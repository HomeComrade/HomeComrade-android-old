package com.shiznatix.mediacomrade.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shiznatix.mediacomrade.android.adapters.DirectoryCellAdapter;
import com.shiznatix.mediacomrade.android.entities.DirectoryListing;
import com.shiznatix.mediacomrade.android.entities.LastDirectory;
import com.shiznatix.mediacomrade.android.entities.Server;
import com.shiznatix.mediacomrade.android.models.CommandsModel;
import com.shiznatix.mediacomrade.android.models.ConnectionModel;
import com.shiznatix.mediacomrade.android.tables.LastDirectoryTable;
import com.shiznatix.mediacomrade.android.tables.ServersTable;
import com.shiznatix.mediacomrade.android.tools.FlowLayout;
import com.shiznatix.mediacomrade.android.R;
import com.shiznatix.mediacomrade.dialogs.CurrentPlaylistDialog;
import com.shiznatix.mediacomrade.dialogs.InsertIntoPlaylistAtDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BrowseActivity extends Activity {
	static private final String LOG_TAG = "mc_BrowseActivity";
	
	private Server mServer;
	private List<String> mFolder = new ArrayList<String>();
	private List<DirectoryListing> mFolderContents = new ArrayList<DirectoryListing>();
	private ListView mDirListView;
	
	private List<String> mSelectedFiles = new ArrayList<String>();
	private Button mPlayAllButton;
	private Button mEnqueueAllButton;
	private Button mInsertAllAtPositionButton;
	
	private ProgressDialog mConnectingDialog;
	
	private BroadcastReceiver mCommandStartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command started");
			
			Bundle bundle = intent.getExtras();
			
			mConnectingDialog = new ProgressDialog(BrowseActivity.this);
			mConnectingDialog.setTitle(R.string.browseFetchingDir);
			mConnectingDialog.setMessage(String.format(getResources().getString(R.string.sendingCommand), bundle.getString("command"), bundle.getString("url")));
			mConnectingDialog.show();
		}
	};
	
	private BroadcastReceiver mCommandSuccessReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command success");
			mConnectingDialog.dismiss();
		}
	};
	
	private BroadcastReceiver mCommandResponseReceivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command response received");
			mConnectingDialog.dismiss();
			
			Bundle bundle = intent.getExtras();
			
			String type = bundle.getString("type");
			String data = bundle.getString("data");
			
			try {
				if (null != type && null != data) {
					if (type.equals(CommandsModel.COMMAND_BROWSE)) {
						try {
							parseJsonData(data);
						}
						catch (Exception e) {
							Log.d(LOG_TAG, e.getMessage());
							finish();
						}
					}
					else if (type.equals(CommandsModel.COMMAND_CURRENT_PLAYLIST)) {
						try {
							JSONArray showsArray = new JSONArray(data);
							
							if (showsArray.length() < 1) {
								throw new Exception(getResources().getString(R.string.emptyPlaylistError));
							}
							
							CurrentPlaylistDialog currentPlaylistDialog = new CurrentPlaylistDialog(BrowseActivity.this, showsArray, mServer);
							currentPlaylistDialog.show();
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else if (type.equals(CommandsModel.COMMAND_CURRENT_PLAYLIST_SELECTION)) {
						InsertIntoPlaylistAtDialog insertIntoPlaylistAtDialog = new InsertIntoPlaylistAtDialog(BrowseActivity.this, data, mSelectedFiles, mServer);
						insertIntoPlaylistAtDialog.show();
					}
					else {
						String message = null;
						if (type.equals(CommandsModel.COMMAND_PLAY) || type.equals(CommandsModel.COMMAND_INSERT_AT)) {
							message = getResources().getString(R.string.browseFileSetToPlay);
						}
						else if (type.equals(CommandsModel.COMMAND_ENQUEUE)) {
							message = getResources().getString(R.string.browseFileAddedToPlaylist);
						}
						
						if (null != message) {
							Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				}
			}
			catch (Exception e) {
				Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};
	
	private BroadcastReceiver mCommandFailureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command failure");
			mConnectingDialog.dismiss();
			
			String command = intent.getExtras().getString("command");
			Toast toast = Toast.makeText(context, String.format(getResources().getString(R.string.commandFailed), command), Toast.LENGTH_SHORT);
			toast.show();
			
			finish();
		}
	};
	
	View.OnClickListener mFolderButtonOnClickListener = new View.OnClickListener() {
		public void onClick(View view) {
			List<String> dirBuilder = new ArrayList<String>();
			
			Log.i(LOG_TAG, "folder button clicked id: "+view.getId());
			
			if (view.getId() > -1 && view.getId() < mFolder.size()) {
				for (int i = 0; i < mFolder.size(); i++) {
					dirBuilder.add(mFolder.get(i));
					
					if (view.getId() == i) {
						break;
					}
				}
			}
			
			mFolder = dirBuilder;
			getDirList(getFullFolderPathFromFolderList());
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		
		mDirListView = (ListView)findViewById(R.id.dirContentsListView);
		
		try
		{
			String url = getIntent().getExtras().getString("url");
			
			if (null == url) {
				throw new Exception("Missing url");
			}
			
			ServersTable serversTable = new ServersTable(this);
			mServer = serversTable.getServerByUrl(url);
			if (null == mServer) {
				mServer = new Server(-1, url);
			}
			
			String dirData = null;
			if (null == savedInstanceState) {
				//first creation, get dir list && setup our broadcast receivers
				LastDirectoryTable lastDirectoryTable = new LastDirectoryTable(this);
				LastDirectory lastDirectory = lastDirectoryTable.getLastDirectoryByServer(mServer.url);
				
				if (null == lastDirectory) {
					getDirList(null);
				}
				else {
					getDirList(lastDirectory.directory);
				}
			}
			else {
				dirData = savedInstanceState.getString("dirData");
				
				if (null == dirData) {
					throw new Exception("Missing saved browsing state");
				}
				
				parseJsonData(dirData);
			}
		}
		catch (Exception e) {
			Log.d(LOG_TAG, "Exception 1: "+e.getMessage());
			finish();
		}
		
		//List view functions
		mDirListView.setAdapter(new DirectoryCellAdapter(this, R.id.serverListView, mFolderContents));
		
		mDirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				DirectoryListing directoryListing = mFolderContents.get(position);
				
				if (directoryListing.isDir) {
					if (directoryListing.name.equals(getResources().getString(R.string.browseBack))) {
						mFolder.remove((mFolder.size() - 1));
					}
					else {
						mFolder.add(directoryListing.name);
					}
				}
				
				String directoryPath = getFullFolderPathFromFolderList();
				final String fullPath = directoryPath+directoryListing.name;
				
				if (directoryListing.isDir) {
					getDirList(directoryPath);
				}
				else if (directoryListing.isPlayable) {
					ImageView checkImage = (ImageView)view.findViewById(R.id.directoryListingCheckImage);
					
					if (!mSelectedFiles.contains(fullPath)) {
						checkImage.setImageResource(android.R.drawable.checkbox_on_background);
						mSelectedFiles.add(fullPath);
						directoryListing.isSelected = true;
					}
					else {
						checkImage.setImageResource(android.R.drawable.checkbox_off_background);
						mSelectedFiles.remove(fullPath);
						directoryListing.isSelected = false;
					}
					
					togglePlayButtonsActive();
					((DirectoryCellAdapter)mDirListView.getAdapter()).notifyDataSetChanged();
				}
			}
		});
		
		//play / enqueue buttons
		mPlayAllButton = (Button)findViewById(R.id.playAll);
		mEnqueueAllButton = (Button)findViewById(R.id.enqueueAll);
		mInsertAllAtPositionButton = (Button)findViewById(R.id.insertAllAtPosition);
		
		mPlayAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "playAllButton: "+mSelectedFiles);
				
				sendPlayCommand(CommandsModel.COMMAND_PLAY);
			}
		});
		
		mEnqueueAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "enqueueAllButton: "+mSelectedFiles);
				
				sendPlayCommand(CommandsModel.COMMAND_ENQUEUE);
			}
		});
		
		mInsertAllAtPositionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "insertAllAtPositionButton: "+mSelectedFiles);
				
				sendBasicCommand(CommandsModel.COMMAND_CURRENT_PLAYLIST_SELECTION);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//setup our broadcast receivers
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandStartReceiver, new IntentFilter(ConnectionModel.COMMAND_START_KEY));
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandSuccessReceiver, new IntentFilter(ConnectionModel.COMMAND_SUCCESS_KEY));
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandResponseReceivedReceiver, new IntentFilter(ConnectionModel.COMMAND_RESPONSE_RECEIVED_KEY));
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandFailureReceiver, new IntentFilter(ConnectionModel.COMMAND_FAILURE_KEY));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//remove our broadcast receivers
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandStartReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandSuccessReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandResponseReceivedReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandFailureReceiver);
		
		LastDirectoryTable lastDirectoryTable = new LastDirectoryTable(this);
		lastDirectoryTable.saveLastDirectory(new LastDirectory(0, mServer.url, getFullFolderPathFromFolderList()));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browse, menu);
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
			case R.id.actionShowCurrentPlaylist:
				sendBasicCommand(CommandsModel.COMMAND_CURRENT_PLAYLIST);
				return true;
			case R.id.actionRefresh:
				getDirList(getFullFolderPathFromFolderList());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		try {
			JSONObject dirDataState = new JSONObject();
			JSONArray dirListState = new JSONArray();
			
			for (int i = 0; i < mFolderContents.size(); i++) {
				String name = mFolderContents.get(i).name;
				
				//dont save the back button...
				if (name.equals(getResources().getString(R.string.browseBack))) {
					continue;
				}
				
				JSONObject entry = new JSONObject();
				
				entry.put("name", mFolderContents.get(i).name);
				entry.put("type", (mFolderContents.get(i).isDir ? "dir" : "file"));
				entry.put("isPlayable", mFolderContents.get(i).isPlayable);
				
				dirListState.put(entry);
			}
			
			dirDataState.put("dir", TextUtils.join("/", mFolder));
			dirDataState.put("dirList", dirListState);
			
			savedInstanceState.putString("dirData", dirDataState.toString());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void getDirList(String fullPath) {
		try {
			ConnectionModel connectionModel = new ConnectionModel(this, mServer.url);
			connectionModel.clearPostParams();
			
			if (null != fullPath) {
				Map<String, String> postParams = new HashMap<String, String>();
				postParams.put("dir", fullPath);
				
				connectionModel.setPostParams(postParams);
				
				Log.i(LOG_TAG, "postParams: "+postParams);
			}
			
			connectionModel.setCommand(CommandsModel.COMMAND_BROWSE);
			connectionModel.sendCommand();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseJsonData(String data) throws JSONException {
		JSONObject jsonObject = new JSONObject(data);
		String browsedDir = jsonObject.getString("dir");
		JSONArray dirListArray = jsonObject.getJSONArray("dirList");
		
		setFolderListFromString(browsedDir);
		setCurrentDirButtonLayout();
		
		mFolderContents.clear();
		
		for (int i = 0; i < dirListArray.length(); i++) {
			String listingName = dirListArray.getJSONObject(i).getString("name");
			String listingType = dirListArray.getJSONObject(i).getString("type");
			boolean listingIsPlayable = dirListArray.getJSONObject(i).getBoolean("isPlayable");
			
			mFolderContents.add(new DirectoryListing(listingName, listingType, listingIsPlayable));
		}
		
		Collections.sort(mFolderContents, new Comparator<DirectoryListing>() {
			public int compare(DirectoryListing d1, DirectoryListing d2) {
				int result = 0;
				
				if (d1.isDir == d2.isDir) {
					if (d1.isPlayable == d2.isPlayable) {
						result = d1.name.compareToIgnoreCase(d2.name);
					}
					else {
						result = (d1.isPlayable ? -1 : 1);
					}
				}
				else {
					result = (d1.isDir ? -1 : 1);
				}
				
				return result;
			}
		});
		
		if (mFolder.size() > 0) {
			mFolderContents.add(0, new DirectoryListing(getResources().getString(R.string.browseBack), "dir", false));
		}
		
		//refresh our list view here
		if (null != mDirListView.getAdapter()) {
			mSelectedFiles.clear();//clear all selected files
			togglePlayButtonsActive();
			((DirectoryCellAdapter)mDirListView.getAdapter()).notifyDataSetChanged();
			
			mDirListView.post( new Runnable() {
				@Override
				public void run() {
					mDirListView.smoothScrollToPosition(0);
				}
			});
		}
	}
	
	private void setFolderListFromString(String string) {
		List<String> checkFolders = Arrays.asList(TextUtils.split(string, "/"));
		
		mFolder.clear();
		
		for (String checkFolder : checkFolders) {
			if (null != checkFolder && !checkFolder.isEmpty() && !checkFolder.equals("")) {
				mFolder.add(checkFolder);
			}
		}
	}
	
	private String getFullFolderPathFromFolderList() {
		String folderPath = TextUtils.join("/", mFolder);
		
		if (folderPath.equals("")) {
			folderPath = "/";
		}
		else {
			if (!folderPath.startsWith("/")) {
				folderPath = "/"+folderPath;
			}
			if (!folderPath.endsWith("/")) {
				folderPath += "/";
			}
		}
		
		return folderPath;
	}
	
	private void setCurrentDirButtonLayout() {
		FlowLayout browsingDirLayout = (FlowLayout)findViewById(R.id.browsingDirLayout);
		browsingDirLayout.removeAllViews();
		
		float fontSize = 20.0f;
		
		for (int i = -1; i < mFolder.size(); i++) {
			String folderName = getResources().getString(R.string.browseRootFolder);
			
			if (i > -1) {
				folderName = mFolder.get(i);
			}
			
			TextView folderButton = new TextView(this);
			folderButton.setPadding(2, 0, 2, 0);
			folderButton.setText(folderName);
			folderButton.setTextColor(Color.BLUE);
			folderButton.setTextSize(fontSize);
			folderButton.setPaintFlags(folderButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			folderButton.setId(i);
			folderButton.setOnClickListener(mFolderButtonOnClickListener);
			
			TextView seperator = new TextView(this);
			seperator.setText("/");
			seperator.setTextColor(Color.BLACK);
			seperator.setTextSize(fontSize);
			
			browsingDirLayout.addView(folderButton);
			browsingDirLayout.addView(seperator);
		}
	}
	
	private void togglePlayButtonsActive() {
		boolean enabled = (mSelectedFiles.size() > 0);
		
		mPlayAllButton.setEnabled(enabled);
		mEnqueueAllButton.setEnabled(enabled);
		mInsertAllAtPositionButton.setEnabled(enabled);
	}
	
	private void sendPlayCommand(String command) {
		if (mSelectedFiles.size() < 1) {
			return;
		}
		
		try {
			JSONArray filePaths = new JSONArray(mSelectedFiles);
			
			ConnectionModel connectionModel = new ConnectionModel(this, mServer.url);
			connectionModel.clearPostParams();
			connectionModel.setPostParam("filePaths", filePaths.toString());
			connectionModel.setCommand(command);
			connectionModel.sendCommand();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendBasicCommand(String command) {
		try {
			ConnectionModel connectionModel = new ConnectionModel(this, mServer.url);
			connectionModel.clearPostParams();
			connectionModel.setCommand(command);
			connectionModel.sendCommand();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}