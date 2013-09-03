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

/*
 * set editor width limit to 120 cols
 */
package hu.fnf.devel.atlas;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

//import net.sqlcipher.database.SQLiteDatabase;

import hu.fnf.devel.atlas.AtlasData.Pos;
import hu.fnf.devel.atlas.backend.*;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.view.ViewGroup.LayoutParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

public class Atlas extends FragmentActivity {

	static AtlasData data;

	static AlarmManager alarmManager;
	static PendingIntent pendingIntent;

	static ViewPager pager;

	static FragmentManager mFragmentManager;

	static CatFragmentAdapter catFragAdapter;
	static TopFragmentAdapter topFragAdapter;

	// TODO: megtudni milyen tulajdonjogban van az ikon
	/* icon: http://www.flickr.com/photos/the-fishbone/3773660264/in/photostream (no contact) */
	AlertDialog dialog;
	public static Map<String, Category> categories = new HashMap<String, Category>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d("onCreate", "create call");
		getActionBar().setTitle("Atlas");
		mFragmentManager = getSupportFragmentManager();
		//SQLiteDatabase.loadLibs(this);
		
		initData();
		
		if (AtlasData.config.getBoolean(AtlasData.FIRST_RUN, true)) {
			configureAtlasFirst();
		} else {
			continueAtlasInit();
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	private void initData() {
		if (AtlasData.config == null) {
			AtlasData.config = getSharedPreferences("prefs", FragmentActivity.MODE_WORLD_READABLE);
		}
		data = (AtlasData) getLastCustomNonConfigurationInstance();
		if (data == null) {
			String uuid = null;
			Log.d("onCreate", "new begining...");
			
			try {
				TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (tManager.getDeviceId() != null) {
					Log.d("Atlas", "SN:" + String.format("%1$24s", tManager.getDeviceId()));
					uuid = String.format("%1$24s", tManager.getDeviceId()); // DESede key is 24 byte long
				} else {
					uuid = "iereileiphah3Eihoh8EeH2a";
				}
			} catch (Exception e) {
				Log.e("Atlas", "no such algorithm " + e.getMessage());
				e.printStackTrace();
			}

			data = new AtlasData(AtlasData.CATEGORY, AtlasData.PSUMMARY, uuid);
			
		} else {
			Log.d("onCreate", "restoring...");
			Log.d("onCreate", "view level: " + AtlasData.peekPos().level);
			Log.d("onCreate", "page level: " + AtlasData.peekPos().page);
		}
	}

	private void continueAtlasInit() {
		switch (AtlasData.peekPos().level) {
		case AtlasData.TOPLEVEL:
			changeViewLevel(R.layout.toplevel_view, AtlasData.peekPos().page);
			break;
		case AtlasData.CATEGORY:
			changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
			break;
		case AtlasData.DETAILS:
			changeViewLevel(R.layout.detail_view, AtlasData.peekPos().page);
			break;
		default:
			break;
		}
	}

	private void configureAtlasFirst() {
		AlertDialog.Builder confwindow = new AlertDialog.Builder(this);

		confwindow.setTitle(getResources().getString(R.string.atlas_configuiration));
		confwindow.setMessage(getResources().getString(R.string.please_provide_data));

		View input = getLayoutInflater().inflate(R.layout.config_view, null);

		confwindow.setView(input);
		confwindow.setOnCancelListener(AtlasData.getCancelListener(this));
		dialog = confwindow.create();
		dialog.show();
	}

	@SuppressLint("SimpleDateFormat")
	public void startParseAllSmsConfig(View v) {
		EditText password = (EditText) v.getRootView().findViewById(R.id.password);
		EditText number = (EditText) v.getRootView().findViewById(R.id.smsnumber);
		Spinner bank = (Spinner) v.getRootView().findViewById(R.id.bankpreset);
		Spinner dbpre = (Spinner) v.getRootView().findViewById(R.id.databasepreset);
		EditText from = (EditText) v.getRootView().findViewById(R.id.from);

		password.setEnabled(false);
		number.setEnabled(false);
		bank.setEnabled(false);
		dbpre.setEnabled(false);
		from.setEnabled(false);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM");
		String date = (formatter.format(new Date()));

		String enc = AtlasData.setDBPassword(password.getText().toString());

		Editor edit = AtlasData.config.edit();
		String warn = "[\';]";
		edit.putString(AtlasData.CONFIG_PASSWORD, enc);
		edit.putString(AtlasData.CONFIG_NUMBER, number.getText().toString().replaceAll(warn, " "));
		edit.putString(AtlasData.CONFIG_BANK, bank.getSelectedItem().toString().replaceAll(warn, " "));
		edit.putString(AtlasData.CONFIG_DATABASE,  dbpre.getSelectedItem().toString().replaceAll(warn, " "));
		edit.putString(AtlasData.CONFIG_FROM, from.getText().toString().replaceAll(warn, " "));
		edit.putString(AtlasData.CONFIG_DATE, date);
		edit.commit();

		Button progress = (Button) v.getRootView().findViewById(R.id.parse);

		new AtlasParseSMSTask(getApplicationContext(), progress,onStartAtlasClick ).execute();
	}

	private void changeViewLevel(int evelId, int pageId, int amountOrId) {

		setContentView(evelId);
		AtlasData.pushPos(AtlasData.DETAILS, pageId);
		switch (evelId) {
		case R.layout.detail_view:
			TextView id = (TextView) findViewById(R.id.id);
			TextView tag = (TextView) findViewById(R.id.tag);
			if ( amountOrId >= 0 ) {
				id.setText(String.valueOf(amountOrId));
			} else {
				id.setText(String.valueOf(amountOrId));
				tag.setText("MANUAL");
			}

			ContentResolver cr = getContentResolver();
			/*
			 * load categories
			 */
			Uri.Builder builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("all");

			Cursor item = cr.query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);

			if (item != null && item.moveToFirst()) {
				do {
					Category c = new Category(item.getString(AtlasData.CATEGORIES_ID),
							item.getString(AtlasData.CATEGORIES_NAME), item.getString(AtlasData.CATEGORIES_AMOUNT),
							item.getString(AtlasData.CATEGORIES_DEPTH), item.getString(AtlasData.CATEGORIES_COLORR),
							item.getString(AtlasData.CATEGORIES_COLORG), item.getString(AtlasData.CATEGORIES_COLORB));
					categories.put(c.getName(), c);
				} while (item.moveToNext());
				item.close();
			}

			/*
			 * transaction details
			 */
			TextView amountText = (TextView) findViewById(R.id.taskAmount);
			TextView fromText = (TextView) findViewById(R.id.taskFrom);
			TextView toText = (TextView) findViewById(R.id.taskTo);
			TextView dateText = (TextView) findViewById(R.id.taskDate);

			ListView cats = (ListView) findViewById(R.id.taskCats);
			ArrayList<Category> cat_list = new ArrayList<Category>();
			CatAddAdapter cat_adapter = new CatAddAdapter(getApplicationContext(), R.layout.addcat_view,
					cat_list);

			if ( amountOrId >= 0) {
				builder = new Builder();
				builder.scheme("content");
				builder.authority(AtlasData.DB_AUTHORITY);
				builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
				builder.appendPath("tasks");
				builder.appendPath(String.valueOf(amountOrId));

				item = cr.query(builder.build(), AtlasData.TRANSACTIONS_COLUMNS, null, null, null);

				if (item.moveToFirst()) {
					switch (item.getInt(AtlasData.TRANSACTIONS_TYPEID)) {
					case AtlasData.CARD_PAYMENT:
						tag.setText(item.getString(AtlasData.TRANSACTIONS_TO));
						break;
					case AtlasData.CARD_CASHWITHDRAWAL:
						tag.setText(item.getString(AtlasData.TRANSACTIONS_TO));
						break;
					case AtlasData.TRANSFER_INCOME:
						tag.setText(item.getString(AtlasData.TRANSACTIONS_FROM));
						break;
					case AtlasData.TRANSFER_OUTCOME:
						tag.setText(item.getString(AtlasData.TRANSACTIONS_TO));
						break;
					case AtlasData.TRANSFER_REPEATING:
						tag.setText(item.getString(AtlasData.TRANSACTIONS_TO));
						break;
					}
					amountText.setText(item.getString(AtlasData.TRANSACTIONS_AMOUNT));
					fromText.setText(item.getString(AtlasData.TRANSACTIONS_FROM));
					toText.setText(item.getString(AtlasData.TRANSACTIONS_TO));
					dateText.setText(AtlasData.getStringDateFromInt(item.getInt(AtlasData.TRANSACTIONS_DATE)));
					
					cat_adapter.setAmount(item.getDouble(AtlasData.TRANSACTIONS_AMOUNT));
				} else {
					Log.e("Atlas", "cannot load data for view!");
					return;
				}
				item.close();
			} else {
				Calendar cal = new GregorianCalendar(TimeZone.getDefault());

				dateText.setText(AtlasData.getStringDateFromInt((int)(cal.getTimeInMillis()/1000L)));
				double amount = amountOrId * -1;
				amountText.setText(String.valueOf(amount));
				cat_adapter.setAmount(amount);
			}
			cats.setAdapter(cat_adapter);
			Spinner spinner = (Spinner) findViewById(R.id.taskSpinner);

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("tip");
			builder.appendQueryParameter(AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG], String.valueOf(tag.getText()));

			item = cr.query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);

