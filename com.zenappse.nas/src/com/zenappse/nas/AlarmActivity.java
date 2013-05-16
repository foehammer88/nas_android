package com.zenappse.nas;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * @author Patrick Ganson
 * @version 1.0
 * 
 * Activity controller class for the alarm chooser screen.
 * Shows a Time Picker object, and returns the Alarm back to
 * AlarmsActivity
 */
public class AlarmActivity extends Activity {

	public TimePicker alarmPicker;
	public Alarm alarm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		alarmPicker = (TimePicker)findViewById(R.id.alarmTimePicker);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alarm, menu);
		return true;
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	
	        case android.R.id.home:
	        	finish();
	            return true;
	        case R.id.menu_settings:
	            // app icon in action bar clicked; go home
	        	Log.d("AlarmActivity", "Settings click");
	            Intent intent_settings = new Intent(this, SettingsActivity.class);
	            intent_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_settings);
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Method that captures the time for the alarm, and converts it
	 * to a format that can be stored in an Alarm object
	 */
	public void addAlarm(View view){
		Log.d("Add Alarm", "Add alarm clicked");
		// Gets hour in 24 hour format
		Integer hour = alarmPicker.getCurrentHour();
		Integer minute = alarmPicker.getCurrentMinute();
		
		Calendar alarmTime = Calendar.getInstance() ;
		alarmTime.set(Calendar.HOUR_OF_DAY, hour.intValue());
		alarmTime.set(Calendar.MINUTE, minute.intValue());
		
		alarm = new Alarm(alarmTime);

		Intent resultIntent = new Intent(this, AlarmsActivity.class);
		resultIntent.putExtra("Alarm", alarm);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
