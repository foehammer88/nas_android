/**
 * 
 */
package com.zenappse.nas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

/**
 * @author Patrick
 *
 */
public class AlarmStorage {

	private Context context;
	final private String pref_name = "alarms_list";
	final private String key = "alarmslist";
	SharedPreferences mPrefs;
    SharedPreferences.Editor ed;
    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
    
    public AlarmStorage(Context context){
    	this.context = context;
    	//mPrefs = getPreferences(Activity.MODE_PRIVATE);
    	mPrefs = context.getSharedPreferences(pref_name, Activity.MODE_PRIVATE);
    	//mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Log.d("AlarmStorage", "Before edit");
    	if (mPrefs != null){
    		Log.d("AlarmStorage", "In edit");
    		ed = mPrefs.edit();
    		Log.d("AlarmStorage", "In after edit");
    	}
    	Log.d("AlarmStorage", "After edit");
    }
	
    public void storeAlarms(ArrayList<Alarm> alarms) {
    	ObjectOutputStream objectOutput;
        try {
            objectOutput = new ObjectOutputStream(arrayOutputStream);
            objectOutput.writeObject(alarms);
            byte[] data = arrayOutputStream.toByteArray();
            objectOutput.close();
            arrayOutputStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
            b64.write(data);
            b64.close();
            out.close();

            ed.putString(key, new String(out.toByteArray()));

            ed.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
    
	public ArrayList<Alarm> retrieveAlarms() {
    	byte[] bytes = mPrefs.getString(key, "{}").getBytes();
        if (bytes.length == 0) {
            return null;
        }
        ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
        Base64InputStream base64InputStream = new Base64InputStream(byteArray, Base64.DEFAULT);
        ObjectInputStream in;
        ArrayList<Alarm> myAlarms = null;
        try {
			in = new ObjectInputStream(base64InputStream);
			myAlarms = (ArrayList<Alarm>) in.readObject();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			myAlarms = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			myAlarms = null;
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			myAlarms = null;
			e.printStackTrace();
		}
		return myAlarms;
	}
}
