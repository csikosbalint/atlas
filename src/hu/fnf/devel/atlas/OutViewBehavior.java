package hu.fnf.devel.atlas;

import java.util.ArrayList;

import android.widget.TextView;

public class OutViewBehavior extends ViewBehavior {

	public OutViewBehavior(OutcomeFragment fragment) {
		out = (TextView) fragment.getView().findViewById(R.id.summaryOutcome);
		out.setText("OUTCOME");
	}

	@Override
	public ArrayList<Integer> getPieTypes() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ret.add(AtlasData.INCOME);
		return ret;
	}
}
