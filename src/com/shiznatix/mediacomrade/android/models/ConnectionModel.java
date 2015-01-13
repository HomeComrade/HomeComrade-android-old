package com.shiznatix.mediacomrade.android.models;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shiznatix.mediacomrade.android.R;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ConnectionModel {
	static private final String LOG_TAG = "mc_ConnectionModel";
	
	static public final String COMMAND_START_KEY = "command-start";
	static public final String COMMAND_SUCCESS_KEY = "command-success";
	static public final String COMMAND_RESPONSE_RECEIVED_KEY = "command-response-received";
	static public final String COMMAND_FAILURE_KEY = "command-failure";
	
	static public final int PORT = 8053;
	
	private Context mContext;
	private String mServerUrl;
	private String mCommand;
	private Map<String, String> mPostParams = new HashMap<String, String>();
	private CommandsModel mCommandsModel;
	
	public ConnectionModel(Context context, String serverUrl) {
		mContext = context;
		
		mServerUrl = serverUrl;
		mCommandsModel = new CommandsModel();
	}
	
	public void setServerUrl(String serverUrl) throws Exception {
		if (serverUrl.isEmpty()) {
			throw new Exception("Server cannot be empty");
		}
		
		mServerUrl = serverUrl;
	}
	
	public void setCommand(String command) throws Exception {
		if (!mCommandsModel.commandUrlMap.containsKey(command)) {
			throw new Exception("Invalid command: "+command);
		}
		
		mCommand = command;	
	}
	
	public void clearPostParams() {
		mPostParams.clear();
	}
	
	public void setPostParam(String key, String value) {
		mPostParams.put(key, value);
	}
	
	public void setPostParams(Map<String, String> postParams) {
		mPostParams.clear();
		mPostParams.putAll(postParams);
	}
	
	public void sendCommand() throws Exception {
		Log.i(LOG_TAG, "sendCommand");
		
		if (mCommand.isEmpty()) {
			throw new Exception("Missing command");
		}
		
		String url = "http://"+mServerUrl+":"+PORT+mCommandsModel.commandUrlMap.get(mCommand);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.setMaxRetriesAndTimeout(1, 5000);
		
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				Intent intent = new Intent(COMMAND_START_KEY);
				intent.putExtra("url", mServerUrl);
				intent.putExtra("command", mCommand);
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
			}
			
			@Override
			public void onSuccess(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					boolean success = jsonObject.optBoolean("success", false);
					
					if (success) {
						Log.i(LOG_TAG, "command response success");
						
						Intent commandSuccessIntent = new Intent(COMMAND_SUCCESS_KEY);
						commandSuccessIntent.putExtra("url", mServerUrl);
						commandSuccessIntent.putExtra("command", mCommand);
						LocalBroadcastManager.getInstance(mContext).sendBroadcast(commandSuccessIntent);
						
						String type = jsonObject.optString("type", null);
						String data = jsonObject.optString("data", null);
						
						Intent receivedResponseIntent = new Intent(COMMAND_RESPONSE_RECEIVED_KEY);
						receivedResponseIntent.putExtra("type", type);
						receivedResponseIntent.putExtra("data", data);
						receivedResponseIntent.putExtra("url", mServerUrl);
						receivedResponseIntent.putExtra("command", mCommand);
						LocalBroadcastManager.getInstance(mContext).sendBroadcast(receivedResponseIntent);
						
						Log.i(LOG_TAG, "intent: "+COMMAND_RESPONSE_RECEIVED_KEY+" sent");
					}
					else {
						Log.i(LOG_TAG, "command response failure");
						
						Intent intent = new Intent(COMMAND_FAILURE_KEY);
						intent.putExtra("command", mCommand);
						intent.putExtra("error", "Received a failure response");
						LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
					}
				}
				catch (JSONException e) {
					Log.i(LOG_TAG, "failed to parse json: "+e.getMessage());
					
					Intent intent = new Intent(COMMAND_FAILURE_KEY);
					intent.putExtra("command", mCommand);
					intent.putExtra("error", "Failed to parse returned JSON");
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
				}
			}
			
			@Override
		 	public void onFailure(Throwable e, String errorResponse) {
				Log.i(LOG_TAG, "server connect failure: "+errorResponse+" e: "+e.getMessage());
				
				String error = (null == errorResponse || errorResponse.equals("") ? e.getMessage() : errorResponse);
				
				if (null == error) {
					error = mContext.getResources().getString(R.string.unknownError);
				}
				
				Intent intent = new Intent(COMMAND_FAILURE_KEY);
				intent.putExtra("command", mCommand);
				intent.putExtra("error", error);
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
			}
		};
		
		Log.i(LOG_TAG, "Loading url: "+url);
		
		if (mPostParams.size() > 0) {
			Log.i(LOG_TAG,  "post params: "+mPostParams);
			
			RequestParams params = new RequestParams();
			
			for (Map.Entry<String, String> entry : mPostParams.entrySet()) {
				params.put(entry.getKey(), entry.getValue());
			}
			
			client.post(url, params, responseHandler);
		}
		else {
			client.get(url, responseHandler);
		}
	}
}
