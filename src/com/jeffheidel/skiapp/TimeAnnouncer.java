package com.jeffheidel.skiapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;


public class TimeAnnouncer implements OnInitListener  {

	public static final String TAG = "SKIAPP_TimeAnnouncer";
	private TextToSpeech tts;
	private Context parent;
	boolean initialized = false;
	
	public TimeAnnouncer(Context parent) {
		this.parent = parent;
		tts = new TextToSpeech(parent, this);
	}
	
	public void announceTime() {
		if (!initialized) return; //cannot announce if TTS not initialized
		
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
		
		tts.speak(currentDateandTime, TextToSpeech.QUEUE_ADD, null);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.US);
			Log.i(TAG, "TTS Init Success");
			initialized = true;
		} else {
			Log.e(TAG, "TTS Initilization Failed");
		}
	}

}
