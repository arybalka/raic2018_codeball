import Lama.HMath;
import Lama.Point;
import Lama.PointI;
import Lama.Renderer.*;
import Lama.V3;
import com.jogamp.opengl.*;
import de.javagl.obj.*;
import model.Arena;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by lamik on 10.09.14.
 */
public class DebugForm extends DebugFormGL implements GLEventListener, MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {
	private static final boolean SIMPLE_SCENE = false;
	private static final boolean SHOW_ALL_PFS = false;
	public static Color[] debug_group_colors = null;
	public static Random rand;
	public final DFCheckBox checkbox_render_world;
	public final DFCheckBox checkbox_show_grid;
	public final DFCheckBox checkbox_show_trajectories;
	public final DFCheckBox checkbox_efficiency;
	public final DFCheckBox checkbox_scores;
	public final DFCheckBox checkbox_follow_ball;
	public final DFMemo memo_best;
	public final DFMemo memo_selected;
	private final DFSlider slider;
	public Point last_clicked_point = new Point(0, 0);
	public ArrayList<String> memo1 = new ArrayList<>();
	public ArrayList<String> memo2 = new ArrayList<>();
	boolean frame_locked = false;
	PointI last_dragged_pos = null;
	int highlighted_formation = -1;
	int mouse_presset_button = -1;
	PointI mouse_pressed_pos = null;
	Obj scene;
	private ArrayList<V3> field_side_left;
	private ArrayList<V3> field_side_right;
	private ArrayList<V3> field_middle;
	//	public DebugSnapshotBase renderable_map;
	private ArrayList<DebugSnapshot> history = new ArrayList<>();

	public DebugForm(RendererConfig cfg) {
		super(cfg);
		rand = new Random(cfg.random_seed);

		canvas.addMouseMotionListener(this);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);

		debug_elements_to_draw = new HashSet<>();

		int y = 20, yadd = 30, right = 170;

		checkbox_render_world = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "Render World", true, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_render_world);
		y += yadd;

		checkbox_show_grid = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "Show Grid", false, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_show_grid);
		y += yadd;

		checkbox_show_trajectories = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "All Trajectories", false, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_show_trajectories);
		y += yadd;

		checkbox_efficiency = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "Efficiency Map", false, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_efficiency);
		y += yadd;

		checkbox_scores = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "Scores", true, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_scores);
		y += yadd;

		checkbox_follow_ball = new DFCheckBox(this, new PointI(width - right, y), right - 10, 24, "Follow Ball", false, new Color(75, 119, 170), new Color(253, 242, 255));
		ui.add(checkbox_follow_ball);
		y += yadd;

		slider = new DFSlider(this, new PointI(0, (int) (height * 0.95)), width, (int) (height * 0.03), 2, 10, Color.white, new Color(1, 1, 1, 0));
		slider.setMax(1);
		ui.add(slider);

		int mw = 300, mh = 200;
		memo_best = new DFMemo(this, new PointI(10, 30), mw, mh, null, new Color(178, 178, 178), 0.35, 15.0);
		memo_best.visible = checkbox_scores.state;
		ui.add(memo_best);

		memo_selected = new DFMemo(this, new PointI(10 + mw + 10, 30), mw, mh, null, new Color(178, 178, 178), 0.35, 15.0);
		memo_selected.visible = checkbox_scores.state;
		ui.add(memo_selected);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		if (displayed_tick == 0) {
			System.out.println("Skipping rendering");
			return;
		}

		DebugSnapshot renderable_map = history.get(displayed_tick - 1);
		super.display(drawable);

