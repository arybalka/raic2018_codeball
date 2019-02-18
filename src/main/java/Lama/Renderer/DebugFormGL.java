package Lama.Renderer;

import Lama.HMath;
import Lama.Point;
import Lama.V3;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by lamik on 10.09.14.
 */
public abstract class DebugFormGL extends JFrame implements GLEventListener {
	protected static final long serialVersionUID = 1L;
	public static double zoom;
	public static int displayed_tick = 0;
	public int x_offset;
	public int y_offset;
	public boolean on_second_screen;
	public boolean on_right_side;
	public int width;
	public int height;
	public Integer wnd_x;
	public Integer wnd_y;
	public boolean mouse_event = false;
	public boolean render_bypass = true;
	public int last_rendered_frame = 0;
	public int prolong = 1;
	//	protected static Vector<DebugElem> debug_elements_to_draw;
//	protected static Vector<DebugElem> debug_elements_to_draw;
	public HashSet<DebugElem> debug_elements_to_draw;
	public Color bg_color;
	public boolean painting = false;
	public GLIsoCamBase cam;
	public ArrayList<DFUIElement> ui;
	public GL2 gl;
	public GLU glu;
	public GLUT glut;
	public CopyOnWriteArrayList<DebugElem> elements_to_remove = new CopyOnWriteArrayList<>();
	public GLCanvas canvas;
	public TextRenderer txt;
	public Point viewport_top_left = new Point(-x_offset / zoom, -y_offset / zoom);
	public Point viewport_right_bottom = viewport_top_left.add(width / zoom, height / zoom);
	protected int text_lines_count = 0;
	protected int last_repaint_frame = 0;
	protected int base_shader_program;
	protected int base_vp;
	protected int base_fp;
	boolean last_frame_rendered = false;

	public DebugFormGL(RendererConfig cfg) {
		super(cfg.wnd_title);
		width = cfg.wnd_width;
		height = cfg.wnd_height;
		wnd_x = cfg.wnd_x;
		wnd_y = cfg.wnd_y;
		x_offset = cfg.x_offset;
		y_offset = cfg.y_offset;
		on_right_side = cfg.on_right_side;
		on_second_screen = cfg.on_second_screen;
		bg_color = cfg.bg_color;
		zoom = cfg.zoom;

		GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setStencilBits(8);
		canvas = new GLCanvas(capabilities);
		canvas.addGLEventListener(this);
		this.getContentPane().add(canvas);

		setSize(width, height + 30);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		GraphicsDevice screen = on_second_screen ? screens[screens.length - 1] : screens[0];

		if (wnd_x == null) {
			wnd_x = on_right_side ?
				screen.getDefaultConfiguration().getBounds().x + screen.getDefaultConfiguration().getBounds().width - width - 20
				: screen.getDefaultConfiguration().getBounds().x + screen.getDefaultConfiguration().getBounds().width / 2 - width / 2;
		}

		if (wnd_y == null) {
			wnd_y = on_right_side ? screen.getDefaultConfiguration().getBounds().y
				: screen.getDefaultConfiguration().getBounds().y + screen.getDefaultConfiguration().getBounds().height / 2 - height / 2;
		}

		setLocation(wnd_x, wnd_y);

//		panel = new DebugPanel();
//		add(panel);

		canvas.requestFocusInWindow();
		debug_elements_to_draw = new HashSet<>();

		ui = new ArrayList<>();
	}

	public static int dTick() {
		return displayed_tick - 1;
	}

	public void bindCamera(GLIsoCamBase camera) {
		cam = camera;
	}

	protected int loadShader(int type, String path) {
		int shader = gl.glCreateShader(type);

		try {
			String[] sources = new String[1];
			int[] sources_lengths = new int[1];

			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			String src = new String(data, "UTF-8");
			sources[0] = src;
			sources_lengths[0] = src.length();

			gl.glShaderSource(shader, 1, sources, sources_lengths, 0);
			gl.glCompileShader(shader);

			int[] compiled = new int[1];
			gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);

			if (compiled[0] != 0) {
				System.out.println("shader compiled");
			} else {
				int[] logLength = new int[1];
				gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

				byte[] log = new byte[logLength[0]];
				gl.glGetShaderInfoLog(shader, logLength[0], (int[]) null, 0, log, 0);

				System.err.println("Error compiling shader: " + src + " -> " + new String(log));
				System.exit(1);
			}

		} catch (Exception e) {
			System.err.println("Could not read vertex shader.");
			e.printStackTrace();
			System.exit(-1);
		}

