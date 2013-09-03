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

public class Transaction {
	int id = 0;
	String amount;
	String from;
	String to;
	String date;
	String typeid;
	String smsid;
	String hash;
	// TODO: DB tipus sajat attributmai
	/*
	 * Jelenleg minden attributum string, s a db-ben is ezek mennek
	 * de természetesen jobb lenne, ha a db-be és onnan csak megszorított
	 * adatípusok kerülhetnének be és ki.
	 */
	public Transaction(String amount, String from, String to, String date,
			String typeid, String smsid, String hash) {
		super();
		if ( amount == null ) { 
			this.amount = AtlasData.getDefault(AtlasData.TRANSACTIONS_AMOUNT); 
		} else {
			this.amount = amount;
		}
		if ( from == null ) { 
			this.from = AtlasData.getDefault(AtlasData.TRANSACTIONS_FROM);
		} else {
			this.from = from;
		}
		if ( to == null ) { 
			this.to = AtlasData.getDefault(AtlasData.TRANSACTIONS_TO); 
		} else {
			this.to = to;
		}
		if ( date == null ) { 
			this.date = AtlasData.getDefault(AtlasData.TRANSACTIONS_DATE); 
		} else {
			this.date = date;
		}
		if ( typeid == null ) { 
			this.typeid = AtlasData.getDefault(AtlasData.TRANSACTIONS_TYPEID); 
		} else {
			this.typeid = typeid;
		}
		if ( smsid == null ) { 
			this.smsid = AtlasData.getDefault(AtlasData.TRANSACTIONS_SMSID); 
		} else {
			this.smsid = smsid;
		}
		if ( hash == null ) { 
			this.hash = AtlasData.getDefault(AtlasData.TRANSACTIONS_HASH); 
		} else {
			this.hash = hash;
		}
	}
	
	public Transaction(int id, String amount, String from, String to, String date,
			String typeid, String smsid, String hash) {
		this(amount,from,to,date,typeid,smsid,hash);
		this.id = id;
	}

	public String getAmount() {
		return amount;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getDate() {
		return date;
	}

	public String getTypeid() {
		return typeid;
	}

	public String getSmsid() {
		return smsid;
	}

	public String getHash() {
		return hash;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public int getId() {
		return id;
	}
	
	
}
 
