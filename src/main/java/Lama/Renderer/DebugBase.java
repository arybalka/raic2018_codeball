package Lama.Renderer;

import Lama.*;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by lamik on 10.09.14.
 */
public class DebugBase {
	public static boolean on = runningFromIntelliJ();
	public static final boolean debug_form = true;
	//	private static final boolean debug_form = false;
	public static final boolean print_scores = true;

	//	public static final int min_log_turn = 1300;
//	public static final int min_log_turn = 1000;
	public static final int min_log_turn = -1;
	public static final int max_log_turn = 25000;
	public static int tick;
	public static HashMap<Integer, DebugFormGL.DebugGroup> groups = new HashMap<>();

	public static HashMap<Integer, ArrayList<String>> debug_log = new HashMap<>();

	public static final int min_render_turn = min_log_turn;

	public static DebugFormGL form;

	public static boolean runningFromIntelliJ() {
		String classPath = System.getProperty("java.class.path");
		return classPath.contains("IntelliJ IDEA");
	}

	public static String prefix(int width_per_level, int level) {
		if (!on) {
			return "";
		}

		String result = "";
		String prefix = "";
		for (int s = 0; s < width_per_level; s++) {
			prefix += " ";
		}

		for (int i = 0; i < level; i++) {
			result += prefix;
		}

		return result;
	}

	public interface MessageProvider {
		String get();
	}

	public static boolean shouldEcho(int tick) {
		return tick > min_log_turn && tick < max_log_turn;
	}

	public static int tabs = 0;

	public static void tab() {
		tabs++;
	}

	public static void untab() {
		tabs = Math.max(tabs - 1, 0);
	}

	public static void echo(String str) {
		if (!on) return;
		if (!shouldEcho(tick)) return;
		str = prefix(4, tabs) + str;

		ArrayList<String> tick_log;

		if (!debug_log.containsKey(tick)) {
			tick_log = new ArrayList<>();
			debug_log.put(tick, tick_log);
		} else {
			tick_log = debug_log.get(tick);
		}

		tick_log.add(str);

		System.out.println(str);
	}

	public static boolean initiated = false;

	public static void frame(int tick_) {
		frame(false, tick_);
	}

	public static void frame(boolean brand_new, int tick_) {
		tick = tick_;
		if (!on || !debug_form) return;

		form.frame(brand_new);
		form.flush();
	}

	public static void die() {
		if (!on || !debug_form) return;

		System.out.println("! Application terminated");
		form.dispose();
	}


	public static DebugFormGL.DebugGroup beginGroup(String msg, int group_id) {
		DebugFormGL.DebugGroup dg;
		if (group_id >= 0) {
			dg = groups.get(group_id);
			dg.setMsg(msg);
		} else {
			dg = new DebugFormGL.DebugGroup(msg);
		}

		DebugFormGL.DebugGroup.active_group = dg;
		groups.put(dg.id, dg);

		return dg;
	}

	public static DebugFormGL.DebugGroup beginGroup() {
		DebugFormGL.DebugGroup dg = new DebugFormGL.DebugGroup();
		DebugFormGL.DebugGroup.active_group = dg;
		groups.put(dg.id, dg);

		return dg;
	}

	public static void endGroup() {
		if (DebugFormGL.DebugGroup.active_group == null) return;

		DebugFormGL.DebugGroup.active_group = null;
	}

	public static void circle(Circle c, Color color, boolean transparent, int lifetime) {
		if (!on || !debug_form) return;
		form.drawCircle(c.pos(), c.radius, color, transparent, lifetime);
	}

	public static void circle(Circle c, Color color, int width) {
		if (!on || !debug_form) return;
		form.drawCircle(c.pos(), c.radius, color, width);
	}

	public static void circle(Circle c, Color color, int width, int lifetime) {
		if (!on || !debug_form) return;
		form.drawCircle(c.pos(), c.radius, color, width, lifetime);
	}

	public static void sphere(V3 pos, double radius, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawSphere(pos.copy(), radius, color, lifetime);
	}