		return shader;
	}

	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();

		gl.glClearColor((float) bg_color.getRed() / 255.0f, (float) bg_color.getGreen() / 255.0f, (float) bg_color.getBlue() / 255.0f, 0);
		gl.glClearDepth(1.0f);      // set clear depth value to farthest
		gl.glEnable(GL2.GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
		gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting

		gl.glEnable(GL2.GL_LINE_SMOOTH);
//		gl.glEnable(GL2.GL_POLYGON_SMOOTH);
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glEnable(GL2.GL_MULTISAMPLE);

		gl.glClearStencil(0x0);
		gl.glClearDepth(1.0f);                         // 0 is near, 1 is far
		gl.glDepthFunc(GL2.GL_LEQUAL);


		base_vp = loadShader(GL2.GL_VERTEX_SHADER, "assets/base_shader_vertex.glsl");
		base_fp = loadShader(GL2.GL_FRAGMENT_SHADER, "assets/base_shader_fragment.glsl");

		base_shader_program = gl.glCreateProgram();
		gl.glAttachShader(base_shader_program, base_vp);
		gl.glAttachShader(base_shader_program, base_fp);
		gl.glLinkProgram(base_shader_program);
		gl.glValidateProgram(base_shader_program);

		gl.glUseProgram(base_shader_program);

		txt = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));
	}

	public void dispose(GLAutoDrawable drawable) {
	}

	public void setColor(Color color) {
		gl.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
	}

	public double coordX(double c) {
		return DebugElem.fitX(c);
	}

	public double coordY(double c) {
		return DebugElem.fitY(c);
	}

	public double coordZ(double c) {
		return DebugElem.fitZ(c);
	}

	public double size(double c) {
		return c * zoom;
	}

	protected void lineS3(V3 p1, V3 p2, int width, Color color) {
		color(color);
		lineS3(p1, p2, width);
	}

	protected void lineS3(V3 p1, V3 p2, int width, double r, double g, double b) {
		color(r, g, b);
		lineS3(p1, p2, width);
	}

	protected void line3(V3 p1, V3 p2, int width, Color color_) { line3(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, width, color_); }

	protected void line3(V3 p1, V3 p2, int width) { line3(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, width, null); }

	protected void lineS3(V3 p1, V3 p2, int width) { line3(coordX(p1.x), coordY(p1.y), coordZ(p1.z), coordX(p2.x), coordY(p2.y), coordZ(p2.z), width, null); }

	protected void lineS3(V3 p1, V3 p2) { line3(coordX(p1.x), coordY(p1.y), coordZ(p1.z), coordX(p2.x), coordY(p2.y), coordZ(p2.z)); }

	protected void line2(double x1, double y1, double x2, double y2) { line3(x1, y1, 0, x2, y2, 0, 1, null); }

	protected void line3(double x1, double y1, double z1, double x2, double y2, double z2) { line3(x1, y1, z1, x2, y2, z2, 1, null); }

	protected void line3(double x1, double y1, double z1, double x2, double y2, double z2, int width, Color color_) {
//		if (!isPointVisible(new V3(x1, y1, z1)) && !isPointVisible(new V3(x2, y2, z2))) return;

		if (color_ != null) color(color_);

		gl.glLineWidth(width);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3d(x1, y1, z1);
		gl.glVertex3d(x2, y2, z2);
		gl.glEnd();
	}

	protected void rectS2(V3 tl, double w, double h) { rect2(coordX(tl.x), coordY(tl.y), size(w), size(h)); }

	protected void rectS2(V3 tl, V3 rb) { rect2(coordX(tl.x), coordY(tl.y), size(rb.x - tl.x), size(rb.y - tl.y)); }

	protected void rectS2(V3 tl, V3 rb, boolean contour_only) { rect2(coordX(tl.x), coordY(tl.y), size(rb.x - tl.x), size(rb.y - tl.y), contour_only); }

	protected void rect2(double x, double y, double w, double h) { rect2(x, y, w, h, false); }

	protected void rect2(double x, double y, double w, double h, boolean contour_only) { rect3(x, y, 0, w, h, contour_only); }

	protected void rect3(double x, double y, double z, double w, double h, boolean contour_only) {
		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_QUADS);

		gl.glVertex3d(x, y, z);
		gl.glVertex3d(x + w, y, z);
		gl.glVertex3d(x + w, y + h, z);
		gl.glVertex3d(x, y + h, z);

		gl.glEnd();
	}

	protected void rectRotated2(double x, double z, double w, double h, double angle, boolean contour_only) {
		rectRotated3(x, 0, z, w, h, angle, contour_only);
	}

	protected void rectRotated3(double x, double y, double z, double w, double h, double angle, boolean contour_only) {
		gl.glPushMatrix();
		gl.glRotated(angle, 0, 0, 1);
		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_QUADS);

		gl.glVertex3d(x, y, z);
		gl.glVertex3d(x + w, y, z);
		gl.glVertex3d(x + w, y, z + h);
		gl.glVertex3d(x, y, z + h);

		gl.glEnd();
		gl.glPopMatrix();
	}

	protected void circleS3(V3 p, double radius, Color color) {
		color(color);
		circleS3(p, radius);
	}

	protected void circleS3(V3 p, double radius, double r, double g, double b) {
		color(r, g, b);
		circleS3(p, radius);
	}

	protected void circleS3(V3 p, double radius, boolean contour_only, Color color) {
		color(color);
		circleS3(p, radius, contour_only);
	}

	protected void circleS3(V3 p, double radius, boolean contour_only, double r, double g, double b) {
		color(r, g, b);
		circleS3(p, radius, contour_only);
	}

	protected void circleS3(V3 p, double radius) { circleS3(p, radius, false); }

	protected void circleS3(V3 p, double radius, boolean contour_only) { circle3(coordX(p.x), coordY(p.y), coordZ(p.z), size(radius), contour_only); }

	protected void circle2(double x, double y, double radius) { circle3(x, y, 0, radius, false); }

	protected void circle2(double x, double y, double radius, boolean contour_only) { circle3(x, y, 0, radius, contour_only); }

	protected void circle3(V3 pos, double radius, Color clr, boolean contour_only) {
		circle3(pos.x, pos.y, pos.z, radius, clr, contour_only);
	}

	protected void circle3(double x, double y, double z, double radius, Color clr, boolean contour_only) {
		color(clr);
		circle3(x, y, z, radius, contour_only);
	}

	protected void circle3(double x, double y, double z, double radius, boolean contour_only) {
//		if (!isCircleVisible(new V3(x, y, 0), radius)) return;

		int i;
		int lod = (int) HMath.clamp(((24 + (int) Math.ceil(radius / 15))) * (1.0 * radius) * zoom, 5, 30); //# of triangles used to draw circle

		//GLfloat radius = 0.8f; //radius
		double TWO_PI = 2.0f * Math.PI;

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_TRIANGLE_FAN);
		if (!contour_only) gl.glVertex3d(x, y, z); // center of circle

		for (i = 0; i <= lod; i++) {
			gl.glVertex3d(
				x + (radius * Math.cos(i * TWO_PI / lod)),
				y,
				z + (radius * Math.sin(i * TWO_PI / lod))
			);
		}
		gl.glEnd();
	}

	protected void sphere(V3 pos, double radius) {
		sphere(pos.x, pos.y, pos.z, radius);
	}

	protected void sphere(double x, double y, double z, double radius) {
//		if (!isCircleVisible(new V3(x, y, 0), radius)) return;

		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glTranslated(x, y, z);
		glut.glutSolidSphere(radius, (int) (HMath.clamp(16.0 * radius, 8, 16)), (int) (HMath.clamp(12.0 * radius, 6, 12)));
		gl.glPopMatrix();
	}

	protected void squareS3(V3 tl, V3 rb, boolean contour_only, Color color) {
		color(color);
		squareS2(tl, rb, contour_only);
	}

	protected void squareS3(V3 tl, V3 rb, boolean contour_only, double r, double g, double b) {
		color(r, g, b);
		squareS2(tl, rb, contour_only);
	}

	protected void squareS2(V3 tl, V3 rb, boolean contour_only) {
		square3(DebugElem.fitX(tl.x), DebugElem.fitY(tl.y), 0, DebugElem.fitX(rb.x), DebugElem.fitY(rb.y), 0, contour_only);
	}

	protected void squareS2(double top_left_x, double top_left_y, double bottom_right_x, double bottom_right_y, boolean contour_only) {
		square3(DebugElem.fitX(top_left_x), DebugElem.fitY(top_left_y), 0, DebugElem.fitX(bottom_right_x), DebugElem.fitY(bottom_right_y), 0, contour_only);
	}

	protected void square2(double top_left_x, double top_left_y, double bottom_right_x, double bottom_right_y, boolean contour_only) {
		square3(top_left_x, top_left_y, 0, bottom_right_x, bottom_right_y, 0, contour_only);
	}

	protected void square3(double top_left_x, double top_left_y, double top_left_z, double bottom_right_x, double bottom_right_y, double bottom_right_z, boolean contour_only) {
		double half_z = (top_left_z + bottom_right_z) / 2.0;

		if (top_left_z == 0 && bottom_right_z == 0 && !isQuadVisible(new V3(top_left_x, top_left_y, 0), new V3(bottom_right_x, bottom_right_y, 0))) return;

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_QUADS);
		gl.glVertex3d(top_left_x, top_left_y, top_left_z);
		gl.glVertex3d(bottom_right_x, top_left_y, half_z);
		gl.glVertex3d(bottom_right_x, bottom_right_y, half_z);
		gl.glVertex3d(top_left_x, bottom_right_y, bottom_right_z);
		gl.glEnd();
	}

	protected void squareSF(V3 tl, V3 rb, boolean contour_only, Color color) {
		color(color);
		squareSF(tl, rb, contour_only);
	}

	protected void squareSF(V3 tl, V3 rb, boolean contour_only, double r, double g, double b) {
		color(r, g, b);
		squareSF(tl, rb, contour_only);
	}

	protected void squareSF(V3 tl, V3 rb, boolean contour_only) {
		squareF(DebugElem.fitX(tl.x), DebugElem.fitY(tl.y), DebugElem.fitX(rb.x), DebugElem.fitY(rb.y), contour_only);
	}

	protected void squareSF(double top_left_x, double top_left_y, double bottom_right_x, double bottom_right_y, boolean contour_only) {
		squareF(DebugElem.fitX(top_left_x), DebugElem.fitY(top_left_y), DebugElem.fitX(bottom_right_x), DebugElem.fitY(bottom_right_y), contour_only);
	}

	protected void squareF(double top_left_x, double top_left_y, double bottom_right_x, double bottom_right_y, boolean contour_only) {
		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_QUADS);
		gl.glVertex2d(top_left_x, top_left_y);
		gl.glVertex2d(bottom_right_x, top_left_y);
		gl.glVertex2d(bottom_right_x, bottom_right_y);
		gl.glVertex2d(top_left_x, bottom_right_y);
		gl.glEnd();
	}

	protected void arrowS3(V3 p1, double angle, double length, int line_width, int arrow_width, int arrow_height, Color color) {
		drawArrowLine(p1, angle, size(length), (int) size(line_width), (int) size(arrow_width), (int) size(arrow_height), color);
	}

	protected void arrowS3(V3 p1, V3 p2, int line_width, int arrow_width, int arrow_height, Color color) {
		drawArrowLine(p1, p2, (int) size(line_width), (int) size(arrow_width), (int) size(arrow_height), color);
	}

	protected void circleD2(double x, double y, double size) { circleD3(x, y, 0, size, false); }

	protected void circleD3(double x, double y, double z, double size) { circleD3(x, y, z, size, false); }

	protected void circleD3(double x, double y, double z, double size, boolean contour_only) {
		circle3(x + size / 2, y + size / 2, z, size / 2, contour_only);
	}

	protected void arcD2(double cx, double cy, double r, int start_angle, int arc_angle, int num_segments) {
		arcD3(cx, cy, 0, r, start_angle, arc_angle, num_segments, false);
	}

	protected void arcD3(double cx, double cy, double cz, double r, int start_angle, int arc_angle, int num_segments) {
		arcD3(cx, cy, cz, r, start_angle, arc_angle, num_segments, false);
	}

	protected void arcD3(double cx, double cy, double cz, double r, int start_angle, int arc_angle, int num_segments, boolean contour_only) {
		arc3(cx + r / 2.0, cy + r / 2.0, cz, r / 2.0,
			 HMath.normalizeAngle(-start_angle / 180.0 * Math.PI),
			 HMath.normalizeAngle(-arc_angle / 180.0 * Math.PI),
			 num_segments, contour_only);
	}

	protected void arc2(double cx, double cy, double r, double start_angle, double arc_angle, boolean contour_only) {
		arc3(cx, cy, 0, r, start_angle, arc_angle, 24 + (int) Math.ceil(r / 15), contour_only);
	}

	protected void arc2(double cx, double cy, double r, double start_angle, double arc_angle) {
		arc3(cx, cy, 0, r, start_angle, arc_angle, 24 + (int) Math.ceil(r / 15), false);
	}

	protected void arc2(double cx, double cy, double r, double start_angle, double arc_angle, int num_segments) {
		arc3(cx, cy, 0, r, start_angle, arc_angle, num_segments, false);
	}

	protected void arc3(double cx, double cy, double cz, double r, double start_angle, double arc_angle, boolean contour_only) {
		arc3(cx, cy, cz, r, start_angle, arc_angle, 24 + (int) Math.ceil(r / 15), contour_only);
	}

	protected void arc3(double cx, double cy, double cz, double r, double start_angle, double arc_angle) {
		arc3(cx, cy, cz, r, start_angle, arc_angle, 24 + (int) Math.ceil(r / 15), false);
	}

