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
package hu.fnf.devel.atlas.base;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import hu.fnf.devel.atlas.AtlasData;
import hu.fnf.devel.atlas.R;
import hu.fnf.devel.atlas.backend.TopLevelView;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class TopFragment extends AtlasFragment {

	public TopFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("TopFragment", "onCreateView called.");
		View ret = inflater.inflate(R.layout.fragment_toplevel, container,
				false);

		return ret;
	}

	public int getUnixTimeFromMonth(int month) {
		Calendar now = new GregorianCalendar(TimeZone.getDefault());
		Calendar req = new GregorianCalendar(now.get(Calendar.YEAR), month, 0);

		Log.d("TopFragment",
				"req: 2013." + (month + 1) + " -> " + req.getTimeInMillis()
						/ 1000L);
		return (int) (req.getTimeInMillis() / 1000L);
	}

	protected void loadChart(TopLevelView chart, int catType) {
		Calendar now = new GregorianCalendar(TimeZone.getDefault());
		Cursor items = null;

		for (int month = 0; month <= now.get(Calendar.MONTH); month++) {
			Uri.Builder builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("nodes");
			builder.appendPath(String.valueOf(AtlasData.OUTCOME));
			Cursor nodes = getActivity().getContentResolver().query(
					builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null,
					null);
			double sum = 0;
			if (nodes != null && nodes.moveToFirst()) {
				do {
					String catid = String.valueOf(nodes.getInt(AtlasData.CATEGORIES_ID));
					Uri.Builder b = new Builder();
					b.scheme("content");
					b.authority(AtlasData.DB_AUTHORITY);
					b.appendPath(AtlasData.TABLE_DATA);
					b.appendPath("summa");
					b.appendPath(catid);
					b.appendQueryParameter(
							AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE],
							String.valueOf(getUnixTimeFromMonth(month)));
					try {
						items = getActivity().getContentResolver().query(
								b.build(), AtlasData.TRANSACTIONS_COLUMNS,
								null, null, null);
					} catch (Exception e) {
						Log.w("TopFragment", "Exception");
					}
					if (items != null && items.moveToFirst()) {
						do {
							sum += items.getDouble(0);
						} while (items.moveToNext());
						items.close();
					}
				} while (nodes.moveToNext());
			}
			switch (catType) {
			case AtlasData.INCOME:
				chart.addIncome(month, sum);
				break;
			case AtlasData.OUTCOME:
				chart.addOutcome(month, sum);
			default:
				break;
			}
		}
	}
}
