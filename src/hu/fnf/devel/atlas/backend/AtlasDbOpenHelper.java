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

//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteOpenHelper;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import hu.fnf.devel.atlas.AtlasData;
import hu.fnf.devel.atlas.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


import android.util.Log;

public class AtlasDbOpenHelper extends SQLiteOpenHelper {
	Context context;
	
	private static final String CATEGORIES_CREATE=
			"CREATE TABLE " + AtlasData.TABLE_CATEGORIES + "("
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]		+ " INTEGER PRIMARY KEY, "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData. CATEGORIES_NAME]	+ " TEXT NOT null,"
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_AMOUNT]	+ " REAL DEFAULT(0), "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_DEPTH]	+ " INTEGER NOT null " 
			+ " CHECK ( depth<="	+ AtlasData.MAX_CAT_DEPTH + "), "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF]	+ " INTEGER, "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORR]	+ " INTEGER NOT null, "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORG]	+ " INTEGER NOT null, "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORB]	+ " INTEGER NOT null );";
	
	private static final String TRANSACTIONS_CREATE=
			"CREATE TABLE " + AtlasData.TABLE_TRANSACTIONS + "("
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] 	  	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_AMOUNT] 	+ " REAL NOT null, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_FROM]   	+ " TEXT NOT null, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TO] 	  	+ " TEXT NOT null, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE]   	+ " INTEGER, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TYPEID] 	+ " INTEGER, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_SMSID]  	+ " INTEGER, "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH]   	+ " INTEGER DEFAULT(0), "
			+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS]		+ " INTEGER DEFAULT(0), "
			+ " FOREIGN KEY(" +  AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TYPEID] + ")"
			+ " REFERENCES " + AtlasData.TABLE_TRANSACTIONTYPES + "("
				+  AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_ID] + "));";
	
	private static final String TRANSACTIONTYPES_CREATE=
			"CREATE TABLE " + AtlasData.TABLE_TRANSACTIONTYPES + "("
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_ID]	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_NAME] 			+ " TEXT NOT null, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN]   		+ " TEXT, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_AMOUNT]	+ " TEXT, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_FROM]	+ " TEXT, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_TO]		+ " TEXT, "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_DATE]	+ " TEXT);";
	
	private static final String DATA_CREATE=
			"CREATE TABLE " + AtlasData.TABLE_DATA + "("
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_ID] 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG] 			+ " TEXT NOT null, "
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID]  + " INTEGER NOT null, "
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID]	  	+ " INTEGER NOT null, "
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_AMOUNT]			+ " REAL NOT null, "
			+ AtlasData.DATA_COLUMNS[AtlasData.DATA_STATUS]	  	+ " INTEGER DEFAULT(0), "
			+ " UNIQUE ( "
				+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG] + ", "
		        + AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] + ", "
		        + AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID] + " ), "
		    + " FOREIGN KEY("  + AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] 
		    		+ ") REFERENCES " +  AtlasData.TABLE_TRANSACTIONS 
		    		+ "(" + AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + ") "
		    + " FOREIGN KEY("  + AtlasData.DATA_COLUMNS[AtlasData.DATA_CATEGORYID] 
		    		+ ") REFERENCES " +  AtlasData.TABLE_CATEGORIES
		    		+ "(" + AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "));";
	private static final String CREATE_CATEGORY_INSERT =
			"CREATE TRIGGER " + AtlasData.TRIGGER_CATEGORY_INSERT + " AFTER INSERT ON " + AtlasData.TABLE_CATEGORIES
			+ " BEGIN"
				+ " UPDATE " + AtlasData.TABLE_TRANSACTIONS + " SET " 
				+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=1"
					+ " WHERE "
					+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "=new."
					+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] + ";"
			+ " END;";
	private static final String CREATE_STATUS_INSERT =
			
			"CREATE TRIGGER " + AtlasData.TRIGGER_STATUS_INSERT + " AFTER INSERT ON " + AtlasData.TABLE_DATA
			+ " BEGIN"
				+ " UPDATE " + AtlasData.TABLE_TRANSACTIONS + " SET " 
				+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=1"
					+ " WHERE "
					+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "=new."
					+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] + ";"
			+ " END;";
	private static final String CREATE_STATUS_DELETE=
			
			"CREATE TRIGGER " + AtlasData.TRIGGER_STATUS_DELETE + " AFTER DELETE ON " + AtlasData.TABLE_DATA
			+ " BEGIN"
				+ " UPDATE " + AtlasData.TABLE_TRANSACTIONS + " SET " 
				+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_STATUS] + "=0"
					+ " WHERE "
					+ AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_ID] + "=old."
					+ AtlasData.DATA_COLUMNS[AtlasData.DATA_TRANSACTIONID] + ";"
			+ " END;";
	
	public String createAncientCategory(int id, String name) {
		String ANCIENT_CATEGORIES_CREATE=
			"INSERT INTO " + AtlasData.TABLE_CATEGORIES + "("
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_NAME]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_DEPTH] + ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF] + ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORR]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORG]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORB]	+ ") VALUES ('" 
			+ id + "','" + name +"'," + AtlasData.MAX_CAT_DEPTH + ","
			+ AtlasData.TRUE + ","
			+ AtlasData.MAX_CAT_DEPTH + AtlasData.CATEGORIES_COLOR[id-1] + ");";
		return ANCIENT_CATEGORIES_CREATE;
	}
	
	public String createCategory(String name, int parentid, SQLiteDatabase db) {
		int id,depth;
		ContentValues leaf = new ContentValues();
		leaf.put(AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF], false);
		db.update(AtlasData.TABLE_CATEGORIES,leaf,
				AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID] + "=" + parentid, null);
		Cursor parent = db.query(true, AtlasData.TABLE_CATEGORIES, AtlasData.CATEGORIES_COLUMNS,
				"_id="+String.valueOf(parentid),
				null, null, null, null, null);
		Cursor child = db.query(false, AtlasData.TABLE_CATEGORIES, AtlasData.CATEGORIES_COLUMNS,
				"_id between " + String.valueOf(parentid*10) + " and " + String.valueOf(((parentid+1)*10)-1),
				null, null, null, null, null);
		id = parentid * ( Integer.valueOf(AtlasData.MAX_CAT_WIDTH) + 1 ) + child.getCount();
		depth = AtlasData.MAX_CAT_DEPTH + 1 - String.valueOf(id).length();

		String CATEGORIES_CREATE=
			"INSERT INTO " + AtlasData.TABLE_CATEGORIES + "("
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_ID]		+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_NAME]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_DEPTH]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_LEAF] 	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORR]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORG]	+ ", "
			+ AtlasData.CATEGORIES_COLUMNS[AtlasData.CATEGORIES_COLORB]	+ ") VALUES ("
			+ id + ",'" + name +"'," + depth + "," + AtlasData.TRUE 
			+ "," + AtlasData.getColor(parent) + ");";
		Log.d("createAncientCategory", CATEGORIES_CREATE);
		return CATEGORIES_CREATE;
	}
	
	public String createAncientTransactionType(int id, String name) {	
		String pattern = null;
		String pattern_amount = null;
		String pattern_from = "MyAccount";
		String pattern_to = null;
		String pattern_date = null;
		String bank = AtlasData.config.getString(AtlasData.CONFIG_BANK, "Erste");
		if (bank.equalsIgnoreCase("erste") || bank.equalsIgnoreCase("erste-debug") ) {
			switch (id) {

			case AtlasData.CARD_PAYMENT:
				pattern = "Vàsàrlàs:";
				pattern_amount = "Vàsàrlàs: | HUF";
				pattern_date = "Idö: | Hely";
				pattern_to = "Hely: | Uj";
				break;
			case AtlasData.CARD_CASHWITHDRAWAL:
				pattern = "Kpfelvét";
				pattern_amount = "Kpfelvét: | HUF";
				pattern_date = "Idö: |Hely";
				pattern_to = "Hely: |Uj";
				break;
			case AtlasData.TRANSFER_REPEATING:
				pattern = "àllandò megbìzàs";
				pattern_amount = "összeg | HUF";
				pattern_date = "";
				pattern_to = "Kedvezményezett |,";
				break;
			case AtlasData.TRANSFER_INCOME:
				// ?
				break;
			case AtlasData.TRANSFER_OUTCOME:
				pattern = "forintàtutalàs";
				pattern_amount = "összeg | HUF";
				pattern_date = "";
				pattern_to = "Kedvezményezett |,";
				break;
			default:

				break;
			}
		} else {
			// TODO: mas bakokat es 'custom' funkcio
			/*
			 * Mas bankokat mint preset is kell a db -ben
			 * tarolni, valamint lehetoseget kell adni a 
			 * custom banki üzenet feldolgozásához.
			 * Ez egy nagyobb feladat. El kell gondolkodni a
			 * banki uzenetek megváltoztatasanak esélyén is.
			 */
			Log.e("AtlasDbOpenHelper","other banks not implmented yet");
		}
		String ANCIENT_TRANSACTIONS_CREATE=
			"INSERT INTO " + AtlasData.TABLE_TRANSACTIONTYPES + "("
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_ID]	  		+ ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_NAME]	  		+ ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN]		+ ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_AMOUNT]+ ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_FROM] + ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_TO] 	+ ", "
			+ AtlasData.TRANSACTIONTYPES_COLUMNS[AtlasData.TRANSACTIONTYPES_PATTERN_DATE] + ") VALUES (" 
			+ id + ", '"
			+ name 				+ "', '"
			+ pattern 			+ "', '"
			+ pattern_amount 	+ "', '"
			+ pattern_from		+ "', '"
			+ pattern_to		+ "', '"
			+ pattern_date		+ "' );";
		Log.d("createAncientCategory", ANCIENT_TRANSACTIONS_CREATE);
		return ANCIENT_TRANSACTIONS_CREATE;
	}

	public AtlasDbOpenHelper(Context context) {
		super(context, AtlasData.DATABASE_NAME, null, AtlasData.DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* database */
		Log.i("Data", "Creating databases...");
		Log.d("AtlasDbOpenHelper", CATEGORIES_CREATE);
		Log.d("AtlasDbOpenHelper", TRANSACTIONS_CREATE);
		Log.d("AtlasDbOpenHelper", TRANSACTIONTYPES_CREATE);
		Log.d("AtlasDbOpenHelper", DATA_CREATE);
		
		db.execSQL(CATEGORIES_CREATE);
		db.execSQL(TRANSACTIONS_CREATE);
		db.execSQL(TRANSACTIONTYPES_CREATE);
		db.execSQL(DATA_CREATE);
		
		/* trigger */
		Log.i("Data", "Creating triggers...");
		Log.d("AtlasDbOpenHelper", CREATE_STATUS_INSERT);
		Log.d("AtlasDbOpenHelper", CREATE_STATUS_DELETE);
		
		db.execSQL(CREATE_STATUS_INSERT);
		db.execSQL(CREATE_STATUS_DELETE);

		/* insert */
		Log.i("Data", "Inserting...");
		db.execSQL(createAncientCategory(AtlasData.INCOME, "INCOME"));
		db.execSQL(createAncientCategory(AtlasData.OUTCOME, "OUTCOME"));
		
		db.execSQL(createCategory(context.getString(R.string.food),	AtlasData.OUTCOME,db));
		db.execSQL(createCategory(context.getString(R.string.fuel), AtlasData.OUTCOME,db));
		db.execSQL(createCategory(context.getString(R.string.fun), AtlasData.OUTCOME,db));
		db.execSQL(createCategory(context.getString(R.string.home), AtlasData.OUTCOME,db));
		
		db.execSQL(createCategory(context.getString(R.string.credit), AtlasData.INCOME,db));
		db.execSQL(createCategory(context.getString(R.string.salary), AtlasData.INCOME,db));
		db.execSQL(createCategory(context.getString(R.string.gift), AtlasData.INCOME,db));
		
		db.execSQL(createAncientTransactionType(AtlasData.CARD_CASHWITHDRAWAL, 	"CARD_CASHWITHDRAWAL"));
		db.execSQL(createAncientTransactionType(AtlasData.CARD_PAYMENT, 		"CARD_PAYMENT"));
		db.execSQL(createAncientTransactionType(AtlasData.TRANSFER_REPEATING, 	"TRANSFER_REPEATING"));
		db.execSQL(createAncientTransactionType(AtlasData.TRANSFER_OUTCOME, 	"TRANSFER_OUTCOME"));
		db.execSQL(createAncientTransactionType(AtlasData.TRANSFER_INCOME,		"TRANSFER_INCOME"));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("onUpgrade",
				"Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + AtlasData.TABLE_DATA);
		db.execSQL("DROP TABLE IF EXISTS " + AtlasData.TABLE_TRANSACTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + AtlasData.TABLE_TRANSACTIONTYPES);
		db.execSQL("DROP TABLE IF EXISTS " + AtlasData.TABLE_CATEGORIES);
		
		db.execSQL("DROP TRIGGER IF EXISTS " + AtlasData.TRIGGER_CHECK_DEPTH);
		db.execSQL("DROP TRIGGER IF EXISTS " + AtlasData.TRIGGER_MOD_COLOR);
		db.execSQL("DROP TRIGGER IF EXISTS " + AtlasData.TRIGGER_STATUS_INSERT);
		
		onCreate(db);
	}

	/*
	 * encrypted or not-encrypted db? this could help when changing it 
	 */
	public SQLiteDatabase getWritableDatabase(String dbPassword) {
		return super.getWritableDatabase();
	}
	
}
 
 
