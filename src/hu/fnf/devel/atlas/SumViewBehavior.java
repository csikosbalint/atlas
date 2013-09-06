package hu.fnf.devel.atlas;

import android.view.View;
import android.widget.TextView;

public class SumViewBehavior extends ViewBehavior {

	public SumViewBehavior(SummaryFragment summaryFragment, View view) {
		super(AtlasData.MIXED, view);
		in = (TextView) summaryFragment.getView().findViewById(R.id.summaryIncome);
		out = (TextView) summaryFragment.getView().findViewById(R.id.summaryOutcome);
		in.setText("INCOME");
		out.setText("OUTCOME");
		load(AtlasData.INCOME, false);
		load(AtlasData.OUTCOME, false);
	}
}
