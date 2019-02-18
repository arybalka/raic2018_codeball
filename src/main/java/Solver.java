import Lama.Perf;
import Lama.V3;

public class Solver {
	private static final boolean PERF_SOLVER = Debug.on && false;
	private static final boolean PERF_SOLVER_DEEP = Debug.on && false;

//	private static V3 delta_position = V3.zero();
	//	private static V3 ce_normal = V3.zero();
	private static V3 vel_diff = V3.zero();
	private static CD.Result cd_res = new CD.Result(0, 0, 0, 0);
	private static Dude t_dude = null;

	public static boolean collideEntities(Entity a, Entity b) {

		if (PERF_SOLVER_DEEP) Perf.start("psd.collideEntities");

		double delta_position_x = b.x - a.x;
		double delta_position_y = b.y - a.y;
		double delta_position_z = b.z - a.z;
		double dplen2 = delta_position_x * delta_position_x + delta_position_y * delta_position_y + delta_position_z * delta_position_z;

		if (dplen2 < (a.radius + b.radius) * (a.radius + b.radius)) {
			double len = Math.sqrt(dplen2);
			double penetration = a.radius + b.radius - len;
			double k_a = (1 / a.mass) / ((1 / a.mass) + (1 / b.mass));
			double k_b = (1 / b.mass) / ((1 / a.mass) + (1 / b.mass));

			double ce_normal_x = delta_position_x / len;
			double ce_normal_y = delta_position_y / len;
			double ce_normal_z = delta_position_z / len;

			a.Sub(ce_normal_x * penetration * k_a, ce_normal_y * penetration * k_a, ce_normal_z * penetration * k_a);
			b.Add(ce_normal_x * penetration * k_b, ce_normal_y * penetration * k_b, ce_normal_z * penetration * k_b);

			vel_diff.x = b.vel.x - a.vel.x;
			vel_diff.y = b.vel.y - a.vel.y;
			vel_diff.z = b.vel.z - a.vel.z;
			double delta_velocity = vel_diff.dotProduct(ce_normal_x, ce_normal_y, ce_normal_z) - b.radius_change_speed - a.radius_change_speed;

			if (delta_velocity < 0) {
				double d = (1 + CFG.HIT_E) * delta_velocity;
				ce_normal_x *= d;
				ce_normal_y *= d;
				ce_normal_z *= d;
//				ce_normal.Scale((1 + CFG.HIT_E) * delta_velocity);

				a.vel.Add(ce_normal_x * k_a, ce_normal_y * k_a, ce_normal_z * k_a);
				b.vel.Sub(ce_normal_x * k_b, ce_normal_y * k_b, ce_normal_z * k_b);
			}

			if (PERF_SOLVER_DEEP) Perf.stop("psd.collideEntities");
			return true;
		}

		if (PERF_SOLVER_DEEP) Perf.stop("psd.collideEntities");
		return false;
	}

	public static V3 collideWithArena(Entity e) {
		if (PERF_SOLVER_DEEP) Perf.start("psd.collideWithArena");

		cd_res.set(0, 0, 0, 0);
		CD.danToArena(cd_res, e.x, e.y, e.z, e.radius);

		double penetration = e.radius - cd_res.distance;
		if (penetration > 0) {
//			e.Add(cd_res.normal.scale(penetration));
			e.Add(cd_res.x * penetration, cd_res.y * penetration, cd_res.z * penetration);

			double velocity = e.vel.dotProduct(cd_res.x, cd_res.y, cd_res.z) - e.radius_change_speed;

			if (velocity < 0) {
//				e.vel.Sub(cd_res.normal.scale((1 + e.arena_e) * velocity));
				double ml = (1 + e.arena_e) * velocity;
				e.vel.Sub(cd_res.x * ml, cd_res.y * ml, cd_res.z * ml);

				if (PERF_SOLVER) Perf.stop("psd.collideWithArena");

				return new V3(cd_res.x, cd_res.y, cd_res.z);
			}
		}

		if (PERF_SOLVER_DEEP) Perf.stop("psd.collideWithArena");
		return null;
	}