/*
	public void drawHPCircle(Veh o, Color clr1, Color clr2) {
		drawHPCircle(o, clr1, clr2, 0);
	}

	public void drawHPCircle(Veh o, Color circle_color, Color below_segment_color, int offset) {
		this.setColor(circle_color);
		double w = size(o.radius * 2);
		this.circleD(coordX(o.pos.x - o.radius) + offset, coordY(o.pos.y - o.radius) + offset, w - offset * 2);

		if (o.hp < o.max_hp) {
			this.setColor(below_segment_color);
			this.arcD(coordX(o.pos.x - o.radius) + offset, coordY(o.pos.y - o.radius) + offset,
				w - offset * 2, 90, (int) -Math.ceil(360 - 360 * o.hp / o.max_hp), 24);
		}
	}
*/

	protected void arc2(double cx, double cy, double r, double start_angle, double arc_angle, int num_segments, boolean contour_only) {
		arc3(cx, cy, 0, r, start_angle, arc_angle, num_segments, contour_only);
	}

	protected void arc3(double cx, double cy, double cz, double r, double start_angle, double arc_angle, int num_segments) {
		if (cz == 0 && !isCircleVisible(new V3(cx, cy, cz), r)) return;
		arc3(cx, cy, cz, r, start_angle, arc_angle, num_segments, false);
	}

	protected void arc3(double cx, double cy, double cz, double r, double start_angle, double arc_angle, int num_segments, boolean contour_only) {

		double theta = arc_angle / (double) (num_segments - 1);//theta is now calculated from the arc angle instead, the - 1 bit comes from the fact that the arc is open

		double tangetial_factor = Math.tan(theta);

		double radial_factor = Math.cos(theta);
		double x = r * Math.cos(start_angle);//we now start at the start angle
		double y = r * Math.sin(start_angle);

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_TRIANGLE_FAN);//since the arc is not a closed curve, this is a strip now

		gl.glVertex3d(cx, cy, cz);
		for (int ii = 0; ii < num_segments; ii++) {
			gl.glVertex3d(x + cx, y + cy, cz);

			double tx = -y;
			double ty = x;

			x += tx * tangetial_factor;
			y += ty * tangetial_factor;

			x *= radial_factor;
			y *= radial_factor;
		}

		gl.glEnd();
	}

	public void textS2(V3 pos, String str, Color color, double scale) {
		txt.beginRendering(width, height);
		txt.setColor(color);
		txt.draw3D(str, (float) coordX(pos.x) + x_offset, height - ((float) coordY(pos.y) + y_offset), 0f, (float) scale);
		txt.endRendering();
	}

	public void textUI(Point pos, String str, Color color, double scale) {
		textUI(pos, str, color, scale, null);
	}

	public void textUI(Point pos, String str, Color color, double scale, Color bg) {
		if (bg != null) {
			double w = str.length() * 8.0, h = 20.0, yo = 3.0;

			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glTranslated(0, 0, -0.1f);
			setColor(bg);

			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2d(pos.x, pos.y + yo);
			gl.glVertex2d(pos.x + w, pos.y + yo);
			gl.glVertex2d(pos.x + w, pos.y + yo - h);
			gl.glVertex2d(pos.x, pos.y + yo - h);
			gl.glEnd();

			gl.glPopMatrix();
		}

		txt.beginRendering(width, height);
		txt.setColor(color);
		txt.draw3D(str, (float) pos.x, (float) (height - pos.y), 0f, (float) scale);
		txt.endRendering();
	}

	public void drawHPCircle(V3 pos, double radius, double percent, Color circle_color, Color below_segment_color) {
		this.setColor(circle_color);
		double w = size(radius * 2);
		this.circleD2(coordX(pos.x - radius), coordY(pos.y - radius), w);

		if (percent < 1.0) {
			this.setColor(below_segment_color);
			this.arcD2(coordX(pos.x - radius), coordY(pos.y - radius), w, 90, (int) -Math.ceil(360 - 360 * percent), 24);
		}
	}

	public void drawArrowLine(V3 p1, double angle, double length, double line_width, int arrow_width, int arrow_height, Color color) {
		V3 p2 = p1.aheadXZ(angle, length);
		drawArrowLine(p1, p2, line_width, arrow_width, arrow_height, color);
	}

	public void drawArrowLine(V3 p1, V3 p2, double line_width, double arrow_width, double arrow_height, Color color) {
		if (p1.equals(p2)) return;

		V3 d = p2.sub(p1);
		double length = d.len();

		double xm = length - arrow_width, xn = xm, ym = arrow_height, yn = -arrow_height, x;
		double sin = d.z / length, cos = d.x / length;

		x = xm * cos - ym * sin + p1.x;
		ym = xm * sin + ym * cos + p1.z;
		xm = x;

		x = xn * cos - yn * sin + p1.x;
		yn = xn * sin + yn * cos + p1.z;
		xn = x;

		V3 p2m = p1.add(d.scale((length - arrow_height) / length));

		gl.glLineWidth(1);
		this.setColor(color);
//		gr.setStroke(new BasicStroke(line_width));
//		gr.drawLine(coordX(p1.x), coordY(p1.y), coordX(p2m.x), coordY(p2m.y));
//		gr.fillPolygon(xpoints, ypoints, 3);
//		gr.setStroke(new BasicStroke(1));

		V3 pl = d.cross(new V3(0, 1.0, 0)).normalize();
		V3 pl1 = p1.add(pl.scale(line_width / 2.0));
		V3 pl2 = p2m.add(pl.scale(line_width / 2.0));
		V3 pl3 = p2m.sub(pl.scale(line_width / 2.0));
		V3 pl4 = p1.sub(pl.scale(line_width / 2.0));

		gl.glBegin(GL2.GL_QUADS);

		vertex(pl1);
		vertex(pl2);
		vertex(pl3);
		vertex(pl4);

		gl.glEnd();

//		gl.glBegin(GL2.GL_LINES);
//		gl.glVertex3d(p1.x, p1.y, p1.z);
//		gl.glVertex3d(p2m.x, p2m.y, p2m.z);
//		gl.glEnd();
		gl.glBegin(GL2.GL_TRIANGLE_STRIP);
		gl.glVertex3d(p2.x, p2.y, p2.z);
		gl.glVertex3d(xm, p2.y, ym);
		gl.glVertex3d(xn, p2.y, yn);
		gl.glEnd();
		gl.glLineWidth(1);

//		gl.glLineWidth(line_width);
//		gl.glBegin(GL2.GL_LINES);
//			gl.glVertex2d(coordX(p1.x), coordY(p1.y));
//			gl.glVertex2d(coordX(p2m.x), coordY(p2m.y));
//		gl.glEnd();
//		gl.glBegin(GL2.GL_TRIANGLE_STRIP);
//			gl.glVertex2d(coordX(p2.x), coordY(p2.y));
//			gl.glVertex2d(coordX(xm), coordY(ym));
//			gl.glVertex2d(coordX(xn), coordY(yn));
//		gl.glEnd();
//		gl.glLineWidth(1);
	}

	protected boolean isCircleVisible(V3 pos, double radius) {
//		if (Debug.on) Debug.echo("viewport_top_left: " + viewport_top_left + ", viewport_right_bottom: " + viewport_right_bottom + ", pos: " + pos.scale(1.0 / zoom) + ", rad: " + radius);
		return HMath.circleInsideRect(viewport_top_left, viewport_right_bottom, pos.toPoint().scale(1.0 / zoom), radius / zoom);
	}

	protected boolean isQuadVisible(V3 qtl, V3 qrb) {
//		if (Debug.on) Debug.echo("viewport_top_left: " + viewport_top_left + ", viewport_right_bottom: " + viewport_right_bottom + ", pos: " + pos.scale(1.0 / zoom) + ", rad: " + radius);
		qtl = qtl.scale(1.0 / zoom);
		qrb = qrb.scale(1.0 / zoom);

		return HMath.pointInsideRect(viewport_top_left, viewport_right_bottom, qtl.toPoint())
			   || HMath.pointInsideRect(viewport_top_left, viewport_right_bottom, qrb.toPoint())
			   || HMath.pointInsideRect(viewport_top_left, viewport_right_bottom, new Point(qtl.x, qrb.y))
			   || HMath.pointInsideRect(viewport_top_left, viewport_right_bottom, new Point(qrb.x, qtl.y));
	}

	private boolean isPointVisible(V3 pos) {
		return HMath.pointInsideRect(viewport_top_left, viewport_right_bottom, pos.toPoint().scale(1.0 / zoom));
	}

	protected abstract void initWnd();

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
		width = w;
		height = h;

		if (h == 0) h = 1;   // prevent divide by zero
		float aspect = (float) w / h;

		initWnd();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
