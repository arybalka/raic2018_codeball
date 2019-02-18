import Lama.V3;
import model.Ball;

import java.awt.*;

public class TheBall extends Entity {
	boolean touch = false;
	boolean had_collision_with_enemy = false;

	public TheBall(Ball ball_) {
		super(new V3(ball_.x, ball_.y, ball_.z), new V3(ball_.velocity_x, ball_.velocity_y, ball_.velocity_z), ball_.radius);
		mass = CFG.BALL_MASS;
		arena_e = CFG.BALL_ARENA_E;
	}

	public TheBall(TheBall ball_) {
		super(ball_, ball_.vel.copy(), ball_.radius);
		mass = CFG.BALL_MASS;
		arena_e = CFG.BALL_ARENA_E;
		touch = ball_.touch;
		had_collision_with_enemy = ball_.had_collision_with_enemy;
	}

	public void updateFromBall(Ball ball_) {
		x = ball_.x;
		y = ball_.y;
		z = ball_.z;

		vel = new V3(ball_.velocity_x, ball_.velocity_y, ball_.velocity_z);
		radius = ball_.radius;
		touch = false;
		had_collision_with_enemy = false;
	}

	@Override
	public TheBall copy() {
		TheBall b = new TheBall(this);
		b.last_pos = last_pos;

		return b;
	}

	public TheBall copyFull() {
		TheBall b = new TheBall(this);
		b.last_pos = last_pos;
		b.touch = touch;
		b.had_collision_with_enemy = had_collision_with_enemy;

		return b;
	}

	public void updateFromTheBall(TheBall ball) {
		x = ball.x;
		y = ball.y;
		z = ball.z;
		radius = ball.radius;
		touch = ball.touch;
		had_collision_with_enemy = ball.had_collision_with_enemy;

		if (vel == null) {
			vel = ball.vel.copy();
		} else {
			vel.x = ball.vel.x;
			vel.y = ball.vel.y;
			vel.z = ball.vel.z;
		}

		if (last_pos != null && ball.last_pos != null) {
			last_pos.x = ball.last_pos.x;
			last_pos.y = ball.last_pos.y;
			last_pos.z = ball.last_pos.z;
		} else if (ball.last_pos != null) {
			last_pos = ball.last_pos.copy();
		} else {
			last_pos = null;
		}
	}

	public void Sim() {
		last_pos = pos();
		Solver.move(this, CFG.DT);
		Solver.collideWithArena(this);
	}
}
