package Lama.Renderer;

import Lama.Point;
import Lama.PointI;
import com.jogamp.opengl.GL2;

import java.awt.*;
import java.util.ArrayList;

public class DFMemo extends DFUIElement {
	public Color background;
	public Color color;
	public double scale;
	public double line_height;
	public ArrayList<String> txt;

	public DFMemo(DebugFormGL form, PointI pos, int width, int height, Color bg, Color font, double scale_, double line_height_) {
		super(form, pos, width, height);
		background = bg;
		color = font;
		scale = scale_;
		line_height = line_height_;
		txt = new ArrayList<>();
	}

	public void setText(ArrayList<String> txt_) {
		txt = txt_;
	}

	public void append(String str) {
		txt.add(str);
	}

	public void clear() {
		txt = new ArrayList<>();
	}

	@Override
	public void render() {
		form.gl.glPushMatrix();
		form.gl.glLoadIdentity();
		form.gl.glTranslated(0, 0, -0.11);

		if (background != null) {
			setColor(background);
			form.gl.glBegin(GL2.GL_QUADS);

			form.gl.glVertex2i(pos.x, pos.y);
			form.gl.glVertex2i(pos.x + width, pos.y);
			form.gl.glVertex2i(pos.x + width, pos.y + height);
			form.gl.glVertex2i(pos.x, pos.y + height);

			form.gl.glEnd();
		}

		Point cp = pos.toPoint().add(15, 10);

		if (txt != null) {
			for (String s : txt) {
				form.textUI(cp, s.trim(), color, scale, null);
				cp.Add(0, line_height);
			}
		}

		form.gl.glPopMatrix();
	}
}
