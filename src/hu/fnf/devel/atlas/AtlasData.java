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

import hu.fnf.devel.atlas.backend.AtlasContentProvider;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AtlasData {
	/*
	 * preferences
	 */
	public static SharedPreferences config;
	public static Stack<Pos> stack = new Stack<Pos>();
	public static SecretKeySpec key;
	public static Cipher ecipher;
	public static Cipher dcipher;

	/*
	 * globals
	 */
	public static final String FIRST_RUN = "first_run";
	public static final String CONFIG_FROM = "default_from";
	public static final String CONFIG_DATABASE = "database";
	public static final String CONFIG_BANK = "bank";
	public static final String CONFIG_NUMBER = "number";
	public static final String CONFIG_PASSWORD = "password";
	public static final String CONFIG_DATE = "date";

	public static final int INCOME = 1;
	public static final int OUTCOME = 2;
	public static final int ALLINCOME = 3;
	public static final int ALLOUTCOME = 4;

	public static final int MAX_CAT_DEPTH = 5;
	public static final int MAX_CAT_WIDTH = 9;

	public static final int FRESH = 0;
	public static final int DONE = 1;
	public static final int IGNORED = 2;

	public static class Pos {
		public int level;
		public int page;

		public Pos(int level, int page) {
			this.level = level;
			this.page = page;
		}
	}
	
	public static OnClickListener cancelClick = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface arg0, int arg1) {
			arg0.dismiss();
		}
	};
	public static OnClickListener ackClick = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface arg0, int arg1) {
			arg0.dismiss();
		}
	};

	public static OnCancelListener getCancelListener(final Atlas atlas) {
		return new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				atlas.finish();
			}
		};
	}
	public static OnClickListener getDelGuessClick(final int id) {
		return new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
					AtlasContentProvider dbProvider = new AtlasContentProvider();
					
					Uri.Builder builder = new Builder();
					builder.scheme("content");
					builder.authority(AtlasData.DB_AUTHORITY);
					builder.appendPath(AtlasData.TABLE_DATA);
					builder.appendPath("delguess");
					builder.appendPath(String.valueOf(id));
					
					dbProvider.delete(builder.build(), null, null);
					
					dialog.dismiss();
			}
		};
	}


	public static OnClickListener getDelDataClick(final int id) {
		
		return new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				AtlasContentProvider dbProvider = new AtlasContentProvider();
				
				Uri.Builder builder = new Builder();
				builder.scheme("content");
				builder.authority(AtlasData.DB_AUTHORITY);
				builder.appendPath(AtlasData.TABLE_DATA);
				builder.appendPath("delete");
				builder.appendPath(String.valueOf(id));
				
				dbProvider.delete(builder.build(), null, null);
				
				arg0.dismiss();
			}
		};
	}


	public static android.content.DialogInterface.OnClickListener getDelTaskClick(final TextView id) {	
		return new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {

				Builder builder = new Builder();
				builder.scheme("content");
				builder.authority(AtlasData.DB_AUTHORITY);
				builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
				builder.appendPath("delete");
				builder.appendPath(String.valueOf(id.getText()));
				
				id.getRootView().getContext().getContentResolver().update(builder.build(), null, null, null);
			}
		};
	}


	public static android.content.DialogInterface.OnClickListener exitClick(final Atlas atlas) {
		return new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				atlas.finish();
				arg0.dismiss();
			}
		};
	}
	
	private static String encrypt(String str) {
		try {
			ecipher = Cipher.getInstance("DESede");

			ecipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] cipertext = ecipher.doFinal(getBytes(str));
			byte[] encoded = Base64.encode(cipertext, Base64.DEFAULT);
			return new String(encoded);
		} catch (Exception e) {
			Log.e("Atlas", "encryption error has happened " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static String decrypt(String enc) {
		try {
			dcipher = Cipher.getInstance("DESede");
			dcipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decoded = Base64.decode(getBytes(enc), Base64.DEFAULT);
			byte[] cipertext = dcipher.doFinal(decoded);
			return new String(cipertext);
		} catch (Exception e) {
			Log.e("Atlas", "decryption error has happened " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static byte[] getBytes(String toGet) {
		try {
			byte[] retVal = new byte[toGet.length()];
			for (int i = 0; i < toGet.length(); i++) {
				char anychar = toGet.charAt(i);
				retVal[i] = (byte) anychar;
			}
			return retVal;
		} catch (Exception e) {

			return null;
		}
	}
	/*
	 * constants
	 */
	public static final int TRUE 	= 1;
	public static final int FALSE	= 0;

	/*
	 * database
	 */
	public static final int DATABASE_VERSION = 11;
	public static final String DATABASE_NAME = "atlasdb";
	public static final String DB_AUTHORITY = "hu.fnf.devel.atlas.data.provider.AtlasDatabaseProvider";
	/*
	 * schema
	 */
	public static final String TABLE_TRANSACTIONS = "transactions";
	public static final int TRANSACTIONS_ID = 0;
	public static final int TRANSACTIONS_AMOUNT = 1;
	public static final int TRANSACTIONS_FROM = 2;
	public static final int TRANSACTIONS_TO = 3;
	public static final int TRANSACTIONS_DATE = 4;
	public static final int TRANSACTIONS_TYPEID = 5;
	public static final int TRANSACTIONS_SMSID = 6;
	public static final int TRANSACTIONS_HASH = 7;
	public static final int TRANSACTIONS_STATUS = 8;

	public static final String[] TRANSACTIONS_COLUMNS = { "_id", "amount", "_from", "_to", "date", "typeid", "smsid",
			"hash", "status" };
	/*----------------------------------------*/
	
	public static final String TABLE_TRANSACTIONTYPES = "transactiontypes";
	public static final int TRANSACTIONTYPES_ID = 0;
	public static final int TRANSACTIONTYPES_NAME = 1;
	public static final int TRANSACTIONTYPES_PATTERN = 2;
	public static final int TRANSACTIONTYPES_PATTERN_AMOUNT = 3;
	public static final int TRANSACTIONTYPES_PATTERN_FROM = 4;
	public static final int TRANSACTIONTYPES_PATTERN_TO = 5;
	public static final int TRANSACTIONTYPES_PATTERN_DATE = 6;

	public static final String[] TRANSACTIONTYPES_COLUMNS = { "_id", "name", "pattern", "pattern_amount",
			"pattern_from", "pattern_to", "pattern_date" };
	
	public static final int CARD_PAYMENT = 0;
	public static final int CARD_CASHWITHDRAWAL = 1;
	public static final int TRANSFER_REPEATING = 2;
	public static final int TRANSFER_OUTCOME = 3;
	public static final int TRANSFER_INCOME = 4;
	
	public static final int MANUAL_TRANSACTION = 5;

	public static final String[] TRANS_TYPES = { "CARD_PAYMENT", "CARD_CASHWITHDRAWAL", "TRANSFER_REPEATING",
			"TRANSFER_OUTCOME", "TRANSFER_INCOME", "MANUAL_TRANSACTION" };
	/*----------------------------------------*/
	
	public static final String TABLE_CATEGORIES = "categories";
	public static final int CATEGORIES_ID = 0;
	public static final int CATEGORIES_NAME = 1;
	public static final int CATEGORIES_AMOUNT = 2;
	public static final int CATEGORIES_DEPTH = 3;
	public static final int CATEGORIES_COLORR = 4;
	public static final int CATEGORIES_COLORG = 5;
	public static final int CATEGORIES_COLORB = 6;
	public static final int CATEGORIES_LEAF = 7;

	public static final String TRIGGER_CHECK_DEPTH = "check_depth";
	public static final String TRIGGER_MOD_COLOR = "mod_color";

	public static final String[] CATEGORIES_COLOR = { "192,100,0", "0,100,192" };
	public static final String[] CATEGORIES_COLUMNS =
		{ "_id", "name", "amount", "depth", "colorr", "colorg", "colorb", "leaf" };
	/*----------------------------------------*/
	
	public static final String TABLE_DATA = "data";
	public static final int DATA_ID = 0;
	public static final int DATA_TRANSACTIONID = 1;
	public static final int DATA_CATEGORYID = 2;
	public static final int DATA_TAG = 3;
	public static final int DATA_AMOUNT = 4;
	public static final int DATA_STATUS = 5;

	public static final String[] DATA_COLUMNS = { "_id", "transid", "catid", "tag", "amount", "status" };

	public static final String TRIGGER_CATEGORY_INSERT 	= "category_insert";
	public static final String TRIGGER_STATUS_INSERT 	= "status_insert";
	public static final String TRIGGER_STATUS_DELETE 	= "status_delete";
	/*----------------------------------------*/
	/*
	 * levels and pages
	 */
	public static final int TOPLEVEL = 0;
	public static final int CATEGORY = 1;
	public static final int DETAILS = 2;

	public static final int LEFT = 0;
	public static final int MIDDLE = 1;
	public static final int RIGHT = 3;

	public static final int DEFAULT_LEVEL = CATEGORY;
	public static final int DEFAULT_PAGE = MIDDLE;

	public static final int PINCOME = 0;
	public static final int PSUMMARY = 1;
	public static final int POUTCOME = 2;
	/*----------------------------------------*/
	public static final int CAT_VIEW_COUNT = 3;

	public static final int TINCOME = 0;
	public static final int TSUMMARY = 1;
	public static final int TOUTCOME = 2;
	/*----------------------------------------*/
	public static final int TOP_VIEW_COUNT = 3;

	public static final int CATFRAGMENTADAPTER = 0;
	public static final int TOPFRAGMENTADAPTER = 1;
	/*----------------------------------------*/
	public static final int FRAGMENTADAPTER_COUNT = 2;

	public static final boolean DEBUG = true;

	public AtlasData(int page_id, int level_id, String uuid) {
		stack.add(new Pos(level_id, page_id));

		try {
			key = new SecretKeySpec(uuid.getBytes(), "DESede");
		} catch (Exception e) {
			Log.e("Atlas", "no such algorithm " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void pushPos(int l, int p) {
		if (stack.size() > 0) {
			if (stack.peek().level == l && stack.peek().page == p) {
				return;
			}
		}
		stack.add(new Pos(l, p));
		Log.d("AtlasApplicationData", "adding new pos to stack " + l + ":" + p);
	}

	public static Pos popPos() {
		return stack.pop();
	}

	public static Pos peekPos() {
		return stack.peek();
	}

	public static int size() {
		return stack.size();
	}

	public static String getDefault(int key) {
		switch (key) {
		case TRANSACTIONS_AMOUNT:
			return "0";
		case TRANSACTIONS_FROM:
			return config.getString(AtlasData.CONFIG_FROM, "unknown");
		case TRANSACTIONS_TO:
			return "unknown";
		case TRANSACTIONS_DATE:
			return "0";
		case TRANSACTIONS_TYPEID:
			return "0";
		case TRANSACTIONS_SMSID:
			return "0";
		case TRANSACTIONS_HASH:
			return "0";
		default:
			break;
		}
		Log.e("Transaction", "no deault value for " + key);
		return null;
	}

	public static String getDBPassword() {
		String ret =decrypt(config.getString(CONFIG_PASSWORD, ""));
		return ret;
	}

	public static String getColor(Cursor parent) {
		Random R = new Random();
		int r = R.nextInt(255);
		int g = R.nextInt(255);
		int b = R.nextInt(255);
		return String.valueOf(r) + "," + String.valueOf(g) + "," + String.valueOf(b);
	}

	public static String getNumber() {
		return config.getString(CONFIG_NUMBER, "");
	}

	public static String getCatName(Context context, int catid) {
		Uri.Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		builder.appendPath(AtlasData.TABLE_CATEGORIES);
		builder.appendPath("cat");
		builder.appendPath(String.valueOf(catid));

		Cursor item = context.getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);
		if ( item.moveToFirst()) {
			return item.getString(AtlasData.CATEGORIES_NAME);
		} else {
			return "N/A";
		}
	}

	public static double getLocaleAmount(String amount) {
		double ret = 0;
		try {
			Locale loc;
			// TODO: maskent felismerni helyi szamformat
			/*
			 *  Jelenleg a Locale.GERMAN van, mert a magyar is azt
			 *  használja számnak (45,6 és 12.013,34 ... stb), de 
			 *  nem vagyunk németek. Tudni kell, hogy milyen nyelvhez
			 *  milyen formátum tartozik.
			 */
			loc = Locale.GERMAN;
			ret = DecimalFormat.getNumberInstance(loc).parse(amount.replace(" ", "")).doubleValue();
		} catch (ParseException e) {
			Log.e("AtlasData", "amount convert error " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Date getDateFromString(String date) {
		Date ret = null;
		try {
			// TODO: maskent felisemerni a datum tipus
			/*
			 * Nyelvfüggő dátumtípusok felismerése.
			 * (lehet h ez duplikátum)
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
			ret = sdf.parse(date);
		} catch (Exception e) {
			Log.e("AtlasData", "date convert error " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}

	public static int getIntDateFromString(String date) {
		return (int) (getDateFromString(date).getTime()/1000L);
	}

	@SuppressLint("SimpleDateFormat")
	public static String getStringDateFromInt(int unixtime) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis((long)unixtime*1000L);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		
		return sdf.format(cal.getTime());
	}

	public static String setDBPassword(String string) {
		return encrypt(string);
	}
	
	public static int getMonthStartUnixTime(View v) {
		TextView reqmonth = (TextView) v.findViewById(R.id.reqmonth);

		String[] date = reqmonth.getText().toString().split("\\.");

		Log.d("CatFragment", "req: " + reqmonth.getText().toString() + "("
				+ date.length + ")");
		Calendar monthstart = new GregorianCalendar(Integer.valueOf(date[0]),
				Integer.valueOf(date[1]) - 1, 0);
		return (int) (monthstart.getTimeInMillis() / 1000L);
	}


}
 
