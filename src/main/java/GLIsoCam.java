import Lama.Renderer.GLIsoCamBase;
import Lama.V3;

public class GLIsoCam extends GLIsoCamBase {
	public GLIsoCam(V3 view, double dist, double angle_h, double angle_v) {
		super(view, dist, angle_h, angle_v);
	}

	@Override
	public void moveHV(double angle_h, double angle_v) {
		super.moveHV(angle_h, angle_v);

//		if (Debug.on) Debug.echo("moveHV | " + angle_h + ", " + angle_v + " (" + angle_horizontal + ", " + angle_vertical + "), pos: " + pos + ", view: " + view);
	}
}
