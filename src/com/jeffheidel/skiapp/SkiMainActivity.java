package com.jeffheidel.skiapp;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SkiMainActivity extends Activity implements SensorEventListener,
		OnClickListener, OnCheckedChangeListener {

	private SensorManager mSensorManager;
	private Sensor magSense;

	private TextView centerText, intervalText, threshText;

	public static final int MAG_THRESH = 125;
	public static final int SKIP_TIME = 1250;

	private MusicController mc;

	private Button musicStart, musicSkip;

	private CheckBox magServiceCheck, announceServiceCheck;

	Intent magService;
	Intent announceService;

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

		// Set label text for dynamic labels
		intervalText.setText(Integer
				.toString(TimeAnnounceService.ANNOUNCE_MIN_INTERVAL) + " min");
		threshText.setText(Integer.toString(MAG_THRESH));

		// create the service intent
		magService = new Intent(this, MagService.class);
		announceService = new Intent(this, TimeAnnounceService.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ski_main, menu);
		return true;
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

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if (arg0.getId() == R.id.checkBox1) {
			if (magServiceCheck.isChecked()) {
				startService(magService);
			} else {
				stopService(magService);
			}
		}

		if (arg0.getId() == R.id.checkBox2) {
			if (announceServiceCheck.isChecked()) {
				startService(announceService);
			} else {
				stopService(announceService);
			}
		}

	}
}
