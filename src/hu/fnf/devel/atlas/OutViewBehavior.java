package hu.fnf.devel.atlas;

import android.view.View;
import android.widget.TextView;

public class OutViewBehavior extends ViewBehavior {

	public OutViewBehavior(OutcomeFragment fragment, View view) {
		super(view);
		out = (TextView) fragment.getView().findViewById(R.id.summaryOutcome);

		load(AtlasData.OUTCOME, true);
	}
}
