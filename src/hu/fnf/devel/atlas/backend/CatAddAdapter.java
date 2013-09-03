/*
 *  * Copyright (c) May 2, 2013 Csikos Balint.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Csikos Balint - initial API and implementation and/or initial documentation
 */
package hu.fnf.devel.atlas.backend;

import hu.fnf.devel.atlas.R;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CatAddAdapter extends ArrayAdapter<Category> {
	Context context;
    int layoutResourceId;   
    double amount;
    OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Category actCat = (Category) seekBar.getTag();
			actCat.setAmount(String.valueOf(progress));
			int id = Integer.valueOf(actCat.getId());
	        TextView amount = (TextView) seekBar.getRootView().findViewById(id);
	        amount.setText(String.valueOf(progress));
		}
	};

	public CatAddAdapter(Context context, int textViewResourceId, ArrayList<Category> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.layoutResourceId = textViewResourceId;
	}

	public CatAddAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);;
        if(convertView == null) {
        	convertView = inflater.inflate(layoutResourceId, parent, false);
			convertView.setTag(getItem(position).getName());

			TextView cat_name = (TextView) convertView.findViewById(R.id.addcat_name);
			TextView cat_amount = (TextView) convertView.findViewById(R.id.addcat_amount);
			SeekBar amount_bar = (SeekBar) convertView.findViewById(R.id.addcat_amount_bar);
			
			cat_name.setText(String.valueOf(getItem(position).getName()));
			cat_name.setTextColor(Color.BLACK);
			
			amount_bar.setTag(getItem(position));
			amount_bar.setMax((int)amount-getSum());

			if ( Integer.valueOf(getItem(position).getAmount()) != 0) {
				amount_bar.setOnSeekBarChangeListener(null);
				amount_bar.setVisibility(android.view.View.INVISIBLE);
				cat_name.setTextSize(cat_name.getTextSize()*0.8f);
				cat_amount.setTextSize(cat_amount.getTextSize()*0.8f);
			} else {
				getItem(position).setAmount(String.valueOf((int)amount-getSum()));
				
				amount_bar.setProgress(Integer.valueOf(getItem(position).getAmount()));
				amount_bar.setOnSeekBarChangeListener(onSeekBarChangeListener);
			}
			cat_amount.setId(Integer.valueOf(getItem(position).getId()));
			cat_amount.setText(getItem(position).getAmount());
			cat_amount.setTextColor(Color.BLACK);
			Log.d("CatAddAdapter", "name(" + position + "): " + getItem(position).getName());
        } else {
        	
        }
        return convertView;
	}

	public  double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	public int getSum() {
		int sum = 0;
		for( int i = 0; i < getCount(); i++) {
			sum += Integer.valueOf(getItem(i).getAmount());
		}
		return sum;
	}

}
 
 
