package circular_visibility_region;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import drawtool.Edge;
import drawtool.MyPoint;

public class CircularVisibility {

	//
	public static List<Cap> calculateCircularVisibility(List<Edge> poly, MyPoint observer, List<DoorSegment> doors) {

		List<Cap> caps = new ArrayList<>();
		int n = poly.size();
		Cap result;
		int pocketNr = 0;
		// for each Pocket, find all convex and concave caps
		for (DoorSegment dSegment : doors) {

			if (!dSegment.ccw) {
				// CW Pocket
				// find convex Caps
				MyPoint next = null; // for skipping to edge, where the cap ends.
				MyPoint last = dSegment.chain.getLast();
				for (MyPoint s2 : dSegment.chain) {
					if (next == null || next.index < s2.index) { // if not skipped, start calculating
						// if convex Vertex, and not the "onEdge" of the doorsegment, as the index is
						// off by 1.
						if (s2.isReflex && s2 != last) {
							result = findConvexCap(s2, dSegment, poly, observer);
							if (result != null) {
								result.pocketNr = pocketNr;
								caps.add(result);
								next = result.q;
							}
						}
					}
				}

				// find concave Caps
				Edge e;
				next = null; // for skipping to edge, where the cap ends.
				// starting at r' = dSegment.onEdge, we go to r = dSegment.reflexVertrx
				for (int i = dSegment.chain.size() - 1; i > 0; i--) {
					if (next == null || next.index + 1 >= dSegment.chain.get(i).index) {
						e = new Edge(dSegment.chain.get(i), dSegment.chain.get(i - 1));
						result = findConcaveCap(e, dSegment, poly, observer);
						if (result != null) {
							result.pocketNr = pocketNr;
							caps.add(result);
							next = result.q;
						}
					}
				}
				pocketNr++;
			} else {
				// CCW Pocket
				// find convex Caps
				// idea: we traverse it backwards, so that the reflex vertex, that cuts our
				// linear view, is, just as in the CCW pocket, the first tested vertex for caps.
				MyPoint next = null; // for skipping to edge, where the cap ends.
				MyPoint last = dSegment.chain.getFirst();
				for (int i = (dSegment.chain.size() - 1); i >= 0; i--) {
					MyPoint s2 = dSegment.chain.get(i);
					if (next == null || next.index >= s2.index) {
						if (Calculator.calculation_of_angle(
								poly.get(((dSegment.chain.get(i).index) + n - 1) % n).start2, s2,
								poly.get(dSegment.chain.get(i).index).end2) > 180 && s2 != last) {
							result = findConvexCap(s2, dSegment, poly, observer);
							if (result != null) {
								result.pocketNr = pocketNr;
								caps.add(result);
								next = result.q;
							}
						}
					}
				}
				// find concave Caps
				Edge e;
				next = null;
				for (int i = 0; i < dSegment.chain.size() - 1; i++) {
					if (next == null || next.index - 1 < dSegment.chain.get(i).index) { // HUH?
						e = new Edge(dSegment.chain.get(i), dSegment.chain.get(i + 1));
						result = findConcaveCap(e, dSegment, poly, observer);
						if (result != null) {
							result.pocketNr = pocketNr;
							caps.add(result);
							next = result.q;
						}
					}
				}
			}
			pocketNr++;
		}
		return caps;
	}

