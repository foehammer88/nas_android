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
 * @author Patrick
 *
 */
public class Alarm implements Serializable{

	public static final String ALARM = "alarm_object";
	public static final String TAG = "Alarm Class";
	private Calendar alarmTime;
	private boolean enabled;
	
	/**
	 * 
	 */
	public Alarm(Calendar alarm) {
		// TODO Auto-generated constructor stub
		this.alarmTime = alarm;
		this.enabled = true;
	}

	public Calendar getAlarm(){
		
		return alarmTime;
	}
	
	public boolean before(Alarm comparedAlarm){
		
		return this.getAlarm().before(comparedAlarm.getAlarm());
	}
	
	public boolean after(Alarm comparedAlarm){
		
		return this.getAlarm().after(comparedAlarm.getAlarm());
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled(boolean isEnabled){
		this.enabled = isEnabled;
	}
	
	public String getAlarmString(){
		return alarmTime.toString();
	}
	
	public String getAlarmString(SimpleDateFormat dateFormat){
		Date alarmDate = alarmTime.getTime();
		return dateFormat.format(alarmDate);
	}
	
	public void setAlarm(Calendar alarmtime){
		alarmTime = alarmtime;
	}

}
