package Lama;

public class Sphere extends V3 {
	V3 pos;
	public double radius = 0;

	public Sphere(double x, double y, double z, double radius) {
		super(x, y, z);
		this.radius = radius;
	}

	public Sphere(V3 p, double radius) {
		super(p);
		this.radius = radius;
	}

	public boolean collidesWithCircle(Sphere c) {
		return (c.x - this.x)*(c.x - this.x) + (c.y - this.y)*(c.y - this.y) <= (c.radius = this.radius) * (c.radius = this.radius);
	}

/*
	public double distanceToLineSegment(Point p1, Point p2) {
		return distanceToLineSegment(p1, p2) - radius;
	}
*/
}