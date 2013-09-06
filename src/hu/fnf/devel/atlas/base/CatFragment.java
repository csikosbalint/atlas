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
import hu.fnf.devel.atlas.backend.CategoryView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class CatFragment extends AtlasFragment {
	protected CategoryView pie;

	public CatFragment() {
		super();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		pie = (CategoryView) getView().findViewById(R.id.catview);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("CatFragment", "onCreateView called.");
		View ret = inflater.inflate(R.layout.fragment_category, container,
				false);

		TextView reqm = (TextView) ret.findViewById(R.id.reqmonth);
		reqm.setText(AtlasData.config.getString(AtlasData.CONFIG_DATE,
				"1900.01"));

		return ret;
	}	
}