	public static void sphere(Sphere sphere, Color color, int lifetime) {
		sphere(sphere.pos(), sphere.radius, color, lifetime);
	}

	public static void circleS(Point pos, double radius, Color color) {
		circle(pos.copy(), radius * DebugFormGL.zoom, color, 1, 1);
//		circle(pos, radius, color, 1, 1);
	}

	public static void circleS(Point pos, double radius, Color color, int width, int lifetime) {
		circle(pos.copy(), radius * DebugFormGL.zoom, color, width, lifetime);
//		circle(pos, radius, color, 1, 1);
	}

	public static void circle(Point pos, double radius, Color color) {
		if (!on || !debug_form) return;
		form.drawCircle(pos.toXZ(), radius, color, 1, 1);
	}

	public static void circle3(V3 pos, double radius, Color color) {
		if (!on || !debug_form) return;
		form.drawCircle(pos.copy(), radius, color, 1, 1);
	}

	public static void circle3(V3 pos, double radius, Color color, boolean transparent) {
		if (!on || !debug_form) return;
		form.drawCircle(pos.copy(), radius, color, transparent, 1);
	}

	public static void circle(Point pos, double radius, Color color, int width) {
		circle(new Circle(pos.copy(), radius), color, width);
	}

	public static void circle(Point pos, double radius, Color color, int width, int lifetime) {
		if (!on || !debug_form) return;
		form.drawCircle(pos.toXZ(), radius, color, width, lifetime);
	}

	public static void circle(double x, double y, double radius, Color color) {
		circle(new Circle(x, y, radius), color, 1, 1);
	}

	public static void circle(Point pos, double radius, Color color, boolean transparent) {
		circle(new Circle(pos.copy(), radius), color, transparent, 1);
	}

	public static void circle(double x, double y, double radius, Color color, boolean transparent, int lifetime) {
		circle(new Circle(x, y, radius), color, transparent, lifetime);
	}

	public static void circle(Point pos, double radius, Color color, boolean transparent, int lifetime) {
		circle(new Circle(pos.x, pos.y, radius), color, transparent, lifetime);
	}

	public static void circle(double x, double y, double radius, Color color, boolean transparent) {
		circle(new Circle(x, y, radius), color, transparent, 1);
	}

	public static void circleOriented(Point pos, double radius, double angle, Color color) {
		if (!on || !debug_form) return;
		form.drawCircleOriented(pos.toXZ(), radius, angle, color);
	}

	public static void point(Point pos, Color color) {
		circle(new Circle(pos.copy(), 1), color, false, 1);
	}

	public static void frame(Point top_left, Point right_bottom, Color color, int width, int lifetime) {
		if (!on || !debug_form) return;
		double w = right_bottom.x - top_left.x, h = right_bottom.y - top_left.y;
/*
		form.drawLine(top_left, top_left.add(w, 0), width, color, lifetime);
		form.drawLine(right_bottom, right_bottom.sub(w, 0), width, color, lifetime);
		form.drawLine(top_left, top_left.add(0, h), width, color, lifetime);
		form.drawLine(right_bottom, right_bottom.sub(0, h), width, color, lifetime);
*/
	}

	public static void square(Point pos, double size, Color color) {
		if (!on || !debug_form) return;
		form.drawSquare(pos.toXZ(), size, color, 1, 1, true);
	}

	public static void squareRotated(Point pos, double size, double angle, Color color, int lifetime, boolean transparent) {
		if (!on || !debug_form) return;
		form.drawSquare(pos.toXZ(), size, angle, color, lifetime, 1, transparent);
	}

	public static void square(Point pos, double size, Color color, int lifetime, boolean transparent) {
		if (!on || !debug_form) return;
		form.drawSquare(pos.toXZ(), size, color, lifetime, 1, transparent);
	}

	public static void square(Point pos, double size, Color color, int lifetime, int width) {
		if (!on || !debug_form) return;
		form.drawSquare(pos.toXZ(), size, color, lifetime, width, true);
	}

