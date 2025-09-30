package circular_visibility_region;


import drawtool.Edge;
import drawtool.MyPoint;

public class IntersectionRes {
	public int vertexNumber=-1;
	public Edge onEdge;
	public boolean intersects;
	public MyPoint intersection;
	public double tParam; // Parameter entlang des Strahls observer->testVert

    IntersectionRes(boolean intersects, MyPoint intersection, double tParam, Edge onEdge) {
        this.intersects = intersects;
        this.intersection = intersection;
        this.tParam = tParam;
        this.onEdge = onEdge;
    }
    public IntersectionRes() {
		
	}
	public String toString() {
        return getClass().getName() + "[intersect=" + intersects + ",at=" + intersection +",pitch "+tParam +"]";
    }
}
