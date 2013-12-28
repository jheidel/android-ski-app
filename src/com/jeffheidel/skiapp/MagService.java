package com.jeffheidel.skiapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

public class MagService extends Service implements SensorEventListener {

	private static final String TAG = "SKIAPP_MagService";
	private static final int SCREEN_OFF_RECEIVER_DELAY = 1250;

	private SensorManager mSensorManager;
	private Sensor magSense;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind");
		return null;
	}

	private PowerManager pm;
	private PowerManager.WakeLock wl;

	private void registerListener() {
		magSense = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(this, magSense,
				SensorManager.SENSOR_DELAY_UI);
	}

	/*
	 * Un-register this as a sensor event listener.
	 */
	private void unregisterListener() {
		mSensorManager.unregisterListener(this);
	}

	// keep the screen on forcibly so we can still get sensor data
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "onReceive(" + intent + ")");

			if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				return;
			}

			Runnable runnable = new Runnable() {
				@SuppressLint("Wakelock")
				public void run() {
					Log.i(TAG, "Forcibly turning on screen.");
					wl.release();
					wl.acquire();
				}
			};

			new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
		}
	};

	private MusicController mc;

	private NotificationManager nm;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mSensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);

		mc = new MusicController(this);

		// A partial wakelock is needed to keep getting sensor data
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);

		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

	}

	private void showNotification() {
		Log.d(TAG, "Showing Notification now");
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Ski App Mag Service Running", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, SkiMainActivity.class), 0);
		notification.setLatestEventInfo(this, "Ski App Magnetic Music Control",
				"Monitoring magnetic field.", contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		nm.notify(0, notification);
	}

	private void hideNotification() {
		nm.cancel(0);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		wl.release();
		hideNotification();
		unregisterReceiver(mReceiver);
		unregisterListener();
		stopForeground(true);
		Toast.makeText(this, "Mag Service Stopped", Toast.LENGTH_LONG).show();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		showNotification();
		Log.d(TAG, "onStart");

		startForeground(Process.myPid(), new Notification());
		registerListener();
		wl.acquire();

		Toast.makeText(this, "Mag Service Started", Toast.LENGTH_LONG).show();

		return START_STICKY;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.i(TAG, "onAccuracyChanged().");
	}

	private boolean state = false;
	private long riseTime = 0;
	private boolean risen = false;

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] mag = event.values;
		int sensorType = event.sensor.getType();

		if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
			float mval = (float) Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1]
					+ mag[2] * mag[2]);
			// at this point we have the sensor info

			if (mval > SkiMainActivity.MAG_THRESH) {

				if (state == false) {
					state = true;
					riseTime = System.currentTimeMillis();
					Log.d("MAG_SENSE", "Sensor Rising");
					// rising
					mc.pauseMusic();
				}

				if (!risen) {
					long diff = System.currentTimeMillis() - riseTime;
					if (diff > SkiMainActivity.SKIP_TIME) {
						risen = true;
						Log.d("MAG_SENSE", "Hold time thresh");
						// sufficient time has passed since rise
						mc.skipMusic();
					}
				}
			} else {
				if (state == true) {
					state = false;
					Log.d("MAG_SENSE", "Sensor Falling");
					// falling
				}
				risen = false;
			}
		}
	}
}
