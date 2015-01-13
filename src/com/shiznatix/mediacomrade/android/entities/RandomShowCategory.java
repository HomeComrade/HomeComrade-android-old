package com.shiznatix.mediacomrade.android.entities;

import java.util.ArrayList;
import java.util.List;

public class RandomShowCategory {
	public String title;
	public List<RandomShow> shows = new ArrayList<RandomShow>();
	
	public RandomShowCategory(String title, List<RandomShow> shows) {
		this.title = title;
		this.shows = shows;
	}
}
