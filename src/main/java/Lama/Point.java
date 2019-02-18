package Lama;

import java.util.Collection;

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.atan2;

import net.jafama.FastMath;

/**
 * Created by lamik on 08.09.14.
 */

public class Point {
	public double x;
	public double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public static Point getDirection(double angle) {
		Point p = new Point(1.0d, 0.0d);
		return p.scale(Math.cos(angle), Math.sin(angle));
	}

	public static Point zero() {
		return new Point(0, 0);
	}

	public static Point getClosestPoint(Collection<Point> points, Point target) {
		Point result = null;
		double min_distance2 = Double.MAX_VALUE;

		for (Point p : points) {
			double distance2 = target.distance2(p);
			if (distance2 < min_distance2) {
				min_distance2 = distance2;
				result = p;
			}
		}

		return result;
	}

	public static Point getFarthestPoint(Collection<Point> points, Point target) {
		Point result = null;
		double max_distance2 = 0;

		for (Point p : points) {
			double distance2 = target.distance2(p);
			if (distance2 > max_distance2) {
				max_distance2 = distance2;
				result = p;
			}
		}

		return result;
	}

	public final Point add(Point p) {
		return new Point(p.x + this.x, p.y + this.y);
	}

	public final Point add(double x, double y) {
		return new Point(x + this.x, y + this.y);
	}

	public final Point sub(Point p) {
		return new Point(this.x - p.x, this.y - p.y);
	}

	public final Point sub(double x, double y) {
		return new Point(this.x - x, this.y - y);
	}

	public final Point invert() {
		return new Point(-this.x, -this.y);
	}

	public final Point scale(double d) {
		return new Point(this.x * d, this.y * d);
	}

	public final Point scale(double x, double y) {
		return new Point(this.x * x, this.y * y);
	}

	public final Point Add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public final Point Add(Point pos) {
		this.x += pos.x;
		this.y += pos.y;
		return this;
	}

	public final Point Sub(Point pos) {
		this.x -= pos.x;
		this.y -= pos.y;
		return this;
	}

	public final Point Sub(double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public final Point Invert() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	public final Point Scale(double x, double y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	public final Point Scale(double d) {
		return Scale(d, d);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) return Math.abs(((Point) o).x - this.x) < 0.00000001 && Math.abs(((Point) o).y - this.y) < 0.00000001;
		return false;
	}

	@Override
	public String toString() {
		return String.format("[%.5f, %.5f]", this.x, this.y);
	}

	public String toStringI() {
		return String.format("[%d, %d]", (int) this.x, (int) this.y);
	}

	@Override
	public int hashCode() {
		int result = (int) (x * 1000);
		result = 31 * result + (int) (y * 1000);
		return result;
	}

	public final double manhattanDistance(Point p) {
		return Math.abs(p.x - this.x) + Math.abs(p.y - this.y);
	}

	public final double distanceTo(Point p) { return distanceTo(p.x, p.y); }

	public final double distanceTo(double x, double y) { return FastMath.hypot(x - this.x, y - this.y); }

	public final double distance2(Point p) { return (p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y); }

	public final double distance2(double x_, double y_) { return (x_ - this.x) * (x_ - this.x) + (y_ - this.y) * (y_ - this.y); }

	public final boolean distanceMore(int x, int y, double dist) { return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) > dist * dist; }

