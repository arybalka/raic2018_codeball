package Lama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import net.jafama.FastMath;

/**
 * Created by lamik on 11.09.14.
 */
public class HMath {
	private static final boolean USE_SQRT_MAP = false;

	public static double circleToLineSegmentDistance(Point center, double radius, Point p1, Point p2) {
		return center.distanceToLineSegment(p1, p2) - radius;
	}

	public static final double ONE_DEGREE = Math.PI / 180.0;
	public static final double TEN_DEGREES = Math.PI / 18.0;
	public static final double THIRTY_DEGREES = Math.PI / 6.0;
	public static final double NINETY_DEGREES = Math.PI / 2.0;
	public static final double PI = Math.PI;
	public static final double HALF_PI = Math.PI / 2.0;
	public static final double QUATER_PI = Math.PI / 4.0;
	public static final double SQUARE_OF_TWO = 1.4142135623730950488016887242097;

	public static boolean lineIntersectsCircle(Point pfrom, Point pto, Point center, double radius) {
		return center.distanceToLineSegment2(pfrom, pto) < radius*radius;
	}

	private static HashMap<Double, Double> sqrt_cache = new HashMap<>();

	public static double sqrt(double num) {
		Double res = sqrt_cache.get(num);
		if (res != null) return res;

		double val = Math.sqrt(num);
		sqrt_cache.put(num, val);
		return val;
	}

	public static double angleBetweenAngles(double ang1, double ang2) {
		return Math.min((2.0 * Math.PI) - Math.abs(ang1 - ang2), Math.abs(ang1 - ang2));
	}
	public static boolean isBetween(double what, double a1, double a2) {
		return what >= a1 && what <= a2;
	}

	public static double normalizeAngle(double ang) {
		while (ang < -Math.PI) {
			ang += 2.0*Math.PI;
		}
		while (ang > Math.PI) {
			ang -= 2.0*Math.PI;
		}
		return ang;
	}

	public static double angleToRotate(Point self_pos, double self_angle, Point target_pos) {
//		return HMath.angleBetweenAngles(self_angle, self_pos.getAngleTo(target_pos));
		double a2 = HMath.normalizeAngle(self_pos.getAngleTo(target_pos));
		double a1 = HMath.normalizeAngle(self_angle);

		if (Math.abs(a1 - a2) < 0.0001) return 0;

		double ang = HMath.normalizeAngle( a2 - a1 );
//		if (ang < -Math.PI) return ang + 2.0*Math.PI;
//		if (ang >  Math.PI) return ang - 2.0*Math.PI;

		return ang;
	}


	public static boolean lineWithPolygonIntersection(Point a1, Point a2, Vector<Point> polygon) {
		Point last_p = polygon.lastElement();
		for (Point p : polygon) {
			if (lineWithLineIntersection(a1, a2, last_p, p) != null) return true;
			last_p = p;
		}
		return false;
	}

	public static Point lineWithPolygonIntersectionPoint(Point a1, Point a2, Vector<Point> polygon) {
		Point last_p = polygon.lastElement();
		for (Point p : polygon) {
			Point it = lineWithLineIntersection(a1, a2, last_p, p);
			if (it != null) return it;
			last_p = p;
		}
		return null;
	}


