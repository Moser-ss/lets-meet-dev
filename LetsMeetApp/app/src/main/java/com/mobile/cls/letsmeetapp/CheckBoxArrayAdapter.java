package com.mobile.cls.letsmeetapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by User on 28/05/2016.
 */
class CheckBoxArrayAdapter<T> extends ArrayAdapter<String> {
    private ArrayList<String> items;
    private HashMap<String, ViewHolder> viewMap = new HashMap<String, ViewHolder>();
    private final Context context;

    public ArrayList<String> getCheckedItems() {
        ArrayList<String> checkedItems = new ArrayList<>();
        for (String item : items) {

            if (isChecked(item)) {
                checkedItems.add(item);
            }
        }

        return checkedItems;
    }

    private class ViewHolder {
        TextView itemName;
        CheckBox itemCheck;
    }

    public CheckBoxArrayAdapter(Context context, int resource, ArrayList<String> output) {
        super(context, resource, output);
        this.context = context;
        this.items = output;
        for (int i = 0; i < output.size(); ++i) {
            Log.d("DEBUG", "Put item " + output.get(i) + " with position " + i);
            viewMap.put(output.get(i), new ViewHolder());
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String item = getItem(position);
        Log.d("DEBUG", "Inflate row to item " + item + " from position " + position);
        View rowView = inflater.inflate(R.layout.checkbox_row, parent, false);
        CheckBox checkBoxView = (CheckBox) rowView.findViewById(R.id.checkBox);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);
        textView.setText(getItem(position).toString());

        ViewHolder viewHolder = viewMap.get(item);
        viewHolder.itemName = textView;
        viewHolder.itemCheck = checkBoxView;
        return rowView;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public boolean isChecked(String item) {

        ViewHolder viewHolder = viewMap.get(item);
        if(viewHolder.itemCheck == null)return false;
        return viewHolder.itemCheck.isChecked();
    }

}
