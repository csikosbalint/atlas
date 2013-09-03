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

public class CategoryType {

	private String name;
	private int id;
	private double volume;

	public CategoryType(String name, int id, double volume) {
		this.name = name;
		this.id = id;
		this.volume = volume;
	}

	public String getName() {
		return name;
	}

	public int getTypeID() {
		return id;
	}

	public double getVolume() {
		return volume;
	}

}
