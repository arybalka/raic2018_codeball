package Lama.Renderer;

import Lama.V3;

public abstract class GLCamBase {
	public V3 pos;
	public V3 view;
	public V3 top;

	public GLCamBase(V3 pos, V3 view) {
		this.pos = pos;
		this.view = view;
		this.top = new V3(0, 0, 1);
	}
}
