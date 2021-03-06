package com.jeffheidel.skiapp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.Log;

public class MusicController {
	private final static String TAG = "SKIAPP_MusicControl";
	private Context parent;
	private TTSSpeaker tts;
	private TimeAnnounceService tas = null;
	private TimeAnnouncer ta;
	
	public static boolean ANNOUNCE_ON_PAUSE = true;
	
	public MusicController(Context parent) {
		this.parent = parent;
		tts = TTSSpeaker.getSpeaker(parent);
		ta = new TimeAnnouncer(parent);
	}

	// vibration option used for haptic feedback of music control
	public void vibrate(int time) {
		Vibrator v = (Vibrator) parent
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	public void setTimeAnnounceOnPause(TimeAnnounceService tas) {
		this.tas = tas;
	}
	
	public boolean announcingOnPause() {
		return tas != null;
	}
	
	public void removeTimeAnnounceOnPause() {
		this.tas = null;
	}
	
	public void pauseMusic() {

		AudioManager manager = (AudioManager) parent
				.getSystemService(Context.AUDIO_SERVICE);
		if (manager.isMusicActive()) {
			tts.speak("music pause", false);
			if (ANNOUNCE_ON_PAUSE) {
				ta.announceTime();
			}
		} else {
			tts.speak("music play", false);
		}

		Log.d(TAG, "Music has been pause / play toggled");
		Intent intent;
		intent = new Intent("com.android.music.musicservicecommand.togglepause");
		parent.getApplicationContext().sendBroadcast(intent);
		vibrate(150);
		
	}

	public void skipMusic() {
		Log.d(TAG, "Music has been skipped");
		Intent intent;
		intent = new Intent("com.android.music.musicservicecommand.next");
		parent.getApplicationContext().sendBroadcast(intent);
		vibrate(150);
		tts.speak("music skip", false);
	}

}
