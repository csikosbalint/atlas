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

import hu.fnf.devel.atlas.base.CatFragment;
import android.os.Bundle;

public class OutcomeFragment extends CatFragment {
	
	public OutcomeFragment() {
		super();
		this.setName(AtlasData.POUTCOME);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pie.setViewBehavior(new OutViewBehavior(this, pie));
	}
}
