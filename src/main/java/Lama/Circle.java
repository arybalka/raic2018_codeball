package Lama;

public class Circle extends V3 {
	public double radius = 0;

	public Circle(double x, double y, double z, double radius) {
		super(x, y, z);
		this.radius = radius;
	}
	public Circle(double x, double y, double radius) {
		super(x, y, 0);
		this.radius = radius;
	}

	public Circle(Point p, double radius) {
		super(p.x, p.y, 0);
		this.radius = radius;
	}

	public Circle(V3 v, double radius) {
		super(v);
		this.radius = radius;
	}

	public boolean collidesWithCircle(Circle c) {
		return (c.x - this.x)*(c.x - this.x) + (c.y - this.y)*(c.y - this.y) <= (c.radius = this.radius) * (c.radius = this.radius);
	}

/*
	public double distanceToLineSegment(Point p1, Point p2) {
		return distanceToLineSegment(p1, p2) - radius;
	}
*/
}