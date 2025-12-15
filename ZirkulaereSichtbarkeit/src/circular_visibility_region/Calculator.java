package circular_visibility_region;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import drawtool.Edge;
import drawtool.MyPoint;

public class Calculator {
	 private static final double EPS = 0; // 1e-12 has error with cicle poly intersection, taking the endpoint as result off by more then eps.

	 
	 
	 
	public static Circle circumCircle(MyPoint a, MyPoint b, MyPoint c) {
		double d = 2 * (a.x_coordinate * (b.y_coordinate - c.y_coordinate) + b.x_coordinate * (c.y_coordinate - a.y_coordinate) + c.x_coordinate * (a.y_coordinate - b.y_coordinate));
		if (Math.abs(d) < 1e-9)
			throw new IllegalArgumentException("Kollinear!");

		double ux = ((a.x_coordinate * a.x_coordinate + a.y_coordinate * a.y_coordinate) * (b.y_coordinate - c.y_coordinate) + (b.x_coordinate * b.x_coordinate + b.y_coordinate * b.y_coordinate) * (c.y_coordinate - a.y_coordinate)
				+ (c.x_coordinate * c.x_coordinate + c.y_coordinate * c.y_coordinate) * (a.y_coordinate - b.y_coordinate)) / d;

		double uy = ((a.x_coordinate * a.x_coordinate + a.y_coordinate * a.y_coordinate) * (c.x_coordinate - b.x_coordinate) + (b.x_coordinate * b.x_coordinate + b.y_coordinate * b.y_coordinate) * (a.x_coordinate - c.x_coordinate)
				+ (c.x_coordinate * c.x_coordinate + c.y_coordinate * c.y_coordinate) * (b.x_coordinate - a.x_coordinate)) / d;

		MyPoint center = new MyPoint(ux, uy);
		double r = Math.hypot(center.x_coordinate - a.x_coordinate, center.y_coordinate - a.y_coordinate);
		return new Circle(center, r);
	}


	public static List<Edge> calculateVertexAngles(List<Edge> edges, Point observer) {

		for (Edge edge : edges) {
			MyPoint p = edge.start2;
			double dx = p.x_coordinate - observer.x;
			double dy = p.y_coordinate - observer.y;

			// CW: negate the atan2
			double angleRad = -Math.atan2(dy, dx);
			double angleDeg = Math.toDegrees(angleRad);

			if (angleDeg < 0) {
				angleDeg += 360; // normalize to [0, 360)
			}

			p.angle = angleDeg;
		}

		return edges;
	}

	public static IntersectionRes rayIntersectsEdge(Point observer, MyPoint testVert, Edge edge) {
		double dxRay = testVert.x_coordinate - observer.x;
		double dyRay = testVert.y_coordinate - observer.y;

		double dxEdge = edge.end2.x_coordinate - edge.start2.x_coordinate;
		double dyEdge = edge.end2.y_coordinate - edge.start2.y_coordinate;

		// Kreuzprodukt zur Prüfung auf Parallelität
		double det = dxRay * (-dyEdge) - dyRay * (-dxEdge);
		if (Math.abs(det) < 1e-9) {
			// Strahl und Kante sind parallel -> kein Schnitt
			return new IntersectionRes(false, null, Double.NaN, null);
		}

		// LGS lösen für t und u
		double dxStart = edge.start2.x_coordinate - observer.x;
		double dyStart = edge.start2.y_coordinate - observer.y;

		double t = (dxStart * (-dyEdge) - dyStart * (-dxEdge)) / det;
		double u = (dxRay * dyStart - dyRay * dxStart) / det;

		// Bedingungen für echten Schnitt
		if ( u >= 0 && u < 1) {
			double ix = observer.x + t * dxRay;
			double iy = observer.y + t * dyRay;
			return new IntersectionRes(true, new MyPoint(ix, iy, edge.start2.index), t, edge);
		}

		return new IntersectionRes(false, null, Double.NaN, null);
	}
	
