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

		// for each Pocket, find all convex and concave caps
		for (DoorSegment dSegment : doors) {

			if (!dSegment.ccw) {
				// CW Pocket
				// find convex Caps
				MyPoint next = null; // for skipping to edge, where the old cap ends.
				MyPoint last = dSegment.chain.getLast();
				for (MyPoint s2 : dSegment.chain) {
					if (next == null || next.index < s2.index) { // if not skipped, start calculating
						// if convex Vertex, and not the "onEdge" of the doorsegment, as the index is
						// off by 1.
						if (Calculator.calculation_of_angle(poly.get((s2.index - 1 + n) % n).start2, s2,
								poly.get((s2.index + 1) % n).start2) > 180 && s2 != last) {
							result = findConvexCap(s2, dSegment, poly, observer);
							if (result != null) {
								caps.add(result);
								next = result.q;
							}
						}
					}
				}
				
				// find concave Caps
				// TODO Find Concave Caps
			} else {
				// CCW Pocket
				// idea: we traverse it backwards, so that the reflex vertex, that cuts our
				// linear view, is, just as in the CCW pocket, the first tested vertex for caps.
				MyPoint next = null; // for skipping to edge, where the old cap ends.
				MyPoint last = dSegment.chain.getFirst();
				for (int i = (dSegment.chain.size() - 1); i >= 0; i--) {
					MyPoint s2 = dSegment.chain.get(i);
					if (next == null || next.index > s2.index) {
						if (Calculator.calculation_of_angle(
								poly.get(((dSegment.chain.get(i).index) + n - 1) % n).start2, s2,
								poly.get(dSegment.chain.get(i).index).end2) > 180 && s2 != last) {
							result = findConvexCap(s2, dSegment, poly, observer);
							if (result != null) {
								caps.add(result);
								next = result.q;
								// TODO ADD THE SKIP
							}
						}
					}

				}
			// find convex Caps

			// find concave Caps
			}

		}
		// TODO "merge" caps, by deleting all caps that are "inside" of another cap, meaning: the point s2-q or s4-q are between another cap s2-q or s4-q respectively.
		
		return caps;
		// return

