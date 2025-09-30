package drawtool;

import java.awt.Point;

public class Edge {
	public MyPoint start2 = new MyPoint();
	public MyPoint end2 = new MyPoint();
	public Point start = new Point();
	public Point end = new Point();

	public Edge(MyPoint start, MyPoint end) {
		this.start2 = start;
		this.end2 = end;
		this.start.x = start.x;
		this.start.y = start.y;
		this.end.x = end.x;
		this.end.y = end.y;
	}
	public Edge(Edge newE) {
		this.start2 = new MyPoint(newE.start2);
		this.end2 = new MyPoint(newE.end2);
		this.start = new Point(newE.start);
		this.end = new Point(newE.end);
	}

	public Edge(Point intPoint, Point intPoint2) {
		this.start = intPoint;
		this.end = intPoint2;
		this.start2 = new MyPoint(intPoint.x  ,intPoint.y);
		this.end2 = new MyPoint(intPoint2.x,intPoint2.y);
	}

	@Override
	public String toString() {
		if(start2.index != -1)
			return "Edge from index " +start2.index+ " " + start + " to " + end;
		return "Edge from " + start + " to " + end;
	}
}