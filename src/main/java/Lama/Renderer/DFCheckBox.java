package Lama.Renderer;

import Lama.Point;
import Lama.PointI;
import com.jogamp.opengl.GL2;

import java.awt.Color;

public class DFCheckBox extends DFUIElement {
	private final Color background;
	private final Color font_color;
	public boolean state;
	public String label;

	public DFCheckBox(DebugFormGL form, PointI pos, int width, int height, String label, boolean default_state, Color background, Color font) {
		super(form, pos, width, height);
		this.state = default_state;
		this.label = label;
		this.background = background;
		this.font_color = font;
	}

	@Override
	public void render() {
		form.gl.glPushMatrix();
		form.gl.glLoadIdentity();
		form.gl.glTranslated(0, 0, -0.15);

		form.gl.glColor4d( background.getRed() / 255.0, background.getGreen() / 255.0, background.getBlue() / 255.0, background.getAlpha() / 255.0);

		form.gl.glBegin(GL2.GL_QUADS);

		form.gl.glVertex2i(pos.x, pos.y);
		form.gl.glVertex2i(pos.x + width, pos.y);
		form.gl.glVertex2i(pos.x + width, pos.y + height);
		form.gl.glVertex2i(pos.x, pos.y + height);

		form.gl.glEnd();

		Point cb_tl = pos.toPoint().add(height * 0.2, height * 0.2);
		Point cb_rb = pos.toPoint().add(height * 0.8, height * 0.8);

		form.gl.glColor4d(font_color.getRed() / 255.0, font_color.getGreen() / 255.0, font_color.getBlue() / 255.0, font_color.getAlpha() / 255.0);

		form.gl.glBegin(GL2.GL_QUADS);

		form.gl.glVertex2d(cb_tl.x, cb_tl.y);
		form.gl.glVertex2d(cb_rb.x, cb_tl.y);
		form.gl.glVertex2d(cb_rb.x, cb_rb.y);
		form.gl.glVertex2d(cb_tl.x, cb_rb.y);

		form.gl.glEnd();

		form.textUI(new Point(cb_rb.x + 10, cb_tl.y + height/2.0), label, font_color, 0.4, null);

		if (state) {
			cb_tl = pos.toPoint().add(height * 0.35, height * 0.35);
			cb_rb = pos.toPoint().add(height * 0.65, height * 0.65);

//			form.gl.glColor4d(background.getRed() / 255.0, background.getGreen() / 255.0, background.getBlue() / 255.0, background.getAlpha() / 255.0);
			form.gl.glColor4d(0.2, 0.5, 0.0, 1.0);

			form.gl.glBegin(GL2.GL_QUADS);

			form.gl.glVertex2d(cb_tl.x, cb_tl.y);
			form.gl.glVertex2d(cb_rb.x, cb_tl.y);
			form.gl.glVertex2d(cb_rb.x, cb_rb.y);
			form.gl.glVertex2d(cb_tl.x, cb_rb.y);

			form.gl.glEnd();
		}

		form.gl.glPopMatrix();
	}

	@Override
	public void click(PointI relative_pos) {
//		if (Debug.on) Debug.echo("CLICCCCCC");
		this.state = !this.state;
	}
}
