package com.hlidskialf.android.preference;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

//NOTE: CODE NOT YET WORKABLE

public class MagneticSeekBarPreference extends SeekBarPreference implements SensorEventListener {

	private SeekBar mMagBar;
	private TextView mnValueText;
	
	private SensorManager mSensorManager;
	private Sensor magSense;
	
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
	
	public MagneticSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateDialogView() {
		
		registerListener();
		
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		mSplashText.setTextColor(Color.WHITE);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		mValueText.setTextColor(Color.WHITE);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		//magnetic display
		
		TextView mnSplashText = new TextView(mContext);
		if (mDialogMessage != null)
			mnSplashText.setText("Current Magnetic Field:");
		mnSplashText.setTextColor(Color.WHITE);
		layout.addView(mnSplashText);

		mnValueText = new TextView(mContext);
		mnValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mnValueText.setTextSize(32);
		mnValueText.setTextColor(Color.WHITE);
		mnValueText.setText("N/A");
		layout.addView(mnValueText, params);
		
			
		mMagBar = new SeekBar(mContext);
		mMagBar.setEnabled(false);
		//mMagBar.setOnSeekBarChangeListener(this);
		layout.addView(mMagBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
	
		//end mag bar
		
		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		
		mMagBar.setMax(mMax);
		mMagBar.setProgress(mValue);
		
		return layout;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		/*
		float[] mag = event.values;
		int sensorType = event.sensor.getType();

		if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
			float mval = (float) Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1]
					+ mag[2] * mag[2]);
			mMagBar.setProgress((int) mval);
			//TODO: set text
		}
		*/
	}

}
