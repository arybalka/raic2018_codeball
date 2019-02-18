package Lama;

import java.util.Objects;

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.atan2;

import net.jafama.FastMath;

/**
 * Created by lamik on 08.09.14.
 */

public class V3 implements Copiable<V3> {
	public static final V3 ZERO = new V3(0, 0, 0);
	public double x;
	public double y;
	public double z;

	public static final V3 NORMAL_M100 = new V3(-1, 0, 0);
	public static final V3 NORMAL_100 = new V3(1, 0, 0);
	public static final V3 NORMAL_0M10 = new V3(0, -1, 0);
	public static final V3 NORMAL_010 = new V3(0, 1, 0);
	public static final V3 NORMAL_00M1 = new V3(0, 0, -1);
	public static final V3 NORMAL_001 = new V3(0, 0, 1);

	public V3(double x_, double y_, double z_) {
		x = x_;
		y = y_;
		z = z_;
	}

	public V3(double x_, double y_) {
		x = x_;
		y = y_;
		z = 0;
	}

	public V3(V3 p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public static V3 zero() {
		return new V3(0, 0, 0);
	}

	public final V3 add(V3 p) {
		return new V3(p.x + x, p.y + y, p.z + z);
	}

	public final V3 add(double x_, double y_, double z_) {
		return new V3(x_ + x, y_ + y, z_ + z);
	}

	public final V3 sub(V3 p) {
		return new V3(x - p.x, y - p.y, z - p.z);
	}

	public final V3 sub(double x_, double y_, double z_) {
		return new V3(x - x_, y - y_, z - z_);
	}

	public final V3 invert() {
		return new V3(-x, -y, -z);
	}

	public final V3 scale(double d) {
		return new V3(x * d, y * d, z * d);
	}

	public final V3 scale(double x_, double y_, double z_) {
		return new V3(x * x_, y * y_, z * z_);
	}

	public final V3 Add(double x_, double y_, double z_) {
		x += x_;
		y += y_;
		z += z_;
		return this;
	}

	public final V3 Add(V3 pos) {
		x += pos.x;
		y += pos.y;
		z += pos.z;
		return this;
	}

	public final V3 Sub(V3 pos) {
		x -= pos.x;
		y -= pos.y;
		z -= pos.z;
		return this;
	}

	public final V3 Sub(double x_, double y_, double z_) {
		x -= x_;
		y -= y_;
		z -= z_;
		return this;
	}

	public final V3 Invert() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public final V3 Scale(double x_, double y_, double z_) {
		x *= x_;
		y *= y_;
		z *= z_;
		return this;
	}

	public final V3 Scale(double d) {
		return Scale(d, d, d);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof V3) return Math.abs(((V3) o).x - this.x) < 0.00000001 && Math.abs(((V3) o).y - this.y) < 0.00000001;
		return false;
	}

	@Override
	public String toString() {
//		return String.format("[%.16f, %.16f, %.16f]", this.x, this.y, this.z);
		return String.format("[%.5f, %.5f, %.5f]", this.x, this.y, this.z);
	}

	public String toStringI() {
		return String.format("[%d, %d, %d]", (int) this.x, (int) this.y, (int) this.z);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	public final double manhattanDistance(V3 p) {
		return Math.abs(p.x - this.x) + Math.abs(p.y - this.y) + Math.abs(p.z - this.z);
	}

	public final double distanceTo(V3 p) { return distanceTo(p.x, p.y, p.z); }

	public final double distanceTo(double x_, double y_, double z_) { return Math.sqrt((x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z)); }

	public final double distanceToF(double x_, double y_, double z_) { return FastMath.hypot(x_ - x, y_ - y, z_ - z); }

	public final double distanceToF(V3 p) { return distanceToF(p.x, p.y, p.z); }

	public final double distance2(V3 p) { return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z); }

	public final double distance2(double x_, double y_, double z_) { return (x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z); }

	public final boolean distanceMore(int x_, int y_, int z_, double dist) { return (x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z) > dist * dist; }

	public final boolean distanceMore(double x_, double y_, double z_, double dist) { return (x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z) > dist * dist; }

