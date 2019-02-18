import Lama.*;
import Lama.Point;
import model.Action;
import model.Game;
import model.Robot;
import model.Rules;

import java.awt.*;
import java.util.ArrayList;

public class Agent {
	public static long total_time = 0;
	private static boolean is_trajectory_accurate = false;

	public static void prepare(Robot me, Rules rules, Game game, Action action) {
		Perf.start("tick.prepare");

		Glob.init(rules, game);

		Point gate_center_p = Glob.my_gate_center.pointFromXZ();
		Point intersect;
		double x_pos;
		V3 ea_x_pred = null;

		if (Glob.enemy_attacker != null && Glob.enemy_attacker.z < 0) {
			Dude test = Glob.enemy_attacker.copy();
			for (int i = 0; i < 15; i++) {
				test.Sim(new Act(test.vel.scale(100.0), 0), null);
			}
			ea_x_pred = test;
		}

		intersect = HMath.lineWithLineIntersection(Glob.ball.add(Glob.ball.vel.normalize().scale(100)).pointFromXZ(), gate_center_p.sub(0, Glob.arena.goal_depth),
												   new Point(-Glob.arena.goal_width / 2.0, -CFG.HALF_DEPTH * Glob.GATE_Z),
												   new Point(Glob.arena.goal_width / 2.0, -CFG.HALF_DEPTH * Glob.GATE_Z));

		double bx = intersect != null ? (intersect.x + Glob.ball.x) * 0.5 : Glob.ball.x;

		if (ea_x_pred != null) {
			intersect = HMath.lineWithLineIntersection(gate_center_p.sub(0, Glob.arena.goal_depth), ea_x_pred.pointFromXZ(),
													   new Point(-Glob.arena.goal_width / 2.0, -CFG.HALF_DEPTH * Glob.GATE_Z),
													   new Point(Glob.arena.goal_width / 2.0, -CFG.HALF_DEPTH * Glob.GATE_Z));

			x_pos = intersect != null ? intersect.x : bx;

		} else if (Glob.ball.z < 0 && Math.abs(Glob.ball.x) < 15) {
			if (Glob.ball_next_landing_pos != null) {
				x_pos = Glob.ball_next_landing_pos.x;
			} else {
				x_pos = bx;
			}
		} else {
			if (Glob.enemy_attacker != null && Glob.enemy_attacker.z < -10 && Math.abs(Glob.enemy_attacker.x) < CFG.HALF_GOAL_WIDTH) {
				x_pos = Glob.enemy_attacker.x;
			} else {
				x_pos = 0;
			}
		}

		double s = Glob.ball.z < -30 && Math.abs(Glob.ball.x) <= 20 && Glob.ball.y < Glob.arena.goal_height ? 0.7 : 0.25;

		Glob.point_for_defence = CFG.FINAL
			? new V3(HMath.clamp(x_pos, -CFG.HALF_GOAL_WIDTH * s, CFG.HALF_GOAL_WIDTH * s), 1.0, -Glob.arena.depth * 0.5 * Glob.GATE_Z)
			: new V3(HMath.clamp(x_pos, -CFG.HALF_GOAL_WIDTH * 0.7, CFG.HALF_GOAL_WIDTH * 0.7), 1.0, -Glob.arena.depth * 0.5 * Glob.GATE_Z);

		for (TheBall b : Glob.trajectory) {
			if (b == null) break;

			if (b.z < -CFG.HALF_DEPTH && Glob.enemy_attacker.distanceMore(Glob.ball, 10)) {
				Glob.point_for_defence = new V3(HMath.clamp(b.x, -10, 10), 1.0, -Glob.arena.depth * 0.5 * Glob.GATE_Z);
				break;
			}
		}

		Glob.angle_to_my_gate = Math.abs(Glob.ball.vel.getAngleToXZ(Glob.my_gate_center));
		Glob.angle_to_enemy_gate = Math.abs(Glob.ball.vel.getAngleToXZ(Glob.enemy_gate_center));
		Glob.players_to_ball_traj = null;

		CD.setup(rules.arena);

		if (Glob.SKIP_EARLY_STEPS || (!Glob.goal_scored && Glob.last_round_started_tick + 15 <= Glob.tick)) {
			getBallTrajectory(Glob.ball, Glob.TRAJECTORY_TICKS);
		}

		if (Debug.on) Debug.sphere(Glob.point_for_defence, CFG.ROBOT_MIN_RADIUS * 0.5, new Color(11, 44, 99), 1);

		Perf.stop("tick.prepare");
	}

