package com.jeffheidel.skiapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TimeAnnounceService extends Service implements OnInitListener {

	public static final int ALARM_INTERVAL = 600;
	public static final int ANNOUNCE_MIN_INTERVAL = 10;

	public static final String TAG = "SKIAPP_TimeAnnounce";
	private TextToSpeech tts;
	private int last_announce = -1;
	private boolean first_announce = true;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private Handler mHandler = new Handler();

	// Run periodically; must announce when applicable
	private void periodicRun() {
		Calendar c = Calendar.getInstance();
		Date d = c.getTime();
		int min = d.getMinutes();
		int hours = d.getHours();

		int minOfDay = hours * 60 + min;

		if (first_announce
				|| (minOfDay % ANNOUNCE_MIN_INTERVAL == 0 && last_announce != minOfDay)) {
			last_announce = minOfDay;
			first_announce = false;
			SimpleDateFormat sdf = new SimpleDateFormat("h mm a");
			String currentDateandTime = sdf.format(new Date());
			Log.d(TAG, "Announcing new time");
			tts.speak(currentDateandTime, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private Runnable periodicTask = new Runnable() {
		public void run() {
			periodicRun();
			mHandler.postDelayed(periodicTask, ALARM_INTERVAL);
		}
	};

	private void showNotification() {
		Log.d(TAG, "Showing Notification now");
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Time Announcer Running", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, SkiMainActivity.class), 0);
		notification.setLatestEventInfo(this, "Ski App Time Announcement",
				"Now announcing time periodically.", contentIntent);
		nm.notify(2, notification);
	}

	private void hideNotification() {
		nm.cancel(2);
	}

	private NotificationManager nm;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		tts = new TextToSpeech(this, this);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mHandler.postDelayed(periodicTask, 1);
		Toast.makeText(this, "Time Announcements Started", Toast.LENGTH_SHORT)
				.show();
		showNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideNotification();
		mHandler.removeCallbacks(periodicTask);
		Toast.makeText(this, "Time Announcements Stopped", Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.US);
			Log.i(TAG, "TTS Init Success");
		} else {
			Log.e(TAG, "TTS Initilization Failed");
		}
	}
}
