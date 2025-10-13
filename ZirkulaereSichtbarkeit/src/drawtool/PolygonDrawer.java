package drawtool;

import javax.swing.*;

import circular_visibility_region.Calculator;
import circular_visibility_region.Cap;
import circular_visibility_region.Circle;
import circular_visibility_region.CircularVisibility;
import circular_visibility_region.DoorSegment;
import circular_visibility_region.LinearVisibility;
import circular_visibility_region.fullLinearResult;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PolygonDrawer extends JPanel {
	private final List<Point> points = new ArrayList<>();
	private final List<Edge> edges = new ArrayList<>();
	private List<Edge> linearVis = new ArrayList<>();
	private final List<Arc2D> arcs = new ArrayList<>(); // List of arcs
	private List<Cap> caps = new ArrayList<>();
	private boolean closed = false;
	private boolean pointPlaced = false;
	private Point visibilityPoint = null;
	private MyPoint observer;
	private fullLinearResult linearRes;

	private static final int CLOSE_DISTANCE = 10;
	private static final int DRAG_DISTANCE = 8;

	// Dragging states
	private int draggedVertexIndex = -1;
	private boolean draggingVisibilityPoint = false;

	public PolygonDrawer() {
		MouseAdapter mouseHandler = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
					System.out.println("Mausklick bei: " + e.getPoint());
					return; // keine weitere Polygon-Logik
				}
				if (!closed) {
					// Polygon wird erstellt
					if (!points.isEmpty() && isNearFirstPoint(e.getPoint())) {
						closed = true;
						edges.add(new Edge(points.get(points.size() - 1), points.get(0)));
						repaint();
						System.out.println("Polygon geschlossen. Bitte Sichtbarkeits-Punkt setzen.");
					} else {
						if (!points.isEmpty()) {
							edges.add(new Edge(points.get(points.size() - 1), e.getPoint()));
						}
						points.add(e.getPoint());
					}
				} else if (!pointPlaced) {
					// Sichtbarkeits-Punkt setzen
					visibilityPoint = e.getPoint();
					pointPlaced = true;
					observer = new MyPoint(visibilityPoint.x, visibilityPoint.y);
					System.out.println("Sichtbarkeits-Punkt gesetzt bei: " + visibilityPoint);
				} else {
					// Prüfen, ob wir einen Vertex ziehen wollen
					draggedVertexIndex = getVertexIndexAt(e.getPoint());
					// Prüfen, ob Sichtbarkeitspunkt gezogen wird
					if (visibilityPoint != null && e.getPoint().distance(visibilityPoint) <= DRAG_DISTANCE) {
						draggingVisibilityPoint = true;
					}
				}
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (draggedVertexIndex != -1) {
					// Vertex verschieben
					points.get(draggedVertexIndex).setLocation(e.getPoint());
					updateEdges();
					repaint();
					recalculateVisibility(); //
				} else if (draggingVisibilityPoint) {
					visibilityPoint.setLocation(e.getPoint());
					repaint();
					recalculateVisibility(); //
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				draggedVertexIndex = -1;
				draggingVisibilityPoint = false;
			}
		};

		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		// Punkte zeichnen
		g.setColor(Color.BLACK);
		for (Point p : points) {
			g.fillOval(p.x - 3, p.y - 3, 6, 6);
		}
		if (points.size() == 4) { // TODO Arc debug
//			System.out.print(Calculator.calculation_of_angle(edges.get((0) ).start2,edges.get((1) ).start2,
//					edges.get((2) ).start2));
			MyPoint s = CircularVisibility.tangentPointDuality(new MyPoint(points.get(0).x, points.get(0).y),
					new MyPoint(points.get(1).x, points.get(1).y), new Edge(new MyPoint(points.get(2).x, points.get(2).y), new MyPoint(points.get(3).x, points.get(3).y)));
		if (s!=null)
			 createArcThroughPoints(new MyPoint(points.get(0).x, points.get(0).y),s,new MyPoint(points.get(1).x, points.get(1).y));
		}
		else {
			
		}
		// Kanten zeichnen
		g.setColor(Color.BLUE);
		for (Edge edge : edges) {
			g.drawLine(edge.start.x, edge.start.y, edge.end.x, edge.end.y);
		}

		// Erster Punkt hervorheben, wenn Polygon nicht geschlossen ist
		if (!points.isEmpty() && !closed) {
			Point first = points.get(0);
			g.setColor(Color.RED);
			g.drawOval(first.x - CLOSE_DISTANCE, first.y - CLOSE_DISTANCE, CLOSE_DISTANCE * 2, CLOSE_DISTANCE * 2);
		}

		// Sichtbarkeits-Punkt zeichnen
		if (visibilityPoint != null) {
			g.setColor(Color.MAGENTA);
			g.fillOval(visibilityPoint.x - 5, visibilityPoint.y - 5, 10, 10);
		}

		if (linearRes != null) {
			for (DoorSegment d : linearRes.doors) {
				if (d.ccw)
					fillPolygonArea(g2d, d.chain, Color.LIGHT_GRAY);
				else {
					fillPolygonArea(g2d, d.chain, Color.LIGHT_GRAY);
				}
			}
		}
