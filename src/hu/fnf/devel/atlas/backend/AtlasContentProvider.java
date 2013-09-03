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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

//import net.sqlcipher.database.DatabaseObjectNotClosedException;
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class AtlasContentProvider extends ContentProvider implements AtlasContentInterface {

	private static AtlasDbOpenHelper dbHelper;
	private static UriMatcher uriMatcher;

	private static final int TOPTASK= 0;
	private static final int TASKS	= 1;
	private static final int HASH 	= 2;
	private static final int TIP 	= 3;
	private static final int TYPES 	= 4;
	private static final int TASK 	= 5;
	private static final int CHILDS = 6;
	private static final int IGNORE = 7;
	private static final int SUMMA	= 8;
	private static final int DELTASK= 9;
	private static final int ALLCATS= 10;
	private static final int GUESS	= 11;
	private static final int DATA	= 12;
	private static final int TAG	= 13;
	private static final int ALLKIDS= 14;
	private static final int CAT	= 15;
	private static final int TOPGUESS= 16;
	private static final int TOPDATA= 17;
	private static final int DELDATA= 18;
	private static final int DELCAT	= 19;
	private static final int DELGUESS= 20;
	private static final int NODES	= 21;
	private static final int PARENT	= 22;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/toptask",	TOPTASK);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/tasks", 	TASKS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/tasks/tag",TAG);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/tasks/#",	TASK);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/hash", 	HASH);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/ignore/#",IGNORE);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONS  + 	"/delete/#",DELTASK);
		
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_TRANSACTIONTYPES +"/types"	,TYPES);
		
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA 	+			"/summa/#"	,SUMMA);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA 	+			"/topguess"	,TOPGUESS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA 	+			"/guess"	,GUESS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA 	+			"/topdata"	,TOPDATA);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA 	+			"/data"		,DATA);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA  + 			"/delete/#"	,DELDATA);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_DATA  + 			"/delguess/#",DELGUESS);
		
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES 	+ 	"/tip"		,TIP);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES 	+ 	"/cat/#"	,CAT);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES 	+ 	"/all"    	,ALLCATS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES 	+ 	"/all/#"    ,ALLKIDS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES 	+	"/childs/#"	,CHILDS);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES  	+ 	"/delete/#" ,DELCAT);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES  	+ 	"/nodes/#" 	,NODES);
		uriMatcher.addURI(AtlasData.DB_AUTHORITY, AtlasData.TABLE_CATEGORIES  	+ 	"/parent/#" ,PARENT);
	}
 
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (AtlasData.config.getString(AtlasData.CONFIG_DATABASE, "SQLite3").equalsIgnoreCase("SQLite3")) {
			Log.d("AtlasContentProvider", uri.getPath() + "&" + uri.getQuery());
			Cursor ret = null;
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			String sql = "";
			String parentid = "";
			String other = "";
			String limit = null;

			switch (uriMatcher.match(uri)) {
			case TASKS:
			case TOPTASK:
				/*
				 * select * from transactions where status=0 order by amount desc;
				 */
				SQLiteDatabase toptask = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_TRANSACTIONS);
				queryBuilder.appendWhere(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=0");
				sortOrder = AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE] + " desc";

				limit = null;
				if (uriMatcher.match(uri) == TOPTASK) {
					limit = "1";
				}
				ret = queryBuilder.query(toptask, projection, null, null, null, null, sortOrder, limit);
				break;
			case TAG:
				/*
				 * select * from transactions where _to='BUDAPEST CBA CORV' or _from='BUDAPEST CBA CORV' and status=0;
				 */
				SQLiteDatabase tagdb = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_TRANSACTIONS);
				String tag = uri.getQueryParameter(AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG]);
				String app = AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TO] + "='" + tag + "' or "
						+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_FROM] + "='" + tag + "' and "
						+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=0";
				queryBuilder.appendWhere(app);

				ret = queryBuilder.query(tagdb, projection, null, null, null, null, null, null);
				break;
			case TASK:
				/*
				 * select * from transactions where status=0 and _id='id';
				 */
				SQLiteDatabase task = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_TRANSACTIONS);
				queryBuilder.appendWhere(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=0");
				queryBuilder.appendWhere(" AND " + AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "="
						+ uri.getPathSegments().get(uri.getPathSegments().size() - 1));
				ret = queryBuilder.query(task, projection, null, null, null, null, null);
				break;
			case HASH:
				/*
				 * select * from transactions where hash='hash'
				 */
				SQLiteDatabase hash = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_TRANSACTIONS);
				queryBuilder.appendWhere(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH] + " = "
						+ uri.getQueryParameter(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH]));
				Log.d("AtlasContentProvider",
						"query: " + queryBuilder.buildQuery(projection, null, null, null, null, null, limit));
				ret = queryBuilder.query(hash, projection, selection, selectionArgs, null, null, null);
				break;
			case TYPES:
				/*
				 * select * from transactiontypes
				 */
				SQLiteDatabase types = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_TRANSACTIONTYPES);
				Log.d("AtlasContentProvider",
						"query: " + queryBuilder.buildQuery(projection, null, null, null, null, null, limit));
				ret = queryBuilder.query(types, projection, null, null, null, null, null);
				break;
			case GUESS:
			case TOPGUESS:
				/*
				 * select * from data where indata=0 order by date desc;
				 */
				SQLiteDatabase auto = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_DATA);
				queryBuilder.appendWhere(AtlasData.DATA_COLUMNS[AtlasData.DATA_STATUS] + "=0");
				sortOrder = AtlasData.DATA_COLUMNS[AtlasData.DATA_ID] + " desc";
				
				limit = null;
				if (uriMatcher.match(uri) == TOPGUESS) {
					limit = "1";
				}
				Log.d("AtlasContentProvider",
						"query: " + queryBuilder.buildQuery(projection, null, null, null, null, sortOrder, limit));
				ret = queryBuilder.query(auto, projection, null, null, null, null, sortOrder, limit);
				Log.d("AtlasContentProvider", "count: " + ret.getCount());
				break;
			case DATA:
			case TOPDATA:
				/*
				 * select * from data where indata=1 order by amount desc;
				 */
				SQLiteDatabase data = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_DATA);
				queryBuilder.appendWhere(AtlasData.DATA_COLUMNS[AtlasData.DATA_STATUS] + "=1");
				sortOrder = AtlasData.DATA_COLUMNS[AtlasData.DATA_ID] + " desc";

				limit = null;
				if (uriMatcher.match(uri) == TOPDATA) {
					limit = "1";
				}
				Log.d("AtlasContentProvider",
						"query: " + queryBuilder.buildQuery(projection, null, null, null, null, sortOrder, limit));
				ret = queryBuilder.query(data, projection, null, null, null, null, sortOrder, limit);
				break;
			case NODES:
				/*
				 * all the nodes under the specified parentid
				 */
				SQLiteDatabase nodes = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				parentid = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				for (int i = 1; i <= AtlasData.MAX_CAT_DEPTH; i++) {
					other += AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
							+ " between " + parentid + "*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + " and (" + parentid
							+ "+1)*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + "-1 ";
					if (i != AtlasData.MAX_CAT_DEPTH) {
						other += " or ";
					}
				}
				sql = "select * from " + AtlasData.TABLE_CATEGORIES + " where " 
						+ AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
						+ "=" + parentid + " or " + other;

				Log.d("AtlasContentProvider", sql);
				try {
					ret = nodes.rawQuery(sql, null);
				} catch ( Exception e ) {
					Log.w("AtlasContentProvider", 
							"Application did not close the cursor or database object that was opened here");
				}
				break;
			case SUMMA:
			/*
			 * see in documentation page 30 
			 */
				// CHANGED: it returns only the summa of the amount from data only for the given node
				SQLiteDatabase summa = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				parentid = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				String reqmonth = uri.getQueryParameter(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE]);
				Calendar nextcal = Calendar.getInstance();
				nextcal.setTimeInMillis(Long.valueOf(reqmonth) * 1000L);
				nextcal.add(Calendar.MONTH, 1);
				String nextmonth = String.valueOf(nextcal.getTimeInMillis() / 1000L);
				
				sql = "select sum(" + AtlasData.TABLE_DATA + "." + AtlasData.DATA_COLUMNS[AtlasData.DATA_AMOUNT] + ") from "
						+ AtlasData.TABLE_DATA + "," + AtlasData.TABLE_TRANSACTIONS + " where "
						+ AtlasData.TABLE_DATA + "." + AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] + "="
						+ AtlasData.TABLE_TRANSACTIONS + "." + AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + " and "
						+ AtlasData.TABLE_DATA + "." + AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID] + "="
						+ parentid+ " and "
						+ AtlasData.TABLE_TRANSACTIONS + "."
						+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE] + " between "
						// TODO: get year and month, and count the unix time
						// interval
						+ reqmonth + " and " + nextmonth;
