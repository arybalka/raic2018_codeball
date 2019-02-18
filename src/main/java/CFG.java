public class CFG {
	public static final int TICKS_PER_SECOND = 60;
//	public static final double MICROTICKS_PER_TICK = 100;
	public static final double MICROTICKS_PER_TICK = 1;
	public static boolean HAS_NITRO = false;
	public static double ACCURATE_MICROTICKS = 100;
	public static final double DELTA_TIME = 1.0 / TICKS_PER_SECOND;
	public static final double DT = DELTA_TIME / MICROTICKS_PER_TICK;
	public static final double ROBOT_MIN_RADIUS = 1;
	public static final double ROBOT_MAX_RADIUS = 1.05;
	public static final double ROBOT_MAX_JUMP_SPEED = 15;
	public static final double ROBOT_ACCELERATION = 100;
	public static final double ROBOT_NITRO_ACCELERATION = 30;
	public static final double ROBOT_MAX_GROUND_SPEED = 30;
	public static final double ROBOT_ARENA_E = 0;
	public static final double ROBOT_RADIUS = 1;
	public static final double ROBOT_MASS = 2;
	public static final int RESET_TICKS = 2 * TICKS_PER_SECOND;
	public static final double BALL_ARENA_E = 0.7;
	public static final double BALL_RADIUS = 2;
	public static final double BALL_MASS = 1;
	public static final double MAX_ENTITY_SPEED = 100;
	public static final double MAX_NITRO_AMOUNT = 100;
	public static final double START_NITRO_AMOUNT = 50;
	public static final double NITRO_POINT_VELOCITY_CHANGE = 0.6;
	public static final double NITRO_PACK_X = 20;
	public static final double NITRO_PACK_Y = 1;
	public static final double NITRO_PACK_Z = 30;
	public static final double NITRO_PACK_RADIUS = 0.5;
	public static final double NITRO_PACK_AMOUNT = 100;
	public static final int NITRO_RESPAWN_TICKS = 10 * TICKS_PER_SECOND;
	public static final double GRAVITY = 30;

	public static final double MIN_HIT_E = 0.4;
	public static final double MAX_HIT_E = 0.5;
	public static final double HIT_E = (CFG.MIN_HIT_E + CFG.MAX_HIT_E) / 2.0;
//	public static final double HIT_E = CFG.MAX_HIT_E;
//	public static final double HIT_E = CFG.MIN_HIT_E + (CFG.MIN_HIT_E + CFG.MAX_HIT_E) * 0.66;

	public static final double JUMP_HEIGHT_APPROX = 4.8;
	public static final boolean KEEP_DISTANCE_FROM_FRIENDS = true;

	public static boolean RULES_FINAL;
	public static boolean FINAL;

	public static final double HALF_WIDTH = 30.0;
	public static final double HALF_HEIGHT = 10.0;
	public static final double HALF_DEPTH = 40.0;
	public static final double HALF_GOAL_DEPTH = 5.0;
	public static final double HALF_GOAL_WIDTH = 15.0;
	public static final double HALF_GOAL_HEIGHT = 5.0;
}
