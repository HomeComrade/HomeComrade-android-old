package com.shiznatix.mediacomrade.android.models;

import java.util.HashMap;
import java.util.Map;

public class CommandsModel {
	static public final String COMMAND_HEARTBEAT = "heartbeat";
	static public final String COMMAND_PLAY_PAUSE = "play-pause";
	static public final String COMMAND_NEXT = "next";
	static public final String COMMAND_PREVIOUS= "previous";
	static public final String COMMAND_SEEK_FWD = "seek-fwd";
	static public final String COMMAND_SEEK_BWD = "seek-bwd";
	static public final String COMMAND_SKIP_TITLE_SEQUENCE = "skip-title-sequence";
	static public final String COMMAND_VOLUME_UP = "volume-up";
	static public final String COMMAND_VOLUME_DOWN = "volume-down";
	static public final String COMMAND_VOLUME_DOWN_TV = "volume-down-tv";
	static public final String COMMAND_VOLUME_UP_TV = "volume-up-tv";
	static public final String COMMAND_MUTE_TV = "mute-tv";
	static public final String COMMAND_POWER_TV = "power-tv";
	static public final String COMMAND_SOURCE_TV = "source-tv";
	static public final String COMMAND_FULLSCREEN = "fullscreen";
	static public final String COMMAND_RANDOM_SETTINGS = "random-settings";
	static public final String COMMAND_RANDOM = "random";
	static public final String COMMAND_BROWSE = "browse";
	static public final String COMMAND_ENQUEUE = "enqueue";
	static public final String COMMAND_PLAY = "play";
	static public final String COMMAND_PLAY_AT = "play-at";
	static public final String COMMAND_INSERT_AT = "insert-at";
	static public final String COMMAND_LAST_PLAYLIST = "last-playlist";
	static public final String COMMAND_TOGGLE_CONTROLS = "toggle-controls";
	static public final String COMMAND_ALL_SHOWS = "all-shows";
	static public final String COMMAND_QUIT = "quit";
	static public final String COMMAND_CURRENT_PLAYLIST = "current-playlist";
	static public final String COMMAND_CURRENT_PLAYLIST_SELECTION = "current-playlist-selection";
	
	public final Map<String, String> commandUrlMap = new HashMap<String, String>();
	
	public CommandsModel() {
		commandUrlMap.put(COMMAND_HEARTBEAT, "/heartbeat/");
		commandUrlMap.put(COMMAND_PLAY_PAUSE, "/play-pause/");
		commandUrlMap.put(COMMAND_NEXT, "/next/");
		commandUrlMap.put(COMMAND_PREVIOUS, "/previous/");
		commandUrlMap.put(COMMAND_SEEK_FWD, "/seek-fwd/");
		commandUrlMap.put(COMMAND_SEEK_BWD, "/seek-bwd/");
		commandUrlMap.put(COMMAND_SKIP_TITLE_SEQUENCE, "/skip-title-sequence/");
		commandUrlMap.put(COMMAND_VOLUME_UP, "/volume-up/");
		commandUrlMap.put(COMMAND_VOLUME_DOWN, "/volume-down/");
		commandUrlMap.put(COMMAND_VOLUME_DOWN_TV, "/volume-down-tv/");
		commandUrlMap.put(COMMAND_VOLUME_UP_TV, "/volume-up-tv/");
		commandUrlMap.put(COMMAND_MUTE_TV, "/mute-tv/");
		commandUrlMap.put(COMMAND_POWER_TV, "/power-tv/");
		commandUrlMap.put(COMMAND_SOURCE_TV, "/source-tv/");
		commandUrlMap.put(COMMAND_FULLSCREEN, "/fullscreen/");
		commandUrlMap.put(COMMAND_RANDOM_SETTINGS, "/random-settings/");
		commandUrlMap.put(COMMAND_RANDOM, "/random/");
		commandUrlMap.put(COMMAND_BROWSE, "/browse/");
		commandUrlMap.put(COMMAND_ENQUEUE, "/enqueue/");
		commandUrlMap.put(COMMAND_PLAY, "/play/");
		commandUrlMap.put(COMMAND_PLAY_AT, "/play-at/");
		commandUrlMap.put(COMMAND_INSERT_AT, "/insert-at/");
		commandUrlMap.put(COMMAND_LAST_PLAYLIST, "/last-playlist/");
		commandUrlMap.put(COMMAND_TOGGLE_CONTROLS, "/toggle-controls/");
		commandUrlMap.put(COMMAND_ALL_SHOWS, "/all-shows/");
		commandUrlMap.put(COMMAND_QUIT, "/quit/");
		commandUrlMap.put(COMMAND_CURRENT_PLAYLIST, "/current-playlist/");
		commandUrlMap.put(COMMAND_CURRENT_PLAYLIST_SELECTION, "/current-playlist-selection/");
	}
}