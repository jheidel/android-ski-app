package com.jeffheidel.skiapp;

import java.util.Calendar;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


public class TimeAnnounceService extends Service {

	public static final int ALARM_INTERVAL = 600;
	public static int ANNOUNCE_MIN_INTERVAL = 10;


	public static final String TAG = "SKIAPP_TimeAnnounceService";
	
	private TimeAnnouncer ta;
	
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
			ta.announceTime();
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
				"Announcing time periodically.", contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		nm.notify(2, notification);
	}

	private void hideNotification() {
		nm.cancel(2);
	}

	private NotificationManager nm;

	private PowerManager pm;
	private PowerManager.WakeLock wl;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		ta = new TimeAnnouncer(this);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mHandler.postDelayed(periodicTask, 1);
		Toast.makeText(this, "Time Announcements Started", Toast.LENGTH_SHORT)
				.show();
		showNotification();
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideNotification();
		mHandler.removeCallbacks(periodicTask);
		Toast.makeText(this, "Time Announcements Stopped", Toast.LENGTH_SHORT)
				.show();
		wl.release();
		Log.d(TAG, "onDestroy");
	}

	
}