	public static void move(Entity e, double dt) {
		if (PERF_SOLVER_DEEP) Perf.start("psd.move");

		e.vel.Clamp(CFG.MAX_ENTITY_SPEED);
		e.Add(e.vel.x * dt, e.vel.y * dt, e.vel.z * dt);
		e.y -= CFG.GRAVITY * dt * dt / 2.0;
		e.vel.y -= CFG.GRAVITY * dt;

		if (PERF_SOLVER_DEEP) Perf.stop("psd.move");
	}

	public static void updateDude(Dude dude, double dt) {
		if (PERF_SOLVER_DEEP) Perf.start("psd.updateDude");

		if (dude.touch) {
			V3 target_velocity = dude.action.vel.clamp(CFG.ROBOT_MAX_GROUND_SPEED);
//			target_velocity.Sub(dude.normal.scale(dude.normal.dotProduct(target_velocity)));
			double dp = dude.normal.dotProduct(target_velocity);
			target_velocity.Sub(dude.normal.x * dp + dude.vel.x, dude.normal.y * dp + dude.vel.y, dude.normal.z * dp + dude.vel.z);

			double tvl2 = target_velocity.len2();
			if (tvl2 > 0) {
				double acceleration = CFG.ROBOT_ACCELERATION * Math.max(0, dude.normal.y);
				double tvl = Math.sqrt(tvl2);
				target_velocity.Scale(acceleration * dt / tvl).Clamp(tvl);
				dude.vel.Add(target_velocity);
			}
		}


		if (dude.action.use_nitro && dude.nitro > 0) {
//			if (Debug.on) Debug.echo("!! Using Nitro");
			V3 target_velocity_change = dude.action.vel.sub(dude.vel);
			target_velocity_change.Clamp(dude.nitro * CFG.NITRO_POINT_VELOCITY_CHANGE);

			double tvl2 = target_velocity_change.len2();
			if (tvl2 > 0) {
				double tvl = Math.sqrt(tvl2);
				V3 acceleration = target_velocity_change.scale(CFG.ROBOT_NITRO_ACCELERATION / tvl);
				V3 velocity_change = acceleration.scale(dt);
				velocity_change.Clamp(tvl);
				dude.vel.Add(velocity_change);
				dude.nitro -= velocity_change.len() / CFG.NITRO_POINT_VELOCITY_CHANGE;
			}
		}

		if (PERF_SOLVER_DEEP) Perf.stop("psd.updateDude");

		move(dude, dt);
		dude.radius = CFG.ROBOT_MIN_RADIUS + (CFG.ROBOT_MAX_RADIUS - CFG.ROBOT_MIN_RADIUS) * dude.action.jump_speed / CFG.ROBOT_MAX_JUMP_SPEED;
		dude.radius_change_speed = dude.action.jump_speed;

	}

	public static void collideDudesWithEachOther(Dude me, SimResult result, Dude[] dudes) {
		if (PERF_SOLVER) Perf.start("solver.dudes");

		for (int i = 0; i < dudes.length; i++) {
			for (int j = 0; j < i; j++) {
				Dude d1 = dudes[i];
				Dude d2 = dudes[j];

				if (collideEntities(d1, d2)) {
					if (me == null) continue;

					if (d1.id == me.id || d2.id == me.id) {
						if ((d1.id == me.id ? d2 : d1).is_mate) {
							result.touching_mate++;
						} else {
							result.touching_enemy++;
						}
					}
				}
			}
		}

		if (PERF_SOLVER) Perf.stop("solver.dudes");
	}