	private static Cap findConcaveCap(Edge e, DoorSegment d, List<Edge> poly, MyPoint observer ) {
		MyPoint q = null;
		MyPoint s = null;
		MyPoint s3 = null;
		MyPoint s4 = null;
		Double t;
		
		Cap result = null;
		Double distance_to_u1;
		
		// CW door
		// IMPORTANT NOTE: I use here end2 as u1 and start2 as u2!
		if (!d.ccw) {
			Double resT = Double.POSITIVE_INFINITY; // used to find the closest point to u1 = e.end2
			for (MyPoint vj : d.chain) {
				// test all reflex vertices in the chain P(r,u2), since we are going CW
				if (vj.isReflex && vj.index <= e.end2.index) {
					// s3 has to be type 2b, orientation test o,u2.
					if (0 > Calculator.cross(observer, e.end2, vj)) {
						// first we test for s4 to be a tangent.
						// teste if edge has tangent point for circle, o,s3,u1,u2.
						
						// tangentPointDuality is tricky, as a long enough edge can have two tangents.
						// dividing the edge into two parts, with the dividing point as the intersection of
						// the edge and the two points forming the line, we yield the correct segment.
						IntersectionRes a = Calculator.rayIntersectsEdge(observer, vj, e);
						if(a.intersects) {
							Edge smallSegment = new Edge(a.intersection,e.end2);
							s = tangentPointDuality(observer, vj, smallSegment);
						}
						else {
							s = tangentPointDuality(observer, vj, e);
							
						}
						
	
						
						if (s != null) {
							s.index = e.end2.index;
							// this test is necessary to stay in bounds. otherwise we can get closer to u1,
							// with a reflex vertex of type 2b, without keeping the order of first convex
							// support then concave support
							if (Calculator.cross(observer, s, vj) < 0) {
								// find the point that is closest to u1.
								distance_to_u1 = s.distance(e.start2);
								if (resT > distance_to_u1) {
									resT = distance_to_u1;
									s4 = s;
									s3 = vj;
								}
							}
						}
					}
				}
			}
			// if there is no tangent, s4 has to be a reflexVertex!!
			// test u2
			if (s3 == null) {
				resT = Double.POSITIVE_INFINITY; // the t value, to find. the biggest one is the one .
				// skip the door vertex r, as s3 and s4 can not be on the same edge
				if (e.end2.isReflex && e.end2 != d.chain.get(0)) {
					s4 = e.end2;
					for (MyPoint vj : d.chain) {
						// test all reflex vertices in the chain P(r,u2), since we are going CW
						if (vj.isReflex&& vj.index <= e.end2.index) {
							// s3 has to be type 2b, orientation test o,u2.
							if (0 > Calculator.cross(observer, e.end2, vj)) {
								// A type 2b segment has to *cross* the line (observer,s4), so vj is type 2b.
								if (observer.distance(e.end2) > observer.distance(vj)) {
								// calculate t Param
								t = computeT(observer, vj, e.end2);
								if (t < resT) {
									resT = t;
									s3 = vj;
								}
							}
						}
					}
				}
					if(s3 == null) {
						System.out.println("Doorvertex Colinear with his previous edge, error.");
						return null;
					}
					// calculate q, as we have a concave cap, with s4 = u2.
					Circle c = Calculator.circumCircle(s3, observer, s4);
					c.radius = s4.distance(c.center);
					// THE CONCAVE CAP CONDITION, just like in  convex cap. with angle 90 on both vertices, as smaller means it intersects the
					// Concave cap, Cap condition for s4
					double isCap1 = Calculator.calculation_of_angle(c.center,s4,e.start2);
					double isCap2 = Calculator.calculation_of_angle(poly.get( (s4.index - 1+poly.size()) % poly.size() ).start2, s4, c.center); //
					// here we test, whether s2 is a real covex support.
					if ((isCap1 > 90 && isCap1<270) && (isCap2 > 90 && isCap2<270)) { 
					q = Calculator.CiclePolygonIntersection(poly, c, s3, s4, false, true, d);
					return new Cap(false,false, s3, s4, q);}
					else {
						System.out.println("Cap discarded: s4 is not a concave support.");
						return null;
					}
				}
				// no tangent point on e, and u2 is no reflex vertex -> no cap
				else {
					return null;
				}
			}
			// return tangent point concave cap
			else {
				// the calculation of q, is inacurate by 1e-4, the 5th number is wrong, meaning we can get a intersection when there should be none.
				
				Circle c = Calculator.circumCircle(s3, observer, s4);
				c.radius = s4.distance(c.center);
				q = Calculator.CiclePolygonIntersection(poly, c, s3, s4, false, true, d);
				// q must be to the left of edge e, to ensure no true intersection between
				// the Polygon and the arc: observer to s4.
				// q can also not on the same edge as s4
				if (Calculator.cross(e.start2, e.end2, q) < 0 )
					return null;
				return new Cap(false, false, s3, s4, q);
			}
		}
		
		
		
		// CCW door
		else {
			// here u1 is e.start2, u2 is e.end2
			// test for tangent point
			Double resT = Double.POSITIVE_INFINITY; 
			for (MyPoint vj : d.chain) {
				// test all reflex vertices in the chain P(u2,r), since we are going CCW
				// vj can be the vertex 0.
				if(vj.isReflex && (vj.index >= e.end2.index || vj.index == 0)) { 
					// if vj is type 2b
					if (Calculator.cross(observer, e.end2, vj) > 0) {
						// tangentPointDuality is tricky, as a long enough edge can have two tangents.
						// dividing the edge into two parts, with the dividing point as the intersection of
						// the edge and the two points forming the line, we yield the correct segment.
						IntersectionRes a = Calculator.rayIntersectsEdge(observer, vj, e);
						if(a.intersects) {
							Edge smallSegment = new Edge(a.intersection,e.end2);
							s = tangentPointDuality(observer, vj, smallSegment);
						}
						else {
							s = tangentPointDuality(observer, vj, e);
						}
						

						if (s != null) {
							s.index = e.end2.index;
							// this test is necessary to stay in bounds. otherwise we can get closer to u1,
							// with a reflex vertex of type 2b, without keeping the order of first convex
							// support then concave support
							if (Calculator.cross(observer, s, vj) > 0) {
								// find the point that is closest to u1.
								distance_to_u1 = s.distance(e.start2);
								if (resT > distance_to_u1) {
									resT = distance_to_u1;
									s4 = s;
									s3 = vj;
								}
							}
						}
					}
				}
			}
			// return Cap with tangent point s4
			if (s3 != null) {
				Circle c = Calculator.circumCircle(s3, observer, s4); 
				c.radius = s4.distance(c.center);
				q = Calculator.CiclePolygonIntersection(poly, c, s3, s4, false, false, d);
				// q must be to the right of edge e, to ensure no true intersection between
				// the Polygon and the arc: observer to s4.
				// q can also not on the same edge as s4
				if (Calculator.cross(e.start2, e.end2, q) > 0 || q.index == s4.index)
					return null;
				return new Cap(false,true, s3, s4, q);
			}
			// test u2
			else {	
				resT = Double.NEGATIVE_INFINITY; 
				// u2, on a concave cap, can only create a concave deficiency if it is reflex vertex, on a concave cap.
				if(e.end2.isReflex && e.end2 != d.chain.getLast()) { 
					resT = Double.NEGATIVE_INFINITY; 
					for (MyPoint vj : d.chain ) {
						// test all reflex vertices in the chain P(u2,r), since we are going CCW 
						// vj can be the vertex 0.
						if (vj.isReflex && (vj.index >= e.end2.index || vj.index == 0)) {
							if (Calculator.cross(observer, e.end2, vj) > 0) {
								// A type 2b segment has to *cross* the line (observer,s4), so vj is type 2b.
								if (observer.distance(e.end2) > observer.distance(vj)) {
									t = computeT(observer, vj, e.end2);
									if (t > resT) {
										resT = t;
										s3 = vj;
									}
								}
							}
						}
					}
					
					if(s3 == null) {
						System.out.println("Doorvertex Colinear with his previous edge, error.");
						return null;
					}
					s4 = e.end2;
					// test the cap conditions, then calculate q
					Circle c = Calculator.circumCircle(s3, observer, s4);
					double isCap1 = Calculator.calculation_of_angle(c.center, e.end2, e.start2);
					double isCap2 = Calculator.calculation_of_angle(poly.get(e.end2.index).end2, e.end2, c.center); 
					if ((isCap1 > 90 && isCap1<270) && (isCap2 > 90 && isCap2<270)) {
						c.radius = s4.distance(c.center); 
						q = Calculator.CiclePolygonIntersection(poly, c, s3, s4, false, false, d);
						return new Cap(false, true, s3, s4, q);
					}
				}
					
				// test the concave support condition.

				// no tangent point on e, and u2 can not be a concave support.
				else {
					return null;
				}
			}
			// teste if edge has tangent point for circle, o,s3,u1,u2.
			// s3 has to be type 2b, orientation test o,u2.
			// if null, and u2 is not reflex -> null
			// else return Cap
		}
		return null;
	}

