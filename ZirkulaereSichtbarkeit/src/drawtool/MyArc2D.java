package drawtool;

import java.awt.geom.Arc2D;

public class MyArc2D extends Arc2D.Double {
	private static final long serialVersionUID = 1L;
	
    private boolean isConvex;

    public MyArc2D() {
        super();
    }

    // Getter and setter for your flag
    public boolean isConvex() {
        return isConvex;
    }

    public void setIsConvex(boolean isConvex) {
        this.isConvex = isConvex;
    }
}