	//	private static V3 cdwab_collision_normal;
	public static void collideDudeWithArenaAndBall(Dude me, SimResult result, Dude dude, TheBall ball) {

		if (!dude.flag) {
			collideDudeWithBall(me, result, dude, ball);
		}

		if (PERF_SOLVER) Perf.start("solver.dude_arena");

		V3 cdwab_collision_normal = collideWithArena(dude);
		if (cdwab_collision_normal == null) {
			dude.touch = false;
		} else {
			dude.touch = true;
			dude.normal.set(cdwab_collision_normal); // TODO: check if ok

			if (me != null && dude.id == me.id) {
				result.touching_arena = true;
			}
		}

		if (PERF_SOLVER) Perf.stop("solver.dude_arena");
	}

	private static void collideDudeWithBall(Dude me, SimResult result, Dude dude, TheBall ball) {
		if (PERF_SOLVER) Perf.start("solver.dude_arena_ball");

		if (ball != null && collideEntities(dude, ball)) {
			if (me != null) {
				if (dude.id == me.id) {
					result.touching_ball_me = true;
				} else if (dude.is_mate) {
					result.touching_ball_mate++;
				} else {
					result.touching_ball_enemy++;
				}
			}

			ball.touch = true;
			ball.had_collision_with_enemy = true;
		}

		if (PERF_SOLVER) Perf.stop("solver.dude_arena_ball");
	}

	public static void simulateSingleDude(Dude dude, TheBall ball, double dt) {
		if (PERF_SOLVER) Perf.start("solver.sim_single_dude");

		for (int t = 0; t < CFG.DELTA_TIME / dt; t++) {
			updateDude(dude, dt);
			collideDudeWithArenaAndBall(dude, new SimResult(), dude, ball);
		}

		if (PERF_SOLVER) Perf.stop("solver.sim_single_dude");
	}

	public static void simulateSingleDudeAndBall(Dude dude, TheBall ball) {
		updateDude(dude, CFG.DT);
		move(ball, CFG.DT);
		collideDudeWithArenaAndBall(dude, new SimResult(), dude, ball);
		collideWithArena(ball);
	}

	public static SimResult update(Dude me, Dude[] dudes, TheBall ball, boolean accurate, boolean use_cached_traj, int tick_offset, double microticks) {
		return update(new SimResult(), me, dudes, ball, accurate, use_cached_traj, tick_offset, microticks);
	}