	public final boolean distanceMore(V3 p, double dist) { return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z) > dist * dist; }

	public final boolean distanceMoreEq(V3 p, double dist) { return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z) >= dist * dist; }

	public final boolean distanceLess(int x_, int y_, int z_, double dist) { return (x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z) < dist * dist; }

	public final boolean distanceLess(double x_, double y_, double z_, double dist) { return (x_ - x) * (x_ - x) + (y_ - y) * (y_ - y) + (z_ - z) * (z_ - z) < dist * dist; }

	public final boolean distanceLess(V3 p, double dist) { return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z) < dist * dist; }

	public final boolean distanceLessEq(V3 p, double dist) { return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z) <= dist * dist; }

	public final double lenF() { return FastMath.hypot(x, y, z); }

	public final double len() { return Math.sqrt(len2()); }

	public final double len2() { return x * x + y * y + z * z; }

	public final double dotProduct(V3 p) { return x * p.x + y * p.y + z * p.z; }

	public final double dotProduct(double x_, double y_, double z_) { return x * x_ + y * y_ + z * z_; }

	public final V3 normalize() {
		double len = this.len();
		return new V3(x / len, y / len, z / len);
	}

	public final V3 aheadXY(double angle, double dist) {
		return aheadXZ(angle, dist, false);
	}

	public final V3 aheadXY(double angle, double dist, boolean fast) {
		if (fast) {
			int ang = (int) HMath.normalizeAngle(Math.round(angle * 180.0 / Math.PI));
			return this.add(HMath.cosFast(ang < 0 ? -ang : ang) * dist, HMath.sinFast(ang < 0 ? -ang : ang) * dist * (ang < 0 ? -1.0 : 1.0), 0);
		}

		return this.add(Math.cos(angle) * dist, Math.sin(angle) * dist, 0);
	}

	public final V3 aheadXZ(double angle, double dist) {
		return aheadXZ(angle, dist, false);
	}

	public final V3 aheadXZ(double angle, double dist, boolean fast) {
		if (fast) {
			int ang = (int) HMath.normalizeAngle(Math.round(angle * 180.0 / Math.PI));
			return this.add(HMath.cosFast(ang < 0 ? -ang : ang) * dist, 0, HMath.sinFast(ang < 0 ? -ang : ang) * dist * (ang < 0 ? -1.0 : 1.0));
		}

		return this.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
	}

	@Override
	public V3 copy() {
		return new V3(this);
	}

	public final V3 reflectAgainstAxisPlane(boolean xy_, boolean xz_, boolean yz_) {
		return new V3(yz_ ? -x : x, xz_ ? -y : y, xy_ ? -z : z);
	}

	public final V3 cross(V3 v) {
		double xx = y * v.z - z * v.y;
		double yy = z * v.x - x * v.z;
		double zz = x * v.y - y * v.x;

		return new V3(xx, yy, zz);
	}

	public final void Cross(V3 v) {
		double xx = y * v.z - z * v.y;
		double yy = z * v.x - x * v.z;
		double zz = x * v.y - y * v.x;

		this.x = xx;
		this.y = yy;
		this.z = zz;
	}


