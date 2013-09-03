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



public class Category {

	private String id;
	private String name;
	private String amount;
	private String depth;
	private String colorr;
	private String colorg;
	private String colorb;
	
	public Category(String id, String name, String amount, String depth,
			String colorr, String colorg, String colorb) {
		super();
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.depth = depth;
		this.colorr = colorr;
		this.colorg = colorg;
		this.colorb = colorb;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getAmount() {
		return amount;
	}
	public String getDepth() {
		return depth;
	}
	public String getColorr() {
		return colorr;
	}
	public String getColorg() {
		return colorg;
	}
	public String getColorb() {
		return colorb;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}

}