//		if (Perf.on) Perf.start("Paint.InitFrame");

		viewport_top_left = new Point(-x_offset / zoom, -y_offset / zoom);//.add(50, 50);
		viewport_right_bottom = viewport_top_left.add(width / zoom, height / zoom);//.sub(100, 100);

		gl = drawable.getGL().getGL2();
		gl.setSwapInterval(0);

		initWnd();

		gl.glClearColor((float) bg_color.getRed() / 255.0f, (float) bg_color.getGreen() / 255.0f, (float) bg_color.getBlue() / 255.0f, 0);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);

//		if (Perf.on) Perf.stop("Paint.InitFrame");
//		if (Perf.on) Perf.start("Paint");
		painting = true;
	}

	public void endDisplay() {
		painting = false;
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
		gl.glLoadIdentity();             // reset projection matrix

		gl.glViewport(0, 0, width, height);
		gl.glOrthof(0, width, height, 0, -1, 1);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity(); // reset

		for (DFUIElement element : ui) {
			if (element.visible) element.render();
		}

		gl.glPopMatrix();

		gl.glFlush();
//		if (Perf.on) Perf.stop("Paint");
	}

	protected void setStroke(int width) {
		gl.glLineWidth(width);
	}

	public void flush() {
		debug_elements_to_draw.clear();
	}

	public void drawCircle(V3 _pos, double _radius, Color _color, boolean transparent, int lifetime) {
		debug_elements_to_draw.add(new DebugCircle(_pos, _radius, _color, transparent, lifetime));
	}

	public void drawCircle(V3 _pos, double _radius, Color _color, int width) {
		debug_elements_to_draw.add(new DebugCircle(_pos, _radius, _color, width));
	}

	public void drawCircle(V3 _pos, double _radius, Color _color, int width, int lifetime) {
		debug_elements_to_draw.add(new DebugCircle(_pos, _radius, _color, width, lifetime));
	}

	public void drawSphere(V3 _pos, double _radius, Color _color, int lifetime) {
		debug_elements_to_draw.add(new DebugSphere(_pos, _radius, _color, lifetime));
	}

	public void drawCircleOriented(V3 _pos, double _radius, double _angle, Color _color) {
		debug_elements_to_draw.add(new DebugOrientedCircle(_pos, _radius, _angle, _color));
	}