	public static Point lineWithLineIntersection(Point a1, Point a2, Point b1, Point b2) {
		double x12 = a1.x - a2.x;
		double x34 = b1.x - b2.x;
		double y12 = a1.y - a2.y;
		double y34 = b1.y - b2.y;

		double c = x12 * y34 - y12 * x34;

		if (Math.abs(c) < 0.01) {
			// No intersection
//			Debug.echo(() -> " no inter: " + Math.abs(c));
			return null;
		} else {
			// Intersection
			double a = a1.x * a2.y - a1.y * a2.x;
			double b = b1.x * b2.y - b1.y * b2.x;

			double rx = ((a * x34 - b * x12) / c);
			double ry = ((a * y34 - b * y12) / c);
			double x = Math.round(rx);
			double y = Math.round(ry);

			double min_x = Math.min(a1.x, a2.x);
			double max_x = Math.max(a1.x, a2.x);
			double min_y = Math.min(a1.y, a2.y);
			double max_y = Math.max(a1.y, a2.y);
			if (x < min_x || x > max_x || y < min_y || y > max_y) {
//				Debug.echo(() -> " no inter: b1");
				return null;
			}

			min_x = Math.min(b1.x, b2.x);
			max_x = Math.max(b1.x, b2.x);
			min_y = Math.min(b1.y, b2.y);
			max_y = Math.max(b1.y, b2.y);
			if (x < min_x || x > max_x || y < min_y || y > max_y) {
//				Debug.echo(() -> " no inter: b2: " + y + ", " + min_x + ", " + max_x + ", " + y + ", " + min_y + ", " + max_x);
				return null;
			}

//			Debug.echo(String.format("y: %f.2, y: %f.2, min_x: %f.2, max_x: %f.2, min_y: %f.2, max_y: %f.2, ", y, y, min_x, max_x, min_y, max_y));


			return new Point(rx, ry);
		}
	}