	private static void getBallTrajectory(TheBall ball, int ticks) {
		boolean reload = false;

		if (Glob.trajectory[0] != null && is_trajectory_accurate) {
			if (Debug.on) Debug.echo("Ball: " + ball + "\n exp: " + Glob.trajectory[0]);

			if (ball.distanceMore(Glob.trajectory[0], 0.001) || ball.vel.distanceMore(Glob.trajectory[0].vel, 0.001)) {
				if (Debug.on) Debug.ball_clr_override = new Color(161, 150, 0);

				reload = true;
			} else {
				for (Dude d : Glob.dudes) {
					if (d.touch) continue;

					boolean found = false;
					for (Dude dia : Glob.dudes_in_air) {
						if (dia.id != d.id) continue;

						found = true;
						break;
					}

					if (!found) {
						reload = true;
					}
				}

			}
		} else {
			reload = true;
		}

		TheBall sim_ball;
		Glob.ball_next_collision_pos = null;
		Glob.ball_next_landing_pos = null;
		Glob.ball_next_landing_target = new V3[Glob.dudes.length];
		Glob.ball_next_punch_point = new V3[Glob.dudes.length];
		Glob.ball_traj_landings = new V3[Glob.BALL_TRAJ_LANDINGS_COUNT];
		int start_from = 0;

		Glob.dudes_in_air = new ArrayList<>();
		for (Dude d : Glob.dudes) {
			if (!d.touch) {
				Dude d_add = d.copy();
				if (d_add.action == null) d_add.action = new Act(V3.zero(), CFG.ROBOT_MAX_JUMP_SPEED);
				Glob.dudes_in_air.add(d_add);
			}
		}

		if (reload) {
			is_trajectory_accurate = true;
			for (Dude d : Glob.dudes) {
				if (d.distanceLess(ball, (d.radius + ball.radius) * 1.1)) {
					is_trajectory_accurate = false;
					break;
				}
			}

			Glob.trajectory = new TheBall[Glob.TRAJECTORY_TICKS];
			sim_ball = ball.copy();
		} else {
			for (int i = 0; i < Glob.trajectory.length - 1; i++) {
				Glob.trajectory[i] = Glob.trajectory[i + 1];
			}
			Glob.trajectory[Glob.trajectory.length - 1] = null;

			start_from = Glob.trajectory.length - 1;
			sim_ball = Glob.trajectory[Glob.trajectory.length - 2].copy();
		}

		Glob.trajectory_last_updated = Glob.tick;
		Glob.trajectory_ends_with_goal = 0;

		Dude[] dudes_to_sim = Glob.dudes_in_air.toArray(new Dude[0]);

		for (int i = start_from; i < ticks; i++) {
			sim_ball.last_pos = sim_ball.pos();

			if (Glob.trajectory_ends_with_goal == 0 && Math.abs(sim_ball.z) > Glob.arena.depth / 2 + sim_ball.radius) {
				Glob.trajectory_ends_with_goal = sim_ball.z > 0 ? 1 : -1;
				if (Debug.on) Debug.echo("Goal scored, interrupting ball trajectory calculation");
			}

			if (Glob.trajectory_ends_with_goal != 0) {
				Glob.trajectory[i] = sim_ball.copy();
				continue;
			}

			for (int t = 0; t < CFG.MICROTICKS_PER_TICK; t++) {
				Solver.update(null, dudes_to_sim, sim_ball, is_trajectory_accurate, is_trajectory_accurate, i, CFG.MICROTICKS_PER_TICK);
			}

			Glob.trajectory[i] = sim_ball.copy();
		}

		int btc = 0;
		int traj_idx = 0;
		int ball_low_times = 0;
		boolean ball_was_low = false;
		Glob.players_to_ball_traj = new Dude[Glob.dudes.length][Glob.TRAJECTORY_TICKS];
		Glob.players_to_ball_traj_len = new Integer[Glob.dudes.length];

		for (TheBall traj_ball : Glob.trajectory) {
			if (Glob.ball_next_collision_pos == null && traj_ball.touch && traj_ball.y < Glob.arena.height * 0.5) {
				Glob.ball_next_collision_pos = traj_ball.pos();
				if (Debug.on)
					Debug.circle3(traj_ball.replace(null, 0.1, null), traj_ball.radius, new Color(129, 11, 103), true);
			}

			if (traj_ball.y <= Glob.LANDING_HEIGHT) {
				if (Glob.ball_next_landing_pos == null) {
					Glob.ball_next_landing_pos = traj_ball.pos();
					if (Debug.on)
						Debug.circle3(traj_ball.replace(null, 0.1, null), traj_ball.radius * 1.1, new Color(115, 167, 167), true);

					if (btc < Glob.BALL_TRAJ_LANDINGS_COUNT) {
						Glob.ball_traj_landings[btc] = traj_ball.pos();
					}

					btc++;
				}

				if (!ball_was_low || traj_idx % 7 == 0) {
					ball_low_times++;

					for (Dude d : Glob.dudes) {
						if (Glob.ball_next_landing_target[d.dude_id] == null) {
							Glob.players_to_ball_traj[d.id - 1] = new Dude[Glob.trajectory.length];

							Dude test = d.copy();
							for (int i = 0; i < traj_idx; i++) {
								test.Sim(new Act(test.dirTo(traj_ball).replace(null, 1.0, null).scale(100.0)), traj_ball.copy());
								Glob.players_to_ball_traj[d.id - 1][i] = test.copy();

								if (test.projectXZ().distanceLess(traj_ball.projectXZ(), 4.0)) {
									Glob.ball_next_landing_target[d.dude_id] = traj_ball.copy();
									Glob.players_to_ball_traj_len[d.id - 1] = i + 1;

									if (Debug.on) {
										Debug.circle3(Glob.ball_next_landing_target[d.dude_id], 0.5, new Color(1, 1, 1, 150), true);

										for (Dude rd : Glob.players_to_ball_traj[d.id - 1]) {
											if (rd == null) break;
											Debug.square3(rd, 0.2, new Color(0, 0, 0, 100), true);
										}
									}

									break;
								}
							}
						}
					}
				}

				ball_was_low = true;
			} else {
				ball_was_low = false;
			}

			traj_idx++;

			if (Debug.on) Debug.sphere(traj_ball, 0.2, is_trajectory_accurate ? new Color(167, 157, 0, 50) : new Color(0, 127, 167, 50), 1);
		}

		int idx = 0;
		for (V3 p : Glob.ball_next_landing_target) {
			if (p != null) {
				Glob.ball_next_punch_point[idx] = p.z < -CFG.HALF_DEPTH * 0.25
					? p.add(p.dirTo(Glob.my_gate_center).scaleTo(1.0, 0, 1.0))
					: p.add(Glob.enemy_gate_center.dirTo(p).scaleTo(1.0, 0, 1.0));

				if (Debug.on) Debug.circle3(Glob.ball_next_punch_point[idx], 0.35, Color.green, false);
			}
			idx++;
		}
	}

