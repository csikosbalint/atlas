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

import java.util.ArrayList;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public abstract class AtlasFragmentAdapter extends FragmentStatePagerAdapter {
	
	protected int type;
	protected ArrayList<AtlasFragment> contents;

	public AtlasFragmentAdapter(FragmentManager fm) {
		super(fm);
		//Log.d("AtlasFragmentAdapter", "constructor called.");
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
		//Log.d("AtlasFragmentAdapter", "finishUpdate");
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		super.restoreState(state, loader);
		//Log.d("AtlasFragmentAdapter", "restoreState");
	}

	@Override
	public Parcelable saveState() {
		//Log.d("AtlasFragmentAdapter", "saveState");
		return super.saveState();
	}

	@Override
	public void startUpdate(ViewGroup container) {
		//Log.d("AtlasFragmentAdapter", "startUpdate");
		super.startUpdate(container);
	}

	public int getType() {
		return type;
	}

	public abstract Fragment getItem(int arg0);

	public abstract int getCount();
	
}
