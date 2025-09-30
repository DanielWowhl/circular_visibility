package drawtool;

import java.awt.Point;

public class MyPoint extends Point {
	private static final long serialVersionUID = 1L; // damit der Warning
	public double angle; // Winkel in Grad
	public double x_coordinate, y_coordinate;
	public int index ; // for removing hidden Points from the list

	public MyPoint(int x, int y) {
		this.x = x;
		this.y = y;
		this.x_coordinate = x;
		this.y_coordinate = y;
		this.angle = 0;
	}

	public MyPoint(double x_coordinate, double y_coordinate) {
		this.x_coordinate = x_coordinate;
		this.y_coordinate = y_coordinate;
		this.x = (int) (x_coordinate + 0.5); // runden wie bei der Klasse Point
		this.y = (int) (y_coordinate + 0.5);
		this.angle = 0;
	}
	public MyPoint(double x_coordinate, double y_coordinate, int index) {
		this.index=index;
		this.x_coordinate = x_coordinate;
		this.y_coordinate = y_coordinate;
		this.x = (int) (x_coordinate + 0.5); // runden wie bei der Klasse Point
		this.y = (int) (y_coordinate + 0.5);
		this.angle = 0;
	}

	public MyPoint() {		
		super();
		this.angle=0;
		
	}

	public MyPoint(MyPoint newP) {
		angle = newP.angle;
		this.x_coordinate = newP.x_coordinate;
		this.y_coordinate = newP.y_coordinate;
		this.index = newP.index;
	}

	public void setCoordinates(double x, double y) {
		this.x_coordinate = x;
		this.y_coordinate = y;
		this.x = (int) (x + 0.5); // runden wie bei der Klasse Point
		this.y = (int) (y + 0.5);
	}

	public String toString() {
		if (index == -1) {
			return getClass().getName() + "[Angle=" + angle + ",x=" + x_coordinate + ",y=" + y_coordinate + "]";
		}
		return getClass().getName() + "[index=" + index + ",x=" + x_coordinate + ",y=" + y_coordinate + "]";
	}
    public double distance(MyPoint pt) {
        double px = pt.x_coordinate- this.x_coordinate;
        double py = pt.y_coordinate - this.y_coordinate;
        return Math.sqrt(px * px + py * py);
    }
}