/*	public double getAngleTo(V3 p) {
		double abs_angle_to = atan2(p.y - this.y, p.x - this.x);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public double getAngleToFastApprox(V3 p) {
		double abs_angle_to = HMath.atan2FastApprox(p.y - this.y, p.x - this.x);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public V3 closestV3OnLineSegment(V3 p1, V3 p2) {
		double len2 = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
		if (len2 == 0.0) return new V3(p1);

		double t = this.sub(p1).dotProduct(p2.sub(p1)) / len2;
		if (t < 0.0) return new V3(p1);
		else if (t > 1.0) return new V3(p2);

		return p1.add((p2.sub(p1)).scale(t));
	}

	public double distanceToLineSegment(V3 p1, V3 p2) {
		return this.distanceTo(closestV3OnLineSegment(p1, p2));
	}

	public double distanceToLineSegment2(V3 p1, V3 p2) {
		return this.distance2(closestV3OnLineSegment(p1, p2));
	}

	public V3 reflect(V3 normal) {
		V3 vel_n = normal.scale(this.dotProduct(normal)); // Normal component
		V3 vel_t = this.sub(vel_n); // Tangential component
		return vel_t.sub(vel_n);
	}

	public V3 projectionOf(V3 p2) {
		return this.normalize().scale(this.dotProduct(p2) / p2.len());
	}

	public V3 projectOntoLine(V3 p1, V3 p2) {
		double x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = this.x, y3 = this.y;
		double px = x2 - x1, py = y2 - y1, dAB = px * px + py * py;
		double u = ((x3 - x1) * px + (y3 - y1) * py) / dAB;
		double x = x1 + u * px, y = y1 + u * py;
		return new V3(x, y);
	}

	public V3 rotate(double angle) {
//        ca*v.X - sa*v.Y, sa*v.X + ca*v.Y
		V3 sincos = HMath.SinCos(angle);
		return new V3(sincos.y * this.x - sincos.x * this.y, sincos.x * this.x + sincos.y * this.y);
	}


	public V3 ahead(double angle, double dist) {
		return ahead(angle, dist, false);
	}

	public V3 ahead(double angle, double dist, boolean fast) {
		if (fast) {
			int ang = (int) HMath.normalizeAngle(Math.round(angle * 180.0 / Math.PI));
			return this.add(HMath.cosFast(ang < 0 ? -ang : ang) * dist, HMath.sinFast(ang < 0 ? -ang : ang) * dist * (ang < 0 ? -1.0 : 1.0));
		}

		return this.add(Math.cos(angle) * dist, Math.sin(angle) * dist);
	}

	public V3 getV3Aside(double angle, double dist) {
		// TODO: fast sinCos
		// TODO: hard-code values for full turn
		return this.add(Math.cos(angle + Math.PI / 2) * dist, Math.sin(angle + Math.PI / 2) * dist);
	}

	public boolean isBetween(V3 p1, V3 p2) {
		return (this.x >= Math.min(p1.x, p2.x)) && (this.x <= Math.max(p1.x, p2.x)) && (this.y >= Math.min(p1.y, p2.y)) && (this.y <= Math.max(p1.y, p2.y));
	}

	public static V3 getDirection(double angle) {
		V3 p = new V3(1.0d, 0.0d);
		return p.scale(Math.cos(angle), Math.sin(angle));
	}

	public static V3 getClosestV3(Collection<V3> V3s, V3 target) {
		V3 result = null;
		double min_distance2 = Double.MAX_VALUE;

		for (V3 p : V3s) {
			double distance2 = target.distance2(p);
			if (distance2 < min_distance2) {
				min_distance2 = distance2;
				result = p;
			}
		}

		return result;
	}

	public static V3 getFarthestV3(Collection<V3> V3s, V3 target) {
		V3 result = null;
		double max_distance2 = 0;

		for (V3 p : V3s) {
			double distance2 = target.distance2(p);
			if (distance2 > max_distance2) {
				max_distance2 = distance2;
				result = p;
			}
		}

		return result;
	}

	public V3 clamp(V3 min, V3 max) {
		return clamp(min.x, min.y, max.x, max.y);
	}

	public V3 clamp(double xmin, double ymin, double xmax, double ymax) {
		return new V3(HMath.clamp(this.x, xmin, xmax), HMath.clamp(this.y, ymin, ymax));
	}

	public V3 perpendicular() {
		return new V3(this.y, -this.x);
	}

	public V3 perpendicular2() {
		return new V3(-this.y, this.x);
	}

	public V3 directionFrom(V3 from, boolean fast_inv_sqrt) {
		V3 res = this.sub(from);
		if (fast_inv_sqrt) res = res.normalizeFast();
		else res = res.normalize();

		return res;
	}

	public V3 between(V3 V3) {
		return new V3((this.x + V3.x) * 0.5, (this.y + V3.y) * 0.5);
	}

	public V3 rotateAround(V3 center, double a) {
		double s, c;
		if (a == 0.0) {
			return this.copy();
		} if (a == HMath.QUATER_PI) {
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
		return new V3(c * x1 - s * y1 + center.x, s * x1 + c * y1 + center.y);
	}

	public V3 rotateAroundF(V3 center, double a) {
		double s, c;
		if (a == 0.0) {
			return this.copy();
		} if (a == HMath.QUATER_PI) {
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
		return new V3(c * x1 - s * y1 + center.x, s * x1 + c * y1 + center.y);
	}


	public V3 directionTo(V3 to, boolean fast_inv_sqrt) {
		return to.directionFrom(this, fast_inv_sqrt);
	}

	public V3 normalizeFast() {
		double inv = HMath.invSqrt(this.x * this.x + this.y * this.y);
		return new V3(this.x * inv, this.y * inv);
	}

	public V3 getV3BetweenV3sWithDistance(V3 target, double distance) {
		V3 dir = target.sub(this).normalize();
		return this.add(dir.scale(distance));
	}

	public V3 getV3BetweenV3sWithRelativeDistance(V3 target, double distance) {
		double dist = target.distanceTo(this);
		V3 dir = target.sub(this).normalize();
		return this.add(dir.scale(dist * distance));
	}

	public V3 reflectForField() {
		return new V3(4000 - y, 4000 - x);
	}
	*/

	public final Point toPoint() {
		return new Point(x, y);
	}

	public final Point pointFromXZ() {
		return new Point(x, z);
	}

	public final Point pointFromXY() {
		return new Point(x, y);
	}

	public final Point pointFromYZ() {
		return new Point(y, z);
	}

	public final V3 clamp(double max) {
		double m2 = max * max;
		double l2 = x * x + y * y + z * z;
		if (l2 > m2) {
			double l = Math.sqrt(l2);
			return this.scale(max / l);
		}

		return this.copy();
	}

	public final void Clamp(double max) {
		double m2 = max * max;
		double l2 = x * x + y * y + z * z;
		if (l2 > m2) {
			double l = Math.sqrt(l2);
			Scale(max / l);
		}
	}

	public final V3 pos() {
		return new V3(this.x, this.y, this.z);
	}

	public final void Replace(Double x_, Double y_, Double z_) {
		if (x_ != null) x = x_;
		if (y_ != null) y = y_;
		if (z_ != null) z = z_;
	}

	public final V3 replace(Double x_, Double y_, Double z_) {
		return new V3(x_ != null ? x_ : x, y_ != null ? y_ : y, z_ != null ? z_ : z);
	}

	public double getAngleToXZ(V3 target) {
		double abs_angle_to = atan2(target.z - this.z, target.x - this.x);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public double getAngleToXZ(double x_, double z_) {
		double abs_angle_to = atan2(z_ - this.z, x_ - this.x);

//		double abs_angle_to = Math.acos(x_ * x + z_ * z);

		while (abs_angle_to < -PI) {
			abs_angle_to += 2.0D * PI;
		}

		while (abs_angle_to > PI) {
			abs_angle_to -= 2.0D * PI;
		}

		return abs_angle_to;
	}

	public double getAngleToV3RelativeToZero(V3 vec) {
		return getAngleToV3RelativeToZero(vec.x, vec.z, vec.z);
	}

	public double getAngleToV3RelativeToZero(double x_, double y_, double z_) {
		double d = this.dotProduct(x_, y_, z_);
		double l2 = this.len2() * (x_ * x_ + y_ * y_ + z_ * z_);
		return Math.acos(d * HMath.invSqrt(l2));
	}

	public double getCosOfAngleToV3RelativeToZero(double x_, double y_, double z_) {
		double d = this.dotProduct(x_, y_, z_);
		double l2 = this.len2() * (x_ * x_ + y_ * y_ + z_ * z_);
		return d * HMath.invSqrt(l2);
	}

	public final double distance2c(V3 target, double min_value) {
		min_value *= min_value;
		double d2 = distance2(target);
		return d2 < min_value ? min_value : d2;
	}

	public final double distance2c(double x_, double y_, double z_, double min_value) {
		min_value *= min_value;
		double d2 = distance2(x_, y_, z_);
		return d2 < min_value ? min_value : d2;
	}

	public final V3 rotateAroundXZ(V3 center, double a) {
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

		double x1 = this.x - center.x, z1 = this.z - center.z;
		return new V3(c * x1 - s * z1 + center.x, this.y, s * x1 + c * z1 + center.z);
	}

	public V3 scaleXY(double v) {
		return new V3(x * v, y * v, z);
	}

	public V3 scaleXZ(double v) {
		return new V3(x * v, y, z * v);
	}

	public V3 scaleYZ(double v) {
		return new V3(x, y * v, z * v);
	}

	public V3 scaleXY(double x_, double y_) {
		return new V3(x * x_, y * y_, z);
	}

	public V3 scaleXZ(double x_, double z_) {
		return new V3(x * x_, y, z * z_);
	}

	public V3 scaleYZ(double y_, double z_) {
		return new V3(x, y * y_, z * z_);
	}

	public V3 dirTo(V3 v) {
		return v.sub(this);
	}

	public V3 scaleTo(double v) {
		return scaleTo(v, v, v);
	}

	public V3 scaleTo(double x_, double y_, double z_) {
		double len = this.len();
		return new V3(x_ * x / len, y_ * y / len, z_ * z / len);
	}

	public V3 ScaleTo(double x_, double y_, double z_) {
		double len = this.len();
		x *= x_ / len;
		y *= y_ / len;
		z *= z_ / len;
		return this;
	}

	public V3 set(V3 v) {
		x = v.x;
		y = v.y;
		z = v.z;

		return this;
	}

	public V3 set(double x_, double y_, double z_) {
		x = x_;
		y = y_;
		z = z_;

		return this;
	}

	public final V3 closestPointOnLineSegment(V3 p1, V3 p2) {
		double len2 = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z);
		if (len2 == 0.0) return p1.copy();

		// TODO: speed up
		double t = this.sub(p1).dotProduct(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z) / len2;
		if (t < 0.0) return p1.copy();
		else if (t > 1.0) return p2.copy();

		return p1.add((p2.x - p1.x) * t, (p2.y - p1.y) * t, (p2.z - p1.z) * t);
	}

	public final double distanceToLineSegment(V3 p1, V3 p2) {
		return this.distanceTo(closestPointOnLineSegment(p1, p2));
	}

	public final double distanceToLineSegment2(V3 p1, V3 p2) {
		return this.distance2(closestPointOnLineSegment(p1, p2));
	}

	public final V3 projectXY() {
		return new V3(x, y, 0);
	}

	public final V3 projectXZ() {
		return new V3(x, 0, z);
	}

	public final V3 projectYZ() {
		return new V3(0, y, z);
	}

	public final V3 NormalizeFast() {
		double inv = HMath.invSqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		this.x *= inv;
		this.y *= inv;
		this.z *= inv;

		return this;
	}
}
