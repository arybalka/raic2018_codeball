import Lama.*;
import Lama.Point;
import Lama.Renderer.DebugBase;
import Lama.Renderer.DebugFormGL;

import java.awt.*;

public class Scores {
	public static final boolean DEBUG = Debug.on;
	private static final boolean PERF_SCORES = false && Debug.on;
	public static double min_score = Double.MAX_VALUE;
	public static double max_score = Double.MIN_VALUE;
	public static boolean any_touched_ball = false;
	public static boolean force_disable_nitro = false;
	public static boolean nitro_in_air_can_touch_ball = false;
	private static Point corner = new Point(CFG.HALF_WIDTH, CFG.HALF_DEPTH);
	private static V3 temp_v = V3.zero();
	private static V3 temp_v2 = V3.zero();
	private static V3 ball_target = V3.zero();
	private static V3 t = V3.zero();
	private static V3 ball_first_touch_pos = V3.zero();

	public static double getScore(
		Score<ScoreType> score,
		Dude dude,
		TheBall ball,
		double goal,
		boolean is_attacker,
		boolean is_closest,
		double jumps,
		double enemy_dist_score,
		double enemy_danger_zone,
		double my_dist_score,
		double me_enemy_distance,
		boolean ball_only,
		boolean me_touched_ball,
		V3 ball_first_touch_pos,
		boolean me_touched_ball_ever,
		boolean enemy_touched_ball,
		boolean can_semi_goal,
		double score_mult,
		double my_dudes_dist_score,
		double touching_enemy) {

		if (PERF_SCORES) Perf.start("eval.scores");


		boolean ball_is_target = true;

		if (me_touched_ball) {
			t.set(ball);
			ball_is_target = true;
		} else {
			t.set(Glob.dudes_targets[dude.id - 1] != null ? Glob.dudes_targets[dude.id - 1] : ball);
			ball_is_target = false;
		}

		if (ball_only && !ball_is_target) {
			ball_target.set(t.x, 1.0, t.z);
		} else {
			Point targ_p = t.pointFromXZ();
			Point targ_v = new Point(Glob.enemy_gate_center.x - t.x, Glob.enemy_gate_center.z - t.z);
			double offset = 5.0;
			double offset_close = 1.0; // mine: offset 1.5

			targ_v.Invert().NormalizeFast();
			targ_p.Add(targ_v.x * offset_close, targ_v.y * offset_close);
			Point p1 = targ_p.add(targ_v.x * offset, targ_v.y * offset);
			p1.CalcPointBetweenPointsWithDistanceFast(targ_p, HMath.clamp(offset * 2.0 - dude.pointFromXZ().distanceTo(targ_p), 0, offset));

			ball_target.set(p1.x, 1.0, p1.y);
		}

		double dude_ball_target_dist = 0, dude_defence_dist = 0, dude_ball_z = 0, dude_x = 0, dude_z = 0, dude_vel_z = 0, dude_jumps = 0, dude_field_center = 0,
			x_ = 0, z_ = 0, dude_corner = 0, b_goal = 0, b_semi_goal = 0, b_ball_vel = 0, ball_x = 0, ball_gate_dist = 0, enemy_dist = 0, my_dist = 0,
			ball_y = 0, ball_danger_gate = 0, a = 0, b_angle = 0, o_me_touched_ball = 0, o_enemy_touched_ball = 0, o_enemy_danger_zone = 0, dude_y = 0,
			b_ball_pos = 0, dude_me_enemy_dist = 0, ball_traj_goal = 0, friends_dist = 0, dude_stay_middle = 0, dude_kick_enemy = 0;

		double ax = Math.abs(ball.x);

		if (!ball_only) {
			if (is_attacker) {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.enemy_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			} else {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.enemy_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			}
		} else {
			if (is_attacker) {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.enemy_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			} else {
				b_semi_goal = can_semi_goal && goal != 0 && (ball.y > CFG.HALF_GOAL_HEIGHT || goal < 0) ? goal * 1000000 * (ball.y + Math.abs(ball.vel.z) * 0.05) : 0;
			}
		}

//		double dude_ball_target_dist_mult = 500.0;
		double dude_ball_target_dist_mult = 250.0;

		if (is_attacker && (ball.distanceMore(Glob.my_gate_center, 20) || dude.distanceMore(Glob.my_gate_center, 20))) {
			dude_ball_target_dist = ball_only ? 0 : 20.0 * 110.0 * HMath.invSqrt(dude.distance2c(ball_target, 1.0)); // bonus for staying close to the ball
			dude_defence_dist = 0;

			// dude score
//			dude_ball_z = ball_only ? 0 : -20.0 * (ball.z > dude.z ? 0 : (dude.z - ball.z)); // penalty for dude being behind closer to enemy gate than the ball
			dude_x = ball_only ? 0 : -2.0 * Math.abs(dude.x); // bonus for staying close to middle line
//			dude_z = ball_only || ball.z < Glob.arena.depth * 0.25 ? 0 : -dude.z;
//			dude_vel_z = ball_only || dude.vel.z < 0 ? 0 : dude.vel.z;

			dude_jumps = ball_only ? 0 : -(me_touched_ball ? 50.0 : 150.0) * (jumps > 0 ? 1 : 0); // penalty for jumping

			// ball score
			b_ball_vel = 40.0 * ball.vel.z; // we want the ball to move towards enemy gate
//			b_ball_vel = -25.0 * (Math.abs(ball.vel.z) + Math.abs(ball.vel.x)); // we want the ball to move towards enemy gate
//			b_ball_pos = 40.0 * ball.z;
			// TODO: change to a vector
//			ball_x = -20.0 * ax; // score for ball being close to middle line
//			ball_gate_dist = 100.0 * 110.0 * HMath.invSqrt(ball.distanceTo(0, Glob.arena.height / 2.0, Glob.arena.depth + Glob.arena.goal_depth)); // bonus for ball being close to enemy gate
			enemy_dist = -10.0 * HMath.clamp(enemy_dist_score, 0.0, 40.0);
			ball_y = 20.0 * ball.y;

			ball_danger_gate = ball.z < -Glob.arena.depth * 0.33 && ax < Glob.arena.goal_width / 2.0 && ball.y < Glob.arena.goal_height * 0.7 ? -800.0 : 0;

			a = ball.vel.getCosOfAngleToV3RelativeToZero(Glob.enemy_gate_center.x - ball.x, Glob.enemy_gate_center.y - ball.y, Glob.enemy_gate_center.z - ball.z);
			b_angle = ball.vel.z <= 0 ? 0 : 1000.0 * a;

			// optional score
			o_me_touched_ball = (me_touched_ball ? 5000 * ball.y : 0);
			o_enemy_touched_ball = (enemy_touched_ball ? -20000 : 0);
			o_enemy_danger_zone = -(enemy_danger_zone * 800);

			dude_kick_enemy = touching_enemy * -500.0;

//			friends_dist = -(my_dudes_dist_score * 5.0);

//			dude_y = -100 * (dude.y - 1.0) * (dude.id != Agent.my_attacker.id && Agent.my_attacker.y > 2.0 ? 2.0 : 1.0);
		} else {

			ball_danger_gate = ax <= CFG.HALF_GOAL_WIDTH && ball.z <= -CFG.HALF_DEPTH * 0.7 && ball.y < Glob.arena.goal_height * 0.7
				? (ball.z <= -CFG.HALF_DEPTH ? 15.0 : 1.0) * -500
				: 0;

			dude_ball_target_dist = dude_ball_target_dist_mult * HMath.invSqrt(
				me_touched_ball_ever
					? ball_target.distance2c(ball_first_touch_pos.x, 1.0, ball_first_touch_pos.z, 1.0)
					: ball_target.distance2c(dude.x, 1.0, dude.z, 1.0)
			);

			enemy_dist = -3000.0 * enemy_dist_score;

			my_dist = 4000.0 * my_dist_score;

			b_ball_vel = ball.z * 50.0;

//			a = ball.vel.getCosOfAngleToV3RelativeToZero(Glob.enemy_gate_center.x - ball.x, Glob.enemy_gate_center.y - ball.y, Glob.enemy_gate_center.z - ball.z);
//			b_angle = ball.vel.z <= 0 ? 0 : 1000.0 * a;

//			b_ball_vel = (CFG.FINAL ? 20.0 : 10.0) * (
//				(ball.z < 0
//					? (
//						  bax <= 10.0
//							  ? Math.abs(ball.vel.x)
//							  : ball.x > 0 ? ball.vel.x : -ball.vel.x
//					  ) * (CFG.FINAL ? 0.0 : 1.5)
//					: 0)
//				+ ball.vel.z
//			);

			ball_y = (CFG.FINAL ? 100 : 100) * Math.min(ball.y - ball.radius, CFG.HALF_HEIGHT);
//			ball_x = Math.abs(Agent.my_attacker.x - ball.x) * -30.0;

			dude_y = ball_only ? 0 : -100 * (Math.min(dude.y - 1.0, CFG.HALF_HEIGHT)) * (me_enemy_distance < 20 && dude.z < -30 ? 3.0 : 1.0);
		}

		if (!(Scores.DEBUG && Debug.on)) {
			// results
			double attack_score = dude_ball_target_dist + dude_defence_dist;
			double dude_score = ball_only ? 0 : (dude_ball_z + dude_x + dude_z + dude_jumps + dude_field_center + dude_corner + dude_vel_z + dude_y + dude_me_enemy_dist + friends_dist + dude_stay_middle + dude_kick_enemy);
			double ball_score = b_goal + b_semi_goal + b_ball_vel + ball_x + ball_gate_dist + enemy_dist + my_dist + ball_y + ball_danger_gate + b_angle + b_ball_pos + ball_traj_goal;
			double optional_score = o_me_touched_ball + o_enemy_touched_ball + o_enemy_danger_zone;

			if (PERF_SCORES) Perf.stop("eval.scores");

			return (dude_score + ball_score + optional_score + attack_score) * score_mult;
		} else {
			score.begin(score_mult);

			score.bonus(ScoreType.B_D_BALL_TARGET_DIST, dude_ball_target_dist);
			score.bonus(ScoreType.B_D_DEFENCE_POINT_DIST, dude_defence_dist);

			score.bonus(ScoreType.D_D_BALL_Z, dude_ball_z);
			score.bonus(ScoreType.D_X, dude_x);
			score.bonus(ScoreType.D_Y, dude_y);
			score.bonus(ScoreType.D_Z, dude_z);
			score.bonus(ScoreType.D_VEL_Z, dude_vel_z);
			score.bonus(ScoreType.D_JUMPS, dude_jumps);
			score.bonus(ScoreType.D_FIELD_CENTER, dude_field_center);
			score.bonus(ScoreType.D_CORNER, dude_corner);
			score.bonus(ScoreType.D_STAY_MIDDLE, dude_stay_middle);
			score.bonus(ScoreType.D_ENEMY_DIST, dude_me_enemy_dist);
			score.bonus(ScoreType.D_TOO_CLOSE_PENALTY, friends_dist);
			score.bonus(ScoreType.D_KICK_ENEMY, dude_kick_enemy);

			score.bonus(ScoreType.B_GOAL, b_goal);
			score.bonus(ScoreType.B_TRAJ_GOAL, ball_traj_goal);
			score.bonus(ScoreType.B_SEMI_GOAL, b_semi_goal);
			score.bonus(ScoreType.B_VEL, b_ball_vel);
			score.bonus(ScoreType.B_POS, b_ball_pos);
			score.bonus(ScoreType.B_X, ball_x);
			score.bonus(ScoreType.B_Y, ball_y);
			score.bonus(ScoreType.B_B_GATE_DISTANCE, ball_gate_dist);
			score.bonus(ScoreType.B_ENEMY_DIST, enemy_dist);
			score.bonus(ScoreType.B_MY_DIST, my_dist);
			score.bonus(ScoreType.B_DANGER_GATE, ball_danger_gate);
			score.bonus(ScoreType.B_ANGLE, b_angle);

			score.bonus(ScoreType.O_ME_TOUCHED_BALL, o_me_touched_ball);
			score.bonus(ScoreType.O_ENEMY_TOUCHED_BALL, o_enemy_touched_ball);
			score.bonus(ScoreType.O_ENEMY_DANGER_ZONE, o_enemy_danger_zone);

			if (PERF_SCORES) Perf.stop("eval.scores");

			return score.end();
		}
	}