//		for (DoorSegment dSegment : doors) {
//			if (!dSegment.ccw) { // if CW pocket
//				System.out.println("Wir sind im CW Pocket");
//				for (int i = dSegment.reflexVertexIndex % poly.size(); i != dSegment.onEdge.index; i++) {
//					// winkelcheck
//					i = i % poly.size();
//					if (Calculator.calculation_of_angle(poly.get((i - 1 + n) % n).start2,
//							poly.get(i).start2, poly.get(i).end2) > 180) {
//						result = findConvexCap(poly.get(i).start2, dSegment, poly, observer);
//						if (result != null)
//							caps.add(result);
//					}
//
//				}
//			} else { // else CCW pocket "analog"
//				for (int i = dSegment.index%n; i%n != dSegment.onEdge.index%n; i=(i-1+n)%n) {
//					// winkelcheck
//					
//					if (Calculator.calculation_of_angle(poly.get((i - 1 + n) % n).start2,
//							poly.get(i).start2, poly.get(i).end2) > 180) {
//
//						result = findConvexCap(poly.get(i).start2, dSegment, poly, observer);
//						if (result != null)
//							caps.add(result);
//					}
//				}
//			}
//		}
//		return caps;
	}

	// input: reflex Vertex s2, a pocket d.
	// output: Convex Cap or Null
	public static Cap findConvexCap(MyPoint s2, DoorSegment d, List<Edge> poly, MyPoint observer) {
		Double resT = Double.NEGATIVE_INFINITY; // the t value, to find. the biggest one is the one .
		Double t;
		double orientationStartPoint, orientationEndPoint; // used for cheching weather a point is left or right of a
															// line
		MyPoint s = null, s1 = null;
		int n = poly.size();
		// finde erstes 3a Segment
		IntersectionRes first3aSegment = Calculator.findFirst3aSegment(poly, observer, s2);
		System.out.println("3aSEgment" + first3aSegment.intersection.toString());
		// Basic Idea
		// For all type 3a Segments
		// calculate D
		// calculate C of Point result of D
		// calculate C of u2, as D will be not an end point
		// Test for Convex Cap.
		// calculate q
		if (!d.ccw) { // check the chain P(r,s2) without s2.
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
					
					MyPoint intersection = Calculator.CiclePolygonIntersection(poly, c, s2.index, s1.index, true, true,
							d);
					if (intersection == null) {
						System.out.println("Cap discarded: too small to draw, though Mathematicaly exists!");
						return null;
					}
					return new Cap(true, s1, s2, intersection);
					
				} else {
					System.out.println("Cap discarded: s2 is not a convex support.");
				}
			}

		} else {
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
//						System.out.println("");
						t = computeT(observer, s, s2);
//						System.out.println("TValue Edge: "+t);
						if (resT > t) {
							s1 = s;
							resT = t;
						}
					} else {

						resT = computeT(observer, s, s2);
						s1 = s;
	//					System.out.println("> T \"first\"Value of: s1 auf Kante WERT: " + resT);
					}
				}
				if (orientationEndPoint > 0) {
					s = poly.get(i).end2; // u2
					if (Calculator.cross(observer, s2, s) > 0) { // teste u2, ob Typ 3a
						if (s1 != null) {
							t = computeT(observer, s, s2);
//							System.out.println("t Value u2: "+t);
							if (resT > t) {
								s1 = s;
								resT = t;
							}
						} else {

							resT = computeT(observer, s, s2);
//						System.out.println("> T \"first\"Value of: u2 WERT: " + resT);
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
					MyPoint intersection = Calculator.CiclePolygonIntersection(poly, c, s2.index, s1.index, true, false,
							d);
					if (intersection == null) {
						System.out.println("Cap discarded: too small to draw, though Mathematicaly exists!");
						return null;
					}
					return new Cap(true, s1, s2, intersection);

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

//		// for each Segment Typ 3a
//		for (int i = (first3aSegment.onEdge.start2.index) % poly.size(); i % poly.size() != s2.index
//				% poly.size(); i++) {
//
//			// D(o,s2,e)
//			MyPoint s = computeContactPoint(observer, s2, poly.get(first3aSegment.intersection.index%poly.size()).start2,
//					poly.get(first3aSegment.intersection.index%poly.size()).end2);
//
//			// wenn s1 ein Punkt auf einer Kante ist
//			if (s != null) { // Punkt auf Kante fuer Concave Support gefunden?
//
//				double orientation;
//				if (d.ccw) { // Die Orientierung fuer 3a Segmenttyp ist umgekehrt fuer CW oder CCW Tasche
//					orientation = -Calculator.cross(observer, s2, s);
//				} else {
//					orientation = Calculator.cross(observer, s2, s);
//				}
//				if (orientation < 0) { // teste ob Typ 3a.
//					System.out.println("punkt der möglich ist gefunden"+ s.toString()); //TODO DEBUG
//					if (s1 != null) { // Vergleiche, sonst speichere
//						t = computeTValue(observer, s, s2);
//						if (resT < t) {
//							s1 = s;
//							resT = t;
//						}
//					} else {
//						resT = computeTValue(observer, s, s2);
//						s1 = s;
//					}
//				}
//			}
//			// Wenn s1 ein Reflexvertex ist
//			s = poly.get(first3aSegment.intersection.index).end2; // u2
//			if (Calculator.cross(observer, s2, s) < 0) { // teste u2, ob Typ 3a
//				if (s1 != null) {
//					t = computeTValue(observer, s, s2);
//					if (resT < t) {
//						s1 = s;
//						resT = t;
//					}
//				} else {
//					resT = computeTValue(observer, s, s2);
//					s1 = s;
//				}
//			}
//		}

//	public static Double computeTValue(MyPoint a, MyPoint b, MyPoint c) {
//	    // Prüfen ob Punkte gültig sind
//	    if (a == null || b == null || c == null) return null;
//
//	    // Vektoren
//	    double ax = a.x_coordinate, ay = a.y_coordinate;
//	    double bx = b.x_coordinate, by = b.y_coordinate;
//	    double cx = c.x_coordinate, cy = c.y_coordinate;
//
//	    // Vektor ac
//	    double acx = cx - ax;
//	    double acy = cy - ay;
//
//	    // Mitte von ac
//	    double mx = (ax + cx) / 2.0;
//	    double my = (ay + cy) / 2.0;
//
//	    // {c-a} = CCW-Rotation von (cx-ax, cy-ay) = (-(cy - ay), cx - ax)
//	    double rx = -acy;
//	    double ry = acx;
//
//	    // Gesuchte Form: C(a b c) = (a+c)/2 + t * {c-a}
//	    // -> finde t
//
//	    // Wir brauchen den Schnittpunkt der Mittelsenkrechten:
//	    // Skalarprodukte nutzen, um t zu bestimmen
//	    double d1x = bx - ax;
//	    double d1y = by - ay;
//	    double d2x = bx - cx;
//	    double d2y = by - cy;
//
//	    double det = acx * d1y - acy * d1x;
//	    if (Math.abs(det) < 1e-12) return null; // fast kollinear
//
//	    // Formel für t: Löse lineares Gleichungssystem
//	    // (mx + t*rx - ?) = ...
//	    double num = ((bx - mx) * d2y - (by - my) * d2x);
//	    double den = (rx * d2y - ry * d2x);
//
//	    if (Math.abs(den) < 1e-12) return null; // degeneriert
//
//	    double t = num / den;
//	    return -t;
//	}

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
			if (dist(bestD, e.start2) < 1e-9 || dist(bestD, e.end2) < 1e-9) {
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
