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

import hu.fnf.devel.atlas.AtlasData;
import hu.fnf.devel.atlas.IncomeTrendFragment;
import hu.fnf.devel.atlas.OutcomeTrendFragment;
import hu.fnf.devel.atlas.SummaryTrendFragment;
import hu.fnf.devel.atlas.base.AtlasFragment;
import hu.fnf.devel.atlas.base.AtlasFragmentAdapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ViewGroup;

public class TopFragmentAdapter extends AtlasFragmentAdapter {
	
	public TopFragmentAdapter(FragmentManager fm) {
		super(fm);
		type = AtlasData.TOPFRAGMENTADAPTER;
		contents = new ArrayList<AtlasFragment>();
		contents.add(AtlasData.TINCOME, new IncomeTrendFragment());
		contents.add(AtlasData.TSUMMARY, new SummaryTrendFragment());
		contents.add(AtlasData.TOUTCOME, new OutcomeTrendFragment());
	}

	@Override
	public CharSequence getPageTitle(int pos) {
		switch (pos) {
		case AtlasData.TINCOME:
			return (" INCOME ");
		case AtlasData.TSUMMARY:
			return (" TREND ");
		case AtlasData.TOUTCOME:
			return (" OUTCOME ");
		}
		return ("UNKNOWN");
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.d("CatFragmentAdapter", "instantiateItem");
		return super.instantiateItem(container, position);
	}
	
	@Override
	public Fragment getItem(int pos) {
		Log.d("TopFragmentAdapter", "getItem pos: " + pos);
		if ( 0 <= pos && pos < AtlasData.TOP_VIEW_COUNT && contents != null &&
				contents.get(pos) != null ) {
			// no change
		} else {
			Log.e("TopFragmentAdapter", "getItem pos: " + pos 
					+ " does not exists creating it");
			
		}
		return contents.get(pos);
	}

	@Override
	public int getCount() {
		return AtlasData.TOP_VIEW_COUNT;
	}

}