	private static double addBonusOnce(Score<ScoreType> score, ScoreType type, double value) {
		if (!(Scores.DEBUG && Debug.on)) {

			return (value) * 0.01;

		} else {
			score.begin(0.01);

			score.bonus(type, value);

			return score.end();
		}
	}

	public static Score<ScoreType> newScore(Genetics.Genotype genotype) {
		return new Score<>(genotype.id);
	}

	public static double evaluate(int actions_count, Dude dude, Genetics.Genotype<Act> genotype, int generation, boolean is_attacker, boolean last_generation, Dude[] dudes_to_sim, Dude[] enemies_to_skip, boolean is_closest_attacker_to_ball) {
		Perf.start("eval");
		if (PERF_SCORES) Perf.start("eval.init");
		DecisionMaker.debug_evaluated_times++;

//		if (Debug.on) Debug.echo("\n--------------------- genotype " +genotype.id);

		if (Scores.DEBUG && Debug.on) genotype.debug_score = newScore(genotype);
//		DecisionMaker.calcActionSequenceFromGenotype(genotype, actions_count);
		TheBall ball = null;

		int goal = 0;
		double jumps = 0;
		double danger_rad = (1.0 + CFG.BALL_RADIUS) * 2.0;

		Act last_act = null;

		Dude[] simulated_dudes = new Dude[dudes_to_sim.length];

		double d_nitro = dude.nitro;
		int act_num = 0;
		double total_score = 0;
		double score_mult = 0.01;
		double score_decay = 0.98;
		boolean me_touched_ball_ever = false;
		boolean enemy_touched_ball_ever = false;
		int accurates_total = 0;
		int ticks_since_ball_touch = 0;
		int add_sim_ticks = 0;
		Solver.SimResult gs = new Solver.SimResult();
		boolean me_close_to_ball = dude.y < 1.33 || dude.distanceLess(Glob.ball, 5.0);
		boolean picked_nitro = false;
		int used_nitro = 0;

		double me_enemy_dist = HMath.invSqrt(dude.distance2c(Glob.enemy_attacker, 1.0));

		int first_gene = 0;

		for (int i = genotype.genes.size() - 1; i >= 0; i--) {
			DecisionMaker.CBGene gene = (DecisionMaker.CBGene) genotype.genes.get(i);

			if (gene.snapshot != null) {
				first_gene = i + 1;

				GeneSnapshot snap = (GeneSnapshot) gene.snapshot;

				total_score = snap.total_score;
				score_mult = snap.score_mult;
				act_num = snap.act_num;
				actions_count = snap.actions_count;
				me_touched_ball_ever = snap.me_touched_ball_ever;
				ticks_since_ball_touch = snap.ticks_since_ball_touch;
				ball_first_touch_pos = snap.ball_first_touch_pos.copy();
				jumps = snap.jumps;
				accurates_total = snap.accurates_total;
				add_sim_ticks = snap.add_sim_ticks;
				last_act = snap.last_act;
				picked_nitro = snap.picked_nitro;
				used_nitro = snap.used_nitro;

				ball = snap.ball.copyFull();
//				dude.updateFromDude(snap.dude);
				dude = snap.dude.copyFull();

				for (int j = 0; j < snap.dudes.length; j++) {
					if (snap.dudes[j].id == dude.id) {
						simulated_dudes[j] = dude;
					} else {
						simulated_dudes[j] = snap.dudes[j].copyFull();
					}
//					simulated_dudes[j] = snap.dudes[j].copyFull();
				}

				if (Scores.DEBUG && Debug.on) {
					int id = genotype.debug_score.id;
					genotype.debug_score = snap.debug_score.copy();
					genotype.debug_score.id = id;
				}

				break;
			}
		}

		if (first_gene == 0) {
			for (int i = 0; i < dudes_to_sim.length; i++) {
				if (dudes_to_sim[i].id == dude.id) {
					simulated_dudes[i] = dude;
				} else {
					simulated_dudes[i] = dudes_to_sim[i].copy();
				}
			}

			ball = Glob.ball.copy();
			ball_first_touch_pos.set(0, 0, 0);
		} else {
			if (Debug.on && ball == null) Debug.echo("!! NO BALL");
		}

		if (PERF_SCORES) Perf.change("eval.loop");


		for (int i = first_gene; i < genotype.genes.size(); i++) {
			if (actions_count <= 0) {

				for (int j = genotype.genes.size() - 1; j >= i; j--) {
					genotype.genes.remove(j);
				}

//				if (Debug.on) Debug.echo("Too many actions in genotype " + genotype.id);
				break;
			}

			DecisionMaker.CBGene gene = (DecisionMaker.CBGene) genotype.genes.get(i);

			actions_count = DecisionMaker.calcActionSequenceFromGene(gene, actions_count, i == genotype.genes.size() - 1);

			for (Act act : gene.sequence) {
				act_num++;
				boolean accurate = false;

				boolean can_be_accurate = !me_touched_ball_ever || ticks_since_ball_touch <= 2;

				for (Dude d : simulated_dudes) {
					if (d.id == dude.id) {
						if (me_touched_ball_ever) {
							act.use_nitro = false;
						} else {
							if (d.nitro > 0 && d.y > 1.1 && !d.touch && d.vel.y > 0 && dude.last_touched_ball < Glob.tick - 20 && nitro_in_air_can_touch_ball) {
								if (genotype.nitro_gene.value > (is_attacker ? 0.5 : 0.5)) {
									act.use_nitro = true;

//								V3 po = ball.dirTo(Glob.enemy_gate_center.dirTo(ball).NormalizeFast().Scale(0.5));
//								V3 po = ball.add(Glob.enemy_gate_center.dirTo(ball).NormalizeFast().Scale(0.66));
//								act.vel.set(dude.dirTo(po).NormalizeFast().Scale(100.0));
//								if (d.distanceMore(ball, 10)) {
//								} else {
//									if (genotype.nitro_gene.value > 0.75) {
////										act.vel.set(0, 100, 0);
//										V3 po = ball.add(Glob.enemy_gate_center.dirTo(ball).NormalizeFast().Scale(0.66));
//										act.vel.set(dude.dirTo(po).NormalizeFast().Scale(100.0));
//									} else {
									act.vel.set(dude.dirTo(ball).NormalizeFast().Scale(100.0));
//									}
//								}


									used_nitro++;
//								act.vel.y = 30.0;
								}
							}
						}

						d.Act(act);

						if (d.touch && act.jump_speed > 0 && (last_act == null || !last_act.equals(act))) {
							jumps++;

							// TODO: bug here, but gives performance boost. decide what to do.
							if (can_be_accurate && accurates_total < DecisionMaker.ACCURATES_LIMIT && (ball.distanceLess(d, 20.0) || Glob.trajectory[20].distanceLess(d.x, Glob.trajectory[20].y, d.z, 20.0))) {
								accurate = true;
							}
						}
					} else {
						if (act_num - 1 < DecisionMaker.ACTIONS_PER_GENE
							&& Glob.dudes_movement[d.id - 1] != null
							&& Glob.dudes_movement[d.id - 1][act_num - 1] != null
							&& Glob.dudes_movement[d.id - 1][act_num - 1].action != null)
						{
							d.Act(Glob.dudes_movement[d.id - 1][act_num - 1].action.copy());
						} else {
							d.Act(new Act(d.dirTo(ball).NormalizeFast().Scale(100.0)));
						}
					}

					if (can_be_accurate && !accurate && me_close_to_ball && accurates_total < DecisionMaker.ACCURATES_LIMIT && !d.touch && d.distanceLess(ball, (d.radius + ball.radius) * 1.33)) {
						accurate = true;
					}
				}

				act.accurate = accurate;
				accurates_total += accurate ? 1 : 0;
				last_act = act;
				ball.updateLastPos();

				int goal_scored = 0;
				boolean enemy_touched_ball = false;
				boolean me_touched_ball = false;

//			double ticks = accurate ? 1.0 / 100.0 : CFG.MICROTICKS_PER_TICK;
//			double ticks = accurate ? 100 : CFG.MICROTICKS_PER_TICK;
				double ticks = accurate ? CFG.ACCURATE_MICROTICKS : CFG.MICROTICKS_PER_TICK;

				for (int t = 0; t < ticks; t++) {

					// ------------
					// Update world
					// ------------
					gs.reset();
					Solver.update(gs, dude, simulated_dudes, ball, accurate, true, act_num - 1, ticks);

					if (gs.touching_ball_enemy > 0) {
						enemy_touched_ball = true;
						enemy_touched_ball_ever = true;
					}
					if (gs.touching_ball_me) {
						me_touched_ball = true;
						me_touched_ball_ever = true;
						ticks_since_ball_touch = 0;
						ball_first_touch_pos.set(dude);
					}

					if (enemy_touched_ball_ever && gs.goal_scored > 0) {
						gs.goal_scored = 0;
					}

					if (goal_scored == 0 && gs.goal_scored != 0) {
						goal_scored = gs.goal_scored;
						break;
					}
				}

//				if (genotype.id == 3403 && Debug.on) Debug.echo("DP " + dude.pos() + ", an: " + act_num + ", i: " + i);

				if (me_touched_ball_ever) {
					ticks_since_ball_touch++;
					if (ticks_since_ball_touch > 5) {

//						if (Debug.on) Debug.echo("Cut genotype from " + genotype.genes.size() + " to " + (i + 1));

						for (int j = genotype.genes.size() - 1; j > i; j--) {
							add_sim_ticks += genotype.genes.get(j).repeats_gene.value;
							genotype.genes.remove(j);
						}
					}
				}

				double enemy_speed = 0;

				if (dude != null) {
					act.dude_applied = dude.copy();
					act.ball_applied = ball.pos();
				} else {
					if (Debug.on) Debug.echo("!! my_dude is empty !!");
				}

				if (goal_scored != 0) {
					goal = goal_scored;
				}

				double enemy_dist_score = 0;
				for (Dude e : Glob.enemy_team) {
					enemy_dist_score += HMath.invSqrt(ball.distance2c(e, 1.0));
				}

				double my_dist_score = 0, my_dudes_dist_score = 0;
				boolean is_closest = true;
				double me_ball2 = dude.distance2(ball);

				for (Dude f : Glob.my_team) {
					if (f.id == dude.id) continue;

					double mdds = CFG.FINAL && CFG.KEEP_DISTANCE_FROM_FRIENDS && Glob.my_semi_attacker != null && dude.id == Glob.my_semi_attacker.id && f.id == Glob.my_attacker.id
						? dude.distance2(Glob.my_attacker) : 0;

					my_dudes_dist_score += mdds;

					if (f.z < 0) continue;
					double d2c = ball.distance2c(f.x, ball.y, f.z, 1.0);
					my_dist_score += HMath.invSqrt(d2c);
					if (d2c < me_ball2) is_closest = false;
				}

				double enemy_danger_zone = 0;

				if (is_attacker && me_touched_ball_ever && ball.y < 5.0 + ball.radius + 1.0 && act_num <= Glob.TRAJECTORY_TICKS) {
					for (int eid = 0; eid < Glob.enemies_to_me_traj.length; eid++) {
						if (ball.distanceLess(Glob.enemies_to_me_traj[eid][act_num - 1], danger_rad)) {
							enemy_danger_zone += 1.0;
						}
					}
				}

				if (CFG.HAS_NITRO && !picked_nitro && dude.touch && dude.nitro < CFG.MAX_NITRO_AMOUNT) {
					double dx = Math.abs(dude.x);
					double dz = Math.abs(dude.z);
					dx = dx - 20;
					dz = dz - 30;

					if (dx * dx + dz * dz <= 1.5 * 1.5) {
						for (Nitro n : Glob.nitro) {
							if ((n.x > 0 == dude.x > 0) && (n.z > 0 == dude.z > 0)) {
								if (n.respawn == null || n.respawn == 0) picked_nitro = true;
								break;
							}
						}
					}
				}

				// -------------------------
				// Get Score
				// -------------------------

				double score = getScore(genotype.debug_score, dude, ball, goal, is_attacker, is_closest_attacker_to_ball, jumps, enemy_dist_score, enemy_danger_zone, my_dist_score,
										me_enemy_dist, false, me_touched_ball, ball_first_touch_pos, me_touched_ball_ever, enemy_touched_ball, true, score_mult,
//										my_dudes_dist_score, gs.touching_enemy);
										my_dudes_dist_score, enemy_speed);

				total_score += score;
				score_mult *= score_decay;

				if (!is_attacker && ball.z > 0 && ball.y < CFG.JUMP_HEIGHT_APPROX && genotype.ball_defender_kick_to == null) {
					genotype.ball_defender_kick_to = ball.pos();
				}

				if (goal != 0) {
					if (Debug.on) Debug.circle3(ball, ball.radius * 0.1, new Color(0, 161, 25, 150), false);
					break;
				}
			}

			if (goal != 0) {
				break;
			}

			if (PERF_SCORES) Perf.start("eval.take_snap");

			gene.snapshot = new GeneSnapshot(simulated_dudes, dude, ball, goal, total_score, score_mult, act_num, actions_count, me_touched_ball_ever, ticks_since_ball_touch,
											 ball_first_touch_pos, jumps, accurates_total, add_sim_ticks, picked_nitro, used_nitro, last_act, genotype.debug_score);

			if (PERF_SCORES) Perf.stop("eval.take_snap");
		}

		Dude[] emp = new Dude[0];
		int post_act_num = 0;

		if (picked_nitro) {
			total_score += addBonusOnce(genotype.debug_score, ScoreType.D_PICKED_NITRO, is_attacker ? (100 - d_nitro) * (dude.z < 0 && ball.z < 0 ? 50 : 100) : 0);
		}

		if (PERF_SCORES) Perf.stop("eval.loop");

		if (goal == 0) {
			if (PERF_SCORES) Perf.start("eval.ball");
			DebugFormGL.DebugGroup dg = null;

			if (Debug.on && (last_generation || Debug.getCheckboxShowTrajectoriesState())) {
				dg = DebugBase.beginGroup();
				genotype.group_id = dg.id;
			}

			ball.updateLastPos();
			int i = 0;
			int max_i = add_sim_ticks + (is_attacker ? DecisionMaker.SIMULATE_BALL_AFTER_SEQ_TICKS : DecisionMaker.SIMULATE_BALL_AFTER_SEQ_TICKS_DEFENDER);
			int max_i2 = 0;// Agent.trajectory_ends_with_goal < 0 ? Math.max(max_i, Agent.trajectory.length) : max_i;

			boolean can_semi_goal = true;

			while (i < max_i || !ball.touch || (i < max_i2 && !me_touched_ball_ever)) {
				post_act_num++;
				int ptick = act_num + i;

				for (int t = 0; t < CFG.MICROTICKS_PER_TICK; t++) {
//					goal = Solver.update(dude, emp, ball, false, true, ptick, CFG.MICROTICKS_PER_TICK).goal_scored;
					goal = Solver.update(null, emp, ball, false, true, ptick, CFG.MICROTICKS_PER_TICK).goal_scored;

					if (enemy_touched_ball_ever && gs.goal_scored > 0) {
						gs.goal_scored = 0;
					}

					if (goal != 0) {
						break;
					}
				}

				double enemy_dist_score = 0;

				for (Dude e : Glob.enemy_team) {
					enemy_dist_score += HMath.invSqrt(ball.distance2c(e, 1.0));
				}

				double my_dist_score = 0;
				for (Dude f : Glob.my_team) {
					if (f.id == dude.id) continue;
					if (f.z < 0) continue;

					double d2c = ball.distance2c(f.x, ball.y, f.z, 1.0);
					my_dist_score += HMath.invSqrt(d2c);
				}

				double enemy_danger_zone = 0;
				if (is_attacker && me_touched_ball_ever && ball.y < 5.0 + ball.radius + 1.0 && ptick < Glob.TRAJECTORY_TICKS) {
					for (int eid = 0; eid < Glob.enemies_to_me_traj.length; eid++) {
						V3 e = Glob.enemies_to_me_traj[eid][ptick];
						if (ball.distanceLess(e, (1.0 + ball.radius) * 2.0)) {
//							if (act_num % 5 == 0 && Debug.on) Debug.sphere(e, (e.radius + ball.radius) * 2.0, new Color(1,1,1, 10), 1);
							enemy_danger_zone += 1.0;
						}
					}
				}

				if (ball.y < CFG.BALL_RADIUS * 1.5 && ball.z > CFG.HALF_DEPTH * 0.6) can_semi_goal = false;

//				double score = getScore(dude, ball, goal * (SIMULATE_BALL_AFTER_SEQ_TICKS - i) * 0.75, jumps, enemy_dist_score, 0, true, false, false);
				double score = getScore(genotype.debug_score, dude, ball, goal, is_attacker, false, jumps, enemy_dist_score,
										enemy_danger_zone, my_dist_score, me_enemy_dist, true, false, ball_first_touch_pos, false,
										false, can_semi_goal, score_mult, 0, 0);
				total_score += score;
//				if (Debug.on) Debug.echo("sc2: " + score + ", m: " + score_mult);
				score_mult *= score_decay;

				if (Debug.on && i % 5 == 0) {
					boolean efficiency = Debug.getCheckboxEfficiencyState();

					if (last_generation || Debug.getCheckboxShowTrajectoriesState() || efficiency) {
						Color color;
						int clr = 0;
						int clr_added = 255 - Math.min(clr + (int) (ball.y * 7.0), 200);
						if (efficiency) {
							double ms = max_score - min_score;
//							if (Debug.on) Debug.echo("ms " + score + " -> " + ms + " -> " + (HMath.clamp(score - min_score, 0.0, ms) / ms));
//							color = Color.getHSBColor((float) (HMath.clamp(score - min_score, 0.0, ms) / ms), 1.0f, 1.0f);
							color = Color.getHSBColor((float) (HMath.clamp(score, 0.0, 5.0) / 5.0), 1.0f, 1.0f);
							color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
						} else {
							color = generation == -1 ? (is_closest_attacker_to_ball ? new Color(255, 0, 243, 150) : new Color(6, 255, 183, 150)) : new Color((int) (clr_added * 0.7), (int) (clr_added * 0.6), i >= max_i ? 150 : 0, 80);
							if (ball.touch) color = Color.black;
						}

						if (Debug.on) Debug.line(ball.last_pos, ball.pos(), goal != 0 ? 3 : 1, color, 1);
						ball.updateLastPos();
					}
				}

				if (!is_attacker && ball.z > 0 && ball.y < CFG.JUMP_HEIGHT_APPROX && genotype.ball_defender_kick_to == null) {
					genotype.ball_defender_kick_to = ball.pos();
				}

				if (goal != 0) {
					if (Debug.on) Debug.circle3(ball, ball.radius * 0.1, new Color(135, 0, 3, 150), false);
					break;
				}

				i++;
			}

			if (dg != null) DebugBase.endGroup();

			if (PERF_SCORES) Perf.stop("eval.ball");
		}

		if (jumps > 0 && !me_touched_ball_ever) {
			total_score += addBonusOnce(genotype.debug_score, ScoreType.D_JUMPS, -500000);
		}

		if (me_touched_ball_ever) any_touched_ball = true;

		Perf.stop("eval");

		if (Debug.on && (last_generation || Debug.getCheckboxShowTrajectoriesState())) {
			Debug.drawGenotype(genotype);
		}

		return total_score;
	}

	public static void reset() {
		any_touched_ball = false;
		force_disable_nitro = false;
		nitro_in_air_can_touch_ball = true;
	}

	public static double evaluateEnemy(int actions_num, Dude dude, Genetics.Genotype<Act> genotype, boolean is_attacker) {
		TheBall ball = Glob.ball.copy();
		double total_score = 0, mult = 1.0, decay = 0.98, goal = 0;

		for (int i = 0; i < genotype.genes.size(); i++) {
			if (actions_num <= 0) {

				for (int j = genotype.genes.size() - 1; j >= i; j--) {
					genotype.genes.remove(j);
				}
				break;
			}

			DecisionMaker.CBGene gene = (DecisionMaker.CBGene) genotype.genes.get(i);

			actions_num = DecisionMaker.calcActionSequenceFromGene(gene, actions_num, i == genotype.genes.size() - 1);

			for (Act act : gene.sequence) {
				dude.touch = dude.y < 1.06;

				if (dude.nitro > 0 && dude.y > 1.1 && dude.vel.y > 0) {
					if (genotype.nitro_gene.value > 0.5) {
						act.use_nitro = true;
						act.vel.set(dude.dirTo(ball).NormalizeFast().Scale(100.0));
					}
				}

				dude.flag = false;
				dude.SimWithBall(act, ball);

				act.dude_applied = dude.copyFull();
				act.ball_applied = ball.copy();

				if (Math.abs(ball.z) > CFG.HALF_DEPTH + ball.radius) {
					goal = ball.z < 0 ? 1 : -1;
				}

				total_score += getEnemyScore(dude, ball, mult, is_attacker, false, goal);
				mult *= decay;

				if (goal != 0) break;
			}

			if (goal != 0) break;
		}

		if (goal == 0) {
			for (int i = 0; i < 30; i++) {
				ball.Sim();
				if (Math.abs(ball.z) > CFG.HALF_DEPTH + ball.radius) {
					goal = ball.z < 0 ? 1 : -1;
				}

				total_score += getEnemyScore(dude, ball, mult, is_attacker, true, goal);
				mult *= decay;
				if (goal != 0) break;
			}
		}

		return total_score;
	}

