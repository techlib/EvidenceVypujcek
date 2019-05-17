package cz.techlib.evidencevypujcek;


import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BookListAdapter extends ArrayAdapter<HashMap<String, String>> {

    private ArrayList<HashMap<String, String>> items;
    private Resources resources;

    public BookListAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> items) {
            super(context, textViewResourceId, items);
            this.resources = context.getResources();
            this.items = items;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            
    	    View v = convertView;
    	    ViewHolder holder; 
    	    final HashMap<String, String> o = items.get(position);
    	    
    	    
            if (v == null) {
            	holder = new ViewHolder();
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.custom_row_view, null);
                
                
                holder.code = (TextView) v.findViewById(R.id.code);
                holder.name = (TextView) v.findViewById(R.id.name);
                holder.author = (TextView) v.findViewById(R.id.author);
                holder.location = (TextView) v.findViewById(R.id.location);
                holder.progress = (ProgressBar) v.findViewById(R.id.progress);
                holder.tmp_code = (TextView) v.findViewById(R.id.tmp_code);
                holder.loaded_part = (LinearLayout) v.findViewById(R.id.loaded_part);
                holder.loading_part = (LinearLayout) v.findViewById(R.id.loading_part);
                holder.loading_part = (LinearLayout) v.findViewById(R.id.loading_part);
                holder.to_send = (CheckBox) v.findViewById(R.id.to_send);

                v.setTag(holder);
            } else {
            	holder = (ViewHolder) v.getTag();
            }
            
            if (o != null) {
                  
	            holder.code.setText(o.get("code"));
	        	holder.name.setText(o.get("name"));
	        	holder.location.setText(o.get("location"));
	        	holder.author.setText(o.get("author"));
	        	holder.to_send.setChecked(o.get("to_send") == "true");

	        	
	        	String loadingText = String.format(this.resources.getString(R.string.loading_message), o.get("code"));
	            holder.tmp_code.setText(loadingText);
	        	
	            if(o.get("location") != null && o.get("location") != ""){
                	holder.progress.setVisibility(View.GONE);
                	holder.location.setVisibility(View.VISIBLE);
                	holder.loaded_part.setVisibility(View.VISIBLE);
                	holder.loading_part.setVisibility(View.GONE);
                	
                } else {
                	holder.progress.setVisibility(View.VISIBLE);
                	holder.location.setVisibility(View.GONE);
                	holder.loaded_part.setVisibility(View.GONE);
                	holder.loading_part.setVisibility(View.VISIBLE);
                }
	            
	            
	            if(o.get("sent") == "true") {
            		holder.to_send.setVisibility(View.INVISIBLE);
	            } else {
	            	holder.to_send.setVisibility(View.VISIBLE);
	            } 
	            
	            int color;
	            if(o.get("error") == "true" && o.get("sent") == "false") {
	            	color = android.graphics.Color.RED;
	            } else if (o.get("loan") == "true" && o.get("sent") == "true") {
                    color = android.graphics.Color.BLUE;
                } else {
	            	if(o.get("sent") == "true") {
	            		color = android.graphics.Color.rgb(200, 200, 200);
		            } else {
		            	color = android.graphics.Color.BLACK;
		            } 
	           }
	            
            	holder.name.setTextColor(color);
            	holder.author.setTextColor(color);
            	holder.code.setTextColor(color); 
            	holder.location.setTextColor(color);
	            
            }
            return v;
    }
    
    private static class ViewHolder {
    	TextView code;
        TextView name;
        TextView author;
        TextView location;
        TextView tmp_code;
        ProgressBar progress;
        LinearLayout loaded_part;
        LinearLayout loading_part;
        CheckBox to_send;

    }
}

