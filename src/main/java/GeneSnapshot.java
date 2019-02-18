import Lama.BaseGeneSnapshot;
import Lama.Score;
import Lama.V3;

import java.util.Arrays;

public class GeneSnapshot extends BaseGeneSnapshot {
	public final double score_mult;
	public final int act_num;
	public final int actions_count;
	public final boolean me_touched_ball_ever;
	public final boolean picked_nitro;
	public final int used_nitro;
	public final int ticks_since_ball_touch;
	public final V3 ball_first_touch_pos;
	public final double jumps;
	public final int accurates_total;
	public final int add_sim_ticks;
	public final int goal;
	public final Score debug_score;
	public final double total_score;
	public final Act last_act;
	public final Dude dude;
	public Dude[] dudes;
	public TheBall ball;

	public GeneSnapshot(Dude[] dudes_, Dude dude_, TheBall ball_, int goal, double total_score_, double score_mult, int act_num, int actions_count,
						boolean me_touched_ball_ever, int ticks_since_ball_touch, V3 ball_first_touch_pos, double jumps, int accurates_total,
						int add_sim_ticks, boolean picked_nitro, int used_nitro, Act last_act, Score debug_score) {

		this.dudes = new Dude[dudes_.length];
		for (int i = 0; i < dudes_.length; i++) {
			this.dudes[i] = dudes_[i].copyFull();
		}

		this.ball = ball_.copyFull();
		this.dude = dude_.copyFull();

		this.goal = goal;
		this.total_score = total_score_;
		this.score_mult = score_mult;
		this.act_num = act_num;
		this.actions_count = actions_count;
		this.me_touched_ball_ever = me_touched_ball_ever;
		this.ticks_since_ball_touch = ticks_since_ball_touch;
		this.ball_first_touch_pos = ball_first_touch_pos.copy();
		this.jumps = jumps;
		this.accurates_total = accurates_total;
		this.add_sim_ticks = add_sim_ticks;
		this.picked_nitro = picked_nitro;
		this.used_nitro = used_nitro;
		this.last_act = last_act.copy();

		this.debug_score = debug_score != null ? debug_score.copy() : null;
	}

	@Override
	public GeneSnapshot copy() {
		GeneSnapshot result = new GeneSnapshot(dudes, dude, ball, goal, total_score, score_mult, act_num, actions_count, me_touched_ball_ever,
											   ticks_since_ball_touch, ball_first_touch_pos, jumps, accurates_total, add_sim_ticks, picked_nitro, used_nitro, last_act, debug_score
		);
		return result;
	}

	@Override
	public String toString() {
		return "GeneSnapshot{" +
			   "total_score=" + total_score +
			   ", score_mult=" + score_mult +
			   ", act_num=" + act_num +
			   ", goal=" + goal +
			   ", dude=" + dude +
			   ", ball=" + ball +
			   ", actions_count=" + actions_count +
			   ", picked_nitro=" + picked_nitro +
			   ", used_nitro=" + used_nitro +
			   ", me_touched_ball_ever=" + me_touched_ball_ever +
			   ", ticks_since_ball_touch=" + ticks_since_ball_touch +
			   ", ball_first_touch_pos=" + ball_first_touch_pos +
			   ", jumps=" + jumps +
			   ", accurates_total=" + accurates_total +
			   ", add_sim_ticks=" + add_sim_ticks +
			   ", debug_score=" + debug_score +
			   ", last_act=" + last_act +
			   ", dudes=" + Arrays.toString(dudes) +
			   '}';
	}
}
