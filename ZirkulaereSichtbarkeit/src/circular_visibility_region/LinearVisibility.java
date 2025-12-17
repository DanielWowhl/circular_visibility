package circular_visibility_region;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import drawtool.Edge;
import drawtool.MyPoint;

public class LinearVisibility {
//	private List<Edge> linearVis = new ArrayList<>();

	// List<Edge> poly is expects first Vertex to be visible.
	public static fullLinearResult calculateLinearVisibilityPolygon(List<Edge> poly, Point observer) {
		double cMax; // current maximum
		List<MyPoint> linearVis = new ArrayList<>();
		List<DoorSegment> doors = new ArrayList<>();
		IntersectionRes intRes;
		DoorSegment doorSeg;
		MyPoint addedPoint;
		int n = poly.size();
		// set the angles.
		poly = Calculator.calculateVertexAngles(poly, observer);
		// follow the Polygon in CCW order and add the visible, or partially visible
		// edges to a list.

		// do the first Vertex
		cMax = poly.get(0).start2.angle;
		// add first Vertex to list and save its index
		addedPoint = poly.get(0).start2;
		addedPoint.index = 0;
		linearVis.add(addedPoint);
		int j = 1; // if we have a CCW door in the first vertex, then we skip to vertex j.
		if (isBeforeCCW(poly.get(1).start2.angle, cMax)) { // we can have only a CCW door here, since a CW door would
															// imply
			// our first Vertex could not be visible
			// calculate the first intersection

			intRes = findDoor(observer, poly.get(0).start, poly, 1);
			// save the found CCW door
			doorSeg = new DoorSegment(poly.get(0).start2, intRes.intersection, false, 0, poly);
			doors.add(doorSeg);
			// add both door vetizes, first reflex, as it is a CCW door
			addedPoint = doorSeg.reflex;
			addedPoint.index = 0;
			linearVis.add(addedPoint);
			// add the on Edge vertex, with the same index as the edge it sits on
			addedPoint = doorSeg.onEdge;
			addedPoint.index = intRes.vertexNumber;
			linearVis.add(addedPoint);

			// update i in loop
			j = intRes.vertexNumber + 1;
		} else {
			addedPoint = poly.get(1).start2;
			addedPoint.index = 1;
			linearVis.add(addedPoint);
		}
		for (int i = j; i < poly.size(); i++) {
			// set Angle
			cMax = poly.get(i).start2.angle;
			// if cMax > then next || or cMax < then next+180 % 360 -> we are going back in
			// terms of angle.
			if (isBeforeCCW(poly.get(i).end2.angle, cMax)) {

				// if reflex, then we have a CCW door.
				if (poly.get(i).start2.isReflex) {
					
					// calculate the first intersection for i+2 till size (i mean use i+1), by
					// saving it
					intRes = findDoor(observer, poly.get(i).start, poly, (i + 1));
					// save the found CCW door
					doorSeg = new DoorSegment(poly.get(i).start2, intRes.intersection, false, i, poly);
					doors.add(doorSeg);
					// add both door vertices, first reflex, as it is a CCW door
					addedPoint = doorSeg.reflex;
					addedPoint.index = i;
					linearVis.add(addedPoint);
					// add the on Edge vertex, with the same index as the edge it sits on
					addedPoint = doorSeg.onEdge;
					addedPoint.index = intRes.vertexNumber;
					linearVis.add(addedPoint);
					// update i in loop

					i = intRes.vertexNumber;
					// i++ comes next
				}
				// else, we need to find the reflexvertex that is visible and the CW door
				else {
					// CW door
					// if (!(reflexvertex && next >= reflexvertex))
					int error=0; // for safety
					while (!(Calculator.isVertexVisible(poly, observer, poly.get((i)%n).start2))||(Calculator.calculation_of_angle(poly.get((i - 1+n)%n).start2, poly.get((i) % poly.size()).start2,
							poly.get((i) % poly.size()).end2) < 180
							|| isBeforeCCW(poly.get((i) % poly.size()).end2.angle,
									poly.get((i) % poly.size()).start2.angle))) {
						// System.out.println(poly.get((i)%n).start2+"is "+ Calculator.isVertexVisible(poly, observer, poly.get((i)%n).start2));
						i++;
						error++;
						if (error == n+1) {
							throw new IllegalArgumentException("Point not in Polygon!");
						}
					}
					// else we have found a possible door
					intRes = findDoor(observer, poly.get((i) % poly.size()).start, poly, 0); // we check the entire
																								// polygon... or we
					doorSeg = new DoorSegment(poly.get((i) % poly.size()).start2, intRes.intersection, true,
							(i % poly.size()), poly);
					doors.add(doorSeg);
					// remove from linear visibility all vertices that are hidden by the reflex
					// vertex
					List<MyPoint> removeList = new ArrayList<>();
					for (MyPoint s : linearVis) {
						if (s.index > intRes.vertexNumber && s.index < i) {
							removeList.add(s);
						}
					}
					for (MyPoint s : removeList)
						linearVis.remove(s);
					doors = removeDoors(doors);

					// add both door vetizes
					addedPoint = intRes.intersection;
					addedPoint.index = intRes.onEdge.start2.index + 1; // +1, weil der onEdge und start2 sichtbar sind
					linearVis.add(addedPoint);
					addedPoint = doorSeg.reflex;
					addedPoint.index = i % poly.size();
					linearVis.add(addedPoint);

				}
			} else {
				// add Edge
				addedPoint = poly.get(i).start2;
				addedPoint.index = i;
				linearVis.add(addedPoint);
			}

		}
		// remove dublicates.
		linearVis = removeDoubles(linearVis);
		fullLinearResult res = new fullLinearResult(calculateLinearPoly(linearVis), doors);
		return res;
	}


