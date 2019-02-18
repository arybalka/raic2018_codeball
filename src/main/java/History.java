import Lama.Utils;

import java.util.HashMap;

public class History {
	public static HashMap<Integer, Dude[]> dudes = new HashMap<>();
	public static HashMap<Integer, TheBall> ball = new HashMap<>();

	public static void save(int tick) {
		if (!Debug.on) return;

		dudes.put(tick, Utils.copyArray(Glob.dudes));
		ball.put(tick, Glob.ball.copy());
	}

	public Dude[] retrieveDudes(int tick) {
		return dudes.getOrDefault(tick, null);
	}

	public TheBall retrieveBall(int tick) {
		return ball.getOrDefault(tick, null);
	}
}
