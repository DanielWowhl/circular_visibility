package circular_visibility_region;

import drawtool.MyPoint;

public class Cap {
	public final boolean isConvex;
	public final boolean inCCWpocket;
	public final MyPoint s1; // s2 for convex, s4 for concave
	public final MyPoint s2;
	public final MyPoint s3;
	public final MyPoint s4;
	public MyPoint q; // exit point
	public int pocketNr;
	
	// depending on the type of cap we use variables s1,s2, or s3,s4.
	public Cap(boolean isConvex, boolean inCCWpocket, MyPoint s1Ors3,
			MyPoint s2Ors4, MyPoint q) {
		if(isConvex) {
			
			this.s1 = s1Ors3;
			this.s2 = s2Ors4;
			this.s3 = null;
			this.s4 = null;
		}
		else {
	
			this.s1 = null;
			this.s2 = null;
			this.s3 = s1Ors3;
			this.s4 = s2Ors4;
		}
		this.inCCWpocket = inCCWpocket;
		this.isConvex = isConvex;
		this.q = q;
	}

	public String toString() {
		if(isConvex)
		return String.format("Cap(convex=%b, \n s1=%s, \n\t s2=%s, \n\t\t q=%s)", isConvex, s1, s2, q);
		return String.format("Cap(convex=%b, \n s3=%s, \n\t s4=%s, \n\t\t q=%s)", isConvex, s3, s4, q);
	}
}