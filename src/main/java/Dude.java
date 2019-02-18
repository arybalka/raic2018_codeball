import Lama.V3;
import model.Robot;

import java.util.ArrayList;
import java.util.HashSet;

public class Dude extends Entity {
	public int id;
	public byte dude_id;
	public boolean is_mate;
	public double nitro;
	public boolean[] picked_nitro;
	public boolean touch;
	public V3 normal;
	public Act action = null;
	public boolean touch_ball;
	public boolean flag = false;
	public int last_touched_ball = -1;

	public Dude(Robot robot_, byte dude_id_) {
		super(new V3(robot_.x, robot_.y, robot_.z), new V3(robot_.velocity_x, robot_.velocity_y, robot_.velocity_z), robot_.radius);
		mass = CFG.ROBOT_MASS;
		arena_e = CFG.ROBOT_ARENA_E;
		id = robot_.id;
		is_mate = robot_.is_teammate;
		nitro = robot_.nitro_amount;
		picked_nitro = new boolean[] { false, false, false, false };
		touch = robot_.touch;
		touch_ball = false;
		last_touched_ball = -1;
		normal = touch ? new V3(robot_.touch_normal_x, robot_.touch_normal_y, robot_.touch_normal_z) : V3.zero();
		dude_id = dude_id_;
	}

	public Dude(Dude dude, byte dude_id_) {
		super(dude, dude.vel.copy(), dude.radius);
		mass = CFG.ROBOT_MASS;
		arena_e = CFG.ROBOT_ARENA_E;
		touch_ball = dude.touch_ball;
		last_touched_ball = dude.last_touched_ball;
		dude_id = dude_id_;
		id = dude.id;
		is_mate = dude.is_mate;
		nitro = dude.nitro;
		picked_nitro = dude.picked_nitro.clone();
		touch = dude.touch;
		normal = dude.normal != null ? dude.normal.copy() : null;
		flag = false;
	}

	public void updateFromRobot(Robot robot_, TheBall ball) {
		if (last_pos == null) {
		last_pos = pos();
		} else {
			last_pos.x = x;
			last_pos.y = y;
			last_pos.z = z;
		}

		x = robot_.x;
		y = robot_.y;
		z = robot_.z;

		vel.x = robot_.velocity_x;
		vel.y = robot_.velocity_y;
		vel.z = robot_.velocity_z;

		touch = robot_.touch;
		normal.x = touch ? robot_.touch_normal_x : 0;
		normal.y = touch ? robot_.touch_normal_y : 0;
		normal.z = touch ? robot_.touch_normal_z : 0;
		touch_ball = ball.distanceLessEq(this, radius + ball.radius + 0.15);
		if (touch_ball) last_touched_ball = Glob.tick;

		radius = robot_.radius;
		id = robot_.id;
		is_mate = robot_.is_teammate;
		nitro = (double)robot_.nitro_amount;
		picked_nitro = new boolean[] { false, false, false, false };
	}

	@Override
	public Dude copy() {
		Dude d = new Dude(this, dude_id);
		d.last_pos = last_pos != null ? last_pos.copy() : null;

		return d;
	}

	public Dude copyFull() {
		Dude d = new Dude(this, dude_id);
		d.last_pos = last_pos != null ? last_pos.copy() : null;
		d.flag = flag;
		d.action = action == null ? null : action.copy();
		d.touch_ball = touch_ball;

		return d;
	}

	@Override
	public String toString() {
		return "D" + id + (is_mate ? "+" : "-") + pos() +
			   " /vel: " + vel + "=" + vel.len() +
			   ", n: " + nitro +
			   ", lp: " + last_pos +
			   ", r: " + radius +
			   ", ltb: " + last_touched_ball +
			   ", did: " + dude_id +
			   ", touch: " + (touch ? "+" : "-") + (touch_ball ? "+" : "-") +
			   ", normal: " + normal +
			   ", action: " + action +
			   '/';
	}

	public void Sim(Act act, TheBall ball, double delta_time) {
		action = act;
		if (action.use_nitro && (nitro <= 0 || touch)) action.use_nitro = false;
		last_pos = pos();
		Solver.simulateSingleDude(this, ball, delta_time);
	}

	public void SimWithBall(Act act, TheBall ball) {
		action = act;
		if (action.use_nitro && (nitro <= 0 || touch)) action.use_nitro = false;
		last_pos = pos();
		Solver.simulateSingleDudeAndBall(this, ball);
	}

	public void Sim(Act act, TheBall ball) {
		Sim(act, ball, CFG.DT);
	}

	public Dude sim(Act act, TheBall ball, double delta_time) {
		Dude d = this.copy();
		d.Sim(act, ball, delta_time);
		return d;
	}

	public Dude sim(Act act, TheBall ball) {
		return sim(act, ball, CFG.DT);
	}

	public void Act(Act act_) {
		action = act_;
		if (action.use_nitro && (nitro <= 0 || touch)) action.use_nitro = false;
	}

	public Dude act(Act act_) {
		action = act_;
		if (action.use_nitro && (nitro <= 0 || touch)) action.use_nitro = false;
		return this;
	}

	public HashSet<Dude> asArrayList() {
		HashSet<Dude> result = new HashSet<>();
		result.add(this);
		return result;
	}

	public Dude[] asArray() {
		return new Dude[] {this};
	}

	public void updateFromDude(Dude dude) {
		updateFromDude(dude, dude.action);
	}

	public void updateFromDude(Dude dude, Act act) {
		if (last_pos == null) {
			last_pos = pos();
		} else {
			last_pos.x = x;
			last_pos.y = y;
			last_pos.z = z;
		}

		x = dude.x;
		y = dude.y;
		z = dude.z;

		vel.x = dude.vel.x;
		vel.y = dude.vel.y;
		vel.z = dude.vel.z;

		touch = dude.touch;
		if (dude.normal != null) {
			normal.x = dude.normal.x;
			normal.y = dude.normal.y;
			normal.z = dude.normal.z;
		} else {
			normal = null;
		}

		touch_ball = dude.touch_ball;

		radius = dude.radius;
		id = dude.id;
		is_mate = dude.is_mate;
		nitro = dude.nitro;
		action = act;
		flag = false;
	}
}
