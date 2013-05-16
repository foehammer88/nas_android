/**
 * 
 */
package com.zenappse.nas;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * @author Patrick Ganson
 * @version 1.0
 * 
 * Container object that holds all the information about an alarm
 * Base data object to hold the time is the Calendar object
 */
public class Alarm implements Serializable{

	public static final String ALARM = "alarm_object";
	public static final String TAG = "Alarm Class";
	private Calendar alarmTime;
	private boolean enabled;
	
	/**
	 * Class constructor
	 */
	public Alarm(Calendar alarm) {
		// TODO Auto-generated constructor stub
		this.alarmTime = alarm;
		this.enabled = true;
	}

	public Calendar getAlarm(){
		
		return alarmTime;
	}
	
	/**
	 * Determines if the current object is before, chronologically
	 * than the Alarm object parameter.
	 * 
	 * Returns true if the current alarm is before comparedAlarm
	 */
	public boolean before(Alarm comparedAlarm){
		
		return this.getAlarm().before(comparedAlarm.getAlarm());
	}
	
	/**
	 * Determines if the current object is after, chronologically
	 * than the Alarm object parameter.
	 * 
	 * Returns true if the current alarm is after comparedAlarm
	 */
	public boolean after(Alarm comparedAlarm){
		
		return this.getAlarm().after(comparedAlarm.getAlarm());
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled(boolean isEnabled){
		this.enabled = isEnabled;
	}
	
	/**
	 * Returns the string representation of the current alarm
	 */
	public String getAlarmString(){
		return alarmTime.toString();
	}
	
	/**
	 * Returns the string representation of the current alarm
	 * with the specified format for the date.
	 * 
	 * Ex. "h:M aa" returns the alarm format of "5:31 pm"
	 */
	public String getAlarmString(SimpleDateFormat dateFormat){
		Date alarmDate = alarmTime.getTime();
		return dateFormat.format(alarmDate);
	}
	
	public void setAlarm(Calendar alarmtime){
		alarmTime = alarmtime;
	}

}
