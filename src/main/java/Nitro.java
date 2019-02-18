import Lama.V3;
import model.NitroPack;

public class Nitro extends V3 {
	double radius;
	Integer respawn;

	public Nitro(NitroPack nitro_pack) {
		super(nitro_pack.x, nitro_pack.y, nitro_pack.z);
		respawn = null;
		radius = 0.5;
	}

	public Nitro(Nitro n) {
		super(n.x, n.y, n.z);
		respawn = n.respawn;
		radius = 0.5;
	}

	public void updateFromNitroPack(NitroPack nitro_pack) {
		this.respawn = nitro_pack.respawn_ticks;
	}

	@Override
	public Nitro copy() {
		return new Nitro(this);
	}
}