//		System.out.println((displayed_tick - 1) + " " + Debug.tick);

		if (displayed_tick - 1 != Debug.tick && renderable_map.debug_log != null) {
			System.out.println("Log:");

			for (String s : renderable_map.debug_log) {
				System.out.println(s);
			}
		}

		memo_best.clear();
		if (memo1 != null) memo_best.setText(renderable_map.memo1);
		memo_best.visible = checkbox_scores.state;

		memo_selected.visible = checkbox_scores.state;

		if (renderable_map == null) {
			if (Debug.on) Debug.echo(" !!!!!! NO MAP TO RENDER !!!!!!!!!!");
			return;
		}

		try {
			gl.glPushMatrix();
			gl.glLoadIdentity();

			gl.glUseProgram(base_shader_program);

			if (checkbox_render_world.state) {

				Color clr_floor = new Color(64, 71, 78);
				double wall_height = 5.0;

				lineWidth(1);

				if (SIMPLE_SCENE) {
					poly(field_side_left, false, clr_floor);
					for (int i = 1; i < field_side_left.size(); i++) {
						wall(field_side_left.get(i - 1), field_side_left.get(i), wall_height, new Color(102, 120, 167));
					}

					poly(field_side_right, false, clr_floor);
					for (int i = 1; i < field_side_right.size(); i++) {
						wall(field_side_right.get(i - 1), field_side_right.get(i), wall_height, new Color(102, 120, 167));
					}

					poly(field_middle, false, clr_floor);
				}

				sphere3(v(cam.view.x, cam.view.y, cam.view.z), 0.15, new Color(176, 176, 176));

				lineWidth(1);

				if (!SIMPLE_SCENE) {
					for (int i = 0; i < scene.getNumFaces(); i++) {
						ObjFace of = scene.getFace(i);

						boolean visible = true;

						double avgY = 0;

						for (int j = 0; j < of.getNumVertices(); j++) {
							FloatTuple v = scene.getVertex(of.getVertexIndex(j));
//						avgY += v.getY();
							if (v.getY() > avgY) avgY = v.getY();

							if (v.getY() > Glob.arena.height * 0.7) {
								visible = false;
								break;
							}
						}

						if (!visible) continue;
						color(Color.getHSBColor((float) (0.25 + (0.5 * avgY / Glob.arena.height)), 0.25f, 0.3f));

						gl.glBegin(GL.GL_TRIANGLES);
						for (int j = 0; j < of.getNumVertices(); j++) {
							FloatTuple v = scene.getVertex(of.getVertexIndex(j));
							FloatTuple n = scene.getNormal(of.getNormalIndex(j));
							gl.glNormal3d(n.getX(), n.getY(), n.getZ());
							gl.glVertex3d(v.getX(), v.getY(), v.getZ());
						}
						gl.glEnd();
					}
				}

				if (checkbox_show_grid.state) {
					for (double x = -Glob.arena.width / 2; x <= Glob.arena.width / 2; x += 1.0) {
						lineS3(v(x, 0.1, -Glob.arena.depth / 2), v(x, 0.1, Glob.arena.depth / 2), 1, x % 10 == 0 ? new Color(180, 180, 180, 150) : new Color(69, 69, 69, 150));
					}
					for (double z = -Glob.arena.depth / 2; z <= Glob.arena.depth / 2; z += 1.0) {
						lineS3(v(-Glob.arena.width / 2, 0.1, z), v(Glob.arena.width / 2, 0.1, z), 1, z % 10 == 0 ? new Color(180, 180, 180, 150) : new Color(69, 69, 69, 150));
					}
				}

				gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{(float) renderable_map.ball.x, (float) (renderable_map.ball.y + 5.0f), (float) renderable_map.ball.z}, 0);
				sphere3(renderable_map.ball, renderable_map.ball.radius, renderable_map.ball_override_clr != null ? renderable_map.ball_override_clr : new Color(39, 139, 45));
				circle3(renderable_map.ball.replace(null, 0.1, null), renderable_map.ball.radius, new Color(0, 0, 0, 30), false);

				int light_num = 0;
				for (Dude d : renderable_map.my_team) {
					gl.glLightfv(GL2.GL_LIGHT1 + light_num, GL2.GL_POSITION, new float[]{(float) d.x, (float) (d.y + 5.0f), (float) d.z}, 0);
					Color c = new Color(147 + d.id * 10, 12 + d.id * 10, 20 + d.id * 30);

//					if (Debug.on) Debug.echo("DA " + d.action);
					if (d.action != null && d.action.use_nitro) c = new Color(0, 255, 237);

					sphere3(d, 1.0, c);
					if (d.radius > 1.0) {
						sphere3(d, d.radius, new Color(255, 255, 255, 50));
					}

					circle3(d.replace(null, 0.1, null), d.radius, new Color(0, 0, 0, 30), false);
					if (!d.touch) line3(d, d.replace(null, 0.0, null), 1, c);
					light_num++;
				}

				for (Dude d : renderable_map.enemy_team) {
					gl.glLightfv(GL2.GL_LIGHT1 + light_num, GL2.GL_POSITION, new float[]{(float) d.x, (float) (d.y + 5.0f), (float) d.z}, 0);
					Color c = new Color(0 + d.id * 15, 10 + d.id * 15, 70 + d.id * 30);
					sphere3(d, 1.0, c);
					if (d.radius > 1.0) {
						sphere3(d, d.radius, new Color(255, 255, 255, 50));
					}
					circle3(d.replace(null, 0.1, null), d.radius, new Color(0, 0, 0, 30), false);
					if (!d.touch) line3(d, d.replace(null, 0.0, null), 1, c);
					light_num++;
				}

				for (Nitro n : renderable_map.nitros) {
					sphere3(n, n.radius, new Color(134, 161, 45, n.respawn != null && n.respawn > 0 ? 30 : 150));
				}

//				if (Debug.on) Debug.echo("Paint");

//				if (Perf.on) Perf.start("Paint.Grid");

//				if (Perf.on) Perf.stop("Paint.Grid");

				Point selection_center = Point.zero();
				int selected_units = 0;

				lineWidth(1);

				if (selected_units > 0) selection_center.Scale(1.0 / selected_units);

				gl.glLineWidth(1);

				for (DebugElem etd : renderable_map.elements) {
					if (etd instanceof DebugText) continue;

					etd.draw(this);
				}

			}

			gl.glUseProgram(0);
			textUI(new Point(10, 20), "" + renderable_map.tick, Color.white, 0.5);

			for (DebugElem etd : renderable_map.elements) {
				if (!(etd instanceof DebugText)) continue;

				etd.draw(this);
			}


			mouse_event = false;
			Debug.ball_clr_override = null;

		} catch (ConcurrentModificationException ignored) {
			Debug.echo("Painting Exception");
		}


		super.endDisplay();
	}

	private void sphere3(V3 pos, double radius, Color clr) {
		color(clr);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		translate(pos);
		glut.glutSolidSphere(radius, 16, 16);
		gl.glPopMatrix();
	}

	public void vertex3(V3 v) {
		gl.glVertex3d(v.x, v.y, v.z);
	}

	public void normal3(V3 v) {
		gl.glNormal3d(v.x, v.y, v.z);
	}

	private void poly(V3 p1, V3 p2, V3 p3, V3 p4, boolean contour_only, Color color) {
		color(color);

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_POLYGON);

		normal3(new V3(0, 1, 0));
		vertex3(p1);
		normal3(new V3(0, 1, 0));
		vertex3(p2);
		normal3(new V3(0, 1, 0));
		vertex3(p3);
		normal3(new V3(0, 1, 0));
		vertex3(p4);

		gl.glEnd();

	}

	private void poly(ArrayList<V3> points, boolean contour_only, Color color) {
		color(color);

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_POLYGON);

		for (V3 v : points) {
			normal3(new V3(0, 1, 0));
			vertex3(v);
		}

		gl.glEnd();

	}

	private void poly(V3[] points, boolean contour_only, Color color) {
		color(color);

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_POLYGON);

		for (V3 v : points) {
//			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[]{0.5f, 0.3f, 0.3f}, 0);
			normal3(new V3(0, 1, 0));
			vertex3(v);
		}

		gl.glEnd();

	}

	private void wall(V3 p1, V3 p2, double height, Color color) {
		wall(p1, p2, height, false, color);
	}

	//region @@@@ MOUSE EVENTS @@@@

	private void wall(V3 p1, V3 p2, double height, boolean contour_only, Color color) {
		color(color);

		V3 norm = p2.sub(p1).cross(v(0, 1, 0));

		gl.glBegin(contour_only ? GL2.GL_LINE_LOOP : GL2.GL_POLYGON);

		gl.glNormal3d(norm.x, norm.y, norm.z);
		gl.glVertex3d(p1.x, p1.y, p1.z);

		gl.glNormal3d(norm.x, norm.y, norm.z);
		gl.glVertex3d(p1.x, p1.y + height, p1.z);

		gl.glNormal3d(norm.x, norm.y, norm.z);
		gl.glVertex3d(p2.x, p1.y + height, p2.z);

		gl.glNormal3d(norm.x, norm.y, norm.z);
		gl.glVertex3d(p2.x, p1.y, p2.z);

		gl.glEnd();

	}

	private void rectScreen(int x1, int y1, int x2, int y2) {
		pushMatrix();
		loadIdentity();
		translate(0, 0, -0.11);
		gl.glBegin(GL2.GL_QUADS);
		vertex(x1, y1);
		vertex(x2, y1);
		vertex(x2, y2);
		vertex(x1, y2);
		gl.glEnd();
		popMatrix();

	}

	private void lineWidth(double w) {
		gl.glLineWidth((int) size(w));
	}

	public void frame() {
		frame(false);
	}

	public void frame(boolean brand_new) {
//		if (Debug.min_render_turn > 0 && Scene.tick < Debug.min_render_turn) return;

		if (Debug.on && brand_new && displayed_tick - 1 != Debug.tick) {
//			DebugSnapshot.elements.removeAll(elements_to_remove);
			elements_to_remove.clear();
//			if (!frame_locked) displayed_tick = Scene.tick;

			DebugSnapshot renderable_map = new DebugSnapshot(this, Glob.tick);
			history.add(renderable_map);
			slider.setMax(Glob.tick);

			if (history.size() > 1000) {
				for (DebugElem de : history.get(history.size() - 1000).elements) {
					if (de.group != null) {
						DebugBase.groups.remove(de.group.id);
					}
				}
				history.get(history.size() - 1000).elements.clear();
//				history.set(0, null);
				displayed_tick = Math.min(displayed_tick + 1, history.size());
			}

			if (!frame_locked) displayed_tick = history.size();
		}

		canvas.display();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse_event = true;

		if (mouse_pressed_pos != null) {
			PointI p = new PointI(e.getX(), e.getY());

			if (last_dragged_pos != null) {
				switch (mouse_presset_button) {
					case 1:
						boolean inside_ui = false;
						for (DFUIElement el : ui) {
							if (el.m_left_down) {
								inside_ui = true;
								el.mousedrag(p);

								if (el instanceof DFSlider) {
									displayed_tick = HMath.clamp((int) (slider.slider_pos), 0, history.size() - 1) + 1;
//									Debug.echo("!! " + slider.slider_pos + " / " + displayed_tick + " / " + slider.max_value
//											   + " / " + history.size() + " -> " + ((int)((double)history.size() * slider.slider_pos)));
								}
							}
						}

						if (!inside_ui) {
							cam.move(v(p.y, 0, p.x).sub(v(last_dragged_pos.y, 0, last_dragged_pos.x)).scale(-1, 1, 1).scale(0.1));
						}

						break;
					case 2:
						cam.move(v(0, p.y, 0).sub(v(0, last_dragged_pos.y, 0)).scale(0.1));

						break;
					case 3:
						cam.moveHV(-(p.x - last_dragged_pos.x), (p.y - last_dragged_pos.y));
						break;
				}
			}

//			if (Debug.on) Debug.echo("dragged M" + mouse_presset_button + " at " + p + " > " + x_offset + " > " + y_offset);
			last_dragged_pos = p;
		}

		Debug.frame();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse_event = true;
//		DebugSnapshotBase renderable_map = history.get(displayed_tick - 1);

		double x = (e.getX() - x_offset) / zoom;
		double y = (e.getY() - y_offset) / zoom;
//		Point pos = new Point(x, y);

/*
		for (Formation f : renderable_map.formations) {
			if (f.pf != null && f.center.distanceLess(pos, 20.0)) {
				highlighted_formation = f.id;
				Debug.tick();
				break;
			}
		}
*/

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	protected void selectDebugElem(int dir) {
//		System.out.println("DT " + (displayed_tick - 1));
//		if (displayed_tick - 1 >= Glob.tick) return;
		if (displayed_tick - 1 >= history.size()) return;

//		int selected_idx = -1;
//		int last_with_message_idx = -1;
//		int first_with_message_idx = -1;
//		int len = history.get(displayed_tick - 1).elements.size();
//		int len = DebugBase.groups.size();
//		int i = (dir == 1 ? 0 : len - 1);
		DebugGroup selected_group = null;
		DebugGroup first_group = null;
		DebugGroup last_group = null;
		DebugGroup the_group = null;
		boolean get_next_mode = false;

		for (DebugGroup dg : DebugBase.groups.values()) {
			dg.update();
			if (dg.expired) continue;

			if (first_group == null) {
				first_group = dg;
			}

			if (get_next_mode) {
				the_group = dg;
				break;
			}

			if (dg.selected) {
				selected_group = dg;
				if (dir < 0) {
					the_group = last_group;
					break;
				} else {
					get_next_mode = true;
					continue;
				}
			}

			last_group = dg;
		}

		if (selected_group != null) {
			selected_group.selected = false;
		}

		if (the_group == null) {
			if (dir > 0) {
				if (first_group != null) first_group.selected = true;
			} else {
				if (last_group != null) last_group.selected = true;
			}
		}

		if (the_group != null) {
			the_group.selected = true;
			Debug.echo("Selected Group: " + the_group.id + ", len: " + the_group.elems.size());
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int step = e.isShiftDown() ? 10 : e.isControlDown() ? 2 : 1;

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			frame_locked = !frame_locked;
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			displayed_tick = Math.max(displayed_tick - step, 1);
			slider.setDisplayedTick(displayed_tick - 1);
			canvas.display();
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			displayed_tick = Math.min(displayed_tick + step, history.size());
			slider.setDisplayedTick(displayed_tick - 1);
			canvas.display();
		}
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			selectDebugElem(1);
			canvas.display();
		}
		if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			selectDebugElem(-1);
			canvas.display();
		}
		if (e.getKeyCode() == KeyEvent.VK_HOME) {
			for (DebugElem db : history.get(displayed_tick - 1).elements) {
				if (db.group != null && db.group.selected) {
					System.out.println(db.group.msg);
					memo_selected.setText(new ArrayList<>(Arrays.asList(db.group.msg.split("\n"))));
					break;
				}
			}
			canvas.display();
		}
		if (e.getKeyCode() == KeyEvent.VK_END) {
			for (DebugElem db : history.get(displayed_tick - 1).elements) {
				if (db.group != null && db.group.selected) {
					db.group.selected = false;
				}
			}
			canvas.display();
		}
