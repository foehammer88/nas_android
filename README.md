NAS Readme

Minimum Android version 16 (Jelly Bean)
Target Version 17 (Jelly Bean)

The basic functionality for sending messages follows a format of control type:

"CON" - Syncing Android time with device time, EX: "CON 13:51"
"ALR" - Setting an alarm on the device, EX: "ALR 16:20"
"SPD" - Setting the vibration period of the LED and Bed shaker, "SPD <float>"
"MSG" - Sending text message to device "MSG <16 chars>/r<16 chars>/r/n"

If the device supports new types of Bluetooth String messages, implementing them within the app is as simple as creating a new method in the MainActivity.java class.

Ex: If you wish to send Off commands to the device to turn it off or put it to sleep

For an example.

sendCommand(String msg){
	btHandler.sendMessage("STAT off");
}

If you wish to add more settings, which are saved automatically in the app's default shared preferences storage when you change a preference, then you need to edit SettingsActivity.java and the cooresponding xml files that determine the look/layout on the screen.

Alarm.java, AlarmStorage.java, BluetoothHandler.java, SMSReceiver.java shouldn't need to be edited. Only MainActivity.java, SettingsActivity.java, AlarmsActivity.java, and AlarmsAdapter.java need to be changed if more functionality is added or removed.

To change the layout appearences, the corresponding xml files to the controller Activity classes need to be edited. Doing so is non-trivial.