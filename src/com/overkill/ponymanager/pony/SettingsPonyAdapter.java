package com.overkill.ponymanager.pony;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.overkill.live.pony.R;

public class SettingsPonyAdapter extends SortablePonyAdapter{
	public interface ValueChangedListener{
		public void onValueChanged();
	}
	
	ValueChangedListener valueChangedListener;
	
	public SettingsPonyAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public SettingsPonyAdapter(Context context, int textViewResourceId,	List<DownloadPony> objects) {
		super(context, textViewResourceId, objects);
	}
	
	public SettingsPonyAdapter(Context context, ValueChangedListener valueChangedListener, int textViewResourceId,	List<DownloadPony> objects) {
		super(context, textViewResourceId, objects);
		this.valueChangedListener = valueChangedListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)super.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(super.layoutResourceId, parent, false);
        }
        final DownloadPony p = super.filteredPonies.get(position);
        if (p != null) {          	
        	((ImageView)v.findViewById(R.id.imagePony)).setImageBitmap(p.getImage());
        	((TextView)v.findViewById(R.id.textName)).setText(p.getName());
        	((TextView)v.findViewById(R.id.textUsageCount)).setText(String.valueOf(p.getUsageCount()));
        	((Button)v.findViewById(R.id.btnPlus)).setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					p.setUsageCount(p.getUsageCount() + 1);
					valueChangedListener.onValueChanged();
					notifyDataSetChanged();
				}
			});
        	((Button)v.findViewById(R.id.btnMinus)).setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					int value = p.getUsageCount();
					value -= 1;
					if(value < 0)
						value = 0;
					p.setUsageCount(value);
					valueChangedListener.onValueChanged();
					notifyDataSetChanged();
				}
			});
        	((Button)v.findViewById(R.id.btnZero)).setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					p.setUsageCount(0);
					valueChangedListener.onValueChanged();
					notifyDataSetChanged();
				}
			});
        	
        }
        return v;
	}
	
	public int getAllUsageCount(){
		int sum = 0;
		for(DownloadPony p : getAllItems()){
			sum += p.getUsageCount();
		}
		return sum;
	}
	
	/**
	 * Will write current values to editor but will NOT commit
	 * @param editor
	 */
	public void saveSettings(Editor editor){
		for(DownloadPony p : super.allPonies){
			editor.putInt("pony_count_" + p.getFolder(), p.getUsageCount());
		}
	}
}