	public static Dude getDudeFromRobot(Robot robot) {
		if (robot.is_teammate) {
			for (Dude d : Glob.my_team) {
				if (d.id == robot.id) return d;
			}
		} else {
			for (Dude d : Glob.enemy_team) {
				if (d.id == robot.id) return d;
			}
		}

		return null;
	}

	public static void act(Robot me, Rules rules, Game game, Action action) {
		long started = System.currentTimeMillis();

		Perf.start("tick");

		if (Glob.isNewTick(game)) {
			prepare(me, rules, game, action);

			if (Scores.DEBUG && Debug.on) Debug.clearMemo1();

			if (Debug.on) {
				Debug.echo("\n================================= NEW TICK " + Glob.tick + " =================================");
			}

			think(me, rules, game, action);
		} else {
			subTick(me, rules, game, action);
		}

		if (Debug.on) {
			Debug.echo("======> Robot " + Glob.subtick + "   :::   " + getDudeFromRobot(me));
		}

		implementTurn(me, action);

		Perf.stop("tick");

		if (Glob.last_subturn) {
			if (Debug.on) {
				Perf.start("paint");

				Debug.frame(true, Glob.tick);

				Perf.stop("paint");
			}
		}

		if (Debug.on && Glob.tick % 10 == 0 && Glob.last_subturn) {
			Perf.print();
		}


		started = System.currentTimeMillis() - started;
		total_time += started;

		if (Debug.output_performance && Glob.tick % (Glob.PRODUCTION_ENV ? 2000 : 100) == 0 && Glob.last_subturn) {
			System.out.println("Tick: " + Glob.tick + ", Time: " + total_time + " ms, per tick: " + HMath.round((double) total_time / (double) (Glob.tick + 1), 1) + " ms\t Scores -> " + game.players[0].score + " : "
							   + game.players[1].score + "\t -> " + HMath.round((double) game.players[0].score / Math.max((double) game.players[1].score, 1.0), 2));
		}

	}

