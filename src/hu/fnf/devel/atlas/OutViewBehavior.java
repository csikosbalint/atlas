package hu.fnf.devel.atlas;

import hu.fnf.devel.atlas.base.AtlasView;

import java.util.ArrayList;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.widget.TextView;

public class OutViewBehavior extends ViewBehavior {

	public OutViewBehavior(OutcomeFragment fragment) {
		((TextView) fragment.getView().findViewById(R.id.summaryIncome)).setText("INCOME");
		((TextView) fragment.getView().findViewById(R.id.summaryOutcome)).setText("OUTCOME");
	}

	@Override
	public void draw(Canvas canvas, AtlasView view) {
		rectf = new RectF(view.getHeight() / 4, view.getHeight() / 4,
				view.getHeight() / 4, view.getHeight() / 4);
		rectf.set(canvas.getHeight() / 8, canvas.getHeight() / 8,
				canvas.getHeight(), canvas.getHeight());
		Log.d("CategoryView", rectf.width() + " " + rectf.height());
		float start = 0;
		int i = 0;
		for (Map.Entry<Integer, Double> entry : data.entrySet()) {
			i++;
			double sum = getSum();
			int catid = entry.getKey();
			double catsum = entry.getValue();
			Log.d("CategoryView", String.valueOf(data.size()) + "/" + i + ":"
					+ String.valueOf(catsum + " of " + 360 / sum * catsum));
			float end = (float) (catsum / sum);

			rgb = getColorRGB(getHue(catid), getSaturation(catid) / 100f, 0.55f);
			paint.setColor(Color.argb(255, rgb.get(0), rgb.get(1), rgb.get(2)));

			canvas.drawArc(rectf, start, end, true, paint);
			start += end;
		}
	}

	@Override
	public ArrayList<Integer> getPieTypes() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ret.add(AtlasData.INCOME);
		return ret;
	}
}
