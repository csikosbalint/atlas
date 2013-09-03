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
import hu.fnf.devel.atlas.IncomeFragment;
import hu.fnf.devel.atlas.OutcomeFragment;
import hu.fnf.devel.atlas.SummaryFragment;
import hu.fnf.devel.atlas.base.AtlasFragment;
import hu.fnf.devel.atlas.base.AtlasFragmentAdapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class CatFragmentAdapter extends AtlasFragmentAdapter {
	
	public CatFragmentAdapter(FragmentManager fm) {
		super(fm);
		type = AtlasData.CATFRAGMENTADAPTER;
		contents = new ArrayList<AtlasFragment>();
		contents.add(AtlasData.PINCOME, new IncomeFragment());
		contents.add(AtlasData.PSUMMARY, new SummaryFragment());
		contents.add(AtlasData.POUTCOME, new OutcomeFragment());
	}

	@Override
	public CharSequence getPageTitle(int pos) {
		switch (pos) {
		case AtlasData.PINCOME:
			return (" INCOME ");
		case AtlasData.PSUMMARY:
			return (" SUMMARY ");
		case AtlasData.POUTCOME:
			return (" OUTCOME ");
		}
		return ("UNKNOWN");
	}
	
	@Override
	public Fragment getItem(int pos) {
		Log.d("CatFragmentAdapter", "getItem pos: " + pos);
		if ( 0 <= pos && pos < AtlasData.CAT_VIEW_COUNT && contents != null &&
				contents.get(pos) != null ) {
			// no change
		} else {
			Log.e("CatFragmentAdapter", "getItem pos: " + pos 
					+ " does not exists creating it");
			
		}
		return contents.get(pos);
	}

	@Override
	public int getCount() {
		return AtlasData.CAT_VIEW_COUNT;
	}
	
}
