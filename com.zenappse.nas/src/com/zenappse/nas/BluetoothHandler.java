/**
 * 
 */
package com.zenappse.nas;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Patrick Ganson
 * @version 1.0
 * 
 * Adapter class to handle all bluetooth communication
 * with the Notification Alert Device.
 */
public class BluetoothHandler implements Serializable{

	private static final String REQUEST_ENABLE_BT = "BTH";
	private static final int BTENABLER_ID = 5;
	private final String TAG = "BluetoothHandler";
	
	private boolean isPaired = false;
	private Activity act;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	String deviceName = "";
	String deviceAddress = "";
	BluetoothDevice btDevice;
	public BluetoothHandler(){
		
	}
	
	/**
	 * Initializes the app for bluetooth communication.
	 * If no bluetooth chip in phone, returns false.
	 * 
	 * @param act
	 * @return
	 */
	public boolean initialize(Activity act){
		this.act = act;
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			Log.d(TAG, "No Bluetooth Adapter");
			return false;
		}
		
		enable();
		
		return setup();
	}
	
	public boolean isPaired(){
		return isPaired;
	}
	
	public String getDeviceName(){
		return deviceName;
	}
	
	/**
	 * Sets up the bluetooth adapter and retrieves basic information
	 * of the paired NAS device.
	 * @return
	 */
	public boolean setup(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			Log.d(TAG, "Retrieve BT devices");
			isPaired = true;
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView

		    	deviceName = device.getName();
		    	deviceAddress = device.getAddress();
		    	btDevice = device;
		    }
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * Method to actually perform the most important functionality
	 * of the app. Initializes bluetooth socket for communication.
	 * Sends the String message parameter to the paired NAS device.
	 * @param msg
	 * @return
	 */
	public boolean sendMessage(String msg){

		BluetoothSocket btSocket;

		Method m;
		try {
			m = btDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
		
			btSocket = (BluetoothSocket) m.invoke(btDevice, Integer.valueOf(1));
	
			btSocket.connect();
	
			//code to send string from my android handset to another
			OutputStream outputStream = btSocket.getOutputStream();

			Log.d(TAG, msg);
			outputStream.write(msg.getBytes()); 
			btSocket.close();

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Turns on bluetooth if it is off.
	 */
	public void enable(){
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    act.startActivityForResult(enableBtIntent, BTENABLER_ID);
		}
	}
	/**
	 * Turns off bluetooth if it is on.
	 */
	public void disable() {
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable(); 
		}
	}
}