	public static void assignTurn(Dude dude, Act act) {
		Glob.robot_to_act.put(dude.id, act);
	}

	public static void implementTurn(Robot me, Action action) {
		Perf.start("implementTurn");

		Act a = Glob.robot_to_act.getOrDefault(me.id, null);
		if (a != null) {
			action.target_velocity_x = a.vel.x;
			action.target_velocity_y = a.vel.y;
			action.target_velocity_z = a.vel.z;
			action.jump_speed = a.jump_speed;
			action.use_nitro = a.use_nitro;

			if (Debug.on) Debug.echo("Implementing action for id" + me.id + " -> " + a);
		} else {
			if (Debug.on) Debug.echo("!! No action for robot " + me.id);
		}

		Perf.stop("implementTurn");
	}

	private static void subTick(Robot me, Rules rules, Game game, Action action) {
		Perf.start("tick.sub");

		Glob.subtick++;
		Glob.last_subturn = Glob.subtick == Glob.my_team.length;

		Perf.stop("tick.sub");
	}

	private static void think(Robot me, Rules rules, Game game, Action action) {
		Perf.start("think");

		if (Glob.goal_scored) return;

		if (Debug.on) debugPrintDudes();

		ArrayList<Dude> to_move = new ArrayList<>();
		Glob.ball_defender_kick_to = null;
		Dude closest_to_ball = Utils.getClosest(Glob.ball, Glob.my_team);
		Dude defender = Glob.TEST_DEFENCE_ONLY ? Glob.my_team[0] : Utils.getClosest(Glob.point_for_defence, Glob.my_team);

		Glob.my_defender = defender;
		Glob.enemy_defender = Utils.getClosest(Glob.enemy_gate_center, Glob.enemy_team);
		Glob.dudes_targets = new V3[Glob.dudes.length];

		boolean early_steps = !Glob.SKIP_EARLY_STEPS && (Glob.last_round_started_tick + 15 > Glob.tick);

		if (defender.id == closest_to_ball.id || gateUnderAttack() || Glob.TEST_DEFENCE_ONLY) {
			to_move.add(defender);
		}

		if (Debug.on) Debug.square3(defender, 2.5, new Color(255, 228, 0, 60), true);

		for (Dude dude : Glob.my_team) {
			if (dude.id == defender.id) continue;

			if (CFG.FINAL) {
				if (isClosestAttackerToBall(dude)) Glob.my_attacker = dude;
				else Glob.my_semi_attacker = dude;
			} else {
				Glob.my_attacker = dude;
				Glob.my_semi_attacker = null;
			}

			if (to_move.contains(dude)) continue;

			to_move.add(dude);
		}

		for (Dude d : Glob.dudes) {
			if (Glob.tick % 5 == 0) Glob.dudes_movement[d.id - 1] = null;
			if (Glob.dudes_movement[d.id - 1] == null) continue;

			int i = 0;
			for (; i <= Glob.dudes_movement[d.id - 1].length - 2; i++) {
				if (Glob.dudes_movement[d.id - 1][i + 1] == null) {
					i++;
					break;
				}
				Glob.dudes_movement[d.id - 1][i] = Glob.dudes_movement[d.id - 1][i + 1];
			}
			Glob.dudes_movement[d.id - 1][i] = null;
		}

		for (Dude dude : to_move) {
			if (!early_steps) {
				calcDudeTarget(dude, defender);
			}

			if (Glob.TEST_DEFENCE_ONLY) {
				testDefence(dude);
				continue;
			}

			if (early_steps) {
				if (dude.id != defender.id) {
					forceRoundBeginningMove(dude);
				} else {
					DecisionMaker.findDefendingAction(dude, Glob.ball);
				}
				continue;
			}

			// ------------------
			// All magic is here
			// ------------------
			if (Debug.on) Debug.echo(dude.id != defender.id ? "### Attacker::\n" : "### Defender::\n");
			boolean cla = isClosestAttackerToBall(dude);

			if (CFG.FINAL && dude.id != defender.id && !cla && dude.distanceMore(Glob.ball, 20)) {
				DecisionMaker.findSemiAttackerAction(dude, Glob.ball);
				continue;
			}

			DecisionMaker.findOptimalActionSequence(dude, dude.id != defender.id, to_move.size(), cla);
		}

		for (Dude dude : Glob.my_team) {
			if (to_move.contains(dude)) continue;

			if (!early_steps) {
				calcDudeTarget(dude, defender);
			}

			if (Glob.TEST_DEFENCE_ONLY) {
				testDefence(dude);
				continue;
			}

			if (early_steps) {
				if (dude.id != defender.id) {
					forceRoundBeginningMove(dude);
				} else {
					DecisionMaker.findDefendingAction(dude, Glob.ball);
				}
				continue;
			}

			DecisionMaker.best_genotypes[dude.dude_id] = null;
			DecisionMaker.findDefendingAction(dude, Glob.ball);
		}

		for (Dude d : Glob.my_team) {
			if (d.action != null) {
				d.action.Implement(d);
			}
		}

		Perf.stop("think");
	}