//	protected static CopyOnWriteArrayList<DebugElem> debug_elements_to_draw;

	public void drawSquare(V3 _pos, double _size, double _angle, Color _color, int lifetime, int width, boolean transparent) {
		debug_elements_to_draw.add(new DebugSquare(_pos, _size, _angle, _color, lifetime, width, transparent));
	}

	public void drawSquare(V3 _pos, double _size, Color _color, int lifetime, int width, boolean transparent) {
		debug_elements_to_draw.add(new DebugSquare(_pos, _size, 0, _color, lifetime, width, transparent));
	}

	public void drawLine(V3 point1, V3 point2, int _width, Color _color) {
		debug_elements_to_draw.add(new DebugLine(point1, point2, _width, _color));
	}

	public void drawLine(V3 point1, V3 point2, int _width, Color _color, int lifetime) {
		debug_elements_to_draw.add(new DebugLine(point1, point2, _width, _color, lifetime));
	}

	public void drawLine(V3 point1, V3 point2, int _width, Color _color, int lifetime, DebugGroup group) {
		debug_elements_to_draw.add(new DebugLine(point1, point2, _width, _color, lifetime, group));
	}

	public void drawArrow(V3 point1, V3 point2, double width, double arrow_width, double arrow_height, Color color, int lifetime) {
		debug_elements_to_draw.add(new DebugArrow(point1, point2, width, arrow_width, arrow_height, color, lifetime));
	}

	public void drawArrow(V3 point1, V3 point2, double width, double arrow_width, double arrow_height, Color color) {
		debug_elements_to_draw.add(new DebugArrow(point1, point2, width, arrow_width, arrow_height, color, 3));
	}

	public void drawArrow(V3 point1, double angle, double length, double width, double arrow_width, double arrow_height, Color color, int lifetime) {
		debug_elements_to_draw.add(new DebugArrow(point1, angle, length, width, arrow_width, arrow_height, color, lifetime));
	}

	public void drawArrow(V3 point1, double angle, double length, double width, double arrow_width, double arrow_height, Color color) {
		debug_elements_to_draw.add(new DebugArrow(point1, angle, length, width, arrow_width, arrow_height, color, 3));
	}

	public void drawText(String text, Color _color) {
		Point pos = new Point(10, 10 + text_lines_count * 20);
		text_lines_count++;
		debug_elements_to_draw.add(new DebugText(text, pos, _color));
	}

	public void drawLabel(Point pos, String text, Color _color) {
		debug_elements_to_draw.add(new DebugText(text, pos, _color));
	}

	public void drawLabel(Point pos, String text, Color _color, int lifetime) {
		debug_elements_to_draw.add(new DebugText(text, pos, _color, lifetime));
	}

	public void drawLabel(Point pos, String text, Color _color, int lifetime, double scale) {
		debug_elements_to_draw.add(new DebugText(text, pos, _color, scale, lifetime));
	}

	protected void color(Color color) { color(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0); }

	protected void color(double r, double g, double b) { gl.glColor3d(r, g, b); }

	protected void color(double r, double g, double b, double a) { gl.glColor4d(r, g, b, a); }

	protected void color3d(double r, double g, double b) { gl.glColor3d(r, g, b); }

	protected void color3b(byte r, byte g, byte b) { gl.glColor3b(r, g, b); }

	protected void color4d(double r, double g, double b, double a) { gl.glColor4d(r, g, b, a); }

	protected void color4b(byte r, byte g, byte b, byte a) { gl.glColor4b(r, g, b, a); }

	protected void vertex(double x, double y, double z) { gl.glVertex3d(x, y, z); }

	protected void vertex(V3 pos) { gl.glVertex3d(pos.x, pos.y, pos.z); }

	protected void vertex(double x, double y) { gl.glVertex2d(x, y); }

	protected void translate(double x, double y, double z) { gl.glTranslated(x, y, z); }

	protected void translate(double x, double y) { gl.glTranslated(x, y, 0); }

	protected void translate(V3 pos) { gl.glTranslated(pos.x, pos.y, pos.z); }

	protected void pushMatrix() { gl.glPushMatrix(); }

	protected void popMatrix() { gl.glPopMatrix(); }

	protected void loadIdentity() { gl.glLoadIdentity(); }

	public abstract void frame(boolean brand_new);

	public void frame() {
		frame(false);
	}

	public static class DebugGroup {
		public static int last_group_id = 0;
		protected static DebugGroup active_group = null;
		public HashSet<DebugElem> elems = new HashSet<>();
		public int id;
		public String msg;
		public boolean selected = false;
		public boolean expired = false;

		public DebugGroup(int id, String msg_) {
			this.id = id;
			this.msg = msg_;
		}

		public DebugGroup(String msg_) {
			this.id = getRandomGroup();
			this.msg = msg_;
		}

		public DebugGroup() {
			this.id = getRandomGroup();
			this.msg = null;
		}

		public void setMsg(String msg_) {
			this.msg = msg_;
		}

		public static int getRandomGroup() {
			return last_group_id++;
		}

		public void add(DebugElem e) {
			elems.add(e);
		}

		public void select() {
			selected = true;
		}

		public void deselect() {
			selected = false;
		}

		public void update() {
			expired = true;

			for (DebugElem e : elems) {
				if (dTick() >= e.created && dTick() < e.expire) {
					expired = false;
					return;
				}
			}
		}
	}

	public static abstract class DebugElem {
		public Color color;
		public int created;
		public int expire;
		public V3 pos;
		public DebugGroup group = null;


		public DebugElem(V3 _pos, Color _color) {
			this.pos = _pos;
			this.color = _color;
			this.created = DebugBase.tick;

			if (DebugGroup.active_group != null) {
				this.group = DebugGroup.active_group;
				DebugGroup.active_group.add(this);
			}
		}

		public DebugElem(V3 _pos, Color _color, DebugGroup group) {
			this.pos = _pos;
			this.color = _color;
			this.group = group;
			this.created = DebugBase.tick;

			group.add(this);
		}

		public static double fitX(double c) {
//			return (c * zoom) + x_offset;
			return c * zoom;
		}

		public static double fitY(double c) {
//			return (c * zoom) + y_offset;
			return c * zoom;
		}

		public static double fitZ(double c) {
//			return (c * zoom) + z_offset;
			return c * zoom;
		}

		public void draw(DebugFormGL form) {
//			if (lifetime > 1 && lifetime <= max_lifetime) this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 255 * (lifetime/max_lifetime));
		}

		public abstract DebugElem copy();
	}

	class DebugCircle extends DebugElem {
		public double radius;
		public boolean transparent;
		public int width;

		public DebugCircle(V3 _pos, double _radius, Color _color) {
			super(_pos, _color);
			this.radius = _radius;
			this.width = 1;
		}

		public DebugCircle(V3 _pos, double _radius, Color _color, boolean transparent, int lifetime) {
			super(_pos, _color);
			this.radius = _radius;
			this.transparent = transparent;
			this.width = 1;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		public DebugCircle(V3 _pos, double _radius, Color _color, int width) {
			super(_pos, _color);
			this.radius = _radius;
			this.transparent = true;
			this.width = width;
		}

		public DebugCircle(V3 _pos, double _radius, Color _color, int width, int lifetime) {
			super(_pos, _color);
			this.radius = _radius;
			this.transparent = true;
			this.width = width;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		@Override
		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);

			boolean selected = group != null && group.selected;

			form.setColor(selected ? new Color(255, 84, 0) : color);
			gl.glLineWidth(width);

			form.circleS3(pos, radius, transparent);

			if (selected) {
				form.setColor(Color.white);
				form.circleS3(pos, radius * 1.5, true);
			}

			gl.glLineWidth(1);
		}

		@Override
		public DebugElem copy() {
			DebugCircle dc = new DebugCircle(pos.copy(), radius, color, transparent, this.expire - dTick());
			dc.width = width;
			dc.expire = this.expire;
			dc.group = this.group;
			return dc;
		}
	}

	class DebugSphere extends DebugElem {
		public double radius;

		public DebugSphere(V3 _pos, double _radius, Color _color) {
			super(_pos, _color);
			this.radius = _radius;
			this.expire = dTick() + 1;
		}

		public DebugSphere(V3 _pos, double _radius, Color _color, int lifetime) {
			super(_pos, _color);
			this.radius = _radius;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		@Override
		public void draw(DebugFormGL form) {
//			System.out.println("Sphere " + dTick() + " " + expire);
			if (dTick() >= this.expire) return;
			super.draw(form);

			form.setColor(color);

			form.sphere(pos, radius);

			gl.glLineWidth(1);
		}

		@Override
		public DebugElem copy() {
			DebugSphere dc = new DebugSphere(pos.copy(), radius, color, this.expire - dTick());
			dc.expire = this.expire;
			return dc;
		}
	}

	class DebugOrientedCircle extends DebugCircle {
		public double angle;

		public DebugOrientedCircle(V3 _pos, double _radius, double _angle, Color _color) {
			super(_pos, _radius, _color);
			this.angle = _angle;
		}

		@Override
		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);

			form.setColor(color);
			form.circleD2((pos.x - radius), (pos.y - radius), radius * 2);
			form.setColor(Color.white);
//			System.out.println(angle + " " + radius);
			form.circleD2((pos.x + (radius * 0.65) * Math.cos(angle) - 3),
						  (pos.y + (radius * 0.65) * Math.sin(angle) - 3),
						  6.0);

		}
	}

	class DebugSquare extends DebugElem {
		protected final int width;
		protected final boolean transparent;
		public double size;
		public double angle;

		public DebugSquare(V3 _pos, double _size, double _angle, Color _color, int lifetime, int width, boolean transparent) {
			super(_pos, _color);
			this.size = _size;
			this.angle = _angle;
			this.expire = DebugBase.tick + lifetime + 1;
			this.width = width;
			this.transparent = transparent;
		}

		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);

			form.setColor(color);
			gl.glLineWidth(this.width);

			if (this.transparent) form.rectRotated3(pos.x - size / 2, pos.y, pos.z - size / 2, size, size, angle, true);
			else form.rectRotated3(pos.x - size / 2, pos.y, pos.z - size / 2, size, size, angle, false);

			gl.glLineWidth(1);
		}

		@Override
		public DebugElem copy() {
			DebugSquare dc = new DebugSquare(pos.copy(), size, angle, color, this.expire - dTick(), width, transparent);
			dc.expire = this.expire;
			return dc;
		}
	}

	class DebugLine extends DebugElem {
		V3 pos2;
		int width;

		public DebugLine(V3 point1, V3 point2, int _width, Color _color) {
			super(point1, _color);
			this.pos2 = point2;
			this.width = _width;
			this.expire = DebugBase.tick + 1 + 1;
		}

		public DebugLine(V3 point1, V3 point2, int _width, Color _color, int lifetime) {
			super(point1, _color);
			this.pos2 = point2;
			this.width = _width;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		public DebugLine(V3 point1, V3 point2, int _width, Color _color, int lifetime, DebugGroup group) {
			super(point1, _color, group);
			this.pos2 = point2;
			this.width = _width;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);

			boolean selected = group != null && group.selected;

			form.setColor(selected ? new Color(255, 84, 0) : color);
//			g.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 255 - 255 * ((lifetime+1)/max_lifetime)));
			form.setStroke((selected ? (Math.max(width * 4, 8)) : width));

			gl.glBegin(GL2.GL_LINES);
			vertex(pos);
			vertex(pos2);
			gl.glEnd();

			if (selected) {
				form.setStroke(3);
				V3 c = pos.add(pos2).scale(0.5);
				form.setColor(new Color(255, 255, 255, 150));
//				rect3(c.x, c.y, c.z, 1.0, 1.0, true);
				circle3(c.x, c.y, c.z, 0.5, true);
			}

			form.setStroke(1);
		}

		@Override
		public DebugElem copy() {
			DebugLine dc = new DebugLine(pos.copy(), pos2.copy(), width, color, this.expire - dTick());
			dc.expire = this.expire;
			dc.group = this.group;
			return dc;
		}
	}

	class DebugArrow extends DebugElem {
		V3 pos2;
		double width;
		double arrow_width;
		double arrow_height;

		public DebugArrow(V3 point1, double angle, double length, double width, double arrow_width, double arrow_height, Color color, int lifetime) {
			this(point1, point1.aheadXZ(angle, length), width, arrow_width, arrow_height, color, lifetime);
		}

		public DebugArrow(V3 point1, V3 point2, double width, double arrow_width, double arrow_height, Color color) {
			this(point1, point2, width, arrow_width, arrow_height, color, 3);
		}

		public DebugArrow(V3 point1, V3 point2, double width, double arrow_width, double arrow_height, Color color, int lifetime) {
			super(point1, color);
			this.pos2 = point2;
			this.width = width;
			this.expire = DebugBase.tick + lifetime + 1;
			this.arrow_width = arrow_width;
			this.arrow_height = arrow_height;
		}

		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);
			form.drawArrowLine(this.pos, this.pos2, this.width, this.arrow_width, this.arrow_height, this.color);
		}

		@Override
		public DebugElem copy() {
			DebugArrow dc = new DebugArrow(pos.copy(), pos2.copy(), width, arrow_width, arrow_height, color, this.expire - dTick());
			dc.expire = this.expire;
			return dc;
		}
	}

	public class DebugText extends DebugElem {
		String text;
		double scale = 1.0;

		public DebugText(String text, Point pos, Color _color) {
			super(pos.toXY(), _color);
			this.text = text;
			this.expire = DebugBase.tick + 1 + 1;
		}

		public DebugText(String text, Point pos, Color _color, int lifetime) {
			super(pos.toXY(), _color);
			this.text = text;
			this.expire = DebugBase.tick + lifetime + 1;
		}

		public DebugText(String text, Point pos, Color _color, double scale_, int lifetime) {
			super(pos.toXY(), _color);
			this.text = text;
			this.expire = DebugBase.tick + lifetime + 1;
			this.scale = scale_;
		}

		public void draw(DebugFormGL form) {
			if (dTick() >= this.expire) return;
			super.draw(form);

			txt.beginRendering(width, height);
//			txt.setColor(color);
//			txt.draw3D(text, (float) coordX(pos.x) + x_offset, height - ((float) coordY(pos.y) + y_offset), 0f, 1.0f);
			textUI(pos.toPoint(), text, color, scale);
			txt.endRendering();

		}

		@Override
		public DebugElem copy() {
			DebugText dc = new DebugText(text, pos.toPoint(), color, this.expire - dTick());
			dc.expire = this.expire;
			dc.scale = this.scale;
			return dc;
		}

	}

}