	public final boolean distanceMore(double x, double y, double dist) { return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) > dist * dist; }

	public final boolean distanceMore(Point p, double dist) { return (p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y) > dist * dist; }

	public final boolean distanceMoreEq(Point p, double dist) { return (p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y) >= dist * dist; }

	public final boolean distanceLess(int x, int y, double dist) { return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) < dist * dist; }

	public final boolean distanceLess(double x, double y, double dist) { return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) < dist * dist; }

	public final boolean distanceLess(Point p, double dist) { return (p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y) < dist * dist; }

	public final boolean distanceLessEq(Point p, double dist) { return (p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y) <= dist * dist; }

	public final double lenF() { return FastMath.hypot(x, y); }

	public final double len() { return Math.sqrt(x * x + y * y); }

	public final double dotProduct(Point p) { return this.x * p.x + this.y * p.y; }

	public final Point normalize() {
		double len = this.len();
		return new Point(this.x / len, this.y / len);
	}

	public final Point normalizeF() {
		double len = this.lenF();
		return new Point(this.x / len, this.y / len);
	}

	public final double getAngleTo(Point p) {
		double abs_angle_to = atan2(p.y - this.y, p.x - this.x);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public final double getAngleToFastApprox(Point p) {
		double abs_angle_to = HMath.atan2FastApprox(p.y - this.y, p.x - this.x);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public final Point closestPointOnLineSegment(Point p1, Point p2) {
		double len2 = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
		if (len2 == 0.0) return new Point(p1);

		double t = this.sub(p1).dotProduct(p2.sub(p1)) / len2;
		if (t < 0.0) return new Point(p1);
		else if (t > 1.0) return new Point(p2);

		return p1.add((p2.sub(p1)).scale(t));
	}

	public final double distanceToLineSegment(Point p1, Point p2) {
		return this.distanceTo(closestPointOnLineSegment(p1, p2));
	}

	public final double distanceToLineSegment2(Point p1, Point p2) {
		return this.distance2(closestPointOnLineSegment(p1, p2));
	}

	public final Point reflect(Point normal) {
		/*
		Point res = new Point( this );
		if (rx != 0) {
			res.x = 2 * rx - this.x;
		} else {
			res.y = 2 * ry - this.y;
		}
		return res;
		*/

		Point vel_n = normal.scale(this.dotProduct(normal)); // Normal component
		Point vel_t = this.sub(vel_n); // Tangential component
		return vel_t.sub(vel_n);
	}

	public final Point projectionOf(Point p2) {
		return this.normalize().scale(this.dotProduct(p2) / p2.len());
	}

	public final Point projectOntoLine(Point p1, Point p2) {
		double x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = this.x, y3 = this.y;
		double px = x2 - x1, py = y2 - y1, dAB = px * px + py * py;
		double u = ((x3 - x1) * px + (y3 - y1) * py) / dAB;
		double x = x1 + u * px, y = y1 + u * py;
		return new Point(x, y);
	}

	public final Point rotate(double angle) {
//        ca*v.X - sa*v.Y, sa*v.X + ca*v.Y
		Point sincos = HMath.SinCos(angle);
		return new Point(sincos.y * this.x - sincos.x * this.y, sincos.x * this.x + sincos.y * this.y);
	}

	public final PointI cell() {
		return PointI.fromPoint(this);
	}

	public final Point getPointAhead(double angle, double dist) {
		return getPointAhead(angle, dist, false);
	}

	public final Point getPointAhead(double angle, double dist, boolean fast) {
		if (fast) {
			int ang = (int) HMath.normalizeAngle(Math.round(angle * 180.0 / Math.PI));
			return this.add(HMath.cosFast(ang < 0 ? -ang : ang) * dist, HMath.sinFast(ang < 0 ? -ang : ang) * dist * (ang < 0 ? -1.0 : 1.0));
		}

		return this.add(Math.cos(angle) * dist, Math.sin(angle) * dist);
	}

	public final Point getPointAside(double angle, double dist) {
		// TODO: fast sinCos
		// TODO: hard-code values for full turn
		return this.add(Math.cos(angle + Math.PI / 2) * dist, Math.sin(angle + Math.PI / 2) * dist);
	}

	public final boolean isBetween(Point p1, Point p2) {
		return (this.x >= Math.min(p1.x, p2.x)) && (this.x <= Math.max(p1.x, p2.x)) && (this.y >= Math.min(p1.y, p2.y)) && (this.y <= Math.max(p1.y, p2.y));
	}

	public final Point copy() {
		return new Point(this);
	}

	public final Point clamp(Point min, Point max) {
		return clamp(min.x, min.y, max.x, max.y);
	}

	public final Point clamp(double xmin, double ymin, double xmax, double ymax) {
		return new Point(HMath.clamp(this.x, xmin, xmax), HMath.clamp(this.y, ymin, ymax));
	}

	public final Point perpendicular() {
		return new Point(this.y, -this.x);
	}

	public final Point perpendicular2() {
		return new Point(-this.y, this.x);
	}

	public final Point directionFrom(Point from, boolean fast_inv_sqrt) {
		Point res = this.sub(from);
		if (fast_inv_sqrt) res = res.normalizeFast();
		else res = res.normalize();

		return res;
	}

	public final Point between(Point point) {
		return new Point((this.x + point.x) * 0.5, (this.y + point.y) * 0.5);
	}

	public final Point rotateAround(Point center, double a) {
		double s, c;
		if (a == 0.0) {
			return this.copy();
		}
		if (a == HMath.QUATER_PI) {
			s = c = 0.70710678118654752440084436210485;
		} else if (a == -HMath.QUATER_PI) {
			s = -0.70710678118654752440084436210485;
			c = 0.70710678118654752440084436210485;
		} else if (a == HMath.HALF_PI) {
			s = 1.0;
			c = 0.0;
		} else if (a == -HMath.HALF_PI) {
			s = -1.0;
			c = 0.0;
		} else {
			s = Math.sin(a);
			c = Math.cos(a);
		}

		double x1 = this.x - center.x, y1 = this.y - center.y;
		return new Point(c * x1 - s * y1 + center.x, s * x1 + c * y1 + center.y);
	}

	public final Point rotateAroundF(Point center, double a) {
		double s, c;
		if (a == 0.0) {
			return this.copy();
		}
		if (a == HMath.QUATER_PI) {
			s = c = 0.70710678118654752440084436210485;
		} else if (a == -HMath.QUATER_PI) {
			s = -0.70710678118654752440084436210485;
			c = 0.70710678118654752440084436210485;
		} else if (a == HMath.HALF_PI) {
			s = 1.0;
			c = 0.0;
		} else if (a == -HMath.HALF_PI) {
			s = -1.0;
			c = 0.0;
		} else {
			s = FastMath.sin(a);
			c = FastMath.cos(a);
		}

		double x1 = this.x - center.x, y1 = this.y - center.y;
		return new Point(c * x1 - s * y1 + center.x, s * x1 + c * y1 + center.y);
	}


	public final Point directionTo(Point to, boolean fast_inv_sqrt) {
		return to.directionFrom(this, fast_inv_sqrt);
	}

	public final Point normalizeFast() {
		double inv = HMath.invSqrt(this.x * this.x + this.y * this.y);
		return new Point(this.x * inv, this.y * inv);
	}

	public final Point getPointBetweenPointsWithDistance(Point target, double distance) {
		Point dir = target.sub(this).normalize();
		return this.add(dir.scale(distance));
	}

	public final Point getPointBetweenPointsWithDistanceFast(Point target, double distance) {
		Point dir = target.sub(this).normalizeFast();
		return this.add(dir.scale(distance));
	}

	public final Point getPointBetweenPointsWithRelativeDistance(Point target, double distance) {
		double dist = target.distanceTo(this);
		Point dir = target.sub(this).normalize();
		return this.add(dir.scale(dist * distance));
	}

	public final Point reflectForField() {
		return new Point(4000 - y, 4000 - x);
	}

	public final V3 toXY(double z_) { return new V3(x, y, z_); }

	public final V3 toXY() { return toXY(0); }

	public final V3 toXZ(double y_) { return new V3(x, y_, y); }

	public final V3 toXZ() { return toXZ(0); }

	public final double len2() {
		return x * x + y * y;
	}

	public Point Normalize() {
		double len = this.len();
		this.x /= len;
		this.y /= len;
		return this;
	}

	public Point NormalizeFast() {
		double inv = HMath.invSqrt(this.x * this.x + this.y * this.y);
		this.x *= inv;
		this.y *= inv;

		return this;
	}

	public final Point CalcPointBetweenPointsWithDistance(Point target, double distance) {
		Point dir = target.sub(this);
		dir.Normalize().Scale(distance);
		this.x += dir.x;
		this.y += dir.y;
		return this;
	}

	public final Point CalcPointBetweenPointsWithDistanceFast(Point target, double distance) {
		Point dir = target.sub(this);
		dir.NormalizeFast().Scale(distance);
		this.x += dir.x;
		this.y += dir.y;
		return this;
	}

	public double distance2c(double x_, double y_, double min_value) {
		min_value *= min_value;
		double d2 = distance2(x_, y_);
		return d2 < min_value ? min_value : d2;
	}

}
