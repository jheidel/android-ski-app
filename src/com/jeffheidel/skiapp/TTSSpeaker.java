package com.jeffheidel.skiapp;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class TTSSpeaker implements OnInitListener {

	public static final String TAG = "SKIAPP_TTS";

	private static TTSSpeaker spkr = null;
	private static int refcount = 0;

	public static TTSSpeaker getSpeaker(Context ctx) {
		if (spkr == null) {
			Log.d(TAG, "Creating new speaker");
			spkr = new TTSSpeaker(ctx);
		} else {
			Log.d(TAG, "Fetch speaker from cache");
		}
		refcount++;
		return spkr;
	}

	public static void shutdownSpeaker() {
		if (refcount-- <= 0 && spkr != null) {
			spkr.destroy();
			spkr = null;
			refcount = 0;
		}
	}

	// ////////

	private final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

	private Context ctx;
	private boolean initialized = false;
	private TextToSpeech tts;
	private AudioManager mAm;

	private HashMap<String, String> ttsParams;

	public TTSSpeaker(Context ctx) {
		this.ctx = ctx;
		ttsParams = new HashMap<String, String>();
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(STREAM_TYPE));
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID");

		tts = new TextToSpeech(ctx, this);
		mAm = (AudioManager) ctx.getApplicationContext().getSystemService(
				Context.AUDIO_SERVICE);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.US);
			tts.setOnUtteranceCompletedListener(ttsOnUtteranceCompletedListener);
			Log.i(TAG, "TTS Init Success !!!!!!!!!!");
			initialized = true;
		} else {
			Log.e(TAG, "TTS Initilization Failed");
		}
	}

	public void destroy() {
		tts.shutdown();
	}
	
	public void speak(String s, boolean duck) {
		if (!initialized)
			return;

		Log.d(TAG, "speaking " + s);

		if (duck) {
			int status = mAm.requestAudioFocus(audioFocus, STREAM_TYPE,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			if (status == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
				Log.e(TAG, "speak() audio focus request failed.");
				return;
			}
		}

		/*
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		MediaPlayer mp = MediaPlayer.create(ctx.getApplicationContext(), notification);
		mp.start();
		 */
		
		tts.speak(s, TextToSpeech.QUEUE_ADD, ttsParams);
	}

	private TextToSpeech.OnUtteranceCompletedListener ttsOnUtteranceCompletedListener = new TextToSpeech.OnUtteranceCompletedListener() {

		/**
		 * Callback when TTS has completed an utterance.
		 */
		public void onUtteranceCompleted(String utteranceId) {
			Log.d(TAG, "onUtteranceCompleted(\"" + utteranceId + "\")");
			mAm.abandonAudioFocus(audioFocus);
		}

	};

	private AudioManager.OnAudioFocusChangeListener audioFocus = new AudioManager.OnAudioFocusChangeListener() {

		public void onAudioFocusChange(int focusChange) {
			// I don't think we actually care.
			Log.d(TAG, "onAudioFocusChange(" + focusChange + ")");
		}
	};

}
