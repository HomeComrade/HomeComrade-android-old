package com.shiznatix.mediacomrade.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.shiznatix.mediacomrade.android.entities.Server;
import com.shiznatix.mediacomrade.android.models.CommandsModel;
import com.shiznatix.mediacomrade.android.models.ConnectionModel;
import com.shiznatix.mediacomrade.android.models.ServerTypesModel;
import com.shiznatix.mediacomrade.android.tables.RandomShowsTable;
import com.shiznatix.mediacomrade.android.tables.ServersTable;
import com.shiznatix.mediacomrade.android.R;
import com.shiznatix.mediacomrade.dialogs.CurrentPlaylistDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteActivity extends Activity {
	static private final String LOG_TAG = "mc_RemoteActivity";
	
	private Server mServer;
	private String mServerType;
	
	protected int mCommandsQueue = 0;
	protected Toast mToast;
	protected ImageView mOneTimeSelectShowsButton;
	protected ProgressDialog mProgressDialog;
	protected List<String> mOneTimeRandomShows = new ArrayList<String>();
	
	private BroadcastReceiver mCommandStartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command started");
			
			mCommandsQueue++;
			
			Bundle bundle = intent.getExtras();
			
			toast(String.format(getResources().getString(R.string.sendingCommand), bundle.getString("command"), bundle.getString("url")));
		}
	};
	
	private BroadcastReceiver mCommandSuccessReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command success");
			
			mCommandsQueue--;
			
			String command = intent.getExtras().getString("command");
			
			if (!command.equals(CommandsModel.COMMAND_CURRENT_PLAYLIST)) {
				toast(String.format(getResources().getString(R.string.commandSuccess), command));
			}
		}
	};
	
	private BroadcastReceiver mCommandResponseReceivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command response received");
			
			Bundle bundle = intent.getExtras();
			
			String type = bundle.getString("type");
			String data = bundle.getString("data");
			
			try {
				if (null != type && null != data) {
					if (type.equals(CommandsModel.COMMAND_RANDOM_SETTINGS)) {
						Intent randomSettingsActivityIntent = new Intent(RemoteActivity.this, RandomSettingsActivity.class);
						randomSettingsActivityIntent.putExtra("showsData", data);
						startActivity(randomSettingsActivityIntent);
					}
					else if (type.equals(CommandsModel.COMMAND_ALL_SHOWS)) {
						try {
							JSONArray showsArray = new JSONArray(data);
							String[] allShowsList = new String[showsArray.length()];
							
							for (int i = 0; i < showsArray.length(); i++) {
								allShowsList[i] = showsArray.getString(i);
							}
							
							Arrays.sort(allShowsList);
							
							selectOneTimeRandomShows(allShowsList);
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else if (type.equals(CommandsModel.COMMAND_LAST_PLAYLIST) || type.equals(CommandsModel.COMMAND_RANDOM)) {
						try {
							JSONArray showsArray = new JSONArray(data);
							
							if (showsArray.length() < 1) {
								throw new Exception(getResources().getString(R.string.emptyPlaylistError));
							}
							
							String[] toToastArray = new String[showsArray.length()];
							
							for (int i = 0; i < showsArray.length(); i++) {
								toToastArray[i] = showsArray.getString(i);
							}
							
							String toastString = TextUtils.join("\n", toToastArray);
							toast(toastString);
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else if (type.equals(CommandsModel.COMMAND_CURRENT_PLAYLIST)) {
						try {
							JSONArray showsArray = new JSONArray(data);
							
							if (showsArray.length() < 1) {
								throw new Exception(getResources().getString(R.string.emptyPlaylistError));
							}
							
							CurrentPlaylistDialog currentPlaylistDialog = new CurrentPlaylistDialog(RemoteActivity.this, showsArray, mServer);
							currentPlaylistDialog.show();
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
			catch (Exception e) {
				toast(e.getMessage());
			}
			
			if (null != mProgressDialog) {
				mProgressDialog.cancel();
			}
		}
	};
	
	private BroadcastReceiver mCommandFailureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command failure");
			
			if (null != mProgressDialog) {
				mProgressDialog.cancel();
			}
			
			mCommandsQueue--;
			
			String command = intent.getExtras().getString("command");
			
			toast(String.format(getResources().getString(R.string.commandFailed), command));
		}
	};
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);
		
		String url = getIntent().getExtras().getString("url");
		String serverType = getIntent().getExtras().getString("serverType");
		ArrayList<String> serverOptions = getIntent().getExtras().getStringArrayList("serverOptions");
		
		if (null == url) {
			Log.d(LOG_TAG, "Missing url for Remote Activity");
			finish();
		}
		
		ServersTable serversTable = new ServersTable(this);
		mServer = serversTable.getServerByUrl(url);
		if (null == mServer) {
			mServer = new Server(-1, url);
		}
		
		//set our options
		mServer.options = serverOptions;
		
		ServerTypesModel serverTypesModel = new ServerTypesModel();
		if (serverTypesModel.isValidServerType(serverType)) {
			mServerType = serverType;
		}
		else {
			mServerType = ServerTypesModel.TOTEM_SERVER;
		}
		
		mProgressDialog = new ProgressDialog(this);
		mToast = Toast.makeText(this, null, Toast.LENGTH_LONG);
		TextView view = (TextView)mToast.getView().findViewById(android.R.id.message);
		
		if (null != view) {
			view.setGravity(Gravity.CENTER);
		}
		
		//main server title
		TextView connectedServerTitle = (TextView)findViewById(R.id.connectedServerTitle);
		connectedServerTitle.setText(mServer.url);
		
		//ir remote options
		if (!mServer.hasOption("irRemote")) {
			Button volumeDownTvButton = (Button)findViewById(R.id.buttonVolDownTv);
			Button volumeUpTvButton = (Button)findViewById(R.id.buttonVolUpTv);
			Button volumeMuteTvButton = (Button)findViewById(R.id.buttonMuteTv);
			Button sourceTvButton = (Button)findViewById(R.id.buttonSourceTv);
			
			volumeDownTvButton.setVisibility(View.GONE);
			volumeUpTvButton.setVisibility(View.GONE);
			volumeMuteTvButton.setVisibility(View.GONE);
			sourceTvButton.setVisibility(View.GONE);
		}
		
		setupLayoutForServerType();
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
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ServerTypesModel serverTypesModel = new ServerTypesModel();
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(serverTypesModel.getMenuForRemote(mServerType), menu);
		
		for (int i = 0; i < menu.size(); i++) {
			if (!mServer.hasOption("irRemote")) {
				if (menu.getItem(i).getItemId() == R.id.actionPowerTv) {
					menu.getItem(i).setVisible(false);
				}
			}
		}
		
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
			case R.id.actionRandomSettings:
				sendBasicCommand(CommandsModel.COMMAND_RANDOM_SETTINGS);
				return true;
			case R.id.actionShowControls:
				sendBasicCommand(CommandsModel.COMMAND_TOGGLE_CONTROLS);
				return true;
			case R.id.actionLastPlaylist:
				sendBasicCommand(CommandsModel.COMMAND_LAST_PLAYLIST);
				return true;
			case R.id.actionQuit:
				sendBasicCommand(CommandsModel.COMMAND_QUIT);
				return true;
			case R.id.actionShowCurrentPlaylist:
				sendBasicCommand(CommandsModel.COMMAND_CURRENT_PLAYLIST);
				return true;
			case R.id.actionPowerTv:
				sendBasicCommand(CommandsModel.COMMAND_POWER_TV);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void buttonBasicCommandClicked(View view) {
		String command = null;
		
		switch (view.getId()) {
			case R.id.buttonPrevious:
				command = CommandsModel.COMMAND_PREVIOUS;
				break;
			case R.id.buttonNext:
				command = CommandsModel.COMMAND_NEXT;
				break;
			case R.id.buttonPlayPause:
				command = CommandsModel.COMMAND_PLAY_PAUSE;
				break;
			case R.id.buttonBack:
				command = CommandsModel.COMMAND_SEEK_BWD;
				break;
			case R.id.buttonForward:
				command = CommandsModel.COMMAND_SEEK_FWD;
				break;
			case R.id.buttonSkipTitleSequence:
				command = CommandsModel.COMMAND_SKIP_TITLE_SEQUENCE;
				break;
			case R.id.buttonVolDown:
				command = CommandsModel.COMMAND_VOLUME_DOWN;
				break;
			case R.id.buttonVolUp:
				command = CommandsModel.COMMAND_VOLUME_UP;
				break;
			case R.id.buttonVolDownTv:
				command = CommandsModel.COMMAND_VOLUME_DOWN_TV;
				break;
			case R.id.buttonVolUpTv:
				command = CommandsModel.COMMAND_VOLUME_UP_TV;
				break;
			case R.id.buttonMuteTv:
				command = CommandsModel.COMMAND_MUTE_TV;
				break;
			case R.id.buttonSourceTv:
				command = CommandsModel.COMMAND_SOURCE_TV;
				break;
			case R.id.buttonFullScreen:
				command = CommandsModel.COMMAND_FULLSCREEN;
				break;
			case R.id.buttonPlayFile:
				command = CommandsModel.COMMAND_CURRENT_PLAYLIST;
				break;
		}
		
		if (null != command) {
			sendBasicCommand(command);
		}
	}
	
	public void buttonRandomClick(View view) {
		if (view.getId() != R.id.buttonRandom) {
			return;
		}
		
		View titleView = getLayoutInflater().inflate(R.layout.alert_dialog_random_title, null);
		
		mOneTimeSelectShowsButton = (ImageView)titleView.findViewById(R.id.randomDialogSelectShowsButton);
		mOneTimeSelectShowsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i(LOG_TAG, "fetch and show all shows");
				
				mProgressDialog = new ProgressDialog(RemoteActivity.this);
				mProgressDialog.setTitle(R.string.remoteLoading);
				mProgressDialog.setMessage(getString(R.string.remoteFetchingAvailableShows));
				mProgressDialog.show();
				
				sendBasicCommand(CommandsModel.COMMAND_ALL_SHOWS);
			}
		});
		
		final String[] playlistSizes = {"1", "2", "5", "10"};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCustomTitle(titleView)
		.setSingleChoiceItems(playlistSizes, 2, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
			}
		})
		.setPositiveButton(R.string.remoteEnqueue, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				
				sendRandomShowsCommand(playlistSizes[selectedPosition], CommandsModel.COMMAND_ENQUEUE);
			}
		})
		.setNegativeButton(R.string.remotePlay, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                
                sendRandomShowsCommand(playlistSizes[selectedPosition], CommandsModel.COMMAND_PLAY);
			}
		})
		.create()
		.show();
	}
	
	public void selectOneTimeRandomShows(final String[] allShowsList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.remoteSelectShows)
		.setMultiChoiceItems(allShowsList, null, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked) {
					mOneTimeRandomShows.add(allShowsList[which]);
				}
				else if (mOneTimeRandomShows.contains(allShowsList[which])) {
					mOneTimeRandomShows.remove(allShowsList[which]);
				}
			}
		})
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mOneTimeRandomShows.isEmpty()) {
					mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_off);
				}
				else {
					mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_on);
				}
				dialog.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_off);
				mOneTimeRandomShows.clear();
				
				dialog.dismiss();
			}
		})
		.create()
		.show();
	}
	
	public void buttonBrowseClick(View view) {
		if (view.getId() != R.id.buttonBrowse) {
			return;
		}
		
		Intent browseActivityIntent = new Intent(RemoteActivity.this, BrowseActivity.class);
		browseActivityIntent.putExtra("url", mServer.url);
		startActivity(browseActivityIntent);
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
	
	private void sendPostCommand(String command, Map<String, String> postParams) {
		try {
			ConnectionModel connectionModel = new ConnectionModel(this, mServer.url);
			connectionModel.setPostParams(postParams);
			connectionModel.setCommand(command);
			connectionModel.sendCommand();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendRandomShowsCommand(String playlistSize, String playMethod) {
		JSONArray allowedShows;
		
		if (!mOneTimeRandomShows.isEmpty()) {
			allowedShows = new JSONArray(mOneTimeRandomShows);
			mOneTimeRandomShows.clear();
		}
		else {
			RandomShowsTable randomShowsTable = new RandomShowsTable(RemoteActivity.this);
			allowedShows = new JSONArray(randomShowsTable.getAllShowTitles());
		}
		
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("playlistSize", playlistSize);
		postParams.put("allowedShows", allowedShows.toString());
		postParams.put("playMethod", playMethod);
		
		Log.i(LOG_TAG, "sending json: "+postParams);
		
		sendPostCommand(CommandsModel.COMMAND_RANDOM, postParams);
	}
	
	private void setupLayoutForServerType() {
		if (mServerType.equals(ServerTypesModel.OMXPLAYER_SERVER)) {
			Button skipTitleSequenceButton = (Button)findViewById(R.id.buttonSkipTitleSequence);
			Button fullScreenButton = (Button)findViewById(R.id.buttonFullScreen);
			
			skipTitleSequenceButton.setVisibility(View.GONE);
			fullScreenButton.setVisibility(View.GONE);
		}
	}
	
	private void toast(String text) {
		String toastText;
		
		if (0 > mCommandsQueue) {
			mCommandsQueue = 0;
		}
		
		if (null == text) {
			toastText = String.format(getResources().getString(R.string.remoteCommandQueue), mCommandsQueue);
		}
		else {
			toastText = String.format(getResources().getString(R.string.remoteCommandStatus), mCommandsQueue, text);
		}
		
		mToast.setText(toastText);
		mToast.show();
	}
}