	private static V3 enemy_gate_def_point = new V3(0, 1.0, CFG.HALF_DEPTH * 1.05);

	private static double getEnemyScore(Dude dude, TheBall ball, double mult, boolean is_attacker, boolean ball_only, double goal) {

		double b_goal = 0, b_semi_goal = 0, dude_ball_target_dist = 0, b_ball_vel = 0, ball_y = 0, b_angle = 0, dude_y = 0;

		if (!ball_only) {
			if (is_attacker) {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.my_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			} else {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.my_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			}
		} else {
			if (is_attacker) {
				b_goal = goal * 1000000 * (goal > 0 ? ball.distanceTo(Glob.my_defender) : 1.0) * (ball.y + Math.abs(ball.vel.z) * 0.05);
			} else {
				b_semi_goal = goal != 0 && (ball.y > CFG.HALF_GOAL_HEIGHT || goal < 0) ? goal * 1000000 * (ball.y + Math.abs(ball.vel.z) * 0.05) : 0;
			}
		}

		if (!ball_only) {
			if (is_attacker) {
				dude_ball_target_dist = ball_only ? 0 : 20.0 * 110.0 * HMath.invSqrt(dude.distance2c(ball, 1.0));
				b_ball_vel = 40.0 * -ball.vel.z;
				ball_y = 20.0 * ball.y;
				double a = ball.vel.getCosOfAngleToV3RelativeToZero(Glob.my_gate_center.x - ball.x, Glob.my_gate_center.y - ball.y, Glob.my_gate_center.z - ball.z);
				b_angle = ball.vel.z <= 0 ? 0 : 1000.0 * a;
				dude_y = -20 * (dude.y - 1.0);
			} else {
				V3 pod = Glob.players_to_ball_traj_len != null && Glob.players_to_ball_traj_len[dude.id - 1] != null && Glob.players_to_ball_traj_len[dude.id - 1] < 25 ? ball : enemy_gate_def_point;
				dude_ball_target_dist = 250 * HMath.invSqrt(pod.distance2c(dude.x, 1.0, dude.z, 1.0));
				b_ball_vel = 40.0 * -ball.vel.z;
				ball_y = 20.0 * ball.y;
				dude_y = -20 * (dude.y - 1.0);
			}
		}

		return (b_goal + b_semi_goal + dude_ball_target_dist + b_ball_vel + ball_y + b_angle + dude_y) * mult;
	}

