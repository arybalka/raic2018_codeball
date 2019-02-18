package Lama.Renderer;

import Lama.HMath;
import Lama.PointI;
import Lama.Renderer.DebugFormGL;

import java.awt.*;

public abstract class DFUIElement {
	public DebugFormGL form;
	public PointI pos;
	public int width;
	public int height;
	public boolean m_left_down = false;
	public boolean visible = true;

	public DFUIElement(DebugFormGL form, PointI pos, int width, int height) {
		this.form = form;
		this.pos = pos;
		this.width = width;
		this.height = height;
	}

	public abstract void render();
	public void click(PointI relative_pos) {}
	public void mousedown(PointI relative_pos) {
		m_left_down = true;
	}
	public void mouseup(PointI relative_pos) {
		m_left_down = false;
	}
	public void mousedrag(PointI relative_pos) {}

	public boolean isInside(PointI pos) {
		return HMath.pointInsideRect(this.pos.toPoint(), this.pos.add(width, height).toPoint(), pos.toPoint());
	}

	protected void setColor(Color clr) {
		form.gl.glColor4d( clr.getRed() / 255.0, clr.getGreen() / 255.0, clr.getBlue() / 255.0, clr.getAlpha() / 255.0);
	}
}
