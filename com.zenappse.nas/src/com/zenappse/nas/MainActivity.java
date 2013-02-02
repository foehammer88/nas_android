package com.zenappse.nas;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zenappse.nas.R;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	final static int ALARM_ID = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//TODO If alarms is empty
		setCurrentTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.add(0, ALARM_ID, 0, "Contacts").setIcon(R.drawable.ic_menu_add)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Log.d("Item ID", item.getItemId());
	    switch (item.getItemId()) {
	    	
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case ALARM_ID:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Add Alarm click");
	        	String nextAlarm = Settings.System.getString(getContentResolver(),
	        		    Settings.System.NEXT_ALARM_FORMATTED);
	        	TextView t = new TextView(this);
	        	//t=(TextView)findViewById(R.id.test_text); 
	        	//t.setText(nextAlarm);
	        	
	            return true;
	        case R.id.menu_settings:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Settings click");
	            Intent intent_settings = new Intent(this, SettingsActivity.class);
	            intent_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_settings);
	        	
	            return true;
	        case R.id.menu_alarms:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Settings click");
	            Intent intent_alarms = new Intent(this, AlarmsActivity.class);
	            intent_alarms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_alarms);
	        	
	            return true;
	        case R.id.menu_bluetooth:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Settings click");
	            Intent intent_bluetooth = new Intent(this, BluetoothActivity.class);
	            intent_bluetooth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_bluetooth);
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void addAlarm(View view){
		Intent intent_alarm = new Intent(this, AlarmActivity.class);
    	intent_alarm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent_alarm);
	}
	
	public void syncAlarm(View view){
		String nextAlarm = Settings.System.getString(getContentResolver(),
    		    Settings.System.NEXT_ALARM_FORMATTED);
		if (nextAlarm.length()==0 || nextAlarm == "") {
			//TODO if(AlarmsArray is empty())
			setCurrentTime();
	    	Toast.makeText(getApplicationContext(), "No alarms found", Toast.LENGTH_SHORT).show();
	        return;
	    } else {
			String timeStr = nextAlarm.substring(4, 8);
			String timePeriodStr = nextAlarm.substring(9);
			DateFormat sdf = new SimpleDateFormat("EEE hh:mm aa");
		    Date alarmDate = null;
		    try {
	            alarmDate = sdf.parse(nextAlarm);
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
		    String nextAlarmFormatted = new SimpleDateFormat("h:mm").format(alarmDate);
		    String nextAlarmPeriod = new SimpleDateFormat("aa").format(alarmDate);
//		    try {
//				nextAlarm = new SimpleDateFormat("h:mm").parse(nextAlarm).toString();
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	//		RelativeLayout rl = (RelativeLayout)findViewById(R.id.relativeAlarmTime);
	//		rl.setLayoutParams(new RelativeLayout.LayoutParams(30,60));
	    	TextView t = new TextView(this);
	    	t=(TextView)findViewById(R.id.textAlarmTime);
	    	t.setText(nextAlarmFormatted);
	    	t=(TextView)findViewById(R.id.textAlarmTimePeriod);
	    	t.setText(nextAlarmPeriod.toLowerCase());
	    	t=(TextView)findViewById(R.id.textAlarmTitle);
	    	t.setText("Next Alarm");
//	    	t.setText(timePeriodStr.toLowerCase());
	    }
	}
	
	public void setCurrentTime(){
		Calendar c = Calendar.getInstance(); 
		Date currentDate = c.getTime();
		String currentTime = new SimpleDateFormat("h:mm").format(currentDate);
		String currentPeriod = new SimpleDateFormat("aa").format(currentDate);
		TextView t = new TextView(this);
    	t=(TextView)findViewById(R.id.textAlarmTime);
    	t.setText(currentTime);
    	t=(TextView)findViewById(R.id.textAlarmTimePeriod);
    	t.setText(currentPeriod.toLowerCase());
    	t=(TextView)findViewById(R.id.textAlarmTitle);
    	t.setText("Current Time");
		
	}
}
