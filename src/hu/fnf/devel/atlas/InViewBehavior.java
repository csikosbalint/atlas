package hu.fnf.devel.atlas;

import java.util.ArrayList;

import android.widget.TextView;

public class InViewBehavior extends ViewBehavior {

	public InViewBehavior(IncomeFragment summaryFragment) {
		in = (TextView) summaryFragment.getView().findViewById(R.id.summaryIncome);
		in.setText("INCOME");
	}
	
	@Override
	public ArrayList<Integer> getPieTypes() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ret.add(AtlasData.OUTCOME);
		return ret;
	}
}
