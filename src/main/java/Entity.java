import Lama.Sphere;
import Lama.V3;

import java.awt.*;

public class Entity extends Sphere {
	public V3 vel;
	double mass = 0;
	double radius_change_speed = 0;
	double arena_e = 0;

	public V3 last_pos = null;

	public Entity(V3 pos_, V3 vel_, double radius_) {
		super(pos_, radius_);
		vel = vel_;
	}

	public void updateLastPos() {
		if (last_pos == null) {
			last_pos = pos();
		} else {
			last_pos.x = x;
			last_pos.y = y;
			last_pos.z = z;
		}
	}

	@Override
	public String toString() {
		return "Entity{" +
			   "pos=" + this.pos() +
			   "vel=" + vel + " (" + vel.len() + ")" +
			   ", m=" + mass +
			   '}';
	}
}
