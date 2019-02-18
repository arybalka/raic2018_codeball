import Lama.Renderer.DebugFormGL;
import Lama.Renderer.DebugSnapshotBase;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class DebugSnapshot extends DebugSnapshotBase {
	public int tick;
	boolean rendered = false;
	Dude[] my_team;
	Dude[] enemy_team;
	Nitro[] nitros;
	TheBall ball;
	Color ball_override_clr;
	ArrayList<String> memo1;
	ArrayList<String> memo2;

	public DebugSnapshot(DebugForm form, int tick_) {
		super(tick_);

		tick = tick_;
		my_team = new Dude[Glob.my_team.length];
		enemy_team = new Dude[Glob.enemy_team.length];
		nitros = new Nitro[CFG.HAS_NITRO ? 4 : 0];

		for (int i = 0; i < nitros.length; i++) {
			nitros[i] = Glob.nitro[i].copy();
		}

		for (int i = 0; i < Glob.my_team.length; i++) {
			my_team[i] = Glob.my_team[i].copy();
			my_team[i].action = Glob.my_team[i].action.copy();
		}

		for (int i = 0; i < Glob.enemy_team.length; i++) {
			enemy_team[i] = Glob.enemy_team[i].copy();
		}

		ball = Glob.ball.copy();
		ball_override_clr = Debug.ball_clr_override;

		try {
			for (DebugFormGL.DebugElem e : form.debug_elements_to_draw) {
				elements.add(e.copy());
			}

			memo1 = new ArrayList<>(form.memo1);
			memo2 = new ArrayList<>(form.memo2);

			if (Debug.on) Debug.echo("ES " + elements.size());
//			elements.addAll(form.debug_elements_to_draw);
		} catch (ConcurrentModificationException ignored) {

		}

	}
}