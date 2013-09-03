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
import hu.fnf.devel.atlas.backend.TopLevelView;
import hu.fnf.devel.atlas.base.TopFragment;
import android.os.Bundle;
import android.util.Log;

public class OutcomeTrendFragment extends TopFragment {

	public OutcomeTrendFragment() {
		super();
		this.setName(AtlasData.TOUTCOME);
		Log.d("MonthlyFragment","constructor called.");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		TopLevelView chart = (TopLevelView) getView().findViewById(R.id.topview);
		
		loadChart(chart, AtlasData.OUTCOME);
		super.onActivityCreated(savedInstanceState);
	}
}