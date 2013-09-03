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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.fnf.devel.atlas.AtlasData;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AtlasParseSMSTask extends AsyncTask<Void, Integer, Void> {
	Context context;
	ListView list;
	TextView counter;
	OnClickListener onClick;
	int max;
	int act;
	static AtlasContentProvider dbProvider = new AtlasContentProvider();

	public AtlasParseSMSTask(Context context) {
		super();
		this.context = context;
		AtlasData.config = context.getSharedPreferences("prefs",
				Context.CONTEXT_IGNORE_SECURITY);
	}

	public AtlasParseSMSTask(Context context, TextView counter) {
		super();
		this.counter = counter;
		this.context = context;
	}

	public AtlasParseSMSTask(Context context, TextView counter, ListView list) {
		super();
		this.counter = counter;
		this.context = context;
		this.list = list;
	}

	public AtlasParseSMSTask(Context applicationContext, Button progress) {
		super();
		this.context = applicationContext;
		this.counter = (TextView) progress;
	}

	public AtlasParseSMSTask(Context applicationContext, Button progress,
			OnClickListener startAtlasClick) {
		super();
		this.context = applicationContext;
		this.counter = (TextView) progress;
		this.onClick = startAtlasClick;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (counter != null && counter instanceof Button && onClick != null) {
			counter.setText("Start Atlas!");
			counter.setOnClickListener(onClick);
		}
	}

	@Override
	protected void onPreExecute() {
		if (this.counter != null) {
			if (AtlasData.DEBUG) {
				// TODO: konnyen telepitheto es frissitheto legyen a debug
				// adatbazis
				/*
				 * Konfiguracios indulaskor keszitsen egy debug adatbazis, s ezt
				 * hasznalja. Legyen frissitheto az adatbazis re-install eseten
				 * mindenfelekeppen. A teszt verzioban csak a DEBUG adatbazis
				 * legyen hasznalhato!
				 */
				act = 0;
			} else {
				act = 0;
				ContentResolver cr = context.getContentResolver();

				Cursor sms_result = cr.query(Uri.parse("content://sms/inbox"),
						null, null, null, null);

				if (sms_result == null) {
					Log.i("AtlasDaemonService", "cursor is null. uri: "
							+ "content://sms/inbox");
					return;
				}

				if (sms_result.moveToFirst()) {
					do {
						String number = AtlasData.getNumber();

						int smsaddress_ind = sms_result
								.getColumnIndex("address");

						if (sms_result.getString(smsaddress_ind).equals(number)) {
							max++;
						}
					} while (sms_result.moveToNext());
				}
			}
			if ( AtlasData.DEBUG ) {
				this.counter.setText(String.valueOf(act) + "/ DEBUG@devel.fnf.hu");
			} else {
				this.counter.setText(String.valueOf(act) + "/" + String.valueOf(max));
			}
		}
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (counter != null) {
			String end;
			if ( AtlasData.DEBUG ) {
				end = "DEBUG@devel.fnf.hu";
			} else {
				end = String.valueOf(max);
			}
			this.counter.setText(String.valueOf(values[0]) + "/" + end);
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (AtlasData.DEBUG) {
			URLConnection confnf;
			URL fnf;
			InputStream is = null;
			File dbfile = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
							+ File.separator + AtlasData.TABLE_TRANSACTIONS);
			FileOutputStream fos;
			try {
				fnf = new URL("http://devel.fnf.hu/transactions");
				confnf = fnf.openConnection();
				is = confnf.getInputStream();
				fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[1024];
				int len = 0;
				while (len != -1) {
					len = is.read(buffer);
					fos.write(buffer, 0, len);
				}
				fos.close();
				is.close();
			} catch (Exception e) {
				Log.e("Hupsz", "ERROR");
				e.printStackTrace();
			}
		}
		parse_all_sms();
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	private void parse_all_sms() {
		Cursor sms_result = null;

		if (AtlasData.DEBUG && AtlasContentProvider.isDataForDebug()) {
			Log.d("AtlasDaemonService", "parsing from debug file");
			sms_result = AtlasContentProvider.recvDataForDebug();
		} else {
			Log.d("AtlasDaemonService", "parsing from sms");
			sms_result = context.getContentResolver().query(
					Uri.parse("content://sms/inbox"), null, null, null, null);
			/*
			 * in case of debug file generation, you have to drop table if
			 * exists
			 */
			// AtlasContentProvider.cloneDataForDebug(sms_result);
		}

		if (sms_result == null) {
			Log.i("AtlasDaemonService", "cursor is null. uri: "
					+ "content://sms/inbox");
			return;
		}

		int counter = 0;
		if (sms_result.moveToFirst()) {
			sms_cycle: do {
				/*
				 * sms cycle start confinue if hash in db
				 */
				Uri.Builder builder = null;
				ContentResolver cr = context.getContentResolver();

				String number = AtlasData.config.getString(
						AtlasData.CONFIG_NUMBER, "0036303444481");

				int smsaddress_ind = sms_result.getColumnIndex("address");
				int smsbody_ind = sms_result.getColumnIndex("body");
				int smsdate_ind = sms_result.getColumnIndex("date");
				int smsid_ind = sms_result.getColumnIndex("_id");

				if (sms_result.getString(smsaddress_ind).equals(number)) {
					counter++;
					publishProgress(counter);

					String body = sms_result.getString(smsbody_ind).replace(
							'\'', ' ');

					int hashcode = body.hashCode();

					builder = new Builder();
					builder.scheme("content");
					builder.authority(AtlasData.DB_AUTHORITY);
					builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
					builder.appendPath("hash");

					builder.appendQueryParameter(
							AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH],
							String.valueOf(hashcode));

					Log.d("AtlasDaemonService", "searching for hash "
							+ hashcode);

					Cursor isInDatabase = cr.query(builder.build(),
							AtlasData.TRANSACTIONS_COLUMNS, null, null, null);

					if (isInDatabase.getCount() != 0) {
						Log.d("AtlasDaemonService", "found hash " + hashcode
								+ " ... next iterate in sms_cycle");
						isInDatabase.close();
						continue sms_cycle;
					}

					builder = new Builder();
					builder.scheme("content");
					builder.authority(AtlasData.DB_AUTHORITY);
					builder.appendPath(AtlasData.TABLE_TRANSACTIONTYPES);
					builder.appendPath("types");

					Cursor type_result = cr.query(builder.build(),
							AtlasData.TRANSACTIONTYPES_COLUMNS, null, null,
							null);

					if (type_result.moveToFirst()) {
						type_cycle: do {
							/*
							 * type cycle start continue if no type
							 */
							HashMap<String, String[]> patterns = new HashMap<String, String[]>();
							HashMap<String, String> matches = new HashMap<String, String>();

							String p = ".*"
									+ type_result
											.getString(AtlasData.TRANSACTIONTYPES_PATTERN)
									+ ".*";

							if (!body.matches(p)) {
								Log.d("AtlasDaemonService", "no match: \"" + p
										+ "\"; ... next iterate in type_cycle");
								continue type_cycle;
							}

							int smsId = sms_result.getInt(smsid_ind);
							int typeId = type_result
									.getInt(AtlasData.TRANSACTIONTYPES_ID);

							Log.d("AtlasDaemonService", "found new "
									+ AtlasData.TRANS_TYPES[typeId]);
							/* fix data */
							matches.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_SMSID],
									String.valueOf(smsId));
							matches.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TYPEID],
									String.valueOf(typeId));
							matches.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH],
									String.valueOf(body.hashCode()));

							String[] ap = new String[2];
							String[] fp = new String[2];
							String[] tp = new String[2];
							String[] dp = new String[2];

							ap = type_result.getString(
									AtlasData.TRANSACTIONTYPES_PATTERN_AMOUNT)
									.split("\\|", 2);
							fp = type_result.getString(
									AtlasData.TRANSACTIONTYPES_PATTERN_FROM)
									.split("\\|", 2);
							tp = type_result.getString(
									AtlasData.TRANSACTIONTYPES_PATTERN_TO)
									.split("\\|", 2);
							dp = type_result.getString(
									AtlasData.TRANSACTIONTYPES_PATTERN_DATE)
									.split("\\|", 2);

							/* data to parse by patterns from db */
							patterns.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_AMOUNT],
									ap);
							patterns.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_FROM],
									fp);
							patterns.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TO],
									tp);
							patterns.put(
									AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE],
									dp);

							for (String key : AtlasData.TRANSACTIONS_COLUMNS) {
								/*
								 * data_cycle start deal with null data
								 */
								if (matches.get(key) == null
										&& patterns.get(key) != null
										&& patterns.get(key).length == 2) {

									String comp = "\\Q" + patterns.get(key)[0]
											+ "\\E(.*?)\\Q"
											+ patterns.get(key)[1] + "\\E";

									Pattern data_pattern = Pattern
											.compile(comp);
									Matcher data_match = data_pattern
											.matcher(body);

									if (data_match.find()) {
										matches.put(key, data_match.group(1)
												.replace('\'', ' '));
									} else {

									}
								} else {
									/*
									 * exceptions
									 */
									if (key.equals(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE])) {
										/*
										 * assume sms date to be the transaction
										 * date
										 */
										try {
											SimpleDateFormat sdf = new SimpleDateFormat(
													"yyyy.MM.dd HH:mm");
											// TODO: felismerni mas datum
											// formatumokat
											Date d = new Date(
													Long.valueOf(sms_result
															.getString(smsdate_ind)));
											matches.put(key, sdf.format(d));
										} catch (Exception e) {
											// FIXME lekezelni a hibas datum
											// formatumokat
											e.printStackTrace();
										}
									}
								}
							}
							/*
							 * data_cycle stop
							 */
							for (String key : AtlasData.TRANSACTIONS_COLUMNS) {
								Log.d("AtlasDaemonService", key + ": "
										+ matches.get(key));
							}

							Transaction transaction = new Transaction(
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_AMOUNT]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_FROM]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TO]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_TYPEID]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_SMSID]),
									matches.get(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_HASH]));
							insert(transaction);
						} while (type_result.moveToNext());
						type_result.close();
					}
					/*
					 * type_cycle end
					 */
				}
			} while (sms_result.moveToNext());
			sms_result.close();
		}
		/*
		 * sms_cycle end
		 */
		Log.d("AtlasDaemonService", "counted msg: " + counter);
	}

	public static void insert(Data data) {
		dbProvider.insert(data);
	}

	public static void insert(Category newcat) {
		dbProvider.insert(newcat);
	}

	public static void insert(Transaction transaction) {
		dbProvider.insert(transaction);
	}

}