//						// TODO: this must be reconstructed;
//				for (int i = 1; i <= AtlasData.MAX_CAT_DEPTH; i++) {
//					other += AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
//							+ " between " + parentid + "*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + " and (" + parentid
//							+ "+1)*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + "-1 ";
//					if (i != AtlasData.MAX_CAT_DEPTH) {
//						other += " or ";
//					}
//				}
//				String reqmonth = uri.getQueryParameter(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE]);
//				Calendar nextcal = Calendar.getInstance();
//				nextcal.setTimeInMillis(Long.valueOf(reqmonth) * 1000L);
//				nextcal.add(Calendar.MONTH, 1);
//				String nextmonth = String.valueOf(nextcal.getTimeInMillis() / 1000L);
//
//				sql = "select " + AtlasData.TABLE_CATEGORIES + ".*,sum(" + AtlasData.TABLE_DATA + "."
//						+ AtlasData.DATA_COLUMNS[AtlasData.DATA_AMOUNT] + ")" + " from "
//						+ AtlasData.TABLE_TRANSACTIONS + "," + AtlasData.TABLE_DATA + "," + AtlasData.TABLE_CATEGORIES
//						+ " where " + AtlasData.TABLE_DATA + "." + AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID]
//						+ "=" + AtlasData.TABLE_TRANSACTIONS + "."
//						+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + " and " + AtlasData.TABLE_DATA
//						+ "." + AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID] + "=" + AtlasData.TABLE_CATEGORIES
//						+ "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + " and "
//						+ AtlasData.TABLE_TRANSACTIONS + "."
//						+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE] + " between "
//						// TODO: get year and month, and count the unix time
//						// interval
//						+ reqmonth + " and " + nextmonth
//						// TODO: this must be reconstructed
//						+ " and (" + AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
//								+ "=" + parentid + " or " + other + ")" + " group by " + AtlasData.TABLE_DATA + "."
//						+ AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID];
//
				Log.d("AtlasContentProvider", sql);
				try {
					ret = summa.rawQuery(sql, null);
				} catch ( Exception e ) {
					Log.w("AtlasContentProvider", 
							"Application did not close the cursor or database object that was opened here");
				}
				break;
			case TIP:
				/*	
				 * select *, count(_id) as c from ( select categories.* from data,categories
				 * where categories._id=data.catid and tag='BUDAPEST T?VSZ?ML' union all
				 * select * from categories ) where _id!= 1 and _id!= 2 group by _id order by c desc;
				 */
				sql = "select " + "*,count("+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] 
						+ ") as c from ( select " + AtlasData.TABLE_CATEGORIES+ ".* from " 
						+ AtlasData.TABLE_DATA + "," + AtlasData.TABLE_CATEGORIES + " where " 
					+ AtlasData.TABLE_CATEGORIES + "." 
						+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "="
					+ AtlasData.TABLE_DATA + "."
						+ AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID] + " and tag='" 
						+ uri.getQueryParameter(AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG])
						+"' union all select * from " + AtlasData.TABLE_CATEGORIES + ") where "
							+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] 
									+ "!=" + AtlasData.INCOME + " and "
							+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] 
									+ "!=" + AtlasData.OUTCOME + " group by "
					+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + " order by c desc";

				SQLiteDatabase guess = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				Log.d("AtlasContentProvider", sql);
				ret = guess.rawQuery(sql, null);

				break;
			case ALLKIDS:
				/*
				 * select * from categories where 
				 * 		   _id between parentid*10 and (parentid+1)*10-1
				 * 		or _id between parentid*100 and (parentid+1)*100-1
				 * 		or _id between parentid*1000 and (parentid+1)*1000-1
				 * 		...
			 	*/
				SQLiteDatabase kids = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				parentid = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				queryBuilder.setTables(AtlasData.TABLE_CATEGORIES);
				other = null;
				for (int i = 1; i <= AtlasData.MAX_CAT_DEPTH; i++) {
					other += AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
							+ " between " + parentid + "*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + " and (" + parentid
							+ "+1)*" + pow(AtlasData.MAX_CAT_WIDTH + 1, i) + "-1 ";
					if (i != AtlasData.MAX_CAT_DEPTH) {
						other += " or ";
					}
				}

				queryBuilder.appendWhere(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + other + ";");
				sortOrder = AtlasData.CATEGORIES_COLUMNS[AtlasData.TRANSACTIONS_ID] + " asc";

				Log.d("AtlasContentProvider", queryBuilder.buildQuery(projection, null, null, null, null, null, null));

				ret = queryBuilder.query(kids, projection, null, null, null, null, sortOrder, null);
				break;
			case CHILDS:
				/*
				 *  only one level childs
				 */
			/*
			 * select * from categories where _id between parentid*10 and (parentid+1)*10-1
			 */
				SQLiteDatabase childs = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				parentid = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				queryBuilder.setTables(AtlasData.TABLE_CATEGORIES);

				other = AtlasData.TABLE_CATEGORIES + "." + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]
						+ " between " + parentid + "*10 and (" + parentid + "+1)*10-1 ";

				queryBuilder.appendWhere(other);

				Log.d("AtlasContentProvider", queryBuilder.buildQuery(projection, null, null, null, null, null, null));

				ret = queryBuilder.query(childs, projection, null, null, null, null, null, null);
				break;

			case ALLCATS:
				SQLiteDatabase allcats = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				queryBuilder.setTables(AtlasData.TABLE_CATEGORIES);
				ret = queryBuilder.query(allcats, projection, null, null, null, null, null);
				break;
			case CAT:
				SQLiteDatabase cat = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
				String id = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				queryBuilder.setTables(AtlasData.TABLE_CATEGORIES);
				String where = AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "=" + id;
				queryBuilder.appendWhere(where);
				Log.d("AtlasContentProvider", queryBuilder.buildQuery(projection, null, null, null, null, null, null));
				ret = queryBuilder.query(cat, projection, null, null, null, null, null, null);
				break;
			default:
				Log.e("AtlasContentProvider", "Not implemented path requred! " + uri.getPath());
				break;
			}
			return ret;
		} else {

			Log.e("", AtlasData.config.getString(AtlasData.CONFIG_DATABASE, "SQLite3")
					+ " database not yet implemented");
			return null;
		}
	}

	private String pow(int base, int power) {
		String ret = "";
		if ( power == 0) {
			return "1";
		}
		for (int i = 0; i < power; i++) {
			ret += "10";
			if ( i != power-1) {
				ret += "*";
			}
		}
		return ret	;
	}

	public void insert(Transaction tr) {
		ContentValues values = new ContentValues();
		SQLiteDatabase database = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
		
		double a = AtlasData.getLocaleAmount(tr.getAmount());
		String date = String.valueOf(AtlasData.getIntDateFromString(tr.getDate()));
		Log.d("AtlasContentProvider", "inserting transaction: "
				+ a + " "
				+ tr.getFrom() + " "
				+ tr.getTo() + " " 
				+ date + " "
				+ tr.getTypeid() + " "
				+ tr.getSmsid() + " " 
				+ tr.getHash());
		if ( tr.getId() != 0 ) {
			// manual input workaround
			values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID], tr.getId());
			Log.d("AtlasContentProvider", "inserting transaction id: " + tr.getId());
		}

		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_AMOUNT], a);
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_FROM], tr.getFrom());
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TO], tr.getTo());
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE], date);
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TYPEID], Integer.valueOf(tr.getTypeid()));
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_SMSID], Integer.valueOf(tr.getSmsid()));
		values.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH], Integer.valueOf(tr.getHash()));

		database.insert(AtlasData.TABLE_TRANSACTIONS, null, values);
		database.close();
	}
	
	public void insert(Data da) {
		ContentValues values = new ContentValues();
		SQLiteDatabase database = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
		Log.d("AtlasContentProvider", "inserting data: "
				+ da.getTag() + " "
				+ da.getTransid() + " "
				+ da.getCatid() + " " 
				+ da.getAmount() + " "
				+ da.getFinalized());
		
		values.put(AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG], da.getTag());
		values.put(AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID], da.getTransid());
		values.put(AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID], da.getCatid());
		values.put(AtlasData.DATA_COLUMNS[AtlasData.DATA_AMOUNT], 	da.getAmount());
		values.put(AtlasData.DATA_COLUMNS[AtlasData.DATA_STATUS], da.getFinalized());
		
		database.insert(AtlasData.TABLE_DATA, null, values);
		database.close();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d("AtlasContentProvider", uri.getPath() + "&" + uri.getQuery());
		SQLiteDatabase database = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
		String id = uri.getPathSegments().get(uri.getPathSegments().size()-1);
		String table = uri.getPathSegments().get(0);
		switch (uriMatcher.match(uri)) {
		case DELTASK:		
			database.delete(table, AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "=" + id, null);
			break;
		case DELCAT:
			database.delete(table, AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "=" + id, null);
			break;
		case DELDATA:
		case DELGUESS:
			database.delete(table, AtlasData.DATA_COLUMNS[AtlasData.DATA_ID] + "=" + id, null);
			break;
		}
		database.close();
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new AtlasDbOpenHelper(getContext());
		return true;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d("AtlasContentProvider", uri.getPath() + "&" + uri.getQuery());
		SQLiteDatabase database = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
		String id = uri.getPathSegments().get(uri.getPathSegments().size()-1);
		Log.d("AtlasContentProvider", "ignoring id " + id);
		switch (uriMatcher.match(uri)) {
		case IGNORE:
			ContentValues ignored = new ContentValues();
			ignored.put(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS], AtlasData.IGNORED);
			
			database.update(AtlasData.TABLE_TRANSACTIONS, ignored, 
					AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "=" + id, null);
			break;
		case PARENT:
			ContentValues parent = new ContentValues();
			parent.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF], AtlasData.FALSE);
			
			database.update(AtlasData.TABLE_CATEGORIES, parent, 
					AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "=" + id, null);
			break;
		}
		database.close();
		return 0;
	}

	public void insert(Category newcat) {
		ContentValues values = new ContentValues();
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID], newcat.getId());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_NAME], newcat.getName());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_AMOUNT], 	newcat.getAmount());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_DEPTH],  	newcat.getDepth());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF],		AtlasData.TRUE);
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORR], 	newcat.getColorr());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORG], 	newcat.getColorg());
		values.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORB], 	newcat.getColorb());
		
		Log.d("AtlasContentProvider",
				"inserting " + values.getAsString(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_NAME]));
		
		SQLiteDatabase database = dbHelper.getWritableDatabase(AtlasData.getDBPassword());
		database.insert(AtlasData.TABLE_CATEGORIES, null, values);
		database.close();
	}

	public static void cloneDataForDebug(Cursor sms_result) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			File dbfile = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS + File.separator + AtlasData.DATABASE_NAME);
			android.database.sqlite.SQLiteDatabase db =
					android.database.sqlite.SQLiteDatabase
						.openOrCreateDatabase(dbfile, null);
			
			String create = "create table atlassms ( _id INTEGER, address TEXT, body TEXT, date TEXT )";
			db.beginTransaction();			
			db.execSQL(create);
			db.setTransactionSuccessful();
			db.endTransaction();
			
			Log.i("AtlasDaemonService","Its open? "  + db.isOpen());
			if ( sms_result.moveToFirst() ) {
				do {
					int smsaddress_ind = sms_result.getColumnIndex("address");
					int smsbody_ind = sms_result.getColumnIndex("body");
					int smsdate_ind = sms_result.getColumnIndex("date");
					int smsid_ind = sms_result.getColumnIndex("_id");
					String number = "0036303444481";
					if (sms_result.getString(smsaddress_ind).equals(number)) {
					String insert = 
							"insert into atlassms ( _id, address, body, date ) values ("
							+ sms_result.getInt(smsid_ind) + ", '"
							+ sms_result.getString(smsaddress_ind) + "', '"
							+ sms_result.getString(smsbody_ind).replace('\'', ' ') + "', '"
							+ sms_result.getString(smsdate_ind) + "');";
					Log.i("AtlasDaemonService", insert);
					db.getVersion();
					db.beginTransaction();
					db.execSQL(insert);
					db.setTransactionSuccessful();
					db.endTransaction();
					}
				} while ( sms_result.moveToNext());
			}
			db.close();
			Log.i("AtlasDaemonService", "inserted: " + recvDataForDebug().getCount());
		} else {
			Log.e("AtlasDaemonService", "cannot get sdcard mount point");
		}
		sms_result.moveToFirst();
	}

	public static Cursor recvDataForDebug() {
		File dbfile = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS + File.separator + AtlasData.TABLE_TRANSACTIONS);
		
		
		android.database.sqlite.SQLiteDatabase db =
				android.database.sqlite.SQLiteDatabase
					.openOrCreateDatabase(dbfile, null);
		Cursor ret = db.rawQuery("select * from atlassms", null);
		Log.d("", "counted: " + ret.getCount());
		return ret;
	}

	public static boolean isDataForDebug() {
		File dbfile = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS + File.separator + AtlasData.TABLE_TRANSACTIONS);
		return dbfile.exists();
	}

	@Override
	public void query(String uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		query(Uri.parse(uri), projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public void delete(String uri, String selection, String[] selectionArgs) {
		delete(Uri.parse(uri), selection, selectionArgs);
	}
	
}
