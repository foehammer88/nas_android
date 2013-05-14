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
 * @author Patrick
 *
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
	
	public boolean setup(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			Log.d(TAG, "Retrieve BT devices");
			isPaired = true;
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    	deviceName = device.getName();
		    	deviceAddress = device.getAddress();
		    	btDevice = device;
		    }
		} else {
			return false;
		}
		return true;
	}
	
	public boolean sendMessage(String msg){

		BluetoothSocket btSocket;

		Method m;
		try {
			m = btDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
		
			btSocket = (BluetoothSocket) m.invoke(btDevice, Integer.valueOf(1));
	
			btSocket.connect();
	
			//code to send string from my android handset to another
			OutputStream outputStream = btSocket.getOutputStream();
			//String message = "Data from Android\n"; 
			Log.d(TAG, msg);
			outputStream.write(msg.getBytes()); 
			btSocket.close();
			//Toast.makeText(act.getBaseContext(), "Bluetooth message sent sucessfully.", Toast.LENGTH_LONG).show();
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
			//Toast.makeText(act.getBaseContext(), "Bluetooth message not sent, try again.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void enable(){
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    act.startActivityForResult(enableBtIntent, BTENABLER_ID);
		}
	}
	
	public void disable() {
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable(); 
		}
	}
}