	private static List<DoorSegment> removeDoors(List<DoorSegment> doors) {
		List<DoorSegment> removeDoors = new ArrayList<>();
		for (DoorSegment d : doors) {
			if (doors.get(doors.size() - 1).onEdge.index < d.onEdge.index)
				removeDoors.add(d);
		}
		for (DoorSegment d : removeDoors)
			doors.remove(d);
		return doors;
	}

	private static List<MyPoint> removeDoubles(List<MyPoint> linearVis) {
		List<MyPoint> removeList = new ArrayList<>();
		for (int i = 0; i < linearVis.size(); i++) {
			if (linearVis.get(i).x == linearVis.get((i + 1) % linearVis.size()).x
					&& linearVis.get(i).y == linearVis.get((i + 1) % linearVis.size()).y) {
				removeList.add(linearVis.get(i));
			}
		}
		for (MyPoint s : removeList)
			linearVis.remove(s);
		return linearVis;
	}


	private static List<Edge> calculateLinearPoly(List<MyPoint> linearVis) {
		List<Edge> linearVisPoly = new ArrayList<>();

		if (linearVis == null || linearVis.size() < 2) {
			return linearVisPoly; // not enough points for edges
		}

		for (int i = 0; i < linearVis.size(); i++) {
			MyPoint start = linearVis.get(i);
			MyPoint end = linearVis.get((i + 1) % linearVis.size()); // wrap around
			linearVisPoly.add(new Edge(start, end));
		}

		return linearVisPoly;
	}

	// calculates true if, from a static observer calculated angles,
	public static boolean isBeforeCCW(double thetaA, double thetaB) {

		// compute CCW difference from A to B
		double diff = (thetaB - thetaA + 360) % 360;

		// if diff > 0 and < 180, then B is after A in CCW direction
		return diff > 0 && diff < 180;
	}
	// implemented with help of ChatGPT
	public static IntersectionRes rayIntersectsEdge(Point observer, Point testVert, Edge edge) {
		double dxRay = testVert.x - observer.x;
		double dyRay = testVert.y - observer.y;

		double dxEdge = edge.end.x - edge.start.x;
		double dyEdge = edge.end.y - edge.start.y;

		// Kreuzprodukt zur Prüfung auf Parallelität
		double det = dxRay * (-dyEdge) - dyRay * (-dxEdge);
		if (Math.abs(det) < 1e-9) {
			// Strahl und Kante sind parallel -> kein Schnitt
			return new IntersectionRes(false, null, Double.NaN, null);
		}

		// LGS lösen für t und u
		double dxStart = edge.start.x - observer.x;
		double dyStart = edge.start.y - observer.y;

		double t = (dxStart * (-dyEdge) - dyStart * (-dxEdge)) / det;
		double u = (dxRay * dyStart - dyRay * dxStart) / det;

		// Bedingungen für echten Schnitt
		if (t >= 0 && u >= 0 && u < 1) {
			double ix = observer.x + t * dxRay;
			double iy = observer.y + t * dyRay;
			return new IntersectionRes(true, new MyPoint(ix, iy, edge.start2.index), t, edge);
		}

		return new IntersectionRes(false, null, Double.NaN, null);
	}

	// finds the two door points
	public static IntersectionRes findDoor(Point observer, Point testVert, List<Edge> p, int start) {
		List<IntersectionRes> candidateList = new ArrayList<>();
		IntersectionRes intersectionResult = null;

		for (int i = start; i < p.size(); i++) {
			intersectionResult = rayIntersectsEdge(observer, testVert, p.get(i));
			if (intersectionResult.intersects) { // we have an intersection with the polygon.
				if (intersectionResult.tParam > 1.0) { // we add the door, even if we know it will be deleted
					intersectionResult.vertexNumber = i;
					candidateList.add(intersectionResult);
				}
			}
		}
		// return closest edge, that is the one with the door.
		if (candidateList.isEmpty()) {
	        throw new NoSuchElementException("No Intersectionpoint, for a door, found!");
	    }
		intersectionResult = candidateList.get(0);
		for (IntersectionRes a : candidateList) {
			if (intersectionResult.tParam > a.tParam)
				intersectionResult = a;
		}
		return intersectionResult;
	}

}