	public static SimResult update(SimResult result, Dude me, Dude[] dudes, TheBall ball, boolean accurate, boolean use_cached_traj, int tick_offset, double microticks) {
		if (PERF_SOLVER) Perf.start("solver");

		double dt = CFG.DELTA_TIME / microticks;

		ball.touch = false;

		for (Dude dude : dudes) {
			dude.flag = false;
		}

		boolean ball_updated = false;
		if (use_cached_traj && !accurate && !ball.had_collision_with_enemy && tick_offset < Glob.trajectory.length && Glob.trajectory[tick_offset] != null) {
			TheBall tb = Glob.trajectory[tick_offset];
			if (!tb.had_collision_with_enemy) {

//				if (Debug.on) Debug.echo("using saved");

				ball.updateFromTheBall(tb);
				ball_updated = true;
				if (ball.touch) {
					result.touching_ball_arena = true;
				}
			}
		}

		if (!ball_updated) {
			if (PERF_SOLVER) Perf.start("solver.part1");

			double iters = 1.0;
			boolean acc = false;

			for (Dude dude : dudes) {
				if (!dude.touch && dude.distanceLess(ball, (ball.radius + dude.radius) * 1.33)) {
					dude.flag = true;
					iters = CFG.ACCURATE_MICROTICKS / microticks;
					acc = true;
				}
			}

			if (!acc && accurate && !ball.had_collision_with_enemy && (ball.y < ball.radius * 1.15 || ball.y > Glob.arena.height - ball.radius * 1.15 || (
				(Math.abs(ball.x) > Glob.arena.width * 0.5 - Glob.arena.bottom_radius - ball.radius)
				|| (Math.abs(ball.z) > Glob.arena.depth * 0.5 - Glob.arena.bottom_radius - ball.radius)
				|| (Math.abs(ball.x) > Glob.arena.width * 0.5 - Glob.arena.corner_radius && Math.abs(ball.z) > Glob.arena.depth * 0.5 - Glob.arena.corner_radius)))) {

				TheBall test = ball.copy();
				move(test, dt);
				if (collideWithArena(test) != null) {
					iters = CFG.ACCURATE_MICROTICKS / microticks;
				} else {
					ball.updateFromTheBall(test);
					iters = 0;
				}
			}

			if (PERF_SOLVER) {
				Perf.stop("solver.part1");
				Perf.start("solver.part2");
			}

			for (Dude dude : dudes) {
				if (!dude.flag) {
					updateDude(dude, dt);
				}
			}

			if (PERF_SOLVER) {
				Perf.stop("solver.part2");
				Perf.start("solver.part3");
			}

			for (double i = 0; i < iters; i++) {
				move(ball, dt / iters);

				for (Dude dude : dudes) {
					if (dude.flag) {
						updateDude(dude, dt / iters);
						collideDudeWithBall(me, result, dude, ball);
					}
				}

				if (collideWithArena(ball) != null) {
					result.touching_ball_arena = true;
					ball.touch = true;
				}
			}

			if (PERF_SOLVER) Perf.stop("solver.part3");
		} else {
			if (PERF_SOLVER) Perf.start("solver.part4");
			for (Dude dude : dudes) {
				updateDude(dude, dt);
			}
			if (PERF_SOLVER) Perf.stop("solver.part4");
		}

/*
		if (PERF_SOLVER) Perf.start("solver.move_ball");
		move(ball, dt);
		if (PERF_SOLVER) Perf.stop("solver.move_ball");
*/


		if (PERF_SOLVER) Perf.start("solver.part5");
		collideDudesWithEachOther(me, result, dudes);
		if (PERF_SOLVER) Perf.stop("solver.part5");

		if (PERF_SOLVER) Perf.start("solver.dude_with_a_b");
		for (Dude dude : dudes) {
			collideDudeWithArenaAndBall(me, result, dude, ball);
		}
		if (PERF_SOLVER) Perf.stop("solver.dude_with_a_b");

/*
		if (PERF_SOLVER) Perf.start("solver.ball_arena");
		if (collideWithArena(ball) != null) {
			result.touching_ball_arena = true;
			ball.touch = true;
		}
		if (PERF_SOLVER) Perf.stop("solver.ball_arena");
*/

		// Goal Scored
		if (Math.abs(ball.z) > Glob.arena.depth / 2 + ball.radius) {

//			if (Debug.on) Debug.echo("!!! GOAL");

			if (PERF_SOLVER) Perf.stop("solver");

			result.goal_scored = ball.z > 0 ? 1 : -1;

//			result.goal_scored *= ball.y;

			return result;
		}

/*
		if (PERF_SOLVER) Perf.start("solver.nitro");
		for (Dude robot : dudes) {
			if (robot.nitro == CFG.MAX_NITRO_AMOUNT) {
				continue;
			}

			// TODO: pick up nitro;
*/
/*
			for (pack in nitro_packs):
			if not pack.alive:
			continue
			if length(robot.position - pack.position) <= robot.radius + pack.radius:
			robot.nitro = MAX_NITRO_AMOUNT
			pack.alive = false
			pack.respawn_ticks = NITRO_PACK_RESPAWN_TICKS
*//*

		}
		if (PERF_SOLVER) Perf.stop("solver.nitro");
*/

		if (PERF_SOLVER) Perf.stop("solver");

		return result;
	}

	public static class SimResult {
		public int touching_mate = 0;
		public int touching_enemy = 0;
		public boolean touching_ball_me = false;
		public int touching_ball_mate = 0;
		public int touching_ball_enemy = 0;
		public boolean touching_arena = false;
		public int goal_scored = 0;
		public boolean touching_ball_arena = false;

		public void reset() {
			touching_mate = 0;
			touching_enemy = 0;
			touching_ball_me = false;
			touching_ball_mate = 0;
			touching_ball_enemy = 0;
			touching_arena = false;
			goal_scored = 0;
			touching_ball_arena = false;
		}
	}

}
