package hu.fnf.devel.atlas;

import android.view.View;
import android.widget.TextView;

public class InViewBehavior extends ViewBehavior {

	public InViewBehavior(IncomeFragment summaryFragment, View view) {
		super(AtlasData.INCOME, view);
		in = (TextView) summaryFragment.getView().findViewById(R.id.summaryIncome);
		in.setText("INCOME");
		load(AtlasData.INCOME, true);
	}
	
}
