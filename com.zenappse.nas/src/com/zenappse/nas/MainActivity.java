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


/**
 * @author Patrick Ganson
 * @version 1.0
 * 
 * Main controller class for the home screen of the app.
 * Contains many methods for navigation and control of the app.
 * Calls Bluetooth handler that actually sends method.
 * Determines which alarm of the alarms list, that is retrieved 
 * from storage, that will be the next alarm to get sent.
 */
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

	/**
	 * Intializes the app for retrieving the alarms list
	 * and sets the current time to the device.
	 */
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

	/**
	 * Retrieves the user preferences that get set in SettingsActivity
	 * and stores the preferences in class variables.
	 * Sends the vibration frequency to the device.
	 */
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
	/**
	 * Sets up the menu on the action bar.
	 */
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
	
	/**
	 * Method to navigate to the Alarms Activity
	 * Sends the list of alarms to the Alarms Activity
	 * to populate the Alarms List View.
	 */
	public void startAlarmsActivity(){
		Intent intent_alarms = new Intent(this, AlarmsActivity.class);
        intent_alarms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(alarms != null){
        	intent_alarms.putExtra("Alarms", alarms);
        }
        startActivityForResult(intent_alarms, ALARMS_ID);
	}
	
	/**
	 * Syncs the device when the button is pressed.
	 * Sends the vibration frequency to the device, along
	 * with the alarm and current time. Should be used if
	 * any bluetooth message send fails
	 * @param view
	 */
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
	
	/**
	 * Method gets called when button is pressed.
	 * If there is a OS alarm, sets that as the next alarm.
	 * Otherwise, retrieves the next alarm from the in app alarms
	 * list. If no alarm is found, then shows the current time.
	 * @param view
	 */
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
	
	/**
	 * Sends the current time to the device as a control signal
	 * to sync the time of the device with the phone.
	 * 
	 * Message format: "CON H:mm", 
	 */
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
	
	/**
	 * Gets the current time from the device and converts it to the
	 * Correct String format and displays it on the home screen.
	 */
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
	
	/**
	 * Sets the alarm given in the parameter to the main screen
	 * in the proper String format, and sends the alarm time to
	 * the device in the following format.
	 * 
	 * Ex: "ALR H:mm" in 24 hour time format
	 */
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
	
	/**
	 * Method that formats a text message, including the sender,
	 * into a format that is supported by the device. And then sends
	 * that message to the device. The string can only be 32 visible
	 * characters long, 16 chars for the top line of the display, 
	 * which is the sender, and 16 chars for the bottom line,
	 * for a snippet of the body of the text message.
	 * 
	 * Ex. "MSG <16 chars>/r<16 chars>/r/n"
	 */
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
	
	/**
	 * Method that receives an ArrayList of alarms and performs
	 * an algorithm which determines which alarm, from the given list,
	 * is the next alarm from the current time. It then calls setAlarmTime()
	 * to set the alarm on the home screen and send it to the device.
	 * If no alarms are found, then it produces a toast notification of "No
	 * alarms found"
	 * 
	 * @param alarms
	 */
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
	/**
	 * Method that handles the return to the main screen,
	 * either from the AlarmsActivity, the Bluetooth enabler popup, 
	 * or from the settings.
	 */
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
	
	/**
	 * Helper method to retrieve the contact name from the given
	 * phone number from a text message.
	 * 
	 * @param source
	 * @return
	 */
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
	
	/**
	 * Helper method to handle the sending of any bluetooth message,
	 * pops up a toast notification is the sending of the message was
	 * succesfull or not.
	 * 
	 * @param msg
	 */
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