	// input: reflex Vertex s2, a pocket d.
	// output: Convex Cap or Null
	public static Cap findConvexCap(MyPoint s2, DoorSegment d, List<Edge> poly, MyPoint observer) {
		
		Double t;
		double orientationStartPoint, orientationEndPoint; // used for cheching weather a point is left or right of a
															// line
		MyPoint s = null, s1 = null;
		int n = poly.size();
		// finde erstes 3a Segment
		IntersectionRes first3aSegment = Calculator.findFirst3aSegment(poly, observer, s2);
		// Basic Idea
		// For all type 3a Segments
		// calculate D
		// calculate C of Point result of D
		// calculate C of u2, as D will be not an end point
		// Test for Convex Cap.
		// calculate q
		if (!d.ccw) { // check the chain P(r,s2) without s2.
			Double resT = Double.NEGATIVE_INFINITY; // the t value, to find. the biggest one is the one .
			for (int i = (s2.index + 1) % n; i != (first3aSegment.vertexNumber + 1) % n; i = (i + 1) % n) {
				//System.out.println("Edge checking: T values" + poly.get(i).toString());
				// Test Endpoints of the intersection: why? because D(o,s2,e) can technically
				// have two points, and we want it to be on a 3a Type segment. So we limit the
				// test only to Segments of type 3a.
				// if No end points are a 3a Segment point, the edge is not a 3a Segment.
				// if one point is a 3a Segment point, we find the segment that is fully 3a
				// type.
				// if both the points are part of a 3a Segment, we take the full edge.

				orientationStartPoint = Calculator.cross(observer, s2, poly.get(i).start2);
				orientationEndPoint = Calculator.cross(observer, s2, poly.get(i).end2);
				if (orientationStartPoint >= 0 && orientationEndPoint >= 0) {
					// no s1 possible
				} else { // here we have to test for concave support s1, as it the edge has a Type 3a
							// Segment
					if (orientationStartPoint <= 0 && orientationEndPoint <= 0) {
						s = tangentPointDuality(observer, s2, poly.get(i));
					} else {
						if (orientationStartPoint < 0) {
						// take start2 and onEdge
							IntersectionRes onEdge = Calculator.rayIntersectsEdge(observer, s2, poly.get(i));
							if(onEdge.intersection != poly.get(i).start2) // ????
								s = tangentPointDuality(observer, s2, new Edge(poly.get(i).start2, new MyPoint(onEdge.intersection.x_coordinate,onEdge.intersection.y_coordinate)));
						} else {
						// take end2 and onEdge
							IntersectionRes onEdge = Calculator.rayIntersectsEdge(observer, s2, poly.get(i));
							if(onEdge.intersection != poly.get(i).end2)
								s = tangentPointDuality(observer, s2, new Edge(new MyPoint(onEdge.intersection.x_coordinate,onEdge.intersection.y_coordinate), poly.get(i).end2));
							}
						}
					// wenn s1 ein Punkt auf einer Kante ist
					if (s != null) { // Punkt auf Kante fuer Concave Support gefunden?
						s.index = i;
						// Die Orientierung fuer 3a Segmenttyp ist umgekehrt fuer CW oder CCW Tasche


						if (s1 != null) { // Vergleiche, sonst speichere 
							//System.out.println("");
							t = computeT(observer, s, s2);
						
							if (resT < t) {
								s1 = s;
								resT = t;
							}
						} else {

							resT = computeT(observer, s, s2);
							s1 = s;
//							System.out.println("> T \"first\"Value of: s1 auf Kante WERT: " + resT);
						}
					}
					if (orientationEndPoint < 0) {
						s = poly.get(i).end2; // u2
						if (Calculator.cross(observer, s2, s) < 0) { // teste u2, ob Typ 3a
							if (s1 != null) {
								t = computeT(observer, s, s2);

								if (resT < t) {
									s1 = s;
									resT = t;
								}
							} else {

								resT = computeT(observer, s, s2);
//								System.out.println("> T \"first\"Value of: u2 WERT: " + resT);
								s1 = s;
							}
						}
					}
				}
			}
			// TODO kann es auch passieren, dass man prev und next vertrauschen muss, also
			// 90 und 270, wenn man die CCW oder CW pocket hat?

			if (s1 != null) { // no cap found?!
				Circle c = Calculator.circumCircle(s1, observer, s2); // center = Kreismittelpunkt o,s1,s2
				double isCap1 = Calculator.calculation_of_angle(c.center, s2, // Variable fuer
																				// ConvexCap,s2,CapBedingung
						poly.get((s2.index - 1 + poly.size()) % poly.size()).start2);
				double isCap2 = Calculator.calculation_of_angle(c.center, s2, poly.get((s2.index % poly.size())).end2);
				// here we test, whether s2 is a real covex support.
				if ((isCap1 < 90 || isCap1 > 270) && (isCap2 < 90 || isCap2 > 270)) {
					// calculate and retur q
					
					MyPoint intersection = Calculator.CiclePolygonIntersection(poly, c, s2, s1, true, true,
							d);
					if (intersection == null) {
						System.out.println("Cap discarded: too small to draw, though Mathematicaly exists!");
						return null;
					}
					return new Cap(true, false, s1, s2, intersection);

				} else {
					System.out.println("Cap discarded: s2 is not a convex support.");
				}
			}

		} else {
			Double resT = Double.POSITIVE_INFINITY; // the t value, to find. the biggest one is the one .
			// hier CCW pocket
			// Basic Idea
			// For all type 3a Segments
			// calculate D
			// calculate C of Point result of D
			// calculate C of u2, as D will be not an end point
			// Test for Convex Cap.
			// calculate q
					// check the chain P(r,s2) without s2.
			for (int i = first3aSegment.vertexNumber ; i != (s2.index-1+n)%n; i = (i + 1) % n) {
				
			
			//System.out.println("Edge checking: T values" + poly.get(i).toString());
			// Test Endpoints of the intersection: why? because D(o,s2,e) can technically
			// have two points, and we want it to be on a 3a Type segment. So we limit the
			// test only to Segments of type 3a.
			// if No end points are a 3a Segment point, the edge is not a 3a Segment.
			// if one point is a 3a Segment point, we find the segment that is fully 3a
			// type.
			// if both the points are part of a 3a Segment, we take the full edge.

			orientationStartPoint = Calculator.cross(observer, s2, poly.get(i).start2);
			orientationEndPoint = Calculator.cross(observer, s2, poly.get(i).end2);
			if (orientationStartPoint < 0 && orientationEndPoint <= 0) {
				// no s1 possible
			} else { // here we have to test for concave support s1, as it the edge has a Type 3a
						// Segment
				if (orientationStartPoint >= 0 && orientationEndPoint >= 0) {
					s = tangentPointDuality(observer, s2, poly.get(i));
				} else {
					if (orientationStartPoint > 0) {
					// take start2 and onEdge
						IntersectionRes onEdge = Calculator.rayIntersectsEdge(observer, s2, poly.get(i));
						if(onEdge.intersection != poly.get(i).start2)
							s = tangentPointDuality(observer, s2, new Edge(poly.get(i).start2, new MyPoint(onEdge.intersection.x_coordinate,onEdge.intersection.y_coordinate)));
					
					} else {
					// take onEdge and end2
						IntersectionRes onEdge = Calculator.rayIntersectsEdge(observer, s2, poly.get(i));
						if(onEdge.intersection != poly.get(i).end2)
							s = tangentPointDuality(observer, s2, new Edge(new MyPoint(onEdge.intersection.x_coordinate,onEdge.intersection.y_coordinate), poly.get(i).end2));
					}
				}
		// wenn s1 ein Punkt auf einer Kante ist
				if (s != null) { // Punkt auf Kante fuer Concave Support gefunden?
					s.index = i;
					// Die Orientierung fuer 3a Segmenttyp ist umgekehrt fuer CW oder CCW Tasche


					if (s1 != null) { // Vergleiche, sonst speichere
						t = computeT(observer, s, s2);
						if (resT > t) {
							s1 = s;
							resT = t;
						}
					} else {

						resT = computeT(observer, s, s2);
						s1 = s;
					}
				}
				if (orientationEndPoint > 0) {
					s = poly.get(i).end2; // u2
					if (Calculator.cross(observer, s2, s) > 0) { // teste u2, ob Typ 3a
						if (s1 != null) {
							t = computeT(observer, s, s2);
							if (resT > t) {
								s1 = s;
								resT = t;
							}
						} else {

							resT = computeT(observer, s, s2);
							s1 = s;
						}
					}
				}
			}
		}
		// TODO kann es auch passieren, dass man prev und next vertrauschen muss, also
		// 90 und 270, wenn man die CCW oder CW pocket hat?

		if (s1 != null) { // no cap found?!
			Circle c = Calculator.circumCircle(s1, observer, s2); // center = Kreismittelpunkt o,s1,s2
			double isCap1 = Calculator.calculation_of_angle(c.center, s2, // Variable fuer
																			// ConvexCap,s2,CapBedingung
					poly.get((s2.index - 1 + poly.size()) % poly.size()).start2);
			double isCap2 = Calculator.calculation_of_angle(c.center, s2, poly.get((s2.index % poly.size())).end2);
			// here we test, whether s2 is a real covex support.
			if ((isCap1 < 90 || isCap1 > 270) && (isCap2 < 90 || isCap2 > 270)) {

					// calculate and retur q
					System.out.println("CAP angles: " + isCap1 + " " + isCap2);
					MyPoint intersection = Calculator.CiclePolygonIntersection(poly, c, s2, s1, true, false,d);
					if (intersection == null) {
						System.out.println("Cap discarded: too small to draw, though Mathematicaly exists!");
						return null;
					}
					return new Cap(true, true, s1, s2, intersection);

				} else {
				System.out.println("Cap discarded: s2 is not a convex support.");
			}
		
					// Test Endpoints of the intersection: why? because D(o,s2,e) can technically
					// have two points, and we want it to be on a 3a Type segment. So we limit the
					// test only to Segments of type 3a.
					// if No end points are a 3a Segment point, the edge is not a 3a Segment.
					// if one point is a 3a Segment point, we find the segment that is fully 3a
					// type.
					// if both the points are part of a 3a Segment, we take the full edge.
		}}
		return null;
	}