	public static boolean isVertexVisible(List<Edge> poly, Point o, MyPoint vertex) {
		IntersectionRes test = new IntersectionRes();
		for (Edge testedEdge : poly) {
			// skip the edges that share the tested Vertex. Tested Vertex: start
			if (!(vertex == testedEdge.start2 || vertex == testedEdge.end2)) {
				test = rayIntersectsEdge(o, vertex, testedEdge);
				//
				if (test.intersects && (test.tParam < 1 && test.tParam > 0)) {
					return false; // vertex is not visible, as there is an edge covering it.
				}
			}
		}
		return true;
	}
	// Finds and reorders the Polygon for first found lineary visible Vertex. Note:
	// taking the edge with the closest parameter makes this run linear.
	public static List<Edge> findFirstVisibleVertex(List<Edge> poly, Point o) {

		IntersectionRes test = new IntersectionRes();
		for (Edge currentEdge : poly) {
			boolean hidden = false; // Vertex hidden by an edge
			for (Edge testedEdge : poly) {
				// skip the edges that share the tested Vertex. Tested Vertex: start
				if (currentEdge.start != testedEdge.start && currentEdge.start != testedEdge.end) {
					test = rayIntersectsEdge(o, currentEdge.start2, testedEdge);
					//
					if (test.intersects && (test.tParam < 1 && test.tParam > 0)) {
						hidden = true;
						break; // test next edge
					}
				}
			}
			if (!hidden) {
				// reorder List
				List<Edge> res = new ArrayList<Edge>();
				for (int i = poly.indexOf(currentEdge); i < poly.size(); i++) {
					res.add(poly.get(i));
				}
				for (int i = 0; i < poly.indexOf(currentEdge); i++) {
					res.add(poly.get(i));
				}
				return res;
			}
		}
		System.out.print("ERROR IN FINDING A VISIBLE VERTEX");
		return null;
		// nothing found, pls help! no edge visible!
	}

	public static double calculation_of_angle(MyPoint p1, MyPoint p2, MyPoint p3) {
		// Vektoren
		double v1x = p1.x_coordinate - p2.x_coordinate;
		double v1y = p1.y_coordinate - p2.y_coordinate;

		double v2x = p3.x_coordinate - p2.x_coordinate;
		double v2y = p3.y_coordinate - p2.y_coordinate;

		// Skalarprodukt und Kreuzprodukt
		double dot = v1x * v2x + v1y * v2y;
		double cross = v1x * v2y - v1y * v2x;

		// Winkel berechnen (in Radiant)
		double angleRad = Math.atan2(cross, dot);

		// Umwandeln in Grad
		double angleDeg = Math.toDegrees(angleRad);

		// Normieren auf [0, 360)
		if (angleDeg < 0) {
			angleDeg += 360;
		}

		return angleDeg;
	}

	public static IntersectionRes findFirst3aSegment(List<Edge> poly, MyPoint o, MyPoint s2) {

		IntersectionRes test = new IntersectionRes();
		IntersectionRes res = new IntersectionRes();
		res.tParam = Double.POSITIVE_INFINITY;
		for (Edge testedEdge : poly) {
			// skip the edges that share the tested Vertex. Tested Vertex: start

			test = rayIntersectsEdge(s2, o, testedEdge);
			if (test.intersects)
				if (test.tParam >= 1)
					if (res.tParam > test.tParam) {
						res = test;
						res.vertexNumber = testedEdge.start2.index;
					}
		}
		return res;
	}
	
	

