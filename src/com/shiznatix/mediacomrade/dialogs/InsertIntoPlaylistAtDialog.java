package com.shiznatix.mediacomrade.dialogs;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.shiznatix.mediacomrade.android.R;
import com.shiznatix.mediacomrade.android.entities.Server;
import com.shiznatix.mediacomrade.android.models.CommandsModel;
import com.shiznatix.mediacomrade.android.models.ConnectionModel;

public class InsertIntoPlaylistAtDialog {
	static private final String LOG_TAG = "mc_InsertIntoPlaylistAtDialog";
	
	protected AlertDialog mAlertDialog;
	protected int mSelectedIndex = -1;
	
	public InsertIntoPlaylistAtDialog(final Context context, String data, final List<String>selectedFiles, final Server server) {
		try {
			JSONArray showsArray = new JSONArray(data);
			
			final int showsArrayLength = showsArray.length() + 1;
			
			CharSequence[] entriesArray = new CharSequence[showsArrayLength];
			
			entriesArray[0] = context.getResources().getString(R.string.browseStartOfPlaylist);
			
			for (int i = 0; i < showsArray.length(); i++) {
				entriesArray[i + 1] = showsArray.getString(i);
			}
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.browseInsertAfter)
			.setSingleChoiceItems(entriesArray, -1, new DialogInterface.OnClickListener() {  
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mSelectedIndex = which;
				}  
			})
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					String selectedIndex = Integer.toString(mSelectedIndex);
					
					if ((showsArrayLength - 1) < mSelectedIndex || 0 > mSelectedIndex) {
						return;
					}
					
					Log.i(LOG_TAG, "selected show position: "+selectedIndex);
					
					try {
						JSONArray filePaths = new JSONArray(selectedFiles);
						
						ConnectionModel connectionModel = new ConnectionModel(context, server.url);
						connectionModel.clearPostParams();
						connectionModel.setPostParam("position", selectedIndex);
						connectionModel.setPostParam("filePaths", filePaths.toString());
						connectionModel.setCommand(CommandsModel.COMMAND_INSERT_AT);
						connectionModel.sendCommand();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					dialog.dismiss();
				}  
			})
			.setNegativeButton(R.string.cancel, null);
			
			mAlertDialog = alertDialogBuilder.create();
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void show() {
		if (null != mAlertDialog) {
			mAlertDialog.show();
		}
	}
}
