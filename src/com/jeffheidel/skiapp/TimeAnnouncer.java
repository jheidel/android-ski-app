package com.jeffheidel.skiapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;


public class TimeAnnouncer {

	public static final String TAG = "SKIAPP_TimeAnnouncer";
	private TTSSpeaker tts;

	public TimeAnnouncer(Context parent) {
		tts = TTSSpeaker.getSpeaker(parent);
	}
	
	public void announceTime() {
		
		Log.d(TAG, "Announcing time w/ TTS");
		Date now = new Date();
		
		SimpleDateFormat hour = new SimpleDateFormat("h");
		String hourStr = hour.format(now);
		
		SimpleDateFormat min = new SimpleDateFormat("mm");
		String minStr = min.format(now);
		
		SimpleDateFormat ampm = new SimpleDateFormat("a");
		String ampmStr = ampm.format(now);
		
		//follow english convention of saying "oh" for minutes 1-9
		String ohStr = (now.getMinutes() >= 1 && now.getMinutes() <= 9 ? "owe" : ""); 
		
		String currentDateandTime = hourStr + " " + ohStr + " " + minStr + " " + ampmStr;
		
		tts.speak(currentDateandTime, true);
	}

}
