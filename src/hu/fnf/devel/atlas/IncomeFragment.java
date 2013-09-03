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
package hu.fnf.devel.atlas;

import hu.fnf.devel.atlas.R;
import hu.fnf.devel.atlas.backend.CategoryView;
import hu.fnf.devel.atlas.base.CatFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class IncomeFragment extends CatFragment {

	public IncomeFragment() {
		super();
		this.setName(AtlasData.PINCOME);
		Log.d("IncomeFragment","constructor called.");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		CategoryView pie = (CategoryView) getView().findViewById(R.id.catview);
		((TextView) getView().findViewById(R.id.summaryIncome)).setText("INCOME");
		pie.setType(AtlasData.INCOME);
		loadPie(pie, AtlasData.INCOME);
		super.onActivityCreated(savedInstanceState);
	}
}
