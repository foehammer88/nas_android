package com.zenappse.nas;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class AlarmsActivity extends Activity {

	final private int ALARM_ADD_ID = 1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarms);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alarms, menu);
		menu.add(0, ALARM_ADD_ID, 0, "Alarm").setIcon(R.drawable.ic_menu_add)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		 case ALARM_ADD_ID:
	            // app icon in action bar clicked; go home
	        	Log.d("Alarms", "Add Alarm click");
	        	
	        	Intent intent_alarm = new Intent(this, AlarmActivity.class);
	        	intent_alarm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_alarm);
	            
//	        	String nextAlarm = Settings.System.getString(getContentResolver(),
//	        		    Settings.System.NEXT_ALARM_FORMATTED);
//	        	TextView t = new TextView(this);
//	        	t=(TextView)findViewById(R.id.test_text); 
//	        	t.setText(nextAlarm);
	        	
	            return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
