package circular_visibility_region;

import drawtool.MyPoint;

public class Cap {
	public final boolean isConvex;
	public final MyPoint s1; // s2 for convex, s4 for concave
	public final MyPoint q; // exit point
	public final MyPoint s2;

	public Cap(boolean isConvex, MyPoint s1,
			MyPoint s2, MyPoint q) {
		this.isConvex = isConvex;
		this.s1 = s1;
		this.s2 = s2;
		this.q = q;
	}

	public String toString() {
		return String.format("Cap(convex=%b, \n s1=%s, \n\t s2=%s, \n\t\t q=%s)", isConvex, s1, s2, q);
	}
}