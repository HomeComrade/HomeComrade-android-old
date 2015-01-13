package com.shiznatix.mediacomrade.android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.shiznatix.mediacomrade.android.adapters.ServerCellAdapter;
import com.shiznatix.mediacomrade.android.entities.Server;
import com.shiznatix.mediacomrade.android.models.CommandsModel;
import com.shiznatix.mediacomrade.android.models.ConnectionModel;
import com.shiznatix.mediacomrade.android.models.ServerTypesModel;
import com.shiznatix.mediacomrade.android.tables.ServersTable;
import com.shiznatix.mediacomrade.android.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ConnectionsActivity extends Activity {
	static private final String LOG_TAG = "mc_MainActivity";
	
	private ProgressDialog mConnectingDialog;
	
	private BroadcastReceiver mCommandStartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command started");
			
			Bundle bundle = intent.getExtras();
			
			mConnectingDialog = new ProgressDialog(ConnectionsActivity.this);
			mConnectingDialog.setTitle(R.string.connectionsConnecting);
			mConnectingDialog.setMessage(String.format(getResources().getString(R.string.connectionsConnectingTo), bundle.getString("url")));
			mConnectingDialog.show();
		}
	};
	
	private BroadcastReceiver mCommandResponseReceivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command success");
			mConnectingDialog.dismiss();
			
			Bundle bundle = intent.getExtras();
			String url = bundle.getString("url");
			String data = bundle.getString("data");
			
			Log.i(LOG_TAG, "data: "+data);
			
			if (null != url && null != data) {
				String serverType = null;
				ArrayList<String> serverOptions = new ArrayList<String>();
				
				try {
					JSONObject jsonObject = new JSONObject(data);
					serverType = jsonObject.getString("serverType");
					
					ServerTypesModel serverTypesModel = new ServerTypesModel();
					
					if (!serverTypesModel.isValidServerType(serverType)) {
						throw new Exception("Invalid server type");
					}
					
					try {
						boolean irRemote = jsonObject.getBoolean("irRemote");
						
						if (irRemote) {
							serverOptions.add("irRemote");
						}
					}
					catch (Exception e) {
					}
					
					Log.i(LOG_TAG, "serverType: "+serverType);
				}
				catch (Exception e) {
					//default to totem
					serverType = ServerTypesModel.TOTEM_SERVER;
				}
				
				Intent remoteActivityIntent = new Intent(ConnectionsActivity.this, RemoteActivity.class);
				remoteActivityIntent.putExtra("url", url);
				remoteActivityIntent.putExtra("serverType", serverType);
				remoteActivityIntent.putStringArrayListExtra("serverOptions", serverOptions);
				startActivity(remoteActivityIntent);
			}
		}
	};
	
	private BroadcastReceiver mCommandFailureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "command failure");
			mConnectingDialog.dismiss();
			
			Bundle bundle = intent.getExtras();
			String error = bundle.getString("error");
			
			if (null != error) {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ConnectionsActivity.this);
				dialogBuilder.setTitle(R.string.connectionsConnectionFailed)
				.setMessage(String.format(getResources().getString(R.string.connectionsCouldNotConnectTo), error))
				.setNeutralButton(R.string.ok, null)
				.show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connections);
		
		//get the saved servers
		final ServersTable serversTable = new ServersTable(this);
		final List<Server> savedServers = serversTable.getAllServers();
		
		//our views
		final EditText serverEditText = (EditText)findViewById(R.id.serverEditText);
		Button connectButton = (Button)findViewById(R.id.connectButton);
		final ListView serverListView = (ListView)findViewById(R.id.serverListView);
		
		//connect button functions
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(serverEditText.getWindowToken(), 0);
				
				final String url = serverEditText.getText().toString();
				
				Server server = serversTable.getServerByUrl(url);
				
				if (null == server) {
					Log.i(LOG_TAG, "server does not exist, ask if we would like it to be saved");
					
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ConnectionsActivity.this);
					dialogBuilder.setMessage(R.string.connectionsSaveOrConnect)
					.setTitle(url)
					.setPositiveButton(R.string.connectionsSave, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							serversTable.addServer(url);
							
							Server newServer = serversTable.getServerByUrl(url);
							
							ServerCellAdapter adapter = ((ServerCellAdapter)serverListView.getAdapter()); 
							adapter.add(newServer);
							adapter.notifyDataSetChanged();
							
							connect(serversTable.getServerByUrl(url));
						}
					})
					.setNegativeButton(R.string.connectionsConnect, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							connect(new Server(-1, url));
						}
					})
					.show();
				}
				else {
					connect(server);
				}
			}
		});
		
		//List view functions
		serverListView.setAdapter(new ServerCellAdapter(this, R.id.serverListView, savedServers));
		
		serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(serverEditText.getWindowToken(), 0);
				
				final Server server = savedServers.get(position);
				
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ConnectionsActivity.this);
				dialogBuilder.setMessage(R.string.connectionsConnectOrDelete)
				.setTitle(server.url)
				.setPositiveButton(R.string.connectionsConnect, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						connect(server);
					}
				})
				.setNegativeButton(R.string.connectionsDelete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						serversTable.deleteServer(server);
						
						ServerCellAdapter adapter = ((ServerCellAdapter)serverListView.getAdapter()); 
						adapter.remove(server);
						adapter.notifyDataSetChanged();
					}
				})
				.show();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//setup our broadcast receivers
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandStartReceiver, new IntentFilter(ConnectionModel.COMMAND_START_KEY));
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandResponseReceivedReceiver, new IntentFilter(ConnectionModel.COMMAND_RESPONSE_RECEIVED_KEY));
		LocalBroadcastManager.getInstance(this).registerReceiver(mCommandFailureReceiver, new IntentFilter(ConnectionModel.COMMAND_FAILURE_KEY));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//remove our broadcast receivers
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandStartReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandResponseReceivedReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandFailureReceiver);
	}
	
	public void connect(Server server) {
		ConnectionModel connectionModel = new ConnectionModel(this, server.url);
		
		try {
			connectionModel.setCommand(CommandsModel.COMMAND_HEARTBEAT);
			connectionModel.clearPostParams();
			connectionModel.sendCommand();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}