	private static boolean isClosestAttackerToBall(Dude dude) {
		Dude closest = null;

		if (Glob.players_to_ball_traj_len == null) return true;

		for (Dude d : Glob.my_team) {
			if (d.id == Glob.my_defender.id || Glob.players_to_ball_traj_len[d.id - 1] == null) continue;
			if (closest == null || Glob.players_to_ball_traj_len[d.id - 1] < Glob.players_to_ball_traj_len[closest.id - 1]) {
				closest = d;
			}
		}

		if (closest != null) return dude.id == closest.id;

		for (Dude d : Glob.my_team) {
			if (d.id == Glob.my_defender.id) continue;
			double d1 = d.distanceTo(Glob.ball);
			Double dc = closest == null ? null : closest.distanceTo(Glob.ball);

			if (dc == null || (d1 < dc)) {
				closest = d;
			}
		}

		return closest == null || closest.id == dude.id;
	}


	private static void calcDudeTarget(Dude dude, Dude defender) {
		Integer defender_to_ball_ticks = Glob.players_to_ball_traj_len[defender.id - 1];

		if (!dude.is_mate) return;
		boolean is_attacker = dude.id != defender.id;

		if (is_attacker) {
			Integer l = Glob.players_to_ball_traj_len[dude.id - 1];

			boolean is_closest_attacker = isClosestAttackerToBall(dude);

			if (l == null || l == 0
				|| (!CFG.FINAL && defender_to_ball_ticks != null && defender_to_ball_ticks * 1.2 < l && l > 30) || (!is_closest_attacker)) {

				if (is_closest_attacker && CFG.FINAL) {

					if (Glob.ball_defender_kick_to != null) {
						Glob.dudes_targets[dude.id - 1] = Glob.ball_defender_kick_to.replace(null, 1.0, null);
						if (Glob.dudes_targets[dude.id - 1].z > CFG.HALF_DEPTH * 0.95) Glob.dudes_targets[dude.id - 1].z = CFG.HALF_DEPTH * 0.95;
						if (Debug.on) Debug.arrow(dude.pos(), Glob.dudes_targets[dude.id - 1].pos(), 0.5, 1, 1, new Color(0, 237, 255, 100), 1);
					} else {
						Glob.dudes_targets[dude.id - 1] = new V3(HMath.clamp(Glob.ball.x, -25, 25), 1.0, HMath.clamp(Glob.ball.z - 5.0, Glob.ATTACKER_IP_Z, Glob.ATTACKER_IP_Z_MAX));
					}

					if (Debug.on) Debug.arrow(dude, Glob.dudes_targets[dude.id - 1], 0.2, 1.0, 1.0, new Color(0, 50, 116, 75), 1);

				} else {

					if (Glob.ball_defender_kick_to != null) {
						Glob.dudes_targets[dude.id - 1] = Glob.ball_defender_kick_to.replace(null, 1.0, null);
						if (Glob.dudes_targets[dude.id - 1].z > CFG.HALF_DEPTH * 0.95) Glob.dudes_targets[dude.id - 1].z = CFG.HALF_DEPTH * 0.95;
						if (Debug.on) Debug.arrow(dude.pos(), Glob.dudes_targets[dude.id - 1].pos(), 0.5, 1, 1, new Color(0, 237, 255, 100), 1);
					} else {
						Glob.dudes_targets[dude.id - 1] = Glob.ball.add(Glob.enemy_gate_center).Scale(0.5).replace(null, 1.0, null);
						if (Debug.on) Debug.arrow(dude.pos(), Glob.dudes_targets[dude.id - 1].pos(), 0.5, 1, 1, new Color(226, 226, 226, 100), 1);
					}

					if (CFG.FINAL && CFG.HAS_NITRO && dude.nitro < CFG.MAX_NITRO_AMOUNT) {
						Nitro n = Utils.getClosest(dude, Glob.nitro);
						if (n.respawn == null) {
							Glob.dudes_targets[dude.id - 1] = n.pos();
							return;
						}
					}
				}

				return;
			}

			Glob.dudes_targets[dude.id - 1] = Glob.players_to_ball_traj[dude.id - 1][l - 1].pos();
			if (Debug.on) Debug.arrow(dude, Glob.dudes_targets[dude.id - 1], 0.2, 1.0, 1.0, new Color(0, 161, 25, 75), 1);
			return;

		} else {
			boolean is_closest = defender_to_ball_ticks != null;

			if (is_closest) {
				for (Dude e : Glob.enemy_team) {
					if (Glob.players_to_ball_traj_len[e.id - 1] != null && Glob.players_to_ball_traj_len[e.id - 1] < defender_to_ball_ticks + 35) {
						is_closest = false;
						break;
					}
				}
			}

			if (is_closest && defender_to_ball_ticks != null) {

				V3 dt = defender_to_ball_ticks == 0 ? Glob.ball.pos() : Glob.players_to_ball_traj[dude.id - 1][defender_to_ball_ticks - 1].pos();

				if (dt.distanceLess(Glob.my_gate_center, 30.0)) {
					Glob.dudes_targets[dude.id - 1] = dt;
					if (Debug.on) Debug.arrow(dude, Glob.dudes_targets[dude.id - 1], 0.2, 1.0, 1.0, new Color(225, 11, 107, 75), 1);
					return;
				}
			}

			Glob.dudes_targets[dude.id - 1] = Glob.point_for_defence.copy();
			if (Debug.on) Debug.arrow(dude, Glob.dudes_targets[dude.id - 1], 0.2, 1.0, 1.0, new Color(161, 109, 39, 75), 1);
		}
	}

