package Lama.Renderer;

import Lama.HMath;
import Lama.Point;
import Lama.PointI;
import com.jogamp.opengl.GL2;

import java.awt.*;

public class DFSlider extends DFUIElement {
	public double max_value;
	public double slider_pos;
	private Color background;
	private Color color;
	private double slider_width;
	private double handler_radius;
	private PointI handler_pos;

	public DFSlider(DebugFormGL form, PointI pos, int width, int height, double slider_width, double handler_rad, Color color_, Color background) {
		super(form, pos, width, height);
		this.color = color_;
		this.slider_width = slider_width;
		this.handler_radius = handler_rad;
		this.background = background;
		this.max_value = 0;
		this.slider_pos = 0;
	}

	public void setMax(int max_val) {
		if (this.slider_pos == this.max_value) this.slider_pos = max_val;
		this.max_value = max_val;

		PointI s_tl = pos.add((int) (width * 0.05), (int) (height * 0.5 - slider_width * 0.5));
		PointI s_rb = pos.add((int) (width * 0.95), (int) (height * 0.5 + slider_width * 0.5));
		PointI s = new PointI((int)(s_tl.x + (slider_pos / max_value) * (s_rb.x - s_tl.x)), (int)((s_rb.y + s_tl.y) / 2.0));

		this.handler_pos = s;
	}

	@Override
	public void render() {
		form.gl.glPushMatrix();
		form.gl.glLoadIdentity();
		form.gl.glTranslated(0, 0, -0.11);

		setColor(background);
		form.gl.glBegin(GL2.GL_QUADS);

		form.gl.glVertex2i(pos.x, pos.y);
		form.gl.glVertex2i(pos.x + width, pos.y);
		form.gl.glVertex2i(pos.x + width, pos.y + height);
		form.gl.glVertex2i(pos.x, pos.y + height);

		form.gl.glEnd();

		setColor(color);
		form.gl.glBegin(GL2.GL_QUADS);

		PointI s_tl = pos.add((int) (width * 0.05), (int) (height * 0.5 - slider_width * 0.5));
		PointI s_rb = pos.add((int) (width * 0.95), (int) (height * 0.5 + slider_width * 0.5));

		form.gl.glVertex2d(s_tl.x, s_tl.y);
		form.gl.glVertex2d(s_rb.x, s_tl.y);
		form.gl.glVertex2d(s_rb.x, s_rb.y);
		form.gl.glVertex2d(s_tl.x, s_rb.y);

		form.gl.glEnd();

		int i;
		int lod = 16;

		double TWO_PI = 2.0f * Math.PI;

		form.gl.glBegin(GL2.GL_TRIANGLE_FAN);
		form.gl.glVertex2d(handler_pos.x, handler_pos.y); // center of circle

		for (i = 0; i <= lod; i++) {
			form.gl.glVertex2d(
				handler_pos.x + (handler_radius * Math.cos(i * TWO_PI / lod)),
				handler_pos.y + (handler_radius * Math.sin(i * TWO_PI / lod))
			);
		}
		form.gl.glEnd();

		form.gl.glPopMatrix();
	}

	@Override
	public void click(PointI relative_pos) {
//		if (DebugBase.on) DebugBase.echo("CLICCCCCC");
//		this.state = !this.state;
	}

	@Override
	public void mousedown(PointI relative_pos) {
		if (relative_pos.distanceTo(handler_pos) <= handler_radius) {
			super.mousedown(relative_pos);
		}
	}

	@Override
	public void mouseup(PointI relative_pos) {
		super.mouseup(relative_pos);
	}

	@Override
	public void mousedrag(PointI relative_pos) {
		int x = (int)HMath.clamp(relative_pos.x, 0.05 * width, 0.95 * width);
		handler_pos.x = x;
		double w = ((double)x - 0.05 * (double)width) / ((double)width * 0.9);

		slider_pos = w * max_value;
//		DebugBase.echo(">> " + x + " >> " + w + " >> " + slider_pos);

		super.mousedrag(relative_pos);
	}

	public void setDisplayedTick(int displayed_tick) {
		this.slider_pos = displayed_tick;

		PointI s_tl = pos.add((int) (width * 0.05), (int) (height * 0.5 - slider_width * 0.5));
		PointI s_rb = pos.add((int) (width * 0.95), (int) (height * 0.5 + slider_width * 0.5));
		PointI s = new PointI((int)(s_tl.x + (slider_pos / max_value) * (s_rb.x - s_tl.x)), (int)((s_rb.y + s_tl.y) / 2.0));

		this.handler_pos = s;
	}
}
