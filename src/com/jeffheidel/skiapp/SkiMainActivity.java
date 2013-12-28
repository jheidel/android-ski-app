package com.jeffheidel.skiapp;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SkiMainActivity extends Activity implements SensorEventListener,
		OnClickListener, OnCheckedChangeListener,
		OnSharedPreferenceChangeListener {

	private SensorManager mSensorManager;
	private Sensor magSense;

	public static final String TAG = "SKIAPP_MainActivity";

	private TextView centerText, intervalText, threshText;

	public static int MAG_THRESH = 125;
	public static int SKIP_TIME = 1250;
	public static boolean STOP_ON_UNPLUG = true;

	private MusicController mc;

	private Button musicStart, musicSkip;

	private CheckBox magServiceCheck, announceServiceCheck;

	Intent magService;
	Intent announceService;

	TimeAnnounceService mService;
	boolean mBound = false;

	private SharedPreferences settings;

	private boolean isServiceRunning(@SuppressWarnings("rawtypes") Class c) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (c.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// required initializations
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ski_main);

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(this);

		// Initialize sensor manager for magnetic field sensor
		mSensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		magSense = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(this, magSense,
				SensorManager.SENSOR_DELAY_UI);

		// Get the mag field text for later usage
		centerText = (TextView) findViewById(R.id.textView2);
		intervalText = (TextView) findViewById(R.id.TextView01);
		threshText = (TextView) findViewById(R.id.TextView03);

		// set up the music controller class
		mc = new MusicController(this);

		// Link all buttons
		musicStart = (Button) findViewById(R.id.button1);
		musicSkip = (Button) findViewById(R.id.button2);
		musicStart.setOnClickListener(this);
		musicSkip.setOnClickListener(this);

		// Link checkboxes
		magServiceCheck = (CheckBox) findViewById(R.id.checkBox1);
		magServiceCheck.setChecked(isServiceRunning(MagService.class));
		magServiceCheck.setOnCheckedChangeListener(this);

		announceServiceCheck = (CheckBox) findViewById(R.id.checkBox2);
		announceServiceCheck
				.setChecked(isServiceRunning(TimeAnnounceService.class));
		announceServiceCheck.setOnCheckedChangeListener(this);

		// register headset plug listener
		initialReceive = false;
		registerReceiver(mReceiver,
				new IntentFilter(Intent.ACTION_HEADSET_PLUG));

		updateSettings(); // initial set of the settings
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ski_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Log.d(TAG, "Starting menu activity");
			startActivity(new Intent(this, SettingsMenuActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		float[] mag = event.values;
		int sensorType = event.sensor.getType();

		if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
			float mval = (float) Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1]
					+ mag[2] * mag[2]);

			centerText.setText(String.format("%.2f", mval));

			if (mval > MAG_THRESH) {
				centerText.setTextColor(Color.RED);
			} else {
				centerText.setTextColor(Color.GREEN);
			}

		}
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.button1:
			mc.pauseMusic();
			break;
		case R.id.button2:
			mc.skipMusic();
			break;
		default:
			break;
		}
	}

	private boolean initialReceive = false;
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "onReceive(" + intent + ")");

			//if (!intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
			//	return;
			//}

			if (!initialReceive) { // an unwanted intent appears to be received
									// when registering
				initialReceive = true;
				return;
			}

			AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (!am.isWiredHeadsetOn()) {
				Log.d(TAG, "Headset unplugged!");
				if (STOP_ON_UNPLUG) {
					// do the stop!
					stopTimeAnnounceService();
				}
			}
		}
	};

	public void stopTimeAnnounceService() {
		Log.d(TAG, "Stopping TA service");
		if (announceService == null) return;
		stopService(announceService);
		announceServiceCheck.setChecked(false);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if (arg0.getId() == R.id.checkBox1) {
			if (magServiceCheck.isChecked()) {
				magService = new Intent(this, MagService.class);
				startService(magService);
			} else {
				stopService(magService);
			}
		}

		if (arg0.getId() == R.id.checkBox2) {
			if (announceServiceCheck.isChecked()) {
				announceService = new Intent(this, TimeAnnounceService.class);
				startService(announceService);
			} else {
				stopService(announceService);
			}
		}
	}

	private void updateSettings() {
		TimeAnnounceService.ANNOUNCE_MIN_INTERVAL = settings.getInt(
				"announce_interval", -1);
		intervalText.setText(Integer
				.toString(TimeAnnounceService.ANNOUNCE_MIN_INTERVAL) + " min");

		MAG_THRESH = settings.getInt("mag_thresh", -1);
		threshText.setText(Integer.toString(MAG_THRESH));

		MusicController.ANNOUNCE_ON_PAUSE = settings.getBoolean(
				"announce_on_pause", false);
		
		STOP_ON_UNPLUG = settings.getBoolean("stop_on_unplug", false);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		updateSettings();
	}

}