	public static boolean pointInsidePolygon(ArrayList<Point> polygon, Point test) {
		int i;
		int j;
		boolean result = false;

		for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
			Point p1 = new Point(polygon.get(i));
			Point p2 = new Point(polygon.get(j));

			if ((p1.y > test.y) != (p2.y > test.y) && (test.x < (p2.x - p1.x) * (test.y - p1.y) / (p2.y-p1.y) + p1.x)) {
				result = !result;
			}
		}
		return result;
	}

	public static boolean pointInsideRect(Point top_left, Point right_bottom, Point test) {
		return test.x >= top_left.x && test.x <= right_bottom.x && test.y >= top_left.y && test.y <= right_bottom.y;
	}

	public static boolean circleInsideRect(Point top_left, Point right_bottom, Point test, double radius) {
		return test.x + radius >= top_left.x && test.x - radius <= right_bottom.x && test.y + radius >= top_left.y && test.y - radius <= right_bottom.y;
	}

	public static Point lineCircleIntersection(Point p1, Point p2, Point center, double radius) {
// compute the euclidean distance between A and B
//		LAB = sqrt((Bx - Ax)² + (By - Ay)²)
//		double dist = p1.distanceTo(p2);

// compute the direction vector D from A to B
//		Dx = (Bx - Ax) / LAB
//		Dy = (By - Ay) / LAB
		Point dir = p2.sub(p1).normalize();

// Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.

// compute the value t of the closest point to the circle center (Cx, Cy)
//		t = Dx * (Cx - Ax) + Dy * (Cy - Ay)
		double t = dir.x * (center.x - p1.x) + dir.y * (center.y - p1.y);

// This is the projection of C on the line from A to B.

// compute the coordinates of the point E on line and closest to C
//		Ex = t * Dx + Ax
//		Ey = t * Dy + Ay
		Point e = dir.scale(t).add(p1);

// compute the euclidean distance from E to C
//		LEC = sqrt((Ex - Cx)² + (Ey - Cy)²)
		double lec = e.distanceTo(center);

// test if the line intersects the circle
//		if (LEC < R) {
		if (lec < radius) {
			// compute distance from t to circle intersection point
//			dt = sqrt(R² - LEC²)
			double dt = Math.sqrt(radius*radius - lec*lec);

			// compute first intersection point
//			Fx = (t - dt) * Dx + Ax
//			Fy = (t - dt) * Dy + Ay
			Point ip1 = p1.add(dir.scale(t - dt));

			// compute second intersection point
//			Gx = (t + dt) * Dx + Ax
//			Gy = (t + dt) * Dy + Ay
			Point ip2 = p1.add(dir.scale(t + dt));

			Point result = ip1.distance2(p2) < ip2.distance2(p2) ? ip1 : ip2;
			double rd = result.distance2(center);
			if (rd < center.distance2(p1) && rd < center.distance2(p2)) return null;
			return result;
		}

		return null;

// else test if the line is tangent to circle
//		else if (LEC == R)
		// tangent point to circle is E

//		else
		// line doesn't touch circle
	}

	public static boolean isOnMap(PointI p) throws NoSuchMethodException {
		throw new NoSuchMethodException();
//		return p.x >= 0 && p.x < Setup.get().scene.getMap().width && p.y >= 0 && p.y < Setup.get().scene.getMap().height;
	}

	public static Point getPerpendicularPoint(Point a, Point b, double distance) {
		Point dir = b.sub(a);
		Point cr = new Point(dir.y, -dir.x);
		Point res = b.add(cr.normalize().scale(distance));

//		Debug.echo(() -> " vector " + a + " > " + b + " perp: " + res);

		return res;
	}

	public static Vector<Point> rectFromPoints(Point topleft, Point bottomright) {

		if (topleft.x > bottomright.x) {
			double x = topleft.x;
			topleft.x = bottomright.x;
			bottomright.x = x;
		}
		if (topleft.y > bottomright.y) {
			double y = topleft.y;
			topleft.y = bottomright.y;
			bottomright.y = y;
		}

		Vector<Point> result = new Vector<>();
		result.add( new Point(topleft) );
		result.add( new Point(bottomright.x, topleft.y) );
		result.add( new Point(bottomright) );
		result.add( new Point(topleft.x, bottomright.y) );
		return result;
	}

	public static double _sin_table[] = null;
	public static double _cos_table[] = null;

	public static double sinFast(int angle) {
		if (_sin_table == null) {
			_sin_table = new double[360];
			for (int i = 0; i < 360; i++) {
				_sin_table[i] = Math.sin((double)i * Math.PI / 180.0);
			}
		}

		return _sin_table[angle];
	}

	public static double cosFast(int angle) {
		if (_cos_table == null) {
			_cos_table = new double[360];
			for (int i = 0; i < 360; i++) {
				_cos_table[i] = Math.cos((double)i * Math.PI / 180.0);
			}
		}

		return _cos_table[angle];
	}

	public static double round(double num) {
		return round(num, 2);
	}
	public static double round(double num, int decimals) {
		double scaler = 100.0;
		if (decimals == 0) return Math.round(num);
		else if (decimals == 1) scaler = 10.0;
		else if (decimals == 3) scaler = 100.0;
		else if (decimals == 4) scaler = 1000.0;
		else if (decimals == 5) scaler = 10000.0;
		else if (decimals == 6) scaler = 100000.0;
		return Math.round(num * scaler)/scaler;
	}

	public static double clamp(double val, double min, double max) {
		return Math.max(Math.min(val, max), min);
	}

	public static int clamp(int val, int min, int max) {
		return Math.max(Math.min(val, max), min);
	}

	public static Point SinCos(double angle) {
		return new Point(Math.sin(angle), Math.cos(angle));
	}

	public static Point CosSin(double angle) {
		return new Point(Math.cos(angle), Math.sin(angle));
//		return new Point(FastMath.cos(angle), Math.sin(angle));
	}

	public static boolean eq(double d1, double d2) {
		return Math.abs(d1 - d2) < 0.00000001;
	}

	public static boolean is_zero(double d) {
		return Math.abs(d) < 0.00000001;
	}

	public static int radToDeg(double rad) {
		return (int)Math.round(rad * 180.0 / Math.PI);
	}
	public static double degToRad(int deg) {
		return deg / 180.0 * Math.PI;
	}

	public static boolean collideCirclePoint(Point c, double radius, Point p) {
		return c.distance2(p) <= radius*radius;
	}

	public static boolean collideCircleCircle(Point c1, double radius1, Point c2, double radius2) {
		return c1.distance2(c2) <= (radius1 + radius2)*(radius1 + radius2);
	}

	public static double lineEquationFromTwoPoints(Point point1, Point point2, float x) {
		double dx = point2.x - point1.x;
		if (dx == 0) return Float.NaN;
		double m = (point2.y - point1.y) / dx;
		double b = point1.y - (m * point1.y);

		return m * x + b;
	}

	public static float invSqrt(float x) {
	    float xhalf = 0.5f*x;
	    int i = Float.floatToIntBits(x);
	    i = 0x5f3759df - (i>>1);
	    x = Float.intBitsToFloat(i);
	    x = x*(1.5f - xhalf*x*x);
	    return x;
	}

	public static double invSqrt(double x) {
	    double xhalf = 0.5d*x;
	    long i = Double.doubleToLongBits(x);
	    i = 0x5fe6ec85e7de30daL - (i>>1);
	    x = Double.longBitsToDouble(i);
	    x = x*(1.5d - xhalf*x*x);
	    return x;
	}

	public static double atan2FastApprox(double x, double y) {

//		double mx = x < 0 ? -x : x;
//		double my = y < 0 ? -y : y;
//		double a = mx < my ? mx / my : my / mx;
//		double s = a * a;
//		double r = ((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a;
//		if (my > mx) r = 1.57079637 - r;
//		if (x < 0) r = Math.PI - r;
//		if (y < 0) r = -r;
//
//		return r;


		double absx = x < 0 ? -x : x,
			absy = y < 0 ? -y : y;
		double m = absx > absy ? absx : absy;

		// undefined behavior in atan2.
		// but here we can safely ignore by setting ort=0
		if (m < 0.0001) return -Math.PI;
		double a = (absx < absy ? absx : absy) / m;
		double s = a * a;
		double r = ((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a;
		if (absy > absx) r = HMath.HALF_PI - r;
		if (x < 0) r = Math.PI - r;
		if (y < 0) r = -r;
		return r;
	}

	private static final double[][] SQRT_MAP_100;

	public static double distanceBetweenPointsI100(PointI from, PointI to) {
		int xo = from.x - to.x;
		if (xo < 0) xo = -xo;

		int yo = from.y - to.y;
		if (yo < 0) yo = -yo;

		if (xo > 100 || yo > 100) return FastMath.hypot(xo, yo);
		return SQRT_MAP_100[xo][yo];
	}

	/**
	 * Determines the point of intersection between a plane defined by a point and a normal vector and a line defined by a point and a direction vector.
	 *
	 * @param plane_point    A point on the plane.
	 * @param plane_normal   The normal vector of the plane.
	 * @param line_point     A point on the line.
	 * @param line_direction The direction vector of the line.
	 * @return The point of intersection between the line and the plane, null if the line is parallel to the plane.
	 */
	public static V3 linePlaneIntersection(V3 plane_point, V3 plane_normal, V3 line_point, V3 line_direction) {
	    if (plane_normal.dotProduct(line_direction) == 0) {
	        return null;
	    }

	    double t = (plane_normal.dotProduct(plane_point) - plane_normal.dotProduct(line_point)) / plane_normal.dotProduct(line_direction);
	    return line_point.add(line_direction.scale(t));
				}

	/**
	 * Determines the point of intersection between a plane defined by a point and a normal vector and a line defined by a point and a direction vector.
	 *
	 * @param plane_point    A point on the plane.
	 * @param plane_normal   The normal vector of the plane.
	 * @param line_point     A point on the line.
	 * @param line_direction The direction vector of the line.
	 * @return The point of intersection between the line and the plane, null if the line is parallel to the plane.
	 */
	public static void LinePlaneIntersection(V3 result, V3 plane_point, V3 plane_normal, V3 line_point, V3 line_direction) {
		double plane_dot_dir = plane_normal.dotProduct(line_direction);
	    if (plane_dot_dir == 0) {
	    	result.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
	        return;
			}

	    double t = (plane_normal.dotProduct(plane_point) - plane_normal.dotProduct(line_point)) / plane_dot_dir;

	    if (result != null) {
			result.set(line_point);
		} else {
	    	result = line_point.copy();
			}

	    result.Add(line_direction.scale(t));
	}

	static {
		if (USE_SQRT_MAP) {
		int DIST_MAP_SIZE = 101;
		SQRT_MAP_100 = new double[DIST_MAP_SIZE][DIST_MAP_SIZE];

		for (int x = 0; x < DIST_MAP_SIZE; x++) {
			for (int y = 0; y < DIST_MAP_SIZE; y++) {
//				SQRT_MAP_100[x][y] = Math.sqrt(x * x + y * y);
				SQRT_MAP_100[x][y] = x <= y ? Math.sqrt(x * x + y * y) : SQRT_MAP_100[y][x];
			}
			}
		} else {
			SQRT_MAP_100 = new double[0][0];
		}
	}

}
