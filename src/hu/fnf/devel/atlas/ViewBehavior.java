package hu.fnf.devel.atlas;

import hu.fnf.devel.atlas.base.AtlasView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public abstract class ViewBehavior {
	final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	protected RectF rectf = null;
	protected Vector<Integer> rgb = null;
	
	protected WeakHashMap<Integer, Double> data = new WeakHashMap<Integer, Double>();
	
	public abstract void draw(Canvas canvas, AtlasView atlasView);
	public abstract ArrayList<Integer> getPieTypes();

	public void addData(int catid, double increm) {
		if ( data.containsKey(catid) ) {
			double append = data.get(catid);
			data.put(catid, increm+append);
		} else {
			data.put(catid, increm);
		}
	}
	
	protected double getSum() {
		double ret = 0;
		for (Map.Entry<Integer, Double> e: data.entrySet()) {
			ret += e.getValue();
		}
		return ret;
	}
	
	protected Vector<Integer> getColorRGB(float hue, float saturation, float value) {
		float r, g, b;

		int h = (int) (hue * 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);

		if (h == 0) {
			r = value;
			g = t;
			b = p;
		} else if (h == 1) {
			r = q;
			g = value;
			b = p;
		} else if (h == 2) {
			r = p;
			g = value;
			b = t;
		} else if (h == 3) {
			r = p;
			g = q;
			b = value;
		} else if (h == 4) {
			r = t;
			g = p;
			b = value;
		} else if (h == 5) {
			r = value;
			g = p;
			b = q;
		} else {
			throw new RuntimeException(
					"Something went wrong when converting from HSV to RGB. Input was "
							+ hue + ", " + saturation + ", " + value);
		}
		Log.d("Categoryview", "Input was " + hue + ", " + saturation + ", "
				+ value);
		Vector<Integer> ret = new Vector<Integer>();

		ret.add(Math.round(r * 256));
		ret.add(Math.round(g * 256));
		ret.add(Math.round(b * 256));

		return ret;
	}
	
	protected int getSaturation(int catid) {
		String num = String.valueOf(catid);
		char[] digits1 = num.toCharArray();
		String ret;

		if (digits1.length > 2) {
			ret = new StringBuilder().append(digits1[1]).append(digits1[2])
					.toString();
		} else if ( digits1.length == 2 ){
			ret = new StringBuilder().append(digits1[1]).append('0').toString();
		} else {
			ret = "50";
		}
		return 100-(Integer.valueOf(ret));
	}
	
	protected float getHue(int catid) {
		String num = String.valueOf(catid);
		char[] digits1 = num.toCharArray();
		String ret;
		float in = 0.580f;
		float out = 0.380f;
		
		if (digits1.length > 2) {
			ret = new StringBuilder().append(digits1[1]).append(digits1[2])
					.toString();
		} else if ( digits1.length == 2) {
			ret = new StringBuilder().append(digits1[1]).append('0').toString();
		} else {
			ret = "0";
		}
		switch (digits1[0]) {
		case '1':	
			return in + Float.valueOf(ret)/300;
		case '2':	
			return out + Float.valueOf(ret)/300;
		default:
			break;
		}
		
		return 0;
	}
}
