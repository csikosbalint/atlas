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

import hu.fnf.devel.atlas.AtlasData;
import hu.fnf.devel.atlas.R;
import hu.fnf.devel.atlas.ViewBehavior;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AtlasView extends View {
	protected ViewBehavior viewBehavior;

	public AtlasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AtlasView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AtlasView(Context context) {
		super(context);
	}

	public void setViewBehavior(ViewBehavior viewBehavior) {
		this.viewBehavior = viewBehavior;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		viewBehavior.draw(canvas, this);
	}

	public void load() {
		for ( int pietype: viewBehavior.getPieTypes() ) {

			Uri.Builder builder = null;

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("nodes");
			builder.appendPath(String.valueOf(String.valueOf(pietype)));
			
			Cursor nodes = ((Activity) this.getContext()).getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS,
					null, null, null);

			double all = 0;
			if (nodes != null && nodes.moveToFirst()) {
				do {
					String catid = String.valueOf(nodes.getInt(AtlasData.CATEGORIES_ID));
					
					builder = new Builder();
					builder.scheme("content");
					builder.authority(AtlasData.DB_AUTHORITY);
					builder.appendPath(AtlasData.TABLE_DATA);
					builder.appendPath("summa");
					builder.appendPath(catid);

					builder.appendQueryParameter(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE],
							String.valueOf(AtlasData.getMonthStartUnixTime(this.getRootView())));
					Cursor items = ((Activity) 
							this.getContext()).getApplication().getContentResolver().query(builder.build(),
							AtlasData.TRANSACTIONS_COLUMNS, null, null, null);
					TextView title = null;
					switch (pietype) {
					case AtlasData.INCOME:
					case AtlasData.ALLINCOME:
						title = (TextView) findViewById(R.id.summaryIncome);
						pietype = AtlasData.INCOME;
						break;
					case AtlasData.OUTCOME:
					case AtlasData.ALLOUTCOME:
						title = (TextView) findViewById(R.id.summaryOutcome);
						pietype = AtlasData.OUTCOME;
						break;
					default:
						break;

					}
					double sum = 0;
					if (items != null && items.moveToFirst()) {
						do {
							double catdata = items.getDouble(0);
							sum += catdata;
						} while (items.moveToNext());
						items.close();
					}
					if (sum > 0.0) {
						if (viewBehavior.getPieTypes().size() == 1) {
							Log.d("CatFragment", "cat: " + catid + " amount: " + sum);
							viewBehavior.addData(Integer.valueOf(catid), sum);
							String append = title.getText().toString();
							title.setText(append
									+ "\n"
									+ AtlasData.getCatName(this.getContext().getApplicationContext(),
											Integer.valueOf(catid)) + " (" + String.valueOf(sum) + ")");
						} else {
							all += sum;
						}
					}
				} while (nodes.moveToNext());
				viewBehavior.addData(Integer.valueOf(pietype), all);
			}
		}
	}
}