			ArrayList<String> array_spinner = new ArrayList<String>();

			if (item.moveToFirst()) {
				do {
					array_spinner.add(item.getString(AtlasData.CATEGORIES_NAME));
				} while (item.moveToNext());
			}
			ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getApplicationContext(),
					R.layout.custom_simple_spinner, array_spinner);
			spinner.setAdapter(arrayadapter);
			item.close();
			break;
		default:
			break;
		}
		Log.d("Atlas", "changeViewLevel to " + AtlasData.DETAILS + " : " + pageId);
		pager.setCurrentItem(pageId);
		setPagerSwipeActions();
	}

	private void changeViewLevel(int view_level, int page_id) {
		setContentView(view_level);
		int level_id = AtlasData.CATEGORY;

		ContentResolver cr = getContentResolver();

		Uri.Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		String[] projection = null;

		switch (view_level) {

		case R.layout.toplevel_view:
			level_id = AtlasData.TOPLEVEL;
			AtlasData.pushPos(level_id, page_id);			

			topFragAdapter = new TopFragmentAdapter(mFragmentManager);

			pager = (ViewPager) findViewById(R.id.toplevel_pager);
			pager.setAdapter(topFragAdapter);

			break;

		case R.layout.category_view:
			level_id = AtlasData.CATEGORY;
			AtlasData.pushPos(level_id, page_id);

			catFragAdapter = new CatFragmentAdapter(mFragmentManager);

			pager = (ViewPager) findViewById(R.id.category_pager);
			pager.setAdapter(catFragAdapter);

			setViewCategoryProperties(page_id);
			break;

		case R.layout.detail_view:
			level_id = AtlasData.DETAILS;
			AtlasData.pushPos(level_id, page_id);

			break;
		case R.layout.detailcat_view:
			level_id = AtlasData.DETAILS;
			AtlasData.pushPos(level_id, page_id);

			setDetailProperties(page_id);
			break;
		case R.layout.detallist_view:
			level_id = AtlasData.DETAILS;
			AtlasData.pushPos(level_id, page_id);
			int listid = 0;
			switch (page_id) {
			case AtlasData.PSUMMARY:
				listid = R.id.expand_list_view;
				builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
				builder.appendPath("tasks");
				projection = AtlasData.TRANSACTIONS_COLUMNS;
				break;
			case AtlasData.PINCOME:
				listid = R.id.expand_list_view;
				builder.appendPath(AtlasData.TABLE_DATA);
				builder.appendPath("guess");
				projection = AtlasData.DATA_COLUMNS;
				break;
			case AtlasData.POUTCOME:
				listid = R.id.expand_list_view;
				builder.appendPath(AtlasData.TABLE_DATA);
				builder.appendPath("data");
				projection = AtlasData.DATA_COLUMNS;
				break;
			default:
				listid = R.id.expand_list_view;
				break;
			}

			Cursor tasks_result = cr.query(builder.build(), projection, null, null, null);
			DatabaseQueryAdapter ta = new DatabaseQueryAdapter(this, tasks_result, 0, builder.build().getPathSegments()
					.get(0));
			ListView lv = (ListView) findViewById(listid);
			lv.setAdapter(ta);
			break;
		default:
			break;
		}
		Log.d("Atlas", "changeViewLevel to " + level_id + " : " + page_id);

		pager.setCurrentItem(page_id);
		setPagerSwipeActions();
	}

	private void setDetailProperties(int page_id) {
		LinearLayout root = (LinearLayout) findViewById(R.id.detailcatRoot);
		root.setOrientation(android.widget.LinearLayout.VERTICAL);

		LinearLayout in = new LinearLayout(getApplicationContext());
		in.setId(AtlasData.INCOME);
		in.setOnClickListener(onCatClick);

		LinearLayout out = new LinearLayout(getApplicationContext());
		out.setId(AtlasData.OUTCOME);
		out.setOnClickListener(onCatClick);

		TextView intext = new TextView(getApplicationContext());
		intext.setText(getResources().getString(R.string.income));
		intext.setOnClickListener(onCatClick);
		intext.setId(AtlasData.INCOME);
		intext.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium_Inverse);

		TextView outtext = new TextView(getApplicationContext());
		outtext.setText(getResources().getString(R.string.outcome));
		outtext.setOnClickListener(onCatClick);
		outtext.setId(AtlasData.OUTCOME);
		outtext.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium_Inverse);

		in.addView(intext);
		out.addView(outtext);

		root.addView(in);
		addChilds(in, root);

		root.addView(out);
		addChilds(out, root);

	}

	/* recursive tree build */
	private void addChilds(View parent, LinearLayout pll) {
		Uri.Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		builder.appendPath(AtlasData.TABLE_CATEGORIES);
		builder.appendPath("childs");
		builder.appendPath(String.valueOf(parent.getId()));

		Cursor items = getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);

		if (items != null && items.moveToFirst()) {
			LinearLayout ll = null;
			do {
				ll = new LinearLayout(getApplicationContext());
				LayoutParams llp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				ll.setLayoutParams(llp);
				TextView child = new TextView(getApplicationContext());
				child.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium_Inverse);

				ll.setId(items.getInt(AtlasData.CATEGORIES_ID));
				child.setId(items.getInt(AtlasData.CATEGORIES_ID));
				child.setText(items.getString(AtlasData.CATEGORIES_NAME));
				for (int i = 0; i < AtlasData.MAX_CAT_DEPTH - items.getInt(AtlasData.CATEGORIES_DEPTH); i++) {
					TextView holder = new TextView(getApplicationContext());
					holder.setText("        ");
					ll.addView(holder);
				}

				child.setClickable(true);
				
				Log.d("Atlas", "build to ll: "+ child.getText().toString());
				ll.addView(child);
				
				
				
				ll.setOnClickListener(onCatClick);
				child.setOnClickListener(onCatClick);

				View line = new View(getApplicationContext());

				line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
				line.setBackgroundColor(Color.GRAY);

				pll.addView(line);
				pll.addView(ll);
				Log.d("Atlas", "build to pll: "+ ll.getId());
				
				addChilds(child, pll);
			} while (items.moveToNext());
			items.close();
		} else {
			// no more kids
			return;
		}
	}

	OnClickListener onCatClick = new OnClickListener() {

			public void onClick(View v) {
				Log.d("Atlas", "cliecked at " + v.getId());
				TextView seltext = (TextView) v.getRootView().findViewById(R.id.selectedParent);
				seltext.setTag(String.valueOf(v.getId()));
				seltext.setText(AtlasData.getCatName(getApplicationContext(), v.getId()));
			}
		
	};

	private void setViewCategoryProperties(int page) {
		/*
		 * List LABEL
		 */
		TextView header = (TextView) findViewById(R.id.header_text_view);
		TextView newe = (TextView) findViewById(R.id.new_etwas_text);
		EditText newc = (EditText) findViewById(R.id.new_etwas);
		ContentResolver cr = getContentResolver();
		Uri.Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		Cursor list = null;
		String[] projection = null;

		switch (page) {
		case AtlasData.PINCOME:
			newe.setText(getResources().getString(R.string.category) + ":");
			newc.setInputType(
					InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);

			builder.appendPath(AtlasData.TABLE_DATA);
			builder.appendPath("guess");

			list = cr.query(builder.build(), AtlasData.DATA_COLUMNS, null, null, null);

			header.setText(getResources().getString(R.string.guess) + "(" + String.valueOf(list.getCount()) + ")");
			list.close();

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_DATA);
			projection = AtlasData.DATA_COLUMNS;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				Log.d("onCreate", "only toptask");
				builder.appendPath("topguess");

			} else {
				Log.d("onCreate", "all tasks");
				builder.appendPath("guess");
			}
			break;
		case AtlasData.PSUMMARY:
			newe.setText(getResources().getString(R.string.amount) + ":");
			newc.setInputType(
					InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
			builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
			builder.appendPath("tasks");

			list = cr.query(builder.build(), AtlasData.TRANSACTIONS_COLUMNS, null, null, null);
			header.setText(getResources().getString(R.string.tasks) + "(" + String.valueOf(list.getCount()) + ")");
			list.close();

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
			projection = AtlasData.TRANSACTIONS_COLUMNS;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				Log.d("onCreate", "only toptask");
				builder.appendPath("toptask");

			} else {
				Log.d("onCreate", "all tasks");
				builder.appendPath("tasks");
			}
			break;
		case AtlasData.POUTCOME:
			newe.setText(getResources().getString(R.string.category) + ":");
			newc.setInputType(
					InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			
			builder.appendPath(AtlasData.TABLE_DATA);
			builder.appendPath("data");

			list = cr.query(builder.build(), AtlasData.DATA_COLUMNS, null, null, null);
			header.setText(getResources().getString(R.string.data) + "(" + String.valueOf(list.getCount()) + ")");
			list.close();

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_DATA);
			projection = AtlasData.DATA_COLUMNS;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				Log.d("onCreate", "only toptask");
				builder.appendPath("topdata");

			} else {
				Log.d("onCreate", "all tasks");
				builder.appendPath("data");
			}
			break;
		default:
			Log.e("onPageSelected", "page?!: " + page);
			break;
		}
		header.setOnClickListener(onHeaderClickListener);
		Cursor tasks_result = cr.query(builder.build(), projection, null, null, null);
		DatabaseQueryAdapter ta = new DatabaseQueryAdapter(this, tasks_result, 0, builder.build().getPathSegments()
				.get(0));

		Log.d("onCreate", "result count: " + tasks_result.getColumnCount());
		ListView lv = (ListView) findViewById(R.id.tasklist);
		lv.setAdapter(ta);
	}

	@Override
	public void onBackPressed() {
		if (AtlasData.size() < 2) {
			AlertDialog.Builder exitdialog = new AlertDialog.Builder(this);
			exitdialog.setTitle("--- "+ getResources().getString(R.string.exit) +"---");
			exitdialog.setMessage(getResources().getString(R.string.do_you_want_to_exit));
			exitdialog.setPositiveButton(getResources().getString(R.string.yes), AtlasData.exitClick(this));
			exitdialog.setNegativeButton(getResources().getString(R.string.no), AtlasData.cancelClick);
			exitdialog.show();
			return;
		}
		Pos act = AtlasData.popPos();
		Log.d("Atlas", "onBackPressed when you are at " + act.level + " : " + act.page);
		if (act.level == AtlasData.peekPos().level) {
			pager.setCurrentItem(AtlasData.peekPos().page);
		} else {
			Pos pre = AtlasData.popPos();
			switch (pre.level) {
			case AtlasData.CATEGORY:
				changeViewLevel(R.layout.category_view, pre.page);
				break;
			case AtlasData.TOPLEVEL:
				changeViewLevel(R.layout.toplevel_view, pre.page	);
				break;
			case AtlasData.DETAILS:
				changeViewLevel(R.layout.detail_view, pre.page);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.atlas, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("Atlas", "onOptionsItemSelected: " + item);
		switch (item.getItemId()) {
		case R.id.action_daemon:
			new AtlasParseSMSTask(getApplicationContext()).execute();
			break;
		case R.id.action_exit:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		Log.d("onRetainCustomNonConfigurationInstance", "Configuration call");
		return data;
	}

	private void setPagerSwipeActions() {

		Log.d("setPagerSwipeActions", "called");
		pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int selected_page) {
				AtlasData.pushPos(AtlasData.peekPos().level, selected_page);
				switch (AtlasData.peekPos().level) {
				case AtlasData.CATEGORY:
					Log.d("Atlas", "category selected_page " + selected_page + " pushed to stack");
					setViewCategoryProperties(selected_page);
					break;
				case AtlasData.TOPLEVEL:
					Log.d("Atlas", "toplevel selected_page " + selected_page + " pushed to stack");
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public OnClickListener onHeaderClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onHeaderClick(v);
		}
	};

	public void onHeaderClick(View view) {
		changeViewLevel(R.layout.detallist_view, AtlasData.peekPos().page);
	}
	
	public OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onListItemClick(v);
		}
	};

	public void onListItemClick(View view) {
		Log.d("Atlas", "clicked at " + view.getId());
		int id = view.getId();
		switch (AtlasData.peekPos().page) {
		case AtlasData.PSUMMARY:
			changeViewLevel(R.layout.detail_view, AtlasData.peekPos().page, view.getId());
			break;
		case AtlasData.PINCOME:
			AlertDialog.Builder guessdialog = new AlertDialog.Builder(this);
			guessdialog.setTitle("--- " + getResources().getString(R.string.guess)+ " ---");
			guessdialog.setMessage(getResources().getString(R.string.delete_this) + getResources().getString(R.string.guess) + "?");
			guessdialog.setPositiveButton(getResources().getString(R.string.yes), AtlasData.getDelGuessClick(id));
			guessdialog.setNegativeButton(getResources().getString(R.string.cancel), AtlasData.cancelClick);
			guessdialog.show();
			break;
		case AtlasData.POUTCOME:
			AlertDialog.Builder datadialog = new AlertDialog.Builder(this);
			datadialog.setTitle("--- " + getResources().getString(R.string.data)+" ---");
			datadialog.setMessage(getResources().getString(R.string.delete_this) + getResources().getString(R.string.data) +"?");
			datadialog.setPositiveButton(getResources().getString(R.string.yes), AtlasData.getDelGuessClick(id));
			datadialog.setNegativeButton(getResources().getString(R.string.cancel), AtlasData.cancelClick);
			datadialog.show();
			break;
		default:
			break;
		}

	}

	public void addCat(View view) {

		Spinner spinner = (Spinner) findViewById(R.id.taskSpinner);
		// In case of null add!
		if ( spinner.getSelectedItem() == null ) {
			Log.d("Atlas", "null selection");
			return;
		}
		String selected = (String) spinner.getSelectedItem();
		/*
		 * add to list
		 */
		Category sel = categories.get(selected);
		Log.d("Atlas", "selected: " + selected);
		Log.d("Atlas", "id: " + sel.getId());
		ListView cats = (ListView) findViewById(R.id.taskCats);
		CatAddAdapter catAddAdapter = (CatAddAdapter) cats.getAdapter();

		if (catAddAdapter.getSum() >= (int)(catAddAdapter.getAmount())+Integer.valueOf(sel.getAmount()) ) {
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
			myAlertDialog.setTitle("--- Task ---");
			myAlertDialog.setMessage("Nothing left to categorize");
			myAlertDialog.setPositiveButton(getResources().getString(R.string.ok), AtlasData.ackClick);
			myAlertDialog.show();
			return;
		}
		catAddAdapter.add(new Category(sel.getId(), sel.getName(), sel.getAmount(), sel.getDepth(), sel.getColorr(),
				sel.getColorg(), sel.getColorb()));
		cats.setAdapter(catAddAdapter);

		//remove from spinner
		
		ArrayList<String> array_spinner = new ArrayList<String>();
		int spin_count = spinner.getAdapter().getCount();
		Log.d("Atlas", "spinner count: " + spin_count);
		if ( spin_count == 1) {
			// no more category in spinner (ASANA: mi legyen ha elfogy a spinner)
			Log.d("Atlas", "no more category in spinner");
			ImageView add = (ImageView) findViewById(R.id.taskAdd);
			add.setVisibility(View.INVISIBLE);
			add.setEnabled(false);
			spinner.setVisibility(View.INVISIBLE);
			spinner.setEnabled(false);
		}
		for (int i = 0; i < spin_count; i++) {
			String s = (String) spinner.getAdapter().getItem(i);
			if (!s.equalsIgnoreCase(selected)) {
				array_spinner.add(s);
			}
		}
		ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.custom_simple_spinner, array_spinner);
		spinner.setAdapter(arrayadapter);

	}

	@SuppressLint("SimpleDateFormat")
	public void saveTask(View v) {
		ListView cats = (ListView) findViewById(R.id.taskCats);
		if (cats.getAdapter().getCount() == 0) {
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
			myAlertDialog.setTitle("--- Task ---");
			myAlertDialog.setMessage("Add some category to save data!");
			myAlertDialog.setPositiveButton("OK",AtlasData.ackClick);
			myAlertDialog.show();
			return;
		}
		
		TextView amount = (TextView) findViewById(R.id.taskAmount);
		TextView id = (TextView) findViewById(R.id.id);
		TextView tag = (TextView) findViewById(R.id.tag);
		TextView from = (TextView) findViewById(R.id.taskFrom);
		TextView to = (TextView) findViewById(R.id.taskTo);
		TextView date = (TextView) findViewById(R.id.taskDate);
		
		if ( id.getText().toString().equalsIgnoreCase("-1")) {
			tag.setText(from.getText()); // we need the same tag, as in the transaction the from
			/*
			 * if this is a manual transaction we need to insert to the transaction table
			 */
			int transid = (amount.getText().toString()+from.getText().toString()+to.getText().toString()).hashCode();
			AtlasParseSMSTask.insert(new Transaction(
					Math.abs(transid)*-1,		// negative "random" id
					amount.getText().toString(),
					from.getText().toString(),
					to.getText().toString(),
					date.getText().toString(),	// data format yyyy.MM.dd HH:mm
					String.valueOf(AtlasData.MANUAL_TRANSACTION),	// typeid
					null,	// smsid
					null)); // impossible hash
			id.setText(String.valueOf(Math.abs(transid)*-1)); // to be able to find this transaction when inserting data??
		}
		
		CatAddAdapter cat_adapter = (CatAddAdapter) cats.getAdapter();

		/*
		 * check data not to save with more amount or less amount
		 */
		Log.d("Atlas", "amount: " + amount.getText().toString());
		double sum = 0;
		for (int i = 0; i < cat_adapter.getCount(); i++) {
			sum += Double.valueOf(cat_adapter.getItem(i).getAmount());
		}
		if (sum > Double.valueOf(amount.getText().toString())) {
			AlertDialog.Builder toomuchdialog = new AlertDialog.Builder(this);
			toomuchdialog.setTitle("--- Task ---");
			toomuchdialog.setMessage("You can only categorize " + amount.getText() + "!");
			toomuchdialog.setPositiveButton("OK", AtlasData.ackClick);
			toomuchdialog.show();
			changeViewLevel(R.layout.detail_view, AtlasData.PSUMMARY, 
					(int)(-1*Double.valueOf(amount.getText().toString())));
		} else if ( sum < Double.valueOf(amount.getText().toString())) {
			AlertDialog.Builder toomuchdialog = new AlertDialog.Builder(this);
			toomuchdialog.setTitle("--- Task ---");
			toomuchdialog.setMessage("You should categorize all " + amount.getText() + "!");
			toomuchdialog.setPositiveButton("OK", AtlasData.ackClick);
			toomuchdialog.show();
			changeViewLevel(R.layout.detail_view, AtlasData.PSUMMARY, 
					(int)(-1*Double.valueOf(amount.getText().toString())));
		}else {
		
			if (cat_adapter.getCount() == 1) {
				/*
				 * if only one category, than I guess other transactions are
				 * also this one category
				 */
				AtlasParseSMSTask.insert(new Data(String.valueOf(tag.getText()), String.valueOf(id.getText()), cat_adapter
						.getItem(0).getId(), cat_adapter.getItem(0).getAmount(), String.valueOf(AtlasData.DONE)));
				Builder builder = new Builder();
				builder.scheme("content");
				builder.authority(AtlasData.DB_AUTHORITY);
				builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
				builder.appendPath("tasks");
				builder.appendPath("tag");
				builder.appendQueryParameter(AtlasData.DATA_COLUMNS[AtlasData.DATA_TAG], String.valueOf(tag.getText()));

				Cursor trswithtag = getContentResolver().query(builder.build(), AtlasData.TRANSACTIONS_COLUMNS, null,
						null, null);
				
				if (trswithtag != null && trswithtag.moveToFirst()) {
					do {
						if (!String.valueOf(id.getText()).equalsIgnoreCase(
								trswithtag.getString(AtlasData.TRANSACTIONS_ID)) && 
								trswithtag.getInt(AtlasData.TRANSACTIONS_STATUS) == 0) {
							AtlasParseSMSTask.insert(new Data(String.valueOf(tag.getText()), trswithtag
									.getString(AtlasData.TRANSACTIONS_ID), cat_adapter.getItem(0).getId(), trswithtag
									.getString(AtlasData.TRANSACTIONS_AMOUNT), String.valueOf(AtlasData.FRESH)));
						}
					} while (trswithtag.moveToNext());
					trswithtag.close();
				}
			} else {
				/*
				 * many categories...too complex transaction to suggest that all
				 * others are the same
				 */
				for (int i = 0; i < cat_adapter.getCount(); i++) {
					AtlasParseSMSTask.insert(new Data(String.valueOf(tag.getText()), String.valueOf(id.getText()), cat_adapter
							.getItem(i).getId(), cat_adapter.getItem(i).getAmount(), String.valueOf(AtlasData.DONE)));
				}
			}
			changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
		}
	}

	public void saveCategory(View v) {
		TextView seltext = (TextView) v.getRootView().findViewById(R.id.selectedParent);
		
		Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		builder.appendPath(AtlasData.TABLE_CATEGORIES);
		builder.appendPath("cat");
		if ( seltext.getTag() == null ) {
			seltext.setTag(String.valueOf(AtlasData.OUTCOME));
		}
		builder.appendPath((String)seltext.getTag());
		
		Cursor parent = getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);

		if (parent != null && parent.moveToFirst()) {
			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("childs");
			builder.appendPath((String) seltext.getTag());

			Cursor kids = getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS, null, null, null);

			String parentid = (String) seltext.getTag();
			String name = (String) ((TextView) v.getRootView().findViewById(R.id.chooseparentfor)).getText();
			String amount = (String) ((TextView) v.getRootView().findViewById(R.id.defaultAmount)).getText().toString();
			int id = Integer.valueOf(parentid) * (AtlasData.MAX_CAT_WIDTH+1) + kids.getCount() + 1;
			int depth = parent.getInt(AtlasData.CATEGORIES_DEPTH) - 1;

			Category newcat = new Category(String.valueOf(id), name, amount, String.valueOf(depth),
					AtlasData.getColor(parent), AtlasData.getColor(parent), AtlasData.getColor(parent));
			AtlasParseSMSTask.insert(newcat);
			
			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("parent");
			builder.appendPath((String)parentid);
			
			getContentResolver().update(builder.build(), null, null, null);

		}
		parent.close();
		changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
	}

	public void deleteTask(View v) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("--- Task ---");
		myAlertDialog.setMessage("Are you sure?");
		myAlertDialog.setPositiveButton("Yes", AtlasData.getDelTaskClick((TextView) findViewById(R.id.id)) );
		myAlertDialog.setNegativeButton("No", AtlasData.cancelClick);
		myAlertDialog.show();
		
		changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
	}

	public void ignoreTask(View v) {
		TextView id = (TextView) findViewById(R.id.id);
		Builder builder = new Builder();
		builder.scheme("content");
		builder.authority(AtlasData.DB_AUTHORITY);
		builder.appendPath(AtlasData.TABLE_TRANSACTIONS);
		builder.appendPath("ignore");
		builder.appendPath(String.valueOf(id.getText()));
		getContentResolver().update(builder.build(), null, null, null);

		changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
	}

	public void exitTask(View v) {
		changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
	}

	public void newEtwasClick(View v) {
		Log.d("Atlas", "newEtwasClick");
		TextView type = (TextView) v.getRootView().findViewById(R.id.new_etwas_text);
		EditText newe = (EditText) v.getRootView().findViewById(R.id.new_etwas);
		if ( newe.getText().toString().isEmpty() ) {
			return;
		}
		if (((String) type.getText()).contains(getResources().getString(R.string.category))) {
			Log.d("Atlas", "new category click");
			if (newe.getText().length() > 0) {
				changeViewLevel(R.layout.detailcat_view, AtlasData.peekPos().page);
			
				TextView newcat = (TextView) findViewById(R.id.chooseparentfor);
				newcat.setText(newe.getText().toString());
			}
		} else if (((String) type.getText()).contains(getResources().getString(R.string.amount))) {
			Log.d("Atlas", "new amount click");
			if (newe.getText().length() > 0) {
				changeViewLevel(R.layout.detail_view, AtlasData.peekPos().page,
						(int)(Math.round(Double.valueOf(newe.getText().toString())* -1)));
			}
		}
	}
	
	public OnClickListener onStartAtlasClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startAtlas(v);
		}
	};

	public void startAtlas(View v) {
		dialog.dismiss();
		AtlasData.config.edit().putBoolean(AtlasData.FIRST_RUN, false).commit();
		continueAtlasInit();
	}

	public void finishAtlas(View v) {
		dialog.dismiss();
		finish();
	}

	public void onMonthClick(View v) {
		AlertDialog.Builder picker = new AlertDialog.Builder(this);
		View pick = getLayoutInflater().inflate(R.layout.number_picker, null);
		Calendar now = new GregorianCalendar(TimeZone.getDefault());
		String[] date = AtlasData.config.getString(AtlasData.CONFIG_DATE, "1900.01").split("\\.");

		NumberPicker y = (NumberPicker) pick.findViewById(R.id.yearpicker);
		y.setMaxValue(now.get(Calendar.YEAR));
		y.setMinValue(1900);
		y.setValue(Integer.valueOf(date[0]));

		NumberPicker m = (NumberPicker) pick.findViewById(R.id.monthpicker);
		m.setMaxValue(12);
		m.setMinValue(1);
		m.setValue(Integer.valueOf(date[1]));

		picker.setView(pick);
		dialog = picker.create();
		dialog.show();
	}

	public void onMonthSelectOkClick(View v) {
		NumberPicker y = (NumberPicker) v.getRootView().findViewById(R.id.yearpicker);
		NumberPicker m = (NumberPicker) v.getRootView().findViewById(R.id.monthpicker);

		String date = String.valueOf(y.getValue()) + "." + String.valueOf(m.getValue());

		AtlasData.config.edit().putString(AtlasData.CONFIG_DATE, date).commit();
		changeViewLevel(R.layout.category_view, AtlasData.peekPos().page);
		dialog.dismiss();
	}
	
	public void onTrendSelectClick(View v) {
		changeViewLevel(R.layout.toplevel_view,AtlasData.peekPos().page);
	}
	
	public void onGoBackToCategoryClick(View v) {
		changeViewLevel(R.layout.category_view,AtlasData.peekPos().page);	
	}
}
 
 
