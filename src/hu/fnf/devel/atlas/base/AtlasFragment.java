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

import android.support.v4.app.Fragment;

public abstract class AtlasFragment extends Fragment {
	private int name;
	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public AtlasFragment() {
		super();
	}
	
}
