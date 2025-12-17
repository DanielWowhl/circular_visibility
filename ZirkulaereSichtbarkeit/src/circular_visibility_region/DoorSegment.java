package circular_visibility_region;

import java.util.ArrayList;
import java.util.List;

import drawtool.Edge;
import drawtool.MyPoint;

// a DoorSegment is where a reflexvertex cuts off parts of the Polygon from the linear visibility.
// note: a onEdge can be also a reflexvertex if it is co-linear with another vertex
public class DoorSegment {
	public int index, reflexVertexIndex; // index of reflex edge in original Polygon.
	public List<MyPoint> chain = new ArrayList<>();
	public MyPoint reflex, onEdge;
	public Edge door;
	public boolean ccw; // "0"-false is CW door, und "1"-true is CCW door.

	public DoorSegment(MyPoint reflex, MyPoint onEdge, boolean type, int index, List<Edge> poly) {
		this.reflex = poly.get(index % poly.size()).start2;
		this.onEdge = onEdge;
		this.index = index % poly.size();
		int u = onEdge.index;
		if (type) { // CWW door first onEdge, then reflex on the CCW defined Polygon.
			chain.add(onEdge);
			while (poly.get(u % poly.size()).start2.index != poly.get(index% poly.size()).start2.index) { // the while adds the chains last vertex respectively.
				u++;
				chain.add(poly.get(u % poly.size()).start2);
			}
			reflexVertexIndex = index% poly.size();
		} else { // CW door first the reflex vertex then comes the onEdge.
			chain.add(reflex);
			reflexVertexIndex = index % poly.size();
			while (poly.get(index % poly.size()).start2.index != (onEdge.index% poly.size())) {
				index++;
				chain.add(poly.get(index % poly.size()).start2);
			}
			chain.add(onEdge);
			
		}

		this.ccw = type;
		door = new Edge(reflex, onEdge);
	}
	
	public DoorSegment(Edge door, boolean type) {
		this.door = door;
		this.ccw = type;
		if (type) { // for the polygon to be in correct CCW order we need to save the vertices in
					// the correct order.
			this.reflex = door.end2;
			this.onEdge = door.start2;
		} else {
			this.reflex = door.start2;
			this.onEdge = door.end2;
		}
	}
	
	public String toString() {
		for(MyPoint p:chain) {
			System.out.println(p.toString());
		}
		if (!ccw)
		return getClass().getName() + " CW chain from:" + reflex.index +" to: " + onEdge.index +" Door coordinates: "+onEdge;
		return getClass().getName() + " CCW chain from:" + onEdge.index +" to: " + reflex.index+" Door coordinates: "+onEdge;
	}
}
