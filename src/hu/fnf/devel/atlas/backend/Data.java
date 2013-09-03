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

public class Data {

	private String tag;
	private String transid;
	private String catid;
	private String amount;
	private String finalized;
	
	public Data(String tag, String transid, String catid, String amount,String finalized) {
		this.tag = tag;
		this.transid = transid;
		this.catid = catid;
		this.amount = amount;
		this.finalized = finalized;
	}

	public String getTag() {
		return tag;
	}
	public String getTransid() {
		return transid;
	}
	public String getCatid() {
		return catid;
	}

	public String getAmount() {
		if ( amount == null || amount.length() == 0) {
			return "0";
		}
		return amount;
	}

	public String getFinalized() {
		if ( finalized == null || finalized.length() == 0) {
			return "0";
		}
		return finalized;
	};
	

}
