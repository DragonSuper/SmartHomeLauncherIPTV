package atn.ontheblue.iptv.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class menuAdapter extends ArrayAdapter<String>{
	private final Context context;
	private final String[] values;
	
	public menuAdapter(Context context, String[] values) {
		super(context, R.layout.row_menu_layout, values);
		this.context = context;
		this.values = values;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_menu_layout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.menu_label);
		//ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(values[position]);	
		return rowView;
	}
	public String getItem(int position){
		return values[position];
	}
	public String getMenuName(int position){
		return values[position];
	}
}