	enum ScoreType {
		B_D_BALL_TARGET_DIST(0),
		B_D_DEFENCE_POINT_DIST(1),
		D_D_BALL_Z(2),
		D_X(3),
		D_JUMPS(4),
		B_GOAL(5),
		B_VEL_Z(6),
		B_X(7),
		B_B_GATE_DISTANCE(8),
		B_ENEMY_DIST(9),
		O_ME_TOUCHED_BALL(10),
		O_ENEMY_TOUCHED_BALL(11),
		O_ENEMY_DANGER_ZONE(12),
		B_SEMI_GOAL(13),
		D_FIELD_CENTER(14),
		B_MY_DIST(15),
		B_Y(16),
		D_Z(17),
		D_CORNER(18),
		B_DANGER_GATE(19),
		B_ANGLE(20),
		D_VEL_Z(21),
		D_NITRO(22),
		D_PICKED_NITRO(23),
		TRAJ_GATE(24),
		HOLD_MIDDLE(25),
		B_ABOVE_GATE(26),
		D_Y(27),
		B_VEL(28),
		B_POS(29),
		D_ENEMY_DIST(30),
		B_TRAJ_GOAL(31),
		D_TOO_CLOSE_PENALTY(32),
		D_WASTED_NITRO(33),
		D_STAY_MIDDLE(34),
		D_KICK_ENEMY(35);

		private final int value;

		ScoreType(int val) {
			value = val;
		}

		public int getValue() {
			return value;
		}
	}
}
