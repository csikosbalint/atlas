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

import java.text.SimpleDateFormat;
import java.util.Date;

import hu.fnf.devel.atlas.AtlasData;
import hu.fnf.devel.atlas.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DatabaseQueryAdapter extends CursorAdapter {
	String database;
	private LayoutInflater mLayoutInflater;
	
	public DatabaseQueryAdapter(Context context, Cursor c, int flags, String database) {
		super(context, c, flags);
		this.database = database;
		Log.d("DatabaseQueryAdapter", "database: " + database);
		mLayoutInflater = LayoutInflater.from(context);
	}
	@SuppressLint("SimpleDateFormat")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String to = null;
		String amount = null;
		String sdate = null;
		long date = 0;
		TextView toText = (TextView) view.findViewById(R.id.to);
		TextView amountText = (TextView) view.findViewById(R.id.amount);
		TextView dateText = (TextView) view.findViewById(R.id.date);

		
		if (database.equalsIgnoreCase(AtlasData.TABLE_TRANSACTIONS)) {
			view.setId(cursor.getInt(AtlasData.TRANSACTIONS_ID));
			to = cursor.getString(AtlasData.TRANSACTIONS_TO);
			amount = cursor.getString(AtlasData.TRANSACTIONS_AMOUNT);
			sdate = cursor.getString(AtlasData.TRANSACTIONS_DATE);
			date = Long.valueOf(sdate) * 1000;
			dateText.setText(new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(date)));
		} else if (database.equalsIgnoreCase(AtlasData.TABLE_DATA)) {
			view.setId(cursor.getInt(AtlasData.DATA_ID));
			to = cursor.getString(AtlasData.DATA_TAG);
			amount = cursor.getString(AtlasData.DATA_AMOUNT);
			dateText.setText(AtlasData.getCatName(context,cursor.getInt(AtlasData.DATA_CATEGORYID)));
		} else {
			Log.e("DatabaseQueryAdapter", "no database set or unknown(" + database + ")");
		}
		toText.setText(to);
		toText.setTextColor(Color.BLACK);
		// TODO: ha mas valutat is ki kell irni
		/*
		 * Ha a felhasználó más pénznemről kap SMS-t
		 * akkor nem lehet HUF-ot írni. Ez egy nagyobb 
		 * feladat, h felismerni és kezelni a valutákat.
		 * Talán a nyelveknél és egyéb formátumoknál kéne.
		 */
		amountText.setText(amount + " HUF");
		
		amountText.setTextColor(Color.BLACK);
		
		dateText.setTextColor(Color.BLACK);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mLayoutInflater.inflate(R.layout.template_list, parent, false);
		bindView(v, context, cursor);
		return v;
	}
}
 
