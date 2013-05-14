/**
 * 
 */
package com.zenappse.nas;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @author Patrick
 *
 */
public class AlarmAdapter extends ArrayAdapter<Alarm> {

	Context context;
	int layoutResourceId;
	ArrayList<Alarm> alarms = null;
	

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public AlarmAdapter(Context context, int layoutResourceId, ArrayList<Alarm> data) {
		super(context, layoutResourceId, data);
		// TODO Auto-generated constructor stub
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.alarms = data;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.alarm_item_view, parent, false);
	    
	    TextView textViewTime = (TextView) rowView.findViewById(R.id.textAlarmItemTime);
	    TextView textViewPeriod = (TextView) rowView.findViewById(R.id.textAlarmItemTimePeriod);
	    CheckBox checkBoxTime = (CheckBox) rowView.findViewById(R.id.checkBoxAlarmItemEnabled);
	    
	    //Set checkbox theme to have high contrast
	    int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
	    ((CheckBox) rowView.findViewById(R.id.checkBoxAlarmItemEnabled)).setButtonDrawable(id);
	    
	    textViewTime.setText(alarms.get(position).getAlarmString(new SimpleDateFormat("h:mm")));
	    textViewPeriod.setText(alarms.get(position).getAlarmString(new SimpleDateFormat("aa")));
	    checkBoxTime.setChecked(alarms.get(position).isEnabled());
	    
	    return rowView;
		
	}
	
}