	private static void forceRoundBeginningMove(Dude actor) {
		if (Debug.on) Debug.echo("Forcing move to center");

		actor.action = new Act(new V3(-actor.x * 100.0, 0, 0));
		actor.action.Implement(actor);
	}

	private static void debugPrintDudes() {
		for (Dude d : Glob.my_team) {
			Debug.echo("Dude :: " + d);
		}
		for (Dude d : Glob.enemy_team) {
			Debug.echo("Enemy :: " + d);
		}
		Debug.echo("");
	}

	private static boolean gateUnderAttack() {
		int score = 0;
		int in_gate_score = 0;

		if (Glob.ball.z > 0 || Glob.trajectory == null || Glob.trajectory.length == 0) return false;

		int i = 0;
		for (TheBall b : Glob.trajectory) {
			i++;

			if (b == null) break;

			if (b.z < -CFG.HALF_DEPTH * 0.95) {
				int sc = b.z < -CFG.HALF_DEPTH ? 3 : 1;
				if (sc > in_gate_score) in_gate_score = sc;
				continue;
			}

			if (CFG.FINAL && in_gate_score < 1 && b.y <= CFG.JUMP_HEIGHT_APPROX && b.z < -CFG.HALF_DEPTH * 0.33 && i <= 45) {
				in_gate_score = 1;
				break;
			}

			if (!CFG.FINAL && in_gate_score < 1 && Math.abs(b.x) <= Glob.arena.goal_width * 1.1 / 2.0 && b.projectXZ().distanceLess(Glob.point_for_defence.projectXZ(), 15.0)) {
				in_gate_score = 1;
				break;
			}
		}

		if (true) {
			return in_gate_score > 0;
		}

		score += in_gate_score;

		if (Glob.ball.distanceLess(Glob.point_for_defence, 20.0)) {
			score += 1;
		}

		if (Glob.ball.vel.z <= 10 && Glob.ball.vel.len() >= 25.0) {
			score += 1;
		}

		int num_dudes_facing_ball = 0;
		for (Dude d : Glob.my_team) {
			if (d.z < Glob.ball.z && d.projectXZ().distanceToLineSegment(Glob.ball.projectXZ(), Glob.my_gate_center.projectXZ()) <= 10.0) {
				num_dudes_facing_ball++;
			}
		}
		if (num_dudes_facing_ball < 2) score++;

		int num_enemies_near_ball = 0;
		for (Dude e : Glob.enemy_team) {
			if (Glob.ball.z < 0 && e.projectXZ().distanceTo(Glob.ball.projectXZ()) <= 10.0) {
				num_enemies_near_ball++;
			}
		}
		if (num_enemies_near_ball < 2) score++;

		return score >= 4;
	}

	private static void testDefence(Dude dude) {
		if (dude.id == Glob.my_team[1].id) {
			V3 des_pos = new V3(Glob.ball.x, 1.0, CFG.HALF_DEPTH * 0.5);
			dude.action = new Act(dude.dirTo(des_pos));
		} else {
			DecisionMaker.findOptimalActionSequence(dude, false, Glob.dudes.length, true);
		}
	}
}
