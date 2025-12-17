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
import java.util.ArrayList;
import java.util.List;

public class PolygonDrawer extends JPanel {
	/**
	 * The base framework was implemented with help of ChatGPT
	 */
	private static final long serialVersionUID = 1L;
	private List<Edge> linearCaps = new ArrayList<>();
	private final List<Point> points = new ArrayList<>();
	private final List<Edge> edges = new ArrayList<>();
	private final List<MyArc2D> arcs = new ArrayList<>(); // List of arcs
	private List<Cap> caps = new ArrayList<>();
	private boolean closed = false;
	private boolean pointPlaced = false;
	private boolean circles = false;
	private boolean save = false;
	private Point visibilityPoint = null;
	private fullLinearResult linearRes;

	private static final int CLOSE_DISTANCE = 10;
	private static final int DRAG_DISTANCE = 8;

	// Dragging states
	private int draggedVertexIndex = -1;
	private boolean draggingVisibilityPoint = false;
	public void test() {
		arcs.clear();
		linearCaps.clear();
		repaint();
	}

	public void cicles() {
		circles = !circles;
	}

	public void save() {
		save = !save;
	}

	public PolygonDrawer() {
		setLayout(null); // wir positionieren manuell
		JButton helloButton = new JButton("Clear arcs");
		JButton saveCaps= new JButton("Keep arcs");
		JButton toggleCircles= new JButton("Circles");
		helloButton.setBounds(10, 30, 95, 30); // Position (x=10, y=10)
		saveCaps.setBounds(10, 60, 95, 30);
		toggleCircles.setBounds(10, 90, 90, 30);
		helloButton.addActionListener(e -> test());
		saveCaps.addActionListener(e -> save());
		toggleCircles.addActionListener(e -> cicles());
		add(helloButton);
		add(saveCaps);
		add(toggleCircles);
		
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
					new MyPoint(visibilityPoint.x, visibilityPoint.y);
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
		// ## Arc debuging. If you uncomment this code, this will help you test how tangents on a segment are found, given two points.
//		if (points.size() == 4) { 
////			System.out.print(Calculator.calculation_of_angle(edges.get((0) ).start2,edges.get((1) ).start2,
////					edges.get((2) ).start2));
//			MyPoint s = CircularVisibility.tangentPointDuality(new MyPoint(points.get(0).x, points.get(0).y),
//					new MyPoint(points.get(1).x, points.get(1).y), new Edge(new MyPoint(points.get(2).x, points.get(2).y), new MyPoint(points.get(3).x, points.get(3).y)));
//		if (s!=null)
//			 createArcThroughPoints(new MyPoint(points.get(0).x, points.get(0).y),s,new MyPoint(points.get(1).x, points.get(1).y));
//		}

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

		if (pointPlaced) {
			System.out.println(visibilityPoint.toString());

		}
		if (linearCaps != null) {
			g.setColor(new Color (148, 0, 255));
			for (Edge edge : linearCaps) {
				g.drawLine(edge.start.x, edge.start.y, edge.end.x, edge.end.y);
			}
		}
		if (arcs != null) {
			g2d.setColor(Color.MAGENTA);
			for (MyArc2D arc : arcs) {
				
				if(arc.isConvex()) {
					g2d.setColor(Color.MAGENTA);
				}
				else {
					g2d.setColor(new Color (148, 0, 255));
				}
				 if(circles)
					 arc.setAngleExtent(360);
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
		linearCaps.clear();
		if (!save) {
			arcs.clear();
		}
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
if (linearRes.doors != null)
			caps = CircularVisibility.calculateCircularVisibility(wedges, observer, linearRes.doors);
		if (!caps.isEmpty()) {
	
			for (Cap cap : caps) {
				if (cap.isConvex)
					createArcThroughPoints(observer, cap.s1, cap.q,true);
				else
					createArcThroughPoints(observer, cap.s4, cap.q,false);
				System.out.println(cap.toString());
			}
		}
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

	@SuppressWarnings("unused")
	private void printDoors(List<DoorSegment> a) {
		System.out.println("Polygon Edges:");
		for (DoorSegment door : a) {
			System.out.println(a.toString());
		}
	}
	@SuppressWarnings("unused")
	private void printEdges(List<Edge> edges) {
		System.out.println("Polygon Edges:");
		for (Edge edge : edges) {
			System.out.println(edge);
		}
	}

	// implemented with help of ChatGPT
	public void createArcThroughPoints(MyPoint p1, MyPoint p2, MyPoint p3, boolean b) {
		try {
			double d = 2 * (p1.x_coordinate * (p2.y_coordinate - p3.y_coordinate) + p2.x_coordinate * (p3.y_coordinate - p1.y_coordinate) + p3.x_coordinate * (p1.y_coordinate - p2.y_coordinate));
			if (Math.abs(d) < 1e-9) {
				linearCaps.add(new Edge(p1,p3));
				throw new IllegalArgumentException("Kollinear!");
				}
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

		MyArc2D arc = new MyArc2D();
		arc.setArcByCenter(c.center.getX(), c.center.getY(), radius, angleStart, sweep, Arc2D.OPEN);

		arcs.add((MyArc2D) arc);
		if(b)
			arc.setIsConvex(true);
		else
			arc.setIsConvex(false);
		repaint();
		} finally {
			
		}
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


	public static void main(String[] args) {
		JFrame frame = new JFrame("Polygon Drawer with Drag & Drop");
		PolygonDrawer drawer = new PolygonDrawer();
		frame.add(drawer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}