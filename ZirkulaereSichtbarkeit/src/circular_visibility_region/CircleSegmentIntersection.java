package circular_visibility_region;

import drawtool.MyPoint;

public class CircleSegmentIntersection {
        public final MyPoint point;
        public final double t;

        public CircleSegmentIntersection(MyPoint point, double t) {
            this.point = point;
            this.t = t;
        }

        @Override
        public String toString() {
            return "Intersection{point=" + point + ", t=" + t + "}";
        }
}
