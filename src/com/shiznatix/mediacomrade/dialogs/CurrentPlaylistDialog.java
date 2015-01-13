package com.shiznatix.mediacomrade.dialogs;

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

public class CurrentPlaylistDialog {
	static private final String LOG_TAG = "mc_CurrentPlaylistDialog";
	
	protected AlertDialog mAlertDialog;
	protected int mSelectedIndex = -1;
	
	public CurrentPlaylistDialog(final Context context, final JSONArray showsArray, final Server server) {
		try {
			CharSequence[] entriesArray = new CharSequence[showsArray.length()];
			
			for (int i = 0; i < showsArray.length(); i++) {
				entriesArray[i] = showsArray.getString(i);
			}
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.currentPlaylist)
			.setSingleChoiceItems(entriesArray, -1, new DialogInterface.OnClickListener() {  
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mSelectedIndex = which;
				}  
			})
			.setCancelable(false)
			.setPositiveButton(R.string.play, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					String selectedIndex = Integer.toString(mSelectedIndex);
					
					if ((showsArray.length() - 1) < mSelectedIndex || 0 > mSelectedIndex) {
						return;
					}
					
					Log.i(LOG_TAG, "selected show position: "+selectedIndex);
					
					try {
						ConnectionModel connectionModel = new ConnectionModel(context, server.url);
						connectionModel.clearPostParams();
						connectionModel.setPostParam("position", selectedIndex);
						connectionModel.setCommand(CommandsModel.COMMAND_PLAY_AT);
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