	public static void square(Point pos, double size, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawSquare(pos.toXZ(), size, color, lifetime, 1, true);
	}


	public static void square3(V3 pos, double size, Color color, boolean transparent) {
		if (!on || !debug_form) return;
		form.drawSquare(pos, size, color, 1, 1, true);
	}


	public static void line(Point point1, Point point2, int width, Color color) {
		if (!on || !debug_form) return;
		form.drawLine(point1.toXZ(), point2.toXZ(), width, color);
	}

	public static void line(Point point1, Point point2, int width, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawLine(point1.toXZ(), point2.toXZ(), width, color, lifetime);
	}

	public static void line(V3 point1, V3 point2, int width, Color color) {
		if (!on || !debug_form) return;
		form.drawLine(point1.copy(), point2.copy(), width, color);
	}

	public static void line(V3 point1, V3 point2, int width, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawLine(point1.copy(), point2.copy(), width, color, lifetime);
	}

	public static void line(V3 point1, V3 point2, int width, Color color, int lifetime, DebugFormGL.DebugGroup group) {
		if (!on || !debug_form) return;
		form.drawLine(point1.copy(), point2.copy(), width, color, lifetime, group);
	}


	public static void arrow(Point point1, Point point2, double width, int arrow_width, int arrow_height, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawArrow(point1.toXZ(), point2.toXZ(), width, arrow_width, arrow_height, color, lifetime);
	}

	public static void arrow(V3 point1, V3 point2, double width, double arrow_width, double arrow_height, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawArrow(point1, point2, width, arrow_width, arrow_height, color, lifetime);
	}

	public static void arrow(Point point1, Point point2, double width, int arrow_width, int arrow_height, Color color) {
		if (!on || !debug_form) return;
		form.drawArrow(point1.toXZ(), point2.toXZ(), width, arrow_width, arrow_height, color, 3);
	}

	public static void rect(Point topleft, Point bottomright, int width, Color color) {
		if (!on || !debug_form) return;
		Vector<Point> rect = HMath.rectFromPoints(topleft, bottomright);
		for (int i = 0; i <= 3; i++) {
			form.drawLine(rect.get(i).toXZ(), rect.get((i + 1) % 4).toXZ(), width, color);
		}
	}

	public static void rect(Point topleft, Point bottomright, int width, Color color, int lifetime) {
		if (!on || !debug_form) return;
		Vector<Point> rect = HMath.rectFromPoints(topleft, bottomright);
		for (int i = 0; i <= 3; i++) {
			form.drawLine(rect.get(i).toXZ(), rect.get((i + 1) % 4).toXZ(), width, color, lifetime);
		}
	}

	public static void text(Point pos, String text, Color color) {
		if (!on || !debug_form) return;
		form.drawLabel(pos, text, color);
	}

	public static void text(Point pos, String text, Color color, int lifetime) {
		if (!on || !debug_form) return;
		form.drawLabel(pos.copy(), text, color, lifetime);
	}

	public static void text(Point pos, String text, Color color, int lifetime, double scale) {
		if (!on || !debug_form) return;
		form.drawLabel(pos.copy(), text, color, lifetime, scale);
	}

	public static void fecho(String s) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("game_result.txt", true)))) {
			out.println(s);
		} catch (IOException e) {
		}
	}

	public static void path(Point pos, ArrayList<Point> path, int width, Color color) {
		if (!on || !debug_form) return;
		Point last_p = pos.copy();
		for (Point p : path) {
			line(last_p.copy(), p.copy(), width, color);
			last_p = p.copy();
		}

	}

	public static void pathArrows(Point pos, ArrayList<Point> path, int line_width, int arrow_width, int arrow_height, Color color) {
		if (!on || !debug_form) return;
		Point last_p = pos.copy();
		for (Point p : path) {
			arrow(last_p.copy(), p.copy(), line_width, arrow_width, arrow_height, color);
			last_p = p.copy();
		}

	}
}
