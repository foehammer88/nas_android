package com.zenappse.nas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.zenappse.nas.R.drawable;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.ClipData.Item;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class AlarmsActivity extends Activity {

	private static final int ALARM_ID = 0;
	final private int ALARM_ADD_ID = 1000;
	final private String TAG = "Alarms Activity";
	final private String key = "alarmslist";

	public ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	public Alarm alarmTime;
	ListView alarmsList;
	AlarmAdapter alarmsAdapter = null;
	AlarmsActivity context;
	private ArrayList<Integer> selectedPositions;
	
	private static final String APP_SHARED_PREFS = AlarmsActivity.class.getSimpleName(); //  Name of the file -.xml
    
	AlarmStorage alarmStorage;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarms);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		alarmsList = (ListView) findViewById(R.id.listViewAlarms);
		alarmsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		context = this;
		
		alarmStorage = new AlarmStorage(getApplicationContext());
		
		ArrayList<Alarm> alarmsTemp = (ArrayList<Alarm>) this.getIntent().getSerializableExtra("Alarms");
		if(alarmsTemp != null){
			alarms = alarmsTemp;
			AlarmAdapter alarmsAdapter = new AlarmAdapter(this, android.R.layout.simple_list_item_1, alarms);
			alarmsList.setAdapter(alarmsAdapter);
			alarmStorage.storeAlarms(alarms);
		} else {
			alarmsTemp = alarmStorage.retrieveAlarms();
			if(alarmsTemp != null){
				alarms = alarmsTemp;
				AlarmAdapter alarmsAdapter = new AlarmAdapter(this, android.R.layout.simple_list_item_1, alarms);
				alarmsList.setAdapter(alarmsAdapter);
			}
		}

		setupContextMenu();
	}

	
	private void setupContextMenu() {
		// TODO Auto-generated method stub
		alarmsList.setMultiChoiceModeListener(new MultiChoiceModeListener() {

		    @Override
		    public void onItemCheckedStateChanged(ActionMode mode, int position,
		                                      long id, boolean checked) {
		    	Log.d(TAG, "Position clicked: " + position);
		    	
		    	Log.d(TAG, "Checked Status: " + checked);
		    	if(checked){
		    		
		    		if( selectedPositions == null){
		    			selectedPositions = new ArrayList<Integer>();
		    			selectedPositions.add(position);
		    		} else {
		    			selectedPositions.add(position);
		    		}
		    
		    	} else {
		    		
		    		if( selectedPositions != null){
		    			selectedPositions.remove(new Integer(position));
		    		}
		    	}
		    	Log.d(TAG, "selectedPositions: " + selectedPositions.toString());
		    	
		    	Alarm alarmItem = (Alarm) alarmsList.getItemAtPosition(position);
		    	Log.d(TAG, alarmItem.getAlarmString(new SimpleDateFormat("H:mm")));
		        // Here you can do something when items are selected/de-selected,
		        // such as update the title in the CAB
		    }

		    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
		        switch (item.getItemId()) {
		            case R.id.menu_delete_alarm:

		            	Log.d(TAG, "Delete clicked");
		            	ArrayList<Alarm> tempAlarms = new ArrayList<Alarm>();
		            	
		            	Collections.sort(selectedPositions);
		            	Collections.reverse(selectedPositions);
		            	Log.d(TAG, "Positions to delete: " + selectedPositions.toString());
		            	for(Integer pos : selectedPositions){
		            		Log.d(TAG, "pos: " + pos.toString());
		            		alarms.remove(pos.intValue());
		            	}
		    			AlarmAdapter alarmsAdapter = new AlarmAdapter(context, android.R.layout.simple_list_item_1, alarms);
		    			alarmsList.setAdapter(alarmsAdapter);
		    			alarmStorage.storeAlarms(alarms);
		                mode.finish(); // Action picked, so close the CAB
		                return true;

		            default:
		                return false;
		        }
		    }

		    @Override
		    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		        // Inflate the menu for the CAB
		    	Log.d(TAG, "CAB Created");
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.alarms_contexual_menu, menu);
		        return true;
		    }

		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
		    	selectedPositions = null;
		    }

		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
		        return false;
		    }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alarms, menu);
		menu.add(0, ALARM_ADD_ID, 0, "Alarm").setIcon(R.drawable.device_access_add_alarm)
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
			Intent resultIntent = new Intent(this, MainActivity.class);
			resultIntent.putExtra("Alarms", alarms);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			//NavUtils.navigateUpFromSameTask(this);
			return true;
		 case ALARM_ADD_ID:
	            // app icon in action bar clicked; go home
	        	Log.d(TAG, "Add Alarm click");
	        	
	        	Intent intent_alarm = new Intent(this, AlarmActivity.class);
	        	intent_alarm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivityForResult(intent_alarm, ALARM_ID);
	          
	            return true;
		 case R.id.menu_settings:
	            // app icon in action bar clicked; go home
	        	Log.d(TAG, "Settings click");
	            Intent intent_settings = new Intent(this, SettingsActivity.class);
	            intent_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent_settings);
	        	
	            return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void parseAlarm(String dateFormat){
		Date alarmDate = alarmTime.getAlarm().getTime();
	    String nextAlarmFormatted = new SimpleDateFormat(dateFormat).format(alarmDate);
	    Log.d(TAG, nextAlarmFormatted);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (ALARM_ID) : { 
	      if (resultCode == Activity.RESULT_OK) {
	    	  Log.d(TAG, "Alarm returned");
	    	  alarmTime = (Alarm) data.getSerializableExtra("Alarm");
	  		if ( alarmTime != null){
	  			parseAlarm("H:mm");
	  			alarms.add(alarmTime);
	  			alarmsAdapter = new AlarmAdapter(this, android.R.layout.simple_list_item_1, alarms);
	  			alarmsList.setAdapter(alarmsAdapter);
	  			alarmStorage.storeAlarms(alarms);
	  		}
	      } 
	      break; 
	    } 
	  } 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        //moveTaskToBack(true);
	    	Intent resultIntent = new Intent(this, MainActivity.class);
			resultIntent.putExtra("Alarms", alarms);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