	public static double computeT(MyPoint a, MyPoint b, MyPoint c) {
		Circle circle = Calculator.circumCircle(a, b, c);
		MyPoint center = circle.center;
		// Mittelpunkt der Sehne AC
		double mx = (a.x_coordinate + c.x_coordinate) / 2.0;
		double my = (a.y_coordinate + c.y_coordinate) / 2.0;

		// Richtungsvektor v = (c - a) rotiert um 90° CCW
		double vx = -(c.y_coordinate - a.y_coordinate);
		double vy = (c.x_coordinate - a.x_coordinate);

		// Differenz von M zum bekannten Mittelpunkt
		double dx = center.x_coordinate - mx;
		double dy = center.y_coordinate - my;

		// t = (d·v) / (v·v)
		double dotDV = dx * vx + dy * vy;
		double dotVV = vx * vx + vy * vy;

		if (dotVV == 0) {
			throw new IllegalArgumentException("A und C dürfen nicht identisch sein!");
		}

		return dotDV / dotVV;
	}



	// Funktion D gibt den Tangentenpunkt auf der Kante fuer den Krais zuruek
	public static MyPoint contactPointD(MyPoint a, MyPoint b, Edge e) {
		MyPoint e1 = e.start2;
		MyPoint e2 = e.end2;

		// Vektor ab und e
		double abx = b.x_coordinate - a.x_coordinate;
		double aby = b.y_coordinate - a.y_coordinate;
		double ex = e2.x_coordinate - e1.x_coordinate;
		double ey = e2.y_coordinate - e1.y_coordinate;

		// Parallelität prüfen (determinante nahe 0)
		double det = abx * ey - aby * ex;
		if (Math.abs(det) < 1e-9) {
			return null; // ab || e
		}

		// X = Mittelpunkt von ab
		MyPoint X = new MyPoint((a.x_coordinate + b.x_coordinate) / 2.0, (a.y_coordinate + b.y_coordinate) / 2.0);

		// Z = Schnittpunkt von ab und e
		MyPoint Z = lineIntersection(a, b, e1, e2);
		if (Z == null)
			return null;

		// Y = Schnittpunkt: Mittelsenkrechte von ab und Linie e
		// Richtung der Mittelsenkrechten = (-aby, abx)
		MyPoint perpDir = new MyPoint(-aby, abx);
		MyPoint Y = lineIntersection(X, new MyPoint(X.x_coordinate + perpDir.x_coordinate, X.y_coordinate + perpDir.y_coordinate), e1, e2);
		if (Y == null)
			return null;

		// Längen
		double XZ = dist(X, Z);
		double YZ = dist(Y, Z);
		double XY = dist(X, Y);

		// Quadratische Gleichung für h
		// r = ( (XY - h) * XZ ) / YZ
		// r^2 = h^2 + |aX|^2
		double aX = dist(a, X);

		// => ( (XY - h) * XZ / YZ )^2 = h^2 + aX^2
		double alpha = (XZ * XZ) / (YZ * YZ);
		// => (XY - h)^2 * alpha = h^2 + aX^2
		// => alpha*(h^2 - 2*XY*h + XY^2) = h^2 + aX^2
		// => (alpha - 1)h^2 - 2*alpha*XY*h + (alpha*XY^2 - aX^2) = 0

		double A = alpha - 1.0;
		double B = -2.0 * alpha * XY;
		double C = alpha * XY * XY - aX * aX;

		double disc = B * B - 4 * A * C;
		if (disc < 0)
			return null;

		// Zwei mögliche Lösungen für h
		double sqrtDisc = Math.sqrt(disc);
		double h1 = (-B + sqrtDisc) / (2 * A);
		double h2 = (-B - sqrtDisc) / (2 * A);

		// Teste beide Lösungen
		MyPoint bestD = null;
		double bestErr = Double.POSITIVE_INFINITY;

		double[] hs = { h1, h2 };
		for (double h : hs) {
			// Mittelpunkt C auf der Mittelsenkrechten
			double norm = Math.sqrt(perpDir.x_coordinate * perpDir.x_coordinate + perpDir.y_coordinate * perpDir.y_coordinate);
			double Cx = X.x_coordinate + (h / norm) * perpDir.x_coordinate;
			double Cy = X.y_coordinate + (h / norm) * perpDir.y_coordinate;

			double r = dist(new MyPoint(Cx, Cy), a);

			// Projektion auf Kante e, Fußpunkt D
			MyPoint D = footOfPerpendicular(new MyPoint(Cx, Cy), e1, e2);

			if (D != null && onSegment(D, e1, e2)) {
				// Berechne Abweichung zum Radius
				double err = Math.abs(dist(D, new MyPoint(Cx, Cy)) - r);
				if (err < bestErr) {
					bestErr = err;
					bestD = D;
				}
			}
		}

		// Rückgabe der besten Lösung
		return bestD;
	}

