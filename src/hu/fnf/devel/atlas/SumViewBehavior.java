package hu.fnf.devel.atlas;

import java.util.ArrayList;

import android.widget.TextView;

public class SumViewBehavior extends ViewBehavior {

	public SumViewBehavior(SummaryFragment summaryFragment) {
		in = (TextView) summaryFragment.getView().findViewById(R.id.summaryIncome);
		out = (TextView) summaryFragment.getView().findViewById(R.id.summaryOutcome);
		in.setText("INCOME");
		out.setText("OUTCOME");
	}
	
	@Override
	public ArrayList<Integer> getPieTypes() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ret.add(AtlasData.INCOME);
		ret.add(AtlasData.OUTCOME);
		return ret;
	}
}
