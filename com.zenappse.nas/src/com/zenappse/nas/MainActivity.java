package com.zenappse.nas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.zenappse.nas.R;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Base64;
import android.util.Base64InputStream;
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

	private final static int ALARM_ID = 1;
	private static final int ALARMS_ID = 2;
	private static final int BTENABLER_ID = 5;
	private static final String TAG = "Main Activity";
	public ArrayList<Alarm> alarms = null;
	BluetoothHandler btHandler;
	AlarmStorage alarmStorage;
	final private String key = "alarmslist";
	
	private String nextAlarmStr = "";
	
	private SMSReceiver smsReceiver;
	private IntentFilter intentFilter;
	
	private boolean btPersist;
	private float vibrateFreq;
	private boolean textNotifications;
	
	private boolean isAlarmSent = false;
	private boolean isTextSent = false;
	private boolean isControlSent = false;
	private boolean isFreqSent = false;
	
	SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Log.d(TAG, "On Create");
		
		//SMS event receiver
		smsReceiver = new SMSReceiver(this);
		intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
		
		btHandler = new BluetoothHandler();
		if (btHandler.initialize(this)){
			TextView t = new TextView(this);
	    	t=(TextView)findViewById(R.id.bt_status);
	    	t.setText(btHandler.getDeviceName());
	    	t.setEnabled(true);
		} else {
			TextView t = new TextView(this);
	    	t=(TextView)findViewById(R.id.bt_status);
	    	t.setText("Device not found");
	    	t.setEnabled(false);
		}
		
		initialize();
		
	}

	private void initialize() {
		// TODO Auto-generated method stub
		//Set preferences
		handlePreferences();
		
		alarmStorage = new AlarmStorage(getApplicationContext());
		setCurrentTime();
		//If alarms is empty
		alarms = alarmStorage.retrieveAlarms();
		if(alarms != null){
			Log.d(TAG, alarms.toString());
			setAlarm(alarms);
		} 
		
		if(btHandler.isPaired()){
			sendCurrentTime();
		}
	}

	private void handlePreferences() {
		// TODO Auto-generated method stub
		
        //boolean autoStart = sharedPreferences.getBoolean("notifications_email", true);
		float tempFreq = vibrateFreq;
		
		btPersist = sharedPreferences.getBoolean("bluetooth_persistance", true);
        vibrateFreq = sharedPreferences.getFloat("vibration_freq", (float) 0.5);
        textNotifications = sharedPreferences.getBoolean("notifications_text", true);
        
        if (!btPersist){
        	textNotifications = false;
        }
        
        vibrateFreq += 0.25;
        if(btHandler.isPaired()){
	    	Thread btThread = new Thread(){
				@Override
			    public void run() {
					try {
						Thread.sleep(2500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        Log.d("BT_Thread", "SPD " + vibrateFreq);
			        sendMessage("SPD " + vibrateFreq + "\r\n");

			    }
			};
			btThread.start();
        }
		if(!btPersist){
			Toast.makeText(getBaseContext(), "Turning off bluetooth...", Toast.LENGTH_LONG).show();
			btHandler.disable(); 
		} else {
			btHandler.enable();
		}
        Log.d(TAG, "BT Persist: "+ btPersist);
		Log.d(TAG, "Vibration Freq: " + vibrateFreq);
		Log.d(TAG, "Text Notification: " + textNotifications);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.add(0, ALARM_ID, 0, "Contacts").setIcon(R.drawable.device_access_alarms)
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
	        	Log.d("Main", "Alarms click");
	        	startAlarmsActivity();
	        	
	            return true;
	        case R.id.menu_settings:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Settings click");
	            Intent intent_settings = new Intent(this, SettingsActivity.class);
	            intent_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivityForResult(intent_settings, R.id.menu_settings);
	            
	            return true;
	        case R.id.menu_alarms:
	            // app icon in action bar clicked; go home
	        	Log.d("Main", "Alarms click");
	        	startAlarmsActivity();
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void startAlarmsActivity(){
		Intent intent_alarms = new Intent(this, AlarmsActivity.class);
        intent_alarms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(alarms != null){
        	intent_alarms.putExtra("Alarms", alarms);
        }
        startActivityForResult(intent_alarms, ALARMS_ID);
	}
	
	public void syncDevice(View view){
		
		if(btHandler.isPaired()){
			Thread btThread = new Thread(){
				@Override
				public void run() {
				try {
					Thread.sleep(2500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				        Log.d("BT_Thread", "SPD " + vibrateFreq);
				        sendMessage("SPD " + vibrateFreq + "\r\n");
				    }
				};
			btThread.start();
    	}
		setAlarm(alarms);
		sendCurrentTime();
	}
	
	public void syncAlarm(View view){
		String nextAlarm = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		if (nextAlarm.length()==0 || nextAlarm == "") {
			//TODO if(AlarmsArray is empty())
			if(alarms != null){
				Log.d(TAG, "Sync alarm: " + alarms.toString());
				setAlarm(alarms);
			} else {
				setCurrentTime();
				Toast.makeText(getApplicationContext(), "No alarms found", Toast.LENGTH_SHORT).show();
			}
	    	
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
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(alarmDate);
		    Alarm newAlarm = new Alarm(cal);
		    setAlarmTime(newAlarm);
		    sendCurrentTime();
		    String nextAlarmFormatted = new SimpleDateFormat("h:mm").format(alarmDate);
		    String nextAlarmPeriod = new SimpleDateFormat("aa").format(alarmDate);
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
	
	public void sendCurrentTime(){
		Calendar c = Calendar.getInstance(); 
		Date currentDate = c.getTime();
		String currentTime = new SimpleDateFormat("H:mm").format(currentDate);
		String curDate = new SimpleDateFormat("yyyy-M-d").format(currentDate);
		final String curtime = "CON " + curDate + " " + currentTime + "\r\n";
		if(btHandler.isPaired() && btPersist){
			Thread btThread = new Thread(){
				@Override
			    public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("BT_Thread", curtime);
					sendMessage(curtime);
					
			    }
			};
			btThread.start();
		}
		
		Log.d(TAG, "Outside send time BT thread");
		
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
	
	public void setAlarmTime(Alarm nextAlarm){
		Date nextAlarmDate = nextAlarm.getAlarm().getTime();
		String currentTime = new SimpleDateFormat("h:mm").format(nextAlarmDate);
		String currentPeriod = new SimpleDateFormat("aa").format(nextAlarmDate);
		TextView t = new TextView(this);
		t=(TextView)findViewById(R.id.textAlarmTime);
		t.setText(currentTime);
		t=(TextView)findViewById(R.id.textAlarmTimePeriod);
		t.setText(currentPeriod.toLowerCase());
		t=(TextView)findViewById(R.id.textAlarmTitle);
		t.setText("Next Alarm");
		
		final String aTime = new SimpleDateFormat("H:mm").format(nextAlarmDate);
		
		if(btHandler.isPaired() && btPersist){
			Thread btThread = new Thread(){
				@Override
			    public void run() {
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        Log.d("BT_Thread", "ALR " + aTime);
			        sendMessage("ALR " + aTime + "\r\n");
			    }
			};
			btThread.start();
			Log.d(TAG, "Outside BT thread");
		}
		nextAlarmStr = aTime;
	}
	
	public void sendNotification(String type, String msgSource, String msgBody){
		if (type.equals("sms")){
						
			//final String message =  getContactName(msgSource) + "\r" + msgBody;
			String name = getContactName(msgSource);
			if (name.length() > 16){
				name = name.substring(0, 16);
			}
			if (msgBody.length() > 16){
				msgBody = msgBody.substring(0, 13);
				msgBody = msgBody + "...";
			} else if(msgBody.length() > 13){
				msgBody = msgBody.substring(0, 13);
				msgBody = msgBody + "...";
			}
			final String message = name +"          " +  "\r" + msgBody + "           ";
			if(btHandler.isPaired() && textNotifications && btPersist){
				
				Thread btThread = new Thread(){
					@Override
				    public void run() {
				        Log.d("BT_Thread", "MSG " + message);
				        sendMessage("MSG " + message + "\r\n");
				    }
				};
				btThread.start();
				Log.d(TAG, "Outside Message BT thread");
			}
		} else if (type.equals("email")){
			// Email code goes here, if OS support gets added.
		}
	}
	
	
	public void setAlarm(ArrayList<Alarm> alarms){
		ArrayList<Alarm> validAlarms = new ArrayList<Alarm>();
  		Calendar currCalendar = Calendar.getInstance();
  		Alarm currAlarm = new Alarm(currCalendar);
  		if(alarms.size() > 0){
	    	for( Alarm alarm : alarms){
	    		if(alarm.after(currAlarm)){
	    			validAlarms.add(alarm);
				}
	    	}
	    	if (validAlarms.size() > 0){
				Alarm nextAlarm = validAlarms.get(0);
				for( Alarm alarm : validAlarms){
					if(alarm.before(nextAlarm)){
						nextAlarm = alarm;
					}
				}
				
				setAlarmTime(nextAlarm);
				Log.d(TAG, nextAlarm.getAlarmString(new SimpleDateFormat("h:mm")));
	    	} else{
	    		Alarm nextAlarm = alarms.get(0);
	    		for( Alarm alarm : alarms){
					if(alarm.before(nextAlarm)){
						nextAlarm = alarm;
					}
				}
	    		setAlarmTime(nextAlarm);
				Log.d(TAG, nextAlarm.getAlarmString(new SimpleDateFormat("h:mm")));
	    	}
  		} else {
			setCurrentTime();
			Toast.makeText(getApplicationContext(), "No alarms found", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  Log.d(TAG, "Result request code: " + requestCode);
	  switch(requestCode) { 
	    case (ALARMS_ID) : { 
	      if (resultCode == Activity.RESULT_OK) {
	    	  Log.d(TAG, "Alarms returned");
	    	  handlePreferences();
	    	  alarms = (ArrayList<Alarm>) data.getSerializableExtra("Alarms");
	    	  
	    	  Log.d(TAG, alarms.toString());
	    	  if (alarms != null){
	      		setAlarm(alarms);

	    	  } else {
	    		alarms = alarmStorage.retrieveAlarms();
	  			if(alarms != null){
	  				setAlarm(alarms);

	  			}
	    	  }
	    	  
	      } 
	      break; 
	    } 
	    case (BTENABLER_ID) : { 
	    	Log.d(TAG, "Back from BT Enabler. ResultCode: " + resultCode);
		      if (resultCode == Activity.RESULT_OK) {
		    	  Log.d(TAG, "BT Enabler returned, result ok");
		    	  
		    	  if ( btHandler.setup()){
		  			TextView t = new TextView(this);
		  	    	t=(TextView)findViewById(R.id.bt_status);
		  	    	t.setText(btHandler.getDeviceName());
		  	    	t.setEnabled(true);
		    	  } else {
		  			TextView t = new TextView(this);
		  	    	t=(TextView)findViewById(R.id.bt_status);
		  	    	t.setText("Device not found");
		  	    	t.setEnabled(false);
		    	  }
		    	  initialize();
		      } 
		      break; 
		}
	    case (R.id.menu_settings) : { 
	    	Log.d(TAG, "Back from Settings. ResultCode: " + resultCode);
	    	handlePreferences();
		      if (resultCode == Activity.RESULT_OK) {
		    	  Log.d(TAG, "Settings returned, result ok");
		    	  
		      } 
		      break; 
		}
	    default:
	  } 
	}
	
	private String getContactName(String source){
		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(source));
		// query time
		Cursor cursor = this.getContentResolver().query(contactUri, new String[]{
				ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
		String sourceName = source;
		if (cursor.moveToFirst()) {
		    // Get values from contacts database:
			sourceName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
         
		    Log.d(TAG, "Contact name: " + sourceName);
		} else {

		    Log.d(TAG, "Contact Not Found: " + sourceName);
		}
		return sourceName;
	}
	
	public void sendMessage(String msg){
		String msgType = msg.substring(0, 3);
		if (btHandler.sendMessage(msg)){
			runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast.makeText(getBaseContext(), "Bluetooth message sent sucessfully.", Toast.LENGTH_LONG).show();
			    }
			});
			if(msgType.equals("ALR")){
				isAlarmSent = true;
			} else if (msgType.equals("MSG")){
				isTextSent = true;
			} else if (msgType.equals("CON")){
				isControlSent = true;
			} else if (msgType.equals("SPD")){
				isFreqSent = true;
			}
		} else {
			runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast.makeText(getBaseContext(), "Bluetooth message not sent, trying again.", Toast.LENGTH_LONG).show();
			    }
			});
			if(msgType.equals("ALR")){
				isAlarmSent = false;
			} else if (msgType.equals("MSG")){
				isTextSent = false;
			} else if (msgType.equals("CON")){
				isControlSent = false;
			} else if (msgType.equals("SPD")){
				isFreqSent = false;
			}
		}
		
	}
}