//		if (Debug.on) Debug.echo("Displayed tick: " + displayed_tick);
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		double x = (e.getX() - x_offset) / zoom;
		double y = (e.getY() - y_offset) / zoom;
		Point pos = new Point(x, y);
		PointI win_pos = new PointI(e.getX(), e.getY());
		DebugSnapshotBase renderable_map = history.get(displayed_tick - 1);

		for (DFUIElement element : ui) {
			if (element.isInside(win_pos)) {
				element.click(win_pos.sub(element.pos));
				Debug.frame();
				return;
			}
		}

//		System.out.println("CLICK at " + pos + ", scale" + size(1.0) + ", xo: " + x_offset + ", yo: " + y_offset + "\n");

		last_clicked_point = pos;
		mouse_event = true;

		Debug.frame();
	}

	@Override
	public void mousePressed(MouseEvent e) {
//		if (Debug.on) Debug.echo("MOUSE " + e);
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouse_pressed_pos = new PointI(e.getX(), e.getY());
			mouse_presset_button = e.getButton();
//			last_dragged_pos = mouse_pressed_pos;
//			if (Debug.on) Debug.echo("pressed at " + mouse_pressed_pos);

			for (DFUIElement el : ui) {
				if (el.isInside(mouse_pressed_pos)) {
					el.mousedown(mouse_pressed_pos);
				}
			}
		}
		if (e.getButton() == MouseEvent.BUTTON2) {
			mouse_pressed_pos = new PointI(e.getX(), e.getY());
			mouse_presset_button = e.getButton();
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			mouse_pressed_pos = new PointI(e.getX(), e.getY());
			mouse_presset_button = e.getButton();
//			last_dragged_pos = mouse_pressed_pos;
//			if (Debug.on) Debug.echo("pressed at " + mouse_pressed_pos);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (mouse_pressed_pos != null) {
//				if (Debug.on) Debug.echo("released at " + mouse_pressed_pos + ", xo: " + x_offset + ", yo: " + y_offset);
				mouse_pressed_pos = null;
				last_dragged_pos = null;
				mouse_presset_button = -1;
			}

			for (DFUIElement el : ui) {
				el.mouseup(new PointI(e.getX(), e.getY()));
			}

		}
		if (e.getButton() == MouseEvent.BUTTON2) {
			if (mouse_pressed_pos != null) {
				mouse_pressed_pos = null;
				last_dragged_pos = null;
				mouse_presset_button = -1;
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (mouse_pressed_pos != null) {
//				if (Debug.on) Debug.echo("released at " + mouse_pressed_pos + ", xo: " + x_offset + ", yo: " + y_offset);
				mouse_pressed_pos = null;
				last_dragged_pos = null;
				mouse_presset_button = -1;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double sc = 2.5;
		/*
		double x = (e.getX() - x_offset) / zoom;
		double y = (e.getY() - y_offset) / zoom;
		Point pos = new Point(x, y);

		double z1 = zoom;
		zoom = HMath.clamp(e.getWheelRotation() < 0 ? zoom + sc : zoom - sc, 1.0, 10.0);
//		if (Debug.on) Debug.echo(">>> " + (e.getX() - (pos.x * zoom + x_offset)));
		x_offset = x_offset + (int) (e.getX() - (pos.x * zoom + x_offset));
		y_offset = y_offset + (int) (e.getY() - (pos.y * zoom + y_offset));
		*/

		cam.zoom(e.getWheelRotation() * sc);

//		x_offset = -(int)(pos.x - ((double)width / zoom) / 2.0);
//		x_offset = (int)(pos.x / zoom);

//		if (Debug.on) Debug.echo("scroll! " + e + " :: " + e.getWheelRotation() + " -> " + cam.distance);
		Debug.frame();
	}

	//endregion reg

	public V3 v(double x, double y, double z) {
		return new V3(x, y, z);
	}

	public V3 v(double x, double z) {
		return new V3(x, 0, z);
	}

	private ArrayList<V3> getArcV3s(V3 center, double radius, double start_angle, double arc_angle, int num_segments) {
		ArrayList<V3> result = new ArrayList<>();
		double theta = arc_angle / (double) (num_segments - 1);//theta is now calculated from the arc angle instead, the - 1 bit comes from the fact that the arc is open

		double tangetial_factor = Math.tan(theta);

		double radial_factor = Math.cos(theta);
		double x = radius * Math.cos(start_angle);//we now start at the start angle
		double z = radius * Math.sin(start_angle);

		for (int ii = 0; ii < num_segments; ii++) {
			result.add(v(x + center.x, center.y, z + center.z));

			double tx = -z;
			double tz = x;

			x += tx * tangetial_factor;
			z += tz * tangetial_factor;

			x *= radial_factor;
			z *= radial_factor;
		}

		return result;
	}

	public void initialize(Arena a) {
		field_side_left = new ArrayList<>();
		field_side_right = new ArrayList<>();
		field_middle = new ArrayList<>();

		if (!SIMPLE_SCENE) {
			try {
				InputStream arena_stream = new FileInputStream("assets/arena.obj");
				scene = ObjUtils.convertToRenderable(ObjReader.read(arena_stream));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			double hw = a.width / 2.0;
			double hd = a.depth / 2.0;
			double hx = hw - a.corner_radius;
			double hz = hd - a.corner_radius;

//		field_side_left.add(v(0, 0, hd));
			field_side_left.add(v(-hx, 0, hd));

			ArrayList<V3> base_arc_field = getArcV3s(v(-hx, 0, hz), a.corner_radius, HMath.HALF_PI, HMath.HALF_PI, 16);
			field_side_left.addAll(base_arc_field);

			field_side_left.add(v(-hw, 0, hz));
			field_side_left.add(v(-hw, 0, 0));
//		field_side_left.add(v(-hx, 0, 0));
//		field_side_left.add(v(0, 0, 0));

			ArrayList<V3> refl1 = new ArrayList<>();
			for (int i = field_side_left.size() - 1; i >= 0; i--) {
				refl1.add(field_side_left.get(i).reflectAgainstAxisPlane(true, false, false));
			}

			field_side_left.addAll(refl1);

			for (int i = field_side_left.size() - 1; i >= 0; i--) {
				field_side_right.add(field_side_left.get(i).reflectAgainstAxisPlane(false, false, true));
			}

			field_middle.add(v(-hx, hd));
			field_middle.add(v(hx, hd));
			field_middle.add(v(hx, -hd));
			field_middle.add(v(-hx, -hd));

		}
	}

	protected void initWnd() {
		float aspect = (float) width / height;
		gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
		gl.glLoadIdentity();             // reset projection matrix

//		gl.glScalef(1.0f, -1.0f, 1.0f);

		gl.glViewport(0, 0, width, height);
		glu.gluPerspective(60, aspect, 0.1, 10000.0); // fovy, aspect, zNear, zFar

//		if (Debug.on) Debug.echo("lookAt | " + cam.pos + " -> " + cam.view);

		if (checkbox_follow_ball.state) {
			cam.view.set(history.get(displayed_tick - 1).ball);
		}

		glu.gluLookAt(cam.pos.x, cam.pos.y, cam.pos.z, cam.view.x, cam.view.y, cam.view.z, 0, 1, 0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity(); // reset

	}

}
