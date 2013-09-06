package hu.fnf.devel.atlas;

import android.view.View;
import android.widget.TextView;

public class OutViewBehavior extends ViewBehavior {

	public OutViewBehavior(OutcomeFragment fragment, View view) {
		super(AtlasData.OUTCOME, view);
		out = (TextView) fragment.getView().findViewById(R.id.summaryOutcome);
		out.setText("OUTCOME");
		load(AtlasData.OUTCOME, true);
	}
}
