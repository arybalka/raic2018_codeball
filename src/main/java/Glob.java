import Lama.*;
import model.Arena;
import model.Game;
import model.Robot;
import model.Rules;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Glob {
	public static final boolean PRODUCTION_ENV = false;
	public static final boolean SKIP_EARLY_STEPS = false;
	public static final int TRAJECTORY_TICKS = 100;
	public static final int BALL_TRAJ_LANDINGS_COUNT = 3;
	public static V3[] ball_traj_landings = new V3[BALL_TRAJ_LANDINGS_COUNT];
	public static final boolean TEST_DEFENCE_ONLY = false;
	public static final double LANDING_HEIGHT = CFG.BALL_RADIUS * 1.5;
	public static final double GATE_Z_2P = 0.95;
	public static final double GATE_Z_3P = 0.95;
	public static final double ATTACKER_IP_Z = -CFG.HALF_DEPTH;
	public static final double ATTACKER_IP_Z_MAX = 35.0;
	public static int tick = -1;
	public static int subtick = 0;
	public static boolean first_tick = false;
	public static Arena arena;
	public static boolean last_subturn = false;

	public static boolean goal_scored = false;
	public static int last_goal_scored_tick = -10000;
	public static int last_round_started_tick = 0;
	public static V3 my_gate_center;
	public static V3 enemy_gate_center;
	public static V3 point_for_defence;
	public static V3 hold_point;
	public static double angle_to_my_gate;
	public static double angle_to_enemy_gate;
	public static Dude enemy_attacker;
	public static Dude[] my_team;
	public static Dude[] enemy_team;
	public static Dude[] dudes;
	public static TheBall[] trajectory;
	public static int trajectory_last_updated = -2;
	public static TheBall ball;
	public static Nitro[] nitro;
	public static HashMap<Integer, Act> robot_to_act;
	public static V3 ball_next_collision_pos;
	public static V3 ball_next_landing_pos;
	public static V3[] ball_next_landing_target;
	public static V3[] ball_next_punch_point;
	public static V3[][] enemies_to_me_traj;
	public static ArrayList<Dude> dudes_in_air = new ArrayList<>();
	public static Dude my_defender = null;
	public static Dude my_attacker = null;
	public static Dude my_semi_attacker = null;
	public static Dude enemy_defender = null;
	public static Dude[][] players_to_ball_traj = null;
	public static Integer[] players_to_ball_traj_len = null;
	public static V3[] dudes_targets = null;
	public static int trajectory_ends_with_goal = 0;
	public static Dude[][] dudes_movement;
	public static V3 ball_defender_kick_to = null;
	static double GATE_Z;


	public static boolean isNewTick(Game game) {
		return tick != game.current_tick;
	}

	static void init(Rules rules, Game game) {
		first_tick = tick == -1;
		tick = game.current_tick;
		subtick = 1;
		last_subturn = false;

		if (first_tick) {
			CFG.RULES_FINAL = false;
			CFG.ACCURATE_MICROTICKS = 50;

			CFG.FINAL = game.robots.length > 4;

			GATE_Z = CFG.FINAL ? GATE_Z_3P : GATE_Z_2P;

			DecisionMaker.FIRST_GENERATION_GENES_NUM = CFG.FINAL ? 35 : 40;
			DecisionMaker.SIMULATE_BALL_AFTER_SEQ_TICKS = CFG.FINAL ? 40 : 40;
			DecisionMaker.SIMULATE_BALL_AFTER_SEQ_TICKS_DEFENDER = CFG.FINAL ? 40 : 50;

			robot_to_act = new HashMap<>();
			my_team = new Dude[game.robots.length / 2];
			enemy_team = new Dude[game.robots.length / 2];
			dudes = new Dude[game.robots.length];
			trajectory = new TheBall[TRAJECTORY_TICKS];
			ball = new TheBall(game.ball);
			DecisionMaker.best_genotypes = new Genetics.Genotype[game.robots.length];
			nitro = new Nitro[game.nitro_packs.length];

			CFG.HAS_NITRO = game.nitro_packs.length > 0;

			for (int i = 0; i < game.nitro_packs.length; i++) {
				nitro[i] = new Nitro(game.nitro_packs[i]);
			}

			enemies_to_me_traj = new V3[game.robots.length / 2][TRAJECTORY_TICKS];
			dudes_movement = new Dude[game.robots.length][DecisionMaker.ACTIONS_PER_GENE];

			int my_team_id = 0;
			int enemy_team_id = 0;
			byte all_teams_id = 0;

			for (Robot r : game.robots) {
				Dude d = new Dude(r, all_teams_id);

				if (d.is_mate) {
					my_team[my_team_id] = d;
					my_team_id++;
				} else {
					enemy_team[enemy_team_id] = d;
					enemy_team_id++;
				}

				dudes[all_teams_id] = d;
				all_teams_id++;
			}

			if (Debug.on) {
				Perf.stop("tick.prepare");
				GLIsoCam cam = new GLIsoCam(new V3(0, 0.2, 0), 75, HMath.PI, HMath.QUATER_PI);
				Debug.initForm(cam, rules.arena);
				Perf.start("tick.prepare");
			}
		} else {

			if (PRODUCTION_ENV) {
				if (Agent.total_time >= (tick > 14000 ? 300000 : 270000) && CFG.ACCURATE_MICROTICKS > 10) {
					System.out.println("Reducing precision #1, tick: " + tick + ", time spent: " + Agent.total_time);
					CFG.ACCURATE_MICROTICKS = 10;
				}
				if (Agent.total_time >= (tick > 16000 ? 340000 : 320000) && CFG.ACCURATE_MICROTICKS > 1) {
					System.out.println("Reducing precision #2, tick: " + tick + ", time spent: " + Agent.total_time);
					CFG.ACCURATE_MICROTICKS = 1;
				}
			}

			History.save(tick);

			ball.updateFromBall(game.ball);

			for (Robot r : game.robots) {
				for (Dude d : (r.is_teammate ? my_team : enemy_team)) {
					if (d.id == r.id) {
						d.updateFromRobot(r, ball);
						break;
					}
				}
			}

			for (int i = 0; i < game.nitro_packs.length; i++) {
				nitro[i].updateFromNitroPack(game.nitro_packs[i]);
			}

			updateScoredGoals();
		}

		arena = rules.arena;

		my_gate_center = new V3(0, CFG.ROBOT_MIN_RADIUS, -arena.depth / 2.0);
		enemy_gate_center = new V3(0, CFG.ROBOT_MIN_RADIUS, arena.depth / 2.0);
		hold_point = new V3(0, 1.0, (arena.depth / 2.0) * 0.0);
		enemy_attacker = Utils.getClosest(my_gate_center, enemy_team);

	}

	private static boolean updateScoredGoals() {
		if ((tick < last_goal_scored_tick + CFG.TICKS_PER_SECOND * 2 || Math.abs(ball.z) > arena.depth / 2 + ball.radius) && ball.x != 0 && ball.z != 0) {
			if (!goal_scored) {
				goal_scored = true;
				last_goal_scored_tick = tick;
			}

			if (Debug.on) Debug.echo("Goal Scored, skipping");

			Perf.stop("think");
			return true;
		}

		if (goal_scored) {
			last_round_started_tick = tick + 1;
			if (Debug.on) Debug.ball_clr_override = new Color(255, 84, 0);
		}

		goal_scored = false;
		return false;
	}
}
