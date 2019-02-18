package Lama;

/**
 * Created by lamik on 14.11.2015.
 */
public class PointI {
	public int x;
	public int y;

	public static PointI fromPoint(Point p) {
		return new PointI((int)Math.round(p.x), (int)Math.round(p.y));
	}

	public PointI(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PointI(Point p) {
		this.x = (int) p.x;
		this.y = (int) p.y;
	}

	public PointI(PointI p) {
		this.x = p.x;
		this.y = p.y;
	}

	public PointI copy() {
		return new PointI(this);
	}

	public double manhattanDistance(PointI p) {
		return Math.abs(p.x - this.x) + Math.abs(p.y - this.y);
	}

	public Point toPoint() {
		return new Point(this.x,
						 this.y);
	}

	public PointI add(PointI p) {
		return new PointI(this.x + p.x, this.y + p.y);
	}

	public PointI add(int x, int y) {
		return new PointI(this.x + x, this.y + y);
	}

	public PointI add(int s) {
		return new PointI(this.x + s, this.y + s);
	}

	public PointI sub(PointI p) {
		return new PointI(this.x - p.x, this.y - p.y);
	}

	public PointI sub(int x, int y) {
		return new PointI(this.x - x, this.y - y);
	}

	public PointI sub(int s) {
		return new PointI(this.x - s, this.y - s);
	}

	public int distance2(PointI p) {
		return (p.x - this.x)*(p.x - this.x) + (p.y - this.y)*(p.y - this.y);
	}

	public double distanceTo(PointI p) {
		return Math.sqrt((p.x - this.x)*(p.x - this.x) + (p.y - this.y)*(p.y - this.y));
	}

	public double distanceToI100(PointI p) { return HMath.distanceBetweenPointsI100(this, p); }

//	public Point toCellCenter() {
//		return new Point(this.x,
//						 this.y * 800 + 800/2.0d);
//	}

	@Override
	public int hashCode() {
		int result = (int) (x * 1000);
		result = 31 * result + (int) (y * 1000);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PointI && ((PointI) o).x == this.x && ((PointI) o).y == this.y;
	}

	@Override
	public String toString() {
		return "[" + this.x + ", " + this.y + "]";
	}

	public PointI scale(int x, int y) {
		return new PointI(this.x * x, this.y * y);
	}

	public PointI scale(int d) {
		return new PointI(this.x * d, this.y * d);
	}

	public PointI clamp(PointI min, PointI max) {
		return clamp(min.x, min.y, max.x, max.y);
	}
	public PointI clamp(int xmin, int ymin, int xmax, int ymax) {
		return new PointI(HMath.clamp(this.x, xmin, xmax), HMath.clamp(this.y, ymin, ymax));
	}

	public V3 toV3() {
		return new V3(x, y, 0);
	}
}
