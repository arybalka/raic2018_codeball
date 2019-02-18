import Lama.V3;
import model.Action;

import java.util.Objects;

public class Act {
	public V3 vel;
	public double jump_speed = 0;
	public boolean use_nitro = false;
	public boolean accurate = false;

	public Dude dude_applied = null;
	public V3 ball_applied = null;

	public Act(V3 vel, double jump_speed, boolean use_nitro) {
		this.vel = vel;
		this.jump_speed = jump_speed;
		this.use_nitro = use_nitro;
	}

	public Act() {
		this.vel = V3.zero();
		this.jump_speed = 0;
		this.use_nitro = false;
	}

	public Act(V3 vel_) {
		this.vel = vel_;
		this.jump_speed = 0;
		this.use_nitro = false;
	}

	public Act(V3 vel_, double jump_speed_) {
		this.vel = vel_;
		this.jump_speed = jump_speed_;
		this.use_nitro = false;
	}

	public Act(V3 vel_, boolean use_nitro_) {
		this.vel = vel_;
		this.jump_speed = 0;
		this.use_nitro = use_nitro_;
	}

	public Act(Action action) {
		this.vel = new V3(action.target_velocity_x, action.target_velocity_y, action.target_velocity_z);
		this.jump_speed = action.jump_speed;
		this.use_nitro = action.use_nitro;
	}

	public Act Go(V3 dir) {
		vel = dir.copy();
		return this;
	}

	public Act Jump() {
		return Jump(CFG.ROBOT_MAX_JUMP_SPEED);
	}

	public Act Jump(double speed) {
		jump_speed = speed;
		return this;
	}

	public Act Nitro() {
		return Nitro(true);
	}

	public Act Nitro(boolean use) {
		use_nitro = use;
		return this;
	}

	public Act Implement(Dude dude) {
		Agent.assignTurn(dude, this);
		return this;
	}

	@Override
	public String toString() {
		return "Act" + vel +
			   ", j: " + jump_speed +
			   ", n: " + use_nitro;
	}

	public Act copy() {
		Act a = new Act(vel, jump_speed, use_nitro);
		a.dude_applied = dude_applied != null ? dude_applied.copy() : null;
		a.ball_applied = ball_applied != null ? ball_applied.copy() : null;
		a.accurate = accurate;
		return a;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Act act = (Act) o;
		return Double.compare(act.jump_speed, jump_speed) == 0 &&
			   use_nitro == act.use_nitro &&
			   Objects.equals(vel, act.vel);
	}

	@Override
	public int hashCode() {

		return Objects.hash(vel, jump_speed, use_nitro);
	}
}
