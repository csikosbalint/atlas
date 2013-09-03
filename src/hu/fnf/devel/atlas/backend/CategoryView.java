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
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class CategoryView extends AtlasView {

	public CategoryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CategoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CategoryView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.viewBehavior.draw(canvas, this);
	}
}