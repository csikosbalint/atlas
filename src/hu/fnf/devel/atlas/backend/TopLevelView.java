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

import hu.fnf.devel.atlas.base.AtlasView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;


public class TopLevelView extends AtlasView {
	List<Double> income = new ArrayList<Double>();
	List<Double> outcome = new ArrayList<Double>();
	private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
	RectF rectf = new RectF (getHeight()/4, getHeight()/4, getHeight()/4, getHeight()/4);
	Calendar month = new GregorianCalendar();
	public TopLevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d("TopLevelView", "TopLevelView(Context context, AttributeSet attrs, int defStyle))");
	}

	public TopLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d("TopLevelView", "TopLevelView(Context context, AttributeSet attrs)");
	}

	public TopLevelView(Context context) {
		super(context);
		Log.d("TopLevelView", "TopLevelView(Context context)");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		double max = 0;
		int size = 0;
		if ( income.size() > 0 && outcome.size() > 0 ) {
			max = 
			(Collections.max(income) > Collections.max(outcome)) ? Collections.max(income) : Collections.max(outcome);
			size = income.size();
		} else if ( income.size() > 0 ) {
			max = Collections.max(income);
			size = income.size();
		} else if ( outcome.size() > 0 ) {
			max = Collections.max(outcome);
			size = outcome.size();
		}
		
		int margin = 30;
		int inc = canvas.getHeight() / (size+1);

		int nullx = margin;
		int nully = canvas.getHeight() - margin;
		int max_x = canvas.getWidth() - margin;
		int max_y = 0;

		canvas.drawLine(nullx, max_y, nullx,nully+5, paint);
		canvas.drawLine(nullx-5, nully,	max_x, nully, paint);
		
		canvas.drawText("HUF", max_x/2, nully+margin/2, paint);
		for (int i = size; i > 0; i--) {
			int x = margin / 6;
			int y = canvas.getHeight() - margin/2 -inc * i;
			
			int width = 8;
			
			month.set(2013, i, 0);
			/*
			 * light out the act month
			 */
			if ( i == size ) {
				paint.setColor(Color.WHITE);
			} else {
				paint.setColor(Color.BLACK);
			}
			canvas.drawText(month.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()), x, y, paint);
			if (income.size() > 0 && income.get(i - 1) != 0.0 ) {
				int lenght = getPos(income.get(i - 1), max, max_x-margin);
				if (lenght > 2*width) {
					paint.setColor(Color.argb(255,50,	100,150));
					canvas.drawRect(nullx + width, y - width - width, nullx + lenght - width, y - width + width, paint);
					paint.setColor(Color.BLACK);
					canvas.drawText(String.valueOf(income.get(i - 1)), nullx + lenght / 2, y - width / 2, paint);
				}
			}
			if ( outcome.size() > 0 && outcome.get(i-1) != 0.0 ) {
				int lenght = getPos(outcome.get(i - 1), max, max_x-margin);
				lenght = getPos(outcome.get(i - 1), max, max_x);
				if (lenght > width*2 ) {
					paint.setColor(Color.argb(255,50,	150,100));
					canvas.drawRect(nullx + width, y + width - width, nullx + lenght - width, y + width + width, paint);
					paint.setColor(Color.BLACK);
					canvas.drawText(String.valueOf(outcome.get(i - 1)), nullx + lenght / 2, y + (width * 1.5F), paint);
				}
			}
			paint.setColor(Color.BLACK);
		}
	}

	public void addIncome(double income) {
		this.income.add(income);
	}
	public void addIncome(int i, double income) {
		this.income.add(i, income);
	}
	public void addOutcome(double outcome) {
		this.outcome.add(outcome);
	}
	public void addOutcome(int i, double outcome) {
		this.outcome.add(i, outcome);
	}
	private int getPos(double pos, double maxval, int maxpos) {
		if ( pos == 0.0 ) {
			return 0;
		}
		double a = pos/maxval;
		Log.d("","div: " + a);
		return (int)(a*maxpos);
	}
}