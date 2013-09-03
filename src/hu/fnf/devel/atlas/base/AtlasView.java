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

import hu.fnf.devel.atlas.ViewBehavior;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class AtlasView extends View {
	protected ViewBehavior viewBehavior;

	public AtlasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		viewBehavior.load();
	}

	public AtlasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		viewBehavior.load();
	}

	public AtlasView(Context context) {
		super(context);
		viewBehavior.load();
	}

	public void setViewBehavior(ViewBehavior viewBehavior) {
		this.viewBehavior = viewBehavior;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		viewBehavior.draw(canvas, this);
	}

}