    public static List<MyPoint> intersectCircleSegment(Circle circle, MyPoint p0, MyPoint p1) {

    	    List<MyPoint> intersections = new ArrayList<>();
    	    double cx = circle.center.x_coordinate;
    	    double cy = circle.center.y_coordinate;
    	    double r = circle.radius;
//    	    double EPS = 1e-6; // kleine Toleranz

    	    // Segment-Richtungsvektor
    	    double dx = p1.x_coordinate - p0.x_coordinate;
    	    double dy = p1.y_coordinate - p0.y_coordinate;
    	    double segLenSq = dx*dx + dy*dy;

    	    if (segLenSq < 1e-10) {
    	        // Degeneriertes Segment: prüfe nur den Punkt
    	        double dist = Math.hypot(p0.x_coordinate - cx, p0.y_coordinate - cy);
    	        if (Math.abs(dist - r) < 0) intersections.add(p0);
    	        return intersections;
    	    }

    	    // 2. Abstand vom Kreismittelpunkt zur Geraden
    	    // d = |(p1 - p0) x (p0 - C)| / |p1 - p0|
    	    double cross = (dx)*(p0.y_coordinate - cy) - (dy)*(p0.x_coordinate - cx);
    	    double d = Math.abs(cross) / Math.sqrt(segLenSq);

    	    if (d > r ) {
    	        // Keine Schnittpunkte auf dieser Geraden
    	        return intersections;
    	    }

    	    // 3. Projektion des Kreismittelpunkts auf die Gerade
    	    double tClosest = ((cx - p0.x_coordinate) * dx + (cy - p0.y_coordinate) * dy) / segLenSq;
    	    double lenAlongLine = Math.sqrt(r*r - d*d);

    	    // Zwei Schnittpunkte entlang der Linie
    	    double t1 = tClosest - lenAlongLine / Math.sqrt(segLenSq);
    	    double t2 = tClosest + lenAlongLine / Math.sqrt(segLenSq);

    	    // Prüfen, ob die Punkte auf dem Segment liegen
    	    if (t1 >= -EPS && t1 <= 1 + EPS) {
    	        MyPoint pt = new MyPoint(p0.x_coordinate + t1 * dx, p0.y_coordinate + t1 * dy);
    	        // Nur hinzufügen, wenn noch nicht als Endpunkt vorhanden
    	        if (!intersections.contains(pt)) intersections.add(pt);
    	    }
    	    if (t2 >= -EPS && t2 <= 1 + EPS) {
    	        MyPoint pt = new MyPoint(p0.x_coordinate + t2 * dx, p0.y_coordinate + t2 * dy);
    	        if (!intersections.contains(pt)) intersections.add(pt);
    	    }

    	    return intersections;
    	}

	
	public static List<MyPoint> allCirclePolygonIntersections(List<Edge> poly, Circle circle, MyPoint convexSupport,
			MyPoint concaveSupport, boolean convexCap, DoorSegment b){
		List<MyPoint> hits = new ArrayList<>();
		List<MyPoint> newHits = new ArrayList<>();
		int steps = b.chain.size() - 1;
		for (int i = 0; i < steps; i++) { // check the full pocket for intersections
			newHits = intersectCircleSegment(circle, b.chain.get(i), b.chain.get((i + 1)));
			if (!newHits.isEmpty()) {
				for (MyPoint hit : newHits) {
					hit.index = b.chain.get(i).index;
					hits.add(hit);
				}
			}
		}
		return hits;
	}
	
	
    /**
	 * Liefert den ersten Schnittpunkt zwischen Kreis und Polygon.
	 * redundant code, but it works.
	 */
	public static MyPoint CiclePolygonIntersection(MyPoint observer, List<Edge> poly, Circle circle, MyPoint convexSupport,
			MyPoint concaveSupport, boolean convexCap, boolean cw , DoorSegment b) {
		// if convex cap
		List<MyPoint> hits = new ArrayList<>();
		List<MyPoint> newHits = new ArrayList<>();
		double hitAngle;
		int steps;

//			if (concaveSupportIndex > convexSupportIndex && concaveSupportIndex < b.chain.getLast().index) {
//				// I use the linear visibility polygon, as Vertex of Edge 0 is always visible.
//				// So there can no pocket that starts at a smaller vertex number, then it ends
//				// Therefore if s1 is inside the pocket, the index has to be in between
//				steps = concaveSupportIndex - 1; // speed up: chain s1,r, if s1 in rr'
//			} else { // sorry, I left this speed up commented out, as I need to get this project done.
				steps = b.chain.size() - 1;

//			}
			for (int i = 0; i < steps; i++) { // check the full pocket for intersections
				newHits = intersectCircleSegment(circle, b.chain.get(i), b.chain.get((i + 1)));
				if (!newHits.isEmpty()) {
					for (MyPoint hit : newHits) {
						hit.index = b.chain.get(i).index;
						hits.add(hit);
					}
				}

			}
			if(cw) {
				if(convexCap) {
					double concaveAngle = calculation_of_angle(observer, circle.center,concaveSupport );
					for(MyPoint hit : hits) {
						hitAngle = calculation_of_angle(observer, circle.center,hit );
						if( hitAngle != 0.0 && hitAngle+1e-4 < concaveAngle)
							return null;
					}
				}
				else {
					double convexAngle = calculation_of_angle(observer, circle.center,convexSupport );
					for(MyPoint hit : hits) {
						hitAngle = calculation_of_angle(observer, circle.center,hit );
						if( hitAngle != 0.0 && hitAngle+1e-4 < convexAngle)
							return null;
					}
				}
			}
			else {
				if(convexCap) {
					double concaveAngle = calculation_of_angle(concaveSupport, circle.center,observer );
					for(MyPoint hit : hits) {
						hitAngle = calculation_of_angle(hit, circle.center,observer );
						if( hitAngle != 0.0 && hitAngle+1e-4 < concaveAngle)
							return null;
					}
				}
				else {
					double convexAngle = calculation_of_angle(convexSupport , circle.center,observer);
					for(MyPoint hit : hits) {
						hitAngle = calculation_of_angle(hit , circle.center, observer);
						if( hitAngle != 0.0 && hitAngle+1e-4 < convexAngle)
							return null;
					}
				}
			}
			MyPoint result = null;
			if (cw) {
				double currentMin = Double.MAX_VALUE;
				for (MyPoint hit : hits) {
					double angle;
					if (convexCap) {
						angle = calculation_of_angle(convexSupport, circle.center, hit);
					}
					else {
						angle = calculation_of_angle(concaveSupport, circle.center, hit);	
					}
					if (angle > 0.0001 && angle < currentMin) { // to ensure even small hits are made
							 // without causing collinear results
						currentMin = angle;
						result = hit;
					}
				}
			} else {
				double currentMin = Double.MAX_VALUE;
				for (MyPoint hit : hits) {
//					System.out.println("hits: "+ hit.toString());
					double angle;
					if (convexCap) {
						angle = calculation_of_angle(hit, circle.center, convexSupport);
					} else {
						angle = calculation_of_angle(hit, circle.center, concaveSupport);
					}
					if (angle > 0.0001 && angle < currentMin) { // to ensure even small hits are made
						 // without causing collinear results
					currentMin = angle;
					result = hit;
				}
				}
			}
			return result;
		
		// start from s2 and check till s1, or full pocket, the smaller one
    	// add all intersections to a list
    	// find the intersection with the smallest angle, angle(c.center,s2,pointfromlist)
    	// return also with index of edge
    	
    	// else concave cap

		}
    	
    	
    	
    	
    	
    	
//    	
//        int n = poly.size();
//        int i = (index)%n;
//
//        for (int steps = 0; steps < n; steps++) {
//            Edge e = poly.get(i);
//            List<MyPoint> hits =
//            		intersectCircleSegment(circle, e.end2, e.start2);
//
//			if (!hits.isEmpty()) {
//				if (hits.size() > 1) {
//
//					System.out.print("mehr als eine Lösung auf Kante!");
//					for (MyPoint s : hits)
//						System.out.println("Lös: " + s.toString());
//				}
//				if (hits.size() == 1) {
//
//					System.out.print(" eine Lösung auf Kante!");
//					for (MyPoint s : hits)
//						System.out.println("Lös: " + s.toString());
//				}
//				// sortiere nach u (0..1)
//
//				if (first) {
//					hits.get(0).index = i;
//					return hits.get(0); // größter u
//				} else {
//					hits.get(hits.size() - 1).index = i;
//                    return hits.get(hits.size() - 1); // größter u
//                }
//            }
//            else {
//            	System.out.println("SP: Kante"+e.toString()+" keinen SP.");
//            }
//
//            // zum nächsten Index
//            if (forward) {
//                i = (i + 1) % n;
//            } else {
//                i = (i - 1 + n) % n;
//            }
//        }
//
//        return null; // kein Schnittpunkt gefunden
    

	// Orientierungstest Gerade a zu b, + wenn c links - wenn c rechts.
    // Scince Computer screens have the y axis inverted, we invert the result, to fit the "computer screen" representation
	public static double cross(MyPoint a, MyPoint b, MyPoint c) {
		return (b.x_coordinate - a.x_coordinate) * (c.y_coordinate - a.y_coordinate) - (b.y_coordinate - a.y_coordinate) * (c.x_coordinate - a.x_coordinate);
	}
}