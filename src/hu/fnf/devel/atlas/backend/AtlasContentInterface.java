package hu.fnf.devel.atlas.backend;

import hu.fnf.devel.atlas.backend.Category;
import hu.fnf.devel.atlas.backend.Data;
import hu.fnf.devel.atlas.backend.Transaction;

public interface AtlasContentInterface {
	/*
	 * own classes insert
	 */
	public void insert(Category newcat);
	public void insert(Data newdata);
	public void insert(Transaction newtrans);
	/*
	 * see android uri sting for more info
	 */
	public void query(String uri, String[] projection, String selection,String[] selectionArgs, String sortOrder);
	public void delete(String uri, String selection, String[] selectionArgs);
}
