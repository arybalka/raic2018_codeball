import Lama.Genetics;
import Lama.Renderer.DebugBase;
import Lama.Renderer.RendererConfig;
import Lama.V3;
import model.Arena;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Debug extends DebugBase {

	public static boolean output_performance = true;

	public static Color ball_clr_override = null;

	static {
//		on = false;
	}

	public static void initForm(GLIsoCam camera, Arena arena) {
		if (initiated) return;
		initiated = true;
		if (!on) return;

		if (debug_form) {
			RendererConfig cfg = new RendererConfig();

			cfg.wnd_title = "CodeBall 2018, Lama's bot";
			cfg.zoom = 1.0;
			cfg.wnd_width = 1920;
			cfg.wnd_height = 1080;
//			cfg.wnd_x = 10;
//			cfg.wnd_y = 10;
//			cfg.wnd_width = 2000;
//			cfg.wnd_height = 1200;
			cfg.x_offset = -512;
			cfg.y_offset = -384;
			cfg.on_second_screen = false;
			cfg.on_right_side = false;
			cfg.random_seed = 42;
			cfg.on_right_side = true;
			cfg.bg_color = new Color(31, 31, 31);

			form = new DebugForm(cfg);
			((DebugForm) form).initialize(arena);
			form.setVisible(true);
			form.bindCamera(camera);
		}
	}

	public static boolean shouldEcho() {
		return Glob.tick > min_log_turn && Glob.tick < max_log_turn;
	}

	public static void frame() {
		frame(Glob.tick);
	}

	public static void drawGenotype(Genetics.Genotype<Act> genotype) {
		if (!on) return;
		int clr = 0;
		int act_num = 0;
		V3 dude_last_pos = null;
		V3 ball_last_pos = null;

		if (Scores.DEBUG && Debug.on) beginGroup(genotype.debug_score.getDebugInfo("\n", 0), genotype.group_id);

		for (int gene_num = 0; gene_num < genotype.genes.size(); gene_num++) {
			DecisionMaker.CBGene gene = (DecisionMaker.CBGene) genotype.genes.get(gene_num);

			if (gene.sequence == null) break;

			int g_act_num = 0;
			int l = gene.sequence.length;

			for (Act a : gene.sequence) {
				Dude d = a.dude_applied;
				V3 ball = a.ball_applied;
				if (d == null || ball == null) break;

				if (genotype.id == 3403 && Debug.on) Debug.echo("DP " + a.dude_applied.pos() + ", an: " + act_num);

				if (act_num % 5 == 0 || g_act_num == l - 1) {

					if (dude_last_pos != null && ball_last_pos != null) {

						float clr_added = (float) Math.min(0.5 + 0.5 * (d.y - 1.0) / (Glob.arena.height - 2.0), 1.0);
						Color c = Color.getHSBColor(clr_added, 1.0f, 1.0f);
						line(dude_last_pos, d.pos(), (a.accurate ? 5 : 1), new Color(c.getRed(), c.getGreen(), c.getBlue(), 100), 1);

						int clr_added_b = 255 - Math.min(clr + (int) (ball.y * 10.0), 255);
						line(ball_last_pos, ball.pos(), (a.accurate ? 5 : 2), new Color(clr_added_b, clr_added_b, 0), 1);
					}

				}


				dude_last_pos = d.pos();
				ball_last_pos = ball.pos();

				g_act_num++;
				act_num++;
			}
		}

		if (Scores.DEBUG && Debug.on) endGroup();
	}

	public static DebugForm getForm() {
		return (DebugForm) form;
	}

	public static boolean getCheckboxEfficiencyState() {
		return ((DebugForm) form).checkbox_efficiency.state;
	}

	public static boolean getCheckboxShowTrajectoriesState() {
		return ((DebugForm) form).checkbox_show_trajectories.state;
	}

	public static void clearMemo1() {
		if (!on) return;
		((DebugForm) form).memo1 = new ArrayList<>();
	}

	public static void setMemo1(String str) {
		if (!on) return;

		((DebugForm) form).memo1 = new ArrayList<>(Arrays.asList(str.split("\n")));
	}

	public static void setMemo2(String str) {
		if (!on) return;

		((DebugForm) form).memo2 = new ArrayList<>(Arrays.asList(str.split("\n")));
	}

	public static void appendMemo1(String str) {
		if (!on) return;

		if (((DebugForm) form).memo1.size() > 0) ((DebugForm) form).memo1.add("----------\n");
		((DebugForm) form).memo1.addAll(new ArrayList<>(Arrays.asList(str.split("\n"))));
	}
}