//		if (linearVis != null) {
//			g.setColor(Color.CYAN);
//			for (Edge edge : linearVis) {
//				g.drawLine(edge.start.x, edge.start.y, edge.end.x, edge.end.y);
//			}
//		}
		if (pointPlaced) {
			System.out.println(visibilityPoint.toString());

		}
		if (arcs != null) {
			g2d.setColor(Color.MAGENTA);
			for (Arc2D arc : arcs) {
				g2d.draw(arc);
			}
		}

	}

	private boolean isNearFirstPoint(Point p) {
		if (points.isEmpty())
			return false;
		return p.distance(points.get(0)) < CLOSE_DISTANCE;
	}

	private int getVertexIndexAt(Point p) {
		for (int i = 0; i < points.size(); i++) {
			if (p.distance(points.get(i)) <= DRAG_DISTANCE) {
				return i;
			}
		}
		return -1;
	}

	private void updateEdges() {
		edges.clear();
		for (int i = 0; i < points.size() - 1; i++) {
			edges.add(new Edge(points.get(i), points.get(i + 1)));
		}
		if (closed) {
			edges.add(new Edge(points.get(points.size() - 1), points.get(0)));
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	private void recalculateVisibility() {
		arcs.clear();
		// TODO: Sichtbarkeitsberechnung implementieren
		MyPoint observer = new MyPoint(visibilityPoint.x, visibilityPoint.y);
		System.out.println("Sichtbarkeit neu berechnen...");
		List<Edge> wedges = Calculator.findFirstVisibleVertex(edges, visibilityPoint);
		int n = wedges.size();
		wedges = Calculator.calculateVertexAngles(wedges, visibilityPoint);
		// fill the edges end also with an angle-
		for (int i = 0; i < wedges.size(); i++) {
			if(Calculator.calculation_of_angle(wedges.get((i-1+n)%n).start2, wedges.get(i).start2, wedges.get(i).end2)>180) {
				wedges.get(i).start2.isReflex = true;
			}
			else {
				wedges.get(i).start2.isReflex = false;
			}
		}
		for (int i = 0; i < wedges.size(); i++) {
			wedges.get(i).start2.index = i;
			wedges.get(i).end2.index = (i+1)%wedges.size();
			wedges.get(i).end2.angle = wedges.get((i + 1) % wedges.size()).start2.angle;
			wedges.get(i).end = wedges.get((i + 1) % wedges.size()).start;
		}
//		printEdges(wedges);
		
		linearRes = LinearVisibility.calculateLinearVisibilityPolygon(wedges, visibilityPoint);
//		printDoors(linearRes.doors);
		linearVis = linearRes.linearVis;
		if (linearRes.doors != null)
			caps = CircularVisibility.calculateCircularVisibility(wedges, observer, linearRes.doors);
		if (!caps.isEmpty()) {
	
			for (Cap cap : caps) {
				if (cap.isConvex)
					createArcThroughPoints(observer, cap.s1, cap.q);
				else
					createArcThroughPoints(observer, cap.s4, cap.q);
				System.out.println(cap.toString());
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	// TODO "merge" caps, by deleting all caps that are "inside" of another cap,
	// meaning: the point s2-q or s4-q are between another cap s2-q or s4-q
	// respectively.
	private List<Cap> filterCaps(List<Cap> caps2) {
		// TODO Auto-generated method stub
		List<Cap> tempCaps = new ArrayList<>();
		List<Cap> samePocketCaps = new ArrayList<>();
		List<Cap> resultCaps = new ArrayList<>();
		for (int i = 0; i <= caps2.getLast().pocketNr; i++) {
			// get all caps in same pocket
			for (Cap c : caps2) {
				if (c.pocketNr == i) {
					samePocketCaps.add(c);
				}
			}
			// CCW pocket rules
			if (samePocketCaps.getFirst().inCCWpocket) {
				for (Cap c1 : samePocketCaps) {
					boolean b = true;
					for (Cap c2 : samePocketCaps) {
						if (c1.isConvex && !c2.isConvex && (c1 != c2)) {
							if(c2.s4.index == 0)
								c2.s4.index = Integer.MAX_VALUE;
							if (!(c1.s2.index >= c2.q.index) && (c1.q.index <= c2.s4.index)) {
								b = false;
							}
						}
						// c1 concave
						else {
							if (!c1.isConvex && c2.isConvex && (c1 != c2)) {
								if(c1.s4.index == 0)
									c1.s4.index = Integer.MAX_VALUE;
								if(!(c2.s2.index >= c1.q.index)&&(c2.q.index <= c1.s4.index)) {
									b = false;
								}
							}
							// c1 and c2 are the same cap type
						}
					}
					if(b) {
						resultCaps.add(c1);
					}
				}
				// CW pocket rules
			} else {
				for (Cap c1 : samePocketCaps) {
					boolean b = true;
					for (Cap c2 : samePocketCaps) {
						if (c1.isConvex && !c2.isConvex && (c1 != c2)) {
							if ((c1.s2.index <= c2.q.index) && (c1.q.index >= c2.s4.index)) {
								b = false;
							}
						}
						// c1 concave
						else {
							if (!c1.isConvex && c2.isConvex && (c1 != c2)) {
								if((c2.s2.index <= c1.q.index)&&(c2.q.index >= c1.s4.index)) {
									b = false;
								}
							}
							// c1 and c2 are the same cap type
						}
					}
					if(b) {
						resultCaps.add(c1);
					}
				}
			}
		}

		return resultCaps;
	}

	public void fillPolygonArea(Graphics2D g2d, List<MyPoint> polygonPoints, Color color) {
		int[] xPoints = polygonPoints.stream().mapToInt(p -> p.x).toArray();
		int[] yPoints = polygonPoints.stream().mapToInt(p -> p.y).toArray();
		Polygon poly = new Polygon(xPoints, yPoints, polygonPoints.size());

		g2d.setColor(color);
		g2d.fillPolygon(poly);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public Point getVisibilityPoint() {
		return visibilityPoint;
	}

	private void printDoors(List<DoorSegment> a) {
		System.out.println("Polygon Edges:");
		for (DoorSegment door : a) {
			System.out.println(a.toString());
		}
	}

	private void printEdges(List<Edge> edges) {
		System.out.println("Polygon Edges:");
		for (Edge edge : edges) {
			System.out.println(edge);
		}
	}
//    arcs.add(arc);
//    repaint();

	public void createArcFromP2ToP3(Point2D p1, Point2D p2, Point2D p3) {
		// Step 1: Compute the circle center
		Point2D center = getCircleCenter(p1, p2, p3);
		double radius = center.distance(p1); // All three lie on the same circle

		// Step 2: Compute angles from center to p2 and p3
		double angleStart = normalizeAngle(
				Math.toDegrees(Math.atan2(p2.getY() - center.getY(), p2.getX() - center.getX())) + 180);
		double angleEnd = normalizeAngle(
				Math.toDegrees(Math.atan2(p3.getY() - center.getY(), p3.getX() - center.getX())) + 180);

		// Step 3: Compute sweep direction (same as original arc from p1 to p3)
		double originalStart = normalizeAngle(
				Math.toDegrees(Math.atan2(p1.getY() - center.getY(), p1.getX() - center.getX())) + 180);
		double sweep = angleEnd - angleStart;
		if (sweep <= 0)
			sweep += 360;

		// Determine if we need to flip direction to match original arc
		double originalSweep = angleEnd - originalStart;
		if (originalSweep <= 0)
			originalSweep += 360;
		if (sweep > originalSweep) {
			sweep -= 360; // go clockwise to match original arc direction
		}

		// Step 4: Create arc using center-based method
		Arc2D.Double arc = new Arc2D.Double();
		arc.setArcByCenter(center.getX(), center.getY(), radius, angleStart, sweep, Arc2D.OPEN);

		arcs.add(arc);
		repaint();
	}

	public void createArcThroughPoints(MyPoint p1, MyPoint p2, MyPoint p3) {
		try {
			Circle c = Calculator.circumCircle(p1, p2, p3);
		
		double radius = c.center.distance(p1);

		double angleStart = normalizeAngle(
				Math.toDegrees(Math.atan2(p1.y_coordinate - c.center.y_coordinate, -(p1.x_coordinate - c.center.x_coordinate))) + 180);
		double angleMid = normalizeAngle(
				Math.toDegrees(Math.atan2(p2.y_coordinate - c.center.y_coordinate, -(p2.x_coordinate - c.center.x_coordinate))) + 180);
		double angleEnd = normalizeAngle(
				Math.toDegrees(Math.atan2(p3.y_coordinate - c.center.y_coordinate, -(p3.x_coordinate - c.center.x_coordinate))) + 180);

		angleStart = normalizeAngle(angleStart);
		angleMid = normalizeAngle(angleMid);
		angleEnd = normalizeAngle(angleEnd);

		double sweep = angleEnd - angleStart;
		if (sweep <= 0)
			sweep += 360;

		if (!isAngleBetween(angleMid, angleStart, angleEnd)) {
			sweep -= 360; // go clockwise
		}

		Arc2D.Double arc = new Arc2D.Double();
		arc.setArcByCenter(c.center.getX(), c.center.getY(), radius, angleStart, sweep, Arc2D.OPEN);

		arcs.add(arc);
		repaint();
		} finally {
			
		}
	}

	private static Point2D getCircleCenter(Point2D a, Point2D b, Point2D c) {
		double ax = a.getX(), ay = a.getY();
		double bx = b.getX(), by = b.getY();
		double cx = c.getX(), cy = c.getY();

		double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
		if (d == 0)
			throw new IllegalArgumentException("Points are collinear: a:x="+a.getX()+" y="+a.getY() +" b: x="+b.getX()+" y="+b.getY() +" c: x="+c.getX()+" y="+c.getY());

		double ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay)
				+ (cx * cx + cy * cy) * (ay - by)) / d;

		double uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx)
				+ (cx * cx + cy * cy) * (bx - ax)) / d;

		return new Point2D.Double(ux, uy);
	}

	private static double normalizeAngle(double angle) {
		angle %= 360;
		return angle < 0 ? angle + 360 : angle;
	}

	private static boolean isAngleBetween(double target, double start, double end) {
		target = normalizeAngle(target);
		start = normalizeAngle(start);
		end = normalizeAngle(end);
		if (start < end)
			return target > start && target < end;
		return target > start || target < end;
	}

	private static boolean pointsClose(Point2D p1, Point2D p2) {
		double tolerance = 0.5; // pixels
		return p1.distance(p2) < tolerance;
	}

	public static void main(String[] args) {
//		Point a = new Point(1,1);
//		Point b = new Point(2,1);
//		Point c = new Point(3,1);
//		System.out.println(Calculator.calculation_of_angle(a,b,c));
//		 b = new Point(2,2);
//		System.out.println(Calculator.calculation_of_angle(a,b,c));
//		 b = new Point(2,0);
//		System.out.println(Calculator.calculation_of_angle(a,b,c));

		JFrame frame = new JFrame("Polygon Drawer with Drag & Drop");
		PolygonDrawer drawer = new PolygonDrawer();
		frame.add(drawer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		List<MyPoint> s2points = new ArrayList<>();
		 Circle circlesss;
		 MyPoint s1 = new MyPoint(452,338);
		 MyPoint s2 = new MyPoint(193,193); 
		 MyPoint s3 = new MyPoint(89,305);
		 MyPoint s5 = new MyPoint(210,316);
		 MyPoint tangent = CircularVisibility.tangentPointDuality(s1, s2, new Edge(s3,s5));
//		 System.out.println(tangent);
		 circlesss = Calculator.circumCircle(s2, s1, s3);
		 s2points= Calculator.intersectCircleSegment(circlesss, s2, s5);
//		 System.out.println("test"+ Calculator.calculation_of_angle(s1, s2, s3));
		 if(!s2points.isEmpty())
//		 {
//			 for(MyPoint s : s2points)
//				 
//			 System.out.println("TROLOLOLOLO"+ Calculator.calculation_of_angle(s2, s3, s2));
//			 
//		 }
			 s1.isReflex=false;
//		 System.out.println(s3.isReflex);
		SwingUtilities.invokeLater(() -> {
			Graphics2D g2d = (Graphics2D) drawer.getGraphics();
			Point2D p1 = new Point2D.Double(100, 500); // Start
			Point2D p2 = new Point2D.Double(150, 510); // Midpoint
			Point2D p3 = new Point2D.Double(200, 500);
			Point center = new Point(500, 500);
			Point start = new Point(400, 700);
			Point end = new Point(500, 900);

			// drawer.createArcThroughPoints(p1, p2, p3);
//
//            arcs.add(makeArcWithCenter(center, start, end, false)); // CCW
//            drawer.addArcWithCenter( center, start, end, true);
//            drawer.addArcWithCenter( center, start, end, true);

//          drawer.addArcWithCenter(center, start, end, true); // CCW
//
//            drawer.addArcWithCenter(center, start, end, false); // CCW
//          
//            Point p1 = new Point(100, 50); 
//            Point p2 = new Point(100, 20);
//            Point p3 = new Point(200, 50);
//            Point p4 = new Point(100, 0); // zurück zum ersten Punkt x=422.27341341949693,y=255.83133674496895]
			// drawer.makeArcThroughPoints(p1, p2, p3);

			// drawer.addArcWithFourPoints(p1, p2, p3, p4, false); // CW
		});
	}
}