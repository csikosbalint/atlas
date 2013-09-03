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

import net.sqlcipher.database.SQLiteDatabase;
import hu.fnf.devel.atlas.backend.AtlasParseSMSTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AtlasIncomingSMSReceiver extends BroadcastReceiver {
	
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AtlasIncomingSMSReceiver","new sms received " + intent.getAction());
		String uuid = null;
		try {
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (tManager.getDeviceId() != null) {
				Log.d("AtlasIncomingSMSReceiver", "SN:" + String.format("%1$24s", tManager.getDeviceId()));
				uuid = String.format("%1$24s", tManager.getDeviceId()); // DESede
																				// key
																				// is
																				// 24
																				// bytes
			} else {
				uuid = "iereileiphah3Eihoh8EeH2a";
			}
		} catch (Exception e) {
			Log.e("Atlas", "no such algorithm " + e.getMessage());
			e.printStackTrace();
		}
		
		
		AtlasData data = new AtlasData(AtlasData.CATEGORY, AtlasData.PSUMMARY, uuid);
		Context app = null;
		
		SQLiteDatabase.loadLibs(context);
		
		try {
			app = context.createPackageContext("hu.fnf.devel.atlas", Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e("AtlasIncomingSMSReceiver","Cannot access config prefs of Atlas!");
			e.printStackTrace();
		}
		new AtlasParseSMSTask(app).execute();
	}

}
