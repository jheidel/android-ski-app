package com.jeffheidel.skiapp;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class MusicController implements OnInitListener {
	private final static String TAG = "SKIAPP_MusicControl";
	private Context parent;
	private TextToSpeech tts;

	public MusicController(Context parent) {
		this.parent = parent;
		tts = new TextToSpeech(parent, this);
	}

	// vibration option used for haptic feedback of music control
	public void vibrate(int time) {
		Vibrator v = (Vibrator) parent
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	public void pauseMusic() {

		AudioManager manager = (AudioManager) parent
				.getSystemService(Context.AUDIO_SERVICE);
		if (manager.isMusicActive()) {
			tts.speak("music pause", TextToSpeech.QUEUE_FLUSH, null);
		} else {
			tts.speak("music play", TextToSpeech.QUEUE_FLUSH, null);
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
		tts.speak("music skip", TextToSpeech.QUEUE_FLUSH, null);
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.US);
			Log.i(TAG, "TTS Init Success");
		} else {
			Log.e(TAG, "TTS Initilization Failed");
		}
	}

}
