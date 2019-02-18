package Lama.Renderer;

import Lama.HMath;
import Lama.V3;
import net.jafama.FastMath;

public abstract class GLIsoCamBase extends GLCamBase {
	public double angle_vertical;
	public double angle_horizontal;
	public double distance;

	public GLIsoCamBase(V3 view, double dist, double angle_h, double angle_v) {
		super(view.add(0, 0, dist), view);
		this.angle_vertical = angle_v;
		this.angle_horizontal = angle_h;
		this.distance = dist;
		moveV(0);
	}

	public void moveH(double angle) {
		moveHV(angle, 0);
	}

	public void moveV(double angle) {
		moveHV(0, angle);
	}

	public void moveHV(double angle_h, double angle_v) {
		angle_vertical = HMath.normalizeAngle(2.0 * Math.PI + angle_vertical - angle_v * 0.003);
		angle_horizontal = HMath.normalizeAngle(2.0 * Math.PI + angle_horizontal - angle_h * 0.003);

		if (angle_vertical < 0.001) angle_vertical = 0.001;
		if (angle_vertical > HMath.HALF_PI) angle_vertical = HMath.HALF_PI;

		double sh = FastMath.sin(angle_horizontal);
		double ch = FastMath.cos(angle_horizontal);
		double sv = FastMath.sin(angle_vertical);
		double cv = FastMath.cos(angle_vertical);

		pos.x = view.x + distance * ch * sv;
		pos.y = view.y + distance * cv;
		pos.z = view.z + distance * sh * sv;
	}

	public void zoom(double d) {
		distance += d;
		if (distance < 1.0) distance = 1.0;
		moveHV(0, 0);
	}

	public void move(V3 offset) {
		view.Add(offset.rotateAroundXZ(V3.zero(), angle_horizontal));
		moveHV(0, 0);
	}
}
