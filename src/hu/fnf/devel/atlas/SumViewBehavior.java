package hu.fnf.devel.atlas;

import hu.fnf.devel.atlas.base.AtlasView;

import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;
import android.widget.TextView;

public class SumViewBehavior extends ViewBehavior {
	
	public SumViewBehavior(SummaryFragment summaryFragment) {
		((TextView) summaryFragment.getView().findViewById(R.id.summaryIncome)).setText("INCOME");
		((TextView) summaryFragment.getView().findViewById(R.id.summaryOutcome)).setText("OUTCOME");
	}
	
	@Override
	public void draw(Canvas canvas, AtlasView view) {
		rectf = new RectF(view.getHeight() / 4, view.getHeight() / 4,view.getHeight() / 4, view.getHeight() / 4);
		rectf.set(canvas.getHeight() / 8, canvas.getHeight() / 8,
				canvas.getHeight(), canvas.getHeight());
		Log.d("CategoryView", rectf.width() + " " + rectf.height());
		float start = 0;
		int i = 0;
		for (Map.Entry<Integer, Double> entry: data.entrySet()) {
			i++;
			double sum = getSum();
			int catid = entry.getKey();
			double catsum = entry.getValue();
			Log.d("CategoryView",
					String.valueOf(data.size()) + "/" + i + ":"
							+ String.valueOf(catsum + " of " + 360 / sum
							* catsum ));
			float end = (float) (catsum / sum);
//			switch (type) {
//			case AtlasData.INCOME:
//				rgb = AtlasData.getColorRGB(AtlasData.getHue(catid.get(i)),
//						AtlasData.getSaturation(catid.get(i)) / 100f, 0.55f);
//				paint.setColor(Color.argb(255, rgb.get(0), rgb.get(1),
//						rgb.get(2)));
//				break;
//			case AtlasData.OUTCOME:
//				rgb = AtlasData.getColorRGB(AtlasData.getHue(catid.get(i)),
//						AtlasData.getSaturation(catid.get(i)) / 100f, 0.55f);
//				paint.setColor(Color.argb(255, rgb.get(0), rgb.get(1),
//						rgb.get(2)));
//				break;
//			default:
				rgb = getColorRGB(getHue(catid),getSaturation(catid) / 100f, 0.55f);
				paint.setColor(Color.argb(255, rgb.get(0), rgb.get(1),rgb.get(2)));

//				break;
//			}
			canvas.drawArc(rectf, start, end, true, paint);
			start += end;
		}
	}



	@Override
	public Vector<Integer> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void load() {
		for (int pietype : viewBehavior.getTypes()) {

			Uri.Builder builder = null;

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_CATEGORIES);
			builder.appendPath("nodes");
			builder.appendPath(String.valueOf(String.valueOf(pietype)));
			
			Cursor nodes = ((Activity) this.getContext()).getApplication().getContentResolver().query(builder.build(), AtlasData.CATEGORIES_COLUMNS,
					null, null, null);

			builder = new Builder();
			builder.scheme("content");
			builder.authority(AtlasData.DB_AUTHORITY);
			builder.appendPath(AtlasData.TABLE_DATA);
			builder.appendPath("summa");

			double all = 0;
			if (nodes != null && nodes.moveToFirst()) {
				do {
					builder = new Builder();
					builder.scheme("content");
					builder.authority(AtlasData.DB_AUTHORITY);
					builder.appendPath(AtlasData.TABLE_DATA);
					builder.appendPath("summa");

					String catid = String.valueOf(nodes.getInt(AtlasData.CATEGORIES_ID));
					builder.appendPath(catid);

					builder.appendQueryParameter(AtlasData.TRANSACTIONS_COLUMNS[AtlasData.TRANSACTIONS_DATE],
							String.valueOf(getMonthStartUnixTime(getView())));
					Cursor items = ((Activity) this.getContext()).getApplication().getContentResolver().query(builder.build(),
							AtlasData.TRANSACTIONS_COLUMNS, null, null, null);
					TextView title = null;
					switch (pietype) {
					case AtlasData.INCOME:
					case AtlasData.ALLINCOME:
						title = (TextView) getView().findViewById(R.id.summaryIncome);
						pietype = AtlasData.INCOME;
						break;
					case AtlasData.OUTCOME:
					case AtlasData.ALLOUTCOME:
						title = (TextView) getView().findViewById(R.id.summaryOutcome);
						pietype = AtlasData.OUTCOME;
						break;
					default:
						break;

					}
					double sum = 0;
					if (items != null && items.moveToFirst()) {
						do {
							double catdata = items.getDouble(0);
							sum += catdata;
						} while (items.moveToNext());
						items.close();
					}
					if (sum > 0.0) {
						if (pie.type != -1) {
							Log.d("CatFragment", "cat: " + catid + " amount: " + sum);
							pie.setData(sum);
							pie.addCatid(Integer.valueOf(catid));
							String append = title.getText().toString();
							title.setText(append
									+ "\n"
									+ AtlasData.getCatName(getActivity().getApplicationContext(),
											Integer.valueOf(catid)) + " (" + String.valueOf(sum) + ")");
						} else {
							all += sum;
						}
					}
				} while (nodes.moveToNext());
				pie.setData(all);
				pie.addCatid(pietype);
			}
		}
	}


}
