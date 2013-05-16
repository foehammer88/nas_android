/**
 * 
 */
package com.zenappse.nas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Patrick Ganson
 * @version 1.0
 * 
 * Receiver class that handles intercepting text messages
 * from the Android OS.
 */
public class SMSReceiver extends BroadcastReceiver {

	private final String TAG = this.getClass().getSimpleName();
	private MainActivity mainActivity = null;
	/**
	 * Constructor that receives a reference to the 
	 * main activity.
	 * 
	 * @param mainActivity 
	 * 
	 */
	public SMSReceiver(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		this.mainActivity = mainActivity;
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	/**
	 * Method that gets called when a text message gets received.
	 * Handles formatting the message and calls the method in the main
	 * activity that actually sends the message to the device.
	 * 
	 */
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();

        String strMessage = "";

        if ( extras != null )
        {
            Object[] smsextras = (Object[]) extras.get( "pdus" );

            for ( int i = 0; i < smsextras.length; i++ )
            {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);

                String strMsgBody = smsmsg.getMessageBody().toString();
                String strMsgSrc = smsmsg.getOriginatingAddress();

                strMessage += "sms: " + strMsgSrc + " : " + strMsgBody;                    

                Log.i(TAG, strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
                
                mainActivity.sendNotification("sms", strMsgSrc, strMsgBody);
            }

        }

	}

}