	// ---- Hilfsfunktionen ----

	private static double dist(MyPoint p1, MyPoint p2) {
		double dx = p1.x_coordinate - p2.x_coordinate;
		double dy = p1.y_coordinate - p2.y_coordinate;
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static MyPoint lineIntersection(MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4) {
		double A1 = p2.y_coordinate - p1.y_coordinate;
		double B1 = p1.x_coordinate - p2.x_coordinate;
		double C1 = A1 * p1.x_coordinate + B1 * p1.y_coordinate;

		double A2 = p4.y_coordinate - p3.y_coordinate;
		double B2 = p3.x_coordinate - p4.x_coordinate;
		double C2 = A2 * p3.x_coordinate + B2 * p3.y_coordinate;

		double det = A1 * B2 - A2 * B1;
		if (Math.abs(det) < 1e-9)
			return null;

		double x = (B2 * C1 - B1 * C2) / det;
		double y = (A1 * C2 - A2 * C1) / det;
		return new MyPoint(x, y);
	}

	private static MyPoint footOfPerpendicular(MyPoint p, MyPoint a, MyPoint b) {
		double dx = b.x_coordinate - a.x_coordinate;
		double dy = b.y_coordinate - a.y_coordinate;
		double t = ((p.x_coordinate - a.x_coordinate) * dx + (p.y_coordinate - a.y_coordinate) * dy) / (dx * dx + dy * dy);
		return new MyPoint(a.x_coordinate + t * dx, a.y_coordinate + t * dy);
	}

	private static boolean onSegment(MyPoint p, MyPoint a, MyPoint b) {
		return p.x_coordinate >= Math.min(a.x_coordinate, b.x_coordinate) - 1e-9 && p.x_coordinate <= Math.max(a.x_coordinate, b.x_coordinate) + 1e-9 && p.y_coordinate >= Math.min(a.y_coordinate, b.y_coordinate) - 1e-9
				&& p.y_coordinate <= Math.max(a.y_coordinate, b.y_coordinate) + 1e-9;
	}

	public static MyPoint computeContactPoint2(MyPoint a, MyPoint b, Edge e) {
		// Schritt 1: Vektoren vorbereiten
		MyPoint e1 = e.start2, e2 = e.end2;
		double ax = a.x_coordinate, ay = a.y_coordinate;
		double bx = b.x_coordinate, by = b.y_coordinate;
		double e1x = e1.x_coordinate, e1y = e1.y_coordinate;
		double e2x = e2.x_coordinate, e2y = e2.y_coordinate;

		// Mitte von AB
		double Xx = (ax + bx) / 2.0;
		double Xy = (ay + by) / 2.0;

		// Richtung von AB
		double vx = bx - ax;
		double vy = by - ay;

		// Senkrechte Richtung (CCW gedreht)
		double perpX = -vy;
		double perpY = vx;

		// Linie durch e1-e2
		double ux = e2x - e1x;
		double uy = e2y - e1y;

		// Schritt 2: Schnitt Y = Schnittpunkt der Senkrechten von X mit e-Linie
		double denom = perpX * (-uy) + perpY * ux;
		if (Math.abs(denom) < 1e-9) {
			return null; // ab || e → kein eindeutiger Kreis
		}
		double t = ((e1x - Xx) * (-uy) + (e1y - Xy) * ux) / denom;
		double Yx = Xx + t * perpX;
		double Yy = Xy + t * perpY;

		// Schritt 3: Schnitt Z = Schnittpunkt der Linien ab und e
		double denomZ = vx * (-uy) + vy * ux;
		if (Math.abs(denomZ) < 1e-9) {
			return null; // ab || e → kein Schnitt
		}
		double tz = ((e1x - ax) * (-uy) + (e1y - ay) * ux) / denomZ;
		double Zx = ax + tz * vx;
		double Zy = ay + tz * vy;

		// Schritt 4: Längen berechnen
		double XY = Math.hypot(Xx - Yx, Xy - Yy);
		double XZ = Math.hypot(Xx - Zx, Xy - Zy);
		double YZ = Math.hypot(Yx - Zx, Yy - Zy);

		// Quadratische Gleichung: r² = h² + |aX|², r = (XY - h) * XZ / YZ
		double aX = Math.hypot(ax - Xx, ay - Xy);

		// h ist unbekannt → setze r(h) ein
		// r = (XY - h) * XZ / YZ
		// r² = h² + aX²
		double alpha = (XZ / YZ) * (XZ / YZ);
		double A = 1 - alpha;
		double B = 2 * XY * alpha;
		double C = (alpha * XY * XY) - (aX * aX);

		double disc = B * B - 4 * A * C;
		if (disc < 0) {
			return null; // keine Lösung
		}

		// zwei Lösungen für h
		double sqrtDisc = Math.sqrt(disc);
		double h1 = (-B + sqrtDisc) / (2 * A);
		double h2 = (-B - sqrtDisc) / (2 * A);

		// Schritt 5: wähle h → bestimme C = X + h * (perp/|perp|)
		double perpLen = Math.hypot(perpX, perpY);
		double nx = perpX / perpLen;
		double ny = perpY / perpLen;

		double[] hs = { h1, h2 };
		for (double h : hs) {
			double Cx = Xx + h * nx;
			double Cy = Xy + h * ny;

			// Radius
			double r = Math.hypot(Cx - ax, Cy - ay);

			// D liegt auf e und Abstand zu C ist r
			// param. e = e1 + s*(e2-e1)
			double ex = ux, ey = uy;
			double fx = e1x - Cx, fy = e1y - Cy;

			double Aeq = ex * ex + ey * ey;
			double Beq = 2 * (fx * ex + fy * ey);
			double Ceq = fx * fx + fy * fy - r * r;

			double disc2 = Beq * Beq - 4 * Aeq * Ceq;
			if (disc2 < 0)
				continue;

			double sqrtDisc2 = Math.sqrt(disc2);
			double s1 = (-Beq + sqrtDisc2) / (2 * Aeq);
			double s2 = (-Beq - sqrtDisc2) / (2 * Aeq);

			for (double s : new double[] { s1, s2 }) {
				if (s > 1e-9 && s < 1 - 1e-9) { // D strikt innerhalb (e1,e2)
					double Dx = e1x + s * ex;
					double Dy = e1y + s * ey;
					return new MyPoint(Dx, Dy);
				}
			}
		}

		return null; // kein gültiger D gefunden
	}

	public static MyPoint tangentPointDuality(MyPoint a, MyPoint b, Edge e) {
		if (a == null || b == null || e == null)
			return null;

		double R = Math.max(1.0, Math.hypot(a.x_coordinate, a.y_coordinate) * 2.0);

		// Invert points relative to A
		MyPoint bPrime = invertPoint(b, a, R);
		MyPoint e1Prime = invertPoint(e.start2, a, R);
		MyPoint e2Prime = invertPoint(e.end2, a, R);

		if (bPrime == null || e1Prime == null || e2Prime == null)
			return null;

		// Circle through three points in inverted space
		Circle cPrime = circleThroughThree(a, e1Prime, e2Prime);
		if (cPrime == null)
			return null;

		// Tangency points from b' to circle in dual space
		List<MyPoint> tangentPoints = tangencyPointsFromPointToCircle(bPrime, cPrime);
		if (tangentPoints.isEmpty())
			return null;

		// Transform back through inversion and choose best
		MyPoint bestD = null;
		double bestErr = Double.POSITIVE_INFINITY;

		for (MyPoint tPrime : tangentPoints) {
			MyPoint D = invertPoint(tPrime, a, R);
			if (D == null)
				continue;

			// Clamp to edge
			double Dx = Math.max(Math.min(D.x_coordinate, Math.max(e.start2.x_coordinate, e.end2.x_coordinate)),
					Math.min(e.start2.x_coordinate, e.end2.x_coordinate));
			double Dy = Math.max(Math.min(D.y_coordinate, Math.max(e.start2.y_coordinate, e.end2.y_coordinate)),
					Math.min(e.start2.y_coordinate, e.end2.y_coordinate));
			MyPoint Dclamped = new MyPoint(Dx, Dy);

			// Check circle radius consistency
			double r1 = Math.hypot(a.x_coordinate - Dclamped.x_coordinate, a.y_coordinate - Dclamped.y_coordinate);
			double r2 = Math.hypot(b.x_coordinate - Dclamped.x_coordinate, b.y_coordinate - Dclamped.y_coordinate);
			double err = Math.abs(r1 - r2);

			if (err < bestErr) {
				bestErr = err;
				bestD = Dclamped;
			}
		}
		if (bestD != null) // Endpunktlösung -> ignorieren
			if (dist(bestD, e.start2) < 1e-4 || dist(bestD, e.end2) < 1e-4) {
				return null;
			}
		return bestD;
	}

	private static MyPoint invertPoint(MyPoint p, MyPoint a, double R) {
		double dx = p.x_coordinate - a.x_coordinate;
		double dy = p.y_coordinate - a.y_coordinate;
		double d2 = dx * dx + dy * dy;
		if (d2 < 1e-12)
			return null;
		double factor = (R * R) / d2;
		return new MyPoint(a.x_coordinate + dx * factor, a.y_coordinate + dy * factor);
	}

	private static Circle circleThroughThree(MyPoint p1, MyPoint p2, MyPoint p3) {
		double x1 = p1.x_coordinate, y1 = p1.y_coordinate;
		double x2 = p2.x_coordinate, y2 = p2.y_coordinate;
		double x3 = p3.x_coordinate, y3 = p3.y_coordinate;

		double a = x1 * (y2 - y3) - y1 * (x2 - x3) + x2 * y3 - x3 * y2;
		if (Math.abs(a) < 1e-12)
			return null;

		double x1sq = x1 * x1 + y1 * y1;
		double x2sq = x2 * x2 + y2 * y2;
		double x3sq = x3 * x3 + y3 * y3;

		double cx = (x1sq * (y2 - y3) + x2sq * (y3 - y1) + x3sq * (y1 - y2)) / (2 * a);
		double cy = (x1sq * (x3 - x2) + x2sq * (x1 - x3) + x3sq * (x2 - x1)) / (2 * a);

		double r = Math.hypot(cx - x1, cy - y1);
		return new Circle(new MyPoint(cx, cy), r);
	}

	private static List<MyPoint> tangencyPointsFromPointToCircle(MyPoint P, Circle C) {
		List<MyPoint> res = new ArrayList<>();
		double dx = P.x_coordinate - C.center.x_coordinate;
		double dy = P.y_coordinate - C.center.y_coordinate;
		double d = Math.hypot(dx, dy);

		if (d < C.radius - 1e-12)
			return res;
		if (Math.abs(d - C.radius) < 1e-12) {
			res.add(new MyPoint(C.center.x_coordinate + C.radius * dx / d, C.center.y_coordinate + C.radius * dy / d));
			return res;
		}

		double angle = Math.atan2(dy, dx);
		double alpha = Math.acos(C.radius / d);

		double t1x = C.center.x_coordinate + C.radius * Math.cos(angle + alpha);
		double t1y = C.center.y_coordinate + C.radius * Math.sin(angle + alpha);
		double t2x = C.center.x_coordinate + C.radius * Math.cos(angle - alpha);
		double t2y = C.center.y_coordinate + C.radius * Math.sin(angle - alpha);

		res.add(new MyPoint(t1x, t1y));
		res.add(new MyPoint(t2x, t2y));
		return res;
	}

	public static List<CircleSegmentIntersection> intersectCircleSegment(double cx, double cy, double r, double x0,
			double y0, double x1, double y1) {

		List<CircleSegmentIntersection> intersections = new ArrayList<>();

		double dx = x1 - x0;
		double dy = y1 - y0;

		// Quadratische Gleichung: (P0 + t*d - C)^2 = r^2
		double fx = x0 - cx;
		double fy = y0 - cy;

		double a = dx * dx + dy * dy;
		double b = 2 * (fx * dx + fy * dy);
		double c = fx * fx + fy * fy - r * r;

		double discriminant = b * b - 4 * a * c;

		if (discriminant < 0) {
			// Keine Loesung -> keine Schnittpunkte
			return intersections;
		}

		discriminant = Math.sqrt(discriminant);

		double t1 = (-b - discriminant) / (2 * a);
		double t2 = (-b + discriminant) / (2 * a);

		// Pruefen ob die Loesungen im Segment liegen: 0 <= t <= 1
		if (t1 >= 0 && t1 <= 1) {
			double ix1 = x0 + t1 * dx;
			double iy1 = y0 + t1 * dy;
			intersections.add(new CircleSegmentIntersection(new MyPoint(ix1, iy1), t1));
		}
		if (t2 >= 0 && t2 <= 1 && discriminant > 1e-12) { // zweiter Punkt, wenn nicht identisch
			double ix2 = x0 + t2 * dx;
			double iy2 = y0 + t2 * dy;
			intersections.add(new CircleSegmentIntersection(new MyPoint(ix2, iy2), t2));
		}

		return intersections;
	}
}
