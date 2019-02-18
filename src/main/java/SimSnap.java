import java.util.ArrayList;

public class SimSnap {
	Dude[] dudes;
	TheBall ball;

	public SimSnap(Dude[] dudes_, TheBall ball_) {
		dudes = new Dude[dudes_.length];
		int i = 0;
		for (Dude d : dudes_) {
			dudes[i++] = d.copy();
		}
		ball = ball_.copy();
	}
}
