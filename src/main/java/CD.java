import Lama.HMath;
import Lama.Perf;
import Lama.Point;
import Lama.V3;
import model.Arena;

public class CD {
	private static final boolean DEBUG_CD = false;

	public static final V3 v_floor = V3.zero();
	public static final V3 v_up = v(0, 1, 0);
	public static final V3 v_down = v(0, -1, 0);
	public static V3 v_ceiling;
	public static V3 v_side_x;
	public static V3 v_side_z_g;
	public static V3 v_side_z;
	public static double A_HALF_HEIGHT;
	public static double A_HALF_WIDTH;
	public static double A_HALF_DEPTH;
	public static double A_HALF_GOAL_WIDTH;
	public static double A_HALF_GOAL_DEPTH;
	public static double A_G_HEIGHT_PLUS_G_TOP_R;
	public static double A_G_TOP_R_PLUS_G_SIDE_R;
	public static double A_G_TOP_R_PLUS_G_SIDE_R_2;
	public static double A_BOTTOM_R_PLUS_G_SIDE_R;
	public static double A_BOTTOM_R_PLUS_G_SIDE_R_2;
	private static Point vv;
	private static Point po;

/*
	private static Result min(Result d1, Result d2) {
		return d1.distance <= d2.distance ? d1 : d2;
	}
*/

	private static V3 v(double x, double y, double z) {
		return new V3(x, y, z);
	}

	private static Point p(double x, double y) {
		return new Point(x, y);
	}

	public static void danToPlane(Result dan, double x, double y, double z, double ox, double oy, double oz, double pnx, double pny, double pnz) {
		dan.set(
//			point.sub(point_on_plane).dotProduct(plane_normal),
			(x - ox) * pnx + (y - oy) * pny + (z - oz) * pnz,
			pnx, pny, pnz
		);
	}

	public static void danToPlaneMin(Result dan, double x, double y, double z, double ox, double oy, double oz, double pnx, double pny, double pnz) {
		double dist = (x - ox) * pnx + (y - oy) * pny + (z - oz) * pnz;

		if (dist < dan.distance) {
			dan.set(
				dist,
				pnx, pny, pnz
			);
		}
	}

	public static Result danToSphereInner(double x, double y, double z, double sphere_center_x, double sphere_center_y, double sphere_center_z, double sphere_radius) {

		double ln = Math.sqrt((sphere_center_x - x) * (sphere_center_x - x) + (sphere_center_y - y) * (sphere_center_y - y) + (sphere_center_z - z) * (sphere_center_z - z));

//		V3 p_to_sc = sphere_center.sub(x, y, z);
//		double l = p_to_sc.len();

		return new Result(
			sphere_radius - ln,
//			v(p_to_sc.x / l, p_to_sc.y / l, p_to_sc.z / l)
			(sphere_center_x - x) / ln, (sphere_center_y - y) / ln, (sphere_center_z - z) / ln

//			sphere_radius - point.sub(sphere_center).len(),
//			sphere_center.sub(point).normalize()
		);
	}

	public static void danToSphereInnerMin(Result dan, double x, double y, double z, double sphere_center_x, double sphere_center_y, double sphere_center_z, double sphere_radius) {
		double ln = Math.sqrt((sphere_center_x - x) * (sphere_center_x - x) + (sphere_center_y - y) * (sphere_center_y - y) + (sphere_center_z - z) * (sphere_center_z - z));
		double dist = sphere_radius - ln;

		if (dist < dan.distance) {
			dan.set(
				dist,
				(sphere_center_x - x) / ln, (sphere_center_y - y) / ln, (sphere_center_z - z) / ln
			);
		}
	}

	public static Result danToSphereOuter(double x, double y, double z, double sphere_center_x, double sphere_center_y, double sphere_center_z, double sphere_radius) {

		double ln = Math.sqrt((x - sphere_center_x) * (x - sphere_center_x) + (y - sphere_center_y) * (y - sphere_center_y) + (z - sphere_center_z) * (z - sphere_center_z));

//		V3 p_to_sc = v(x - sphere_center.x, y - sphere_center.y, z - sphere_center.z);
//		double l = p_to_sc.len();

		return new Result(
			ln - sphere_radius,
			(x - sphere_center_x) / ln, (y - sphere_center_y) / ln, (z - sphere_center_z) / ln
//			point.sub(sphere_center).len() - sphere_radius,
//			point.sub(sphere_center).normalize()
		);
	}

	public static void danToSphereOuterMin(Result dan, double x, double y, double z, double sphere_center_x, double sphere_center_y, double sphere_center_z, double sphere_radius) {
		double ln = Math.sqrt((x - sphere_center_x) * (x - sphere_center_x) + (y - sphere_center_y) * (y - sphere_center_y) + (z - sphere_center_z) * (z - sphere_center_z));
		double dist = ln - sphere_radius;

		if (dist < dan.distance) {
			dan.set(
				dist,
				(x - sphere_center_x) / ln, (y - sphere_center_y) / ln, (z - sphere_center_z) / ln
			);
		}
	}

	public static void setup(Arena arena) {
		A_HALF_HEIGHT = arena.height / 2.0;
		A_HALF_WIDTH = arena.width / 2.0;
		A_HALF_DEPTH = arena.depth / 2.0;
		A_HALF_GOAL_WIDTH = arena.goal_width / 2.0;
		A_HALF_GOAL_DEPTH = arena.goal_depth / 2.0;
		A_G_HEIGHT_PLUS_G_TOP_R = arena.goal_height + arena.goal_top_radius;
		A_G_TOP_R_PLUS_G_SIDE_R = arena.goal_top_radius + arena.goal_side_radius;
		A_G_TOP_R_PLUS_G_SIDE_R_2 = A_G_TOP_R_PLUS_G_SIDE_R * A_G_TOP_R_PLUS_G_SIDE_R;

		A_BOTTOM_R_PLUS_G_SIDE_R = arena.bottom_radius + arena.goal_side_radius;
		A_BOTTOM_R_PLUS_G_SIDE_R_2 = A_BOTTOM_R_PLUS_G_SIDE_R * A_BOTTOM_R_PLUS_G_SIDE_R;

		v_ceiling = v(0, arena.height, 0);
		v_side_x = v(A_HALF_WIDTH, 0, 0);
		v_side_z = v(0, 0, A_HALF_DEPTH);
		v_side_z_g = v(0, 0, A_HALF_DEPTH + arena.goal_depth);
	}

	public static void danToArenaQuarter(Result dan, double x, double y, double z, double rad) {
		// Ground

		if (DEBUG_CD) Perf.start("cd.part1");

		if (vv == null) vv = Point.zero();
		if (po == null) po = Point.zero();

		if (y <= rad) {
			danToPlane(dan, x, y, z, v_floor.x, v_floor.y, v_floor.z, v_up.x, v_up.y, v_up.z);
		} else {
			dan.set(y, v_up.x, v_up.y, v_up.z);

			// Ceiling
			if (y >= A_HALF_HEIGHT) {
				//if (Debug.on && Glob.test1) Debug.echo("1");
				danToPlaneMin(dan, x, y, z, v_ceiling.x, v_ceiling.y, v_ceiling.z, v_down.x, v_down.y, v_down.z);
			}
		}


		// Side x
		if (x >= A_HALF_WIDTH - rad) {
			//if (Debug.on && Glob.test1) Debug.echo("2");
			danToPlaneMin(dan, x, y, z, v_side_x.x, v_side_x.y, v_side_x.z, -1, 0, 0);
		}

		if (DEBUG_CD) Perf.change("cd.part2");

		// Side z
		if (z >= A_HALF_DEPTH - rad) {
			if (z >= A_HALF_DEPTH + Glob.arena.goal_depth - rad) {
				// Side z (goal)
				danToPlaneMin(dan, x, y, z, v_side_z_g.x, v_side_z_g.y, v_side_z_g.z, 0, 0, -1);
			}

			//if (Debug.on && Glob.test1) Debug.echo("4");
			p(vv,
				x - A_HALF_GOAL_WIDTH - Glob.arena.goal_top_radius,
				y - A_G_HEIGHT_PLUS_G_TOP_R
			);

			if (
				(x >= A_HALF_GOAL_WIDTH + Glob.arena.goal_side_radius)
				|| (y >= Glob.arena.goal_height + Glob.arena.goal_side_radius)
				|| (vv.x > 0 && vv.y > 0 && vv.len2() >= A_G_TOP_R_PLUS_G_SIDE_R_2)
				) {

				danToPlaneMin(dan, x, y, z, v_side_z.x, v_side_z.y, v_side_z.z, 0, 0, -1);
			}
		}

		// Side x & ceiling (goal)
		if (z >= A_HALF_DEPTH + Glob.arena.goal_side_radius) {
			//if (Debug.on && Glob.test1) Debug.echo("5");
			// x
			danToPlaneMin(dan, x, y, z, A_HALF_GOAL_WIDTH, 0, 0, -1, 0, 0);

			// y
			danToPlaneMin(dan, x, y, z, 0, Glob.arena.goal_height, 0, 0, -1, 0);
		}

//		 Goal back corners
//		assert Glob.arena.bottom_radius == Glob.arena.goal_top_radius;


		if (z > A_HALF_DEPTH + Glob.arena.goal_depth - Glob.arena.bottom_radius) {
			danToSphereInnerMin(dan,
				x, y, z,
				HMath.clamp(
					x,
					Glob.arena.bottom_radius - A_HALF_GOAL_WIDTH,
					A_HALF_GOAL_WIDTH - Glob.arena.bottom_radius
				),
				HMath.clamp(
					y,
					Glob.arena.bottom_radius,
					Glob.arena.goal_height - Glob.arena.goal_top_radius
				),
				(Glob.arena.depth / 2) + Glob.arena.goal_depth - Glob.arena.bottom_radius,
				Glob.arena.bottom_radius);
		}


		if (DEBUG_CD) Perf.change("cd.part3");

		// Corner
		if (x > A_HALF_WIDTH - Glob.arena.corner_radius && z > A_HALF_DEPTH - Glob.arena.corner_radius) {
			//if (Debug.on && Glob.test1) Debug.echo("6");
			danToSphereInnerMin(dan,
				x, y, z,
				A_HALF_WIDTH - Glob.arena.corner_radius, y, A_HALF_DEPTH - Glob.arena.corner_radius,
				Glob.arena.corner_radius);
		}

		// Goal outer corner
		// mine (if)
		if (z < A_HALF_DEPTH + Glob.arena.goal_side_radius
			&& z > A_HALF_DEPTH - Glob.arena.bottom_radius - rad
			/*&& point.x > A_HALF_GOAL_WIDTH - Glob.arena.bottom_radius - rad
			&& point.x < A_HALF_GOAL_WIDTH + Glob.arena.bottom_radius + rad*/) {

			//if (Debug.on && Glob.test1) Debug.echo("7");
			// Side x
			if (x < A_HALF_GOAL_WIDTH + Glob.arena.goal_side_radius) {
				danToSphereOuterMin(dan,
					x, y, z,
					A_HALF_GOAL_WIDTH + Glob.arena.goal_side_radius, y, A_HALF_DEPTH + Glob.arena.goal_side_radius,
					Glob.arena.goal_side_radius);
			}

			// Ceiling
			if (y < Glob.arena.goal_height + Glob.arena.goal_side_radius) {
				danToSphereOuterMin(dan,
					x, y, z,
					x, Glob.arena.goal_height + Glob.arena.goal_side_radius, A_HALF_DEPTH + Glob.arena.goal_side_radius,
					Glob.arena.goal_side_radius);
			}

			// Top corner
			p(vv,
				A_HALF_GOAL_WIDTH - Glob.arena.goal_top_radius,
				Glob.arena.goal_height - Glob.arena.goal_top_radius
			);

			if (x > vv.x && y > vv.y) {
				p(po, x - vv.x, y - vv.y);
				po.Normalize();
				double poa = Glob.arena.goal_top_radius + Glob.arena.goal_side_radius;
//				o.Add(po.normalize().scale(Glob.arena.goal_top_radius + Glob.arena.goal_side_radius));
				vv.Add(po.x * poa, po.y * poa);
				danToSphereOuterMin(dan, x, y, z, vv.x, vv.y, A_HALF_DEPTH + Glob.arena.goal_side_radius, Glob.arena.goal_side_radius);
			}
		}

		// Goal inside top corners
		if (z > A_HALF_DEPTH + Glob.arena.goal_side_radius && y > Glob.arena.goal_height - Glob.arena.goal_top_radius) {
			//if (Debug.on && Glob.test1) Debug.echo("8");
			// Side x
			if (x > A_HALF_GOAL_WIDTH - Glob.arena.goal_top_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					A_HALF_GOAL_WIDTH - Glob.arena.goal_top_radius,
					Glob.arena.goal_height - Glob.arena.goal_top_radius,
					z,
					Glob.arena.goal_top_radius
				);
			}
			// Side z
			if (z > A_HALF_DEPTH + Glob.arena.goal_depth - Glob.arena.goal_top_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					x,
					Glob.arena.goal_height - Glob.arena.goal_top_radius,
					A_HALF_DEPTH + Glob.arena.goal_depth - Glob.arena.goal_top_radius,
					Glob.arena.goal_top_radius
				);
			}
		}

		if (DEBUG_CD) Perf.change("cd.part4");

		// Bottom corners
		if (y < Glob.arena.bottom_radius) {

			//if (Debug.on && Glob.test1) Debug.echo("9");
			// Side x
			if (x > A_HALF_WIDTH - Glob.arena.bottom_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					(Glob.arena.width / 2) - Glob.arena.bottom_radius,
					Glob.arena.bottom_radius,
					z,
					Glob.arena.bottom_radius);
			}

			// Side z
			if (z > A_HALF_DEPTH - Glob.arena.bottom_radius && x >= A_HALF_GOAL_WIDTH + Glob.arena.goal_side_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					x,
					Glob.arena.bottom_radius,
					(Glob.arena.depth / 2) - Glob.arena.bottom_radius,
					Glob.arena.bottom_radius);
			}

			// Side z (goal)
			if (z > A_HALF_DEPTH + Glob.arena.goal_depth - Glob.arena.bottom_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					x,
					Glob.arena.bottom_radius,
					(Glob.arena.depth / 2) + Glob.arena.goal_depth - Glob.arena.bottom_radius,
					Glob.arena.bottom_radius);
			}

			if (z > A_HALF_DEPTH - Glob.arena.corner_radius - rad) {
				// Goal outer corner
				p(vv,
					(Glob.arena.goal_width / 2) + Glob.arena.goal_side_radius,
					(Glob.arena.depth / 2) + Glob.arena.goal_side_radius
				);

				p(po, x - vv.x, z - vv.y);

				if (po.x < 0 && po.y < 0 && po.len2() < A_BOTTOM_R_PLUS_G_SIDE_R_2) {
					double poa = Glob.arena.goal_side_radius + Glob.arena.bottom_radius;
					po.Normalize();
					vv.Add(po.x * poa, po.y * poa);
					danToSphereInnerMin(dan,
						x, y, z,
						vv.x, Glob.arena.bottom_radius, vv.y,
						Glob.arena.bottom_radius
					);
				}
			}

			// Side x (goal)
			if (z >= A_HALF_DEPTH + Glob.arena.goal_side_radius && x > A_HALF_GOAL_WIDTH - Glob.arena.bottom_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					(Glob.arena.goal_width / 2) - Glob.arena.bottom_radius,
					Glob.arena.bottom_radius,
					z,
					Glob.arena.bottom_radius);
			}

			// Corner
			if (x > A_HALF_WIDTH - Glob.arena.corner_radius && z > A_HALF_DEPTH - Glob.arena.corner_radius) {
				p(vv,
					(Glob.arena.width / 2) - Glob.arena.corner_radius,
					(Glob.arena.depth / 2) - Glob.arena.corner_radius
				);

				p(po, x, z);
				po.Sub(vv);
				double dist2 = po.len2();
				double d = Glob.arena.corner_radius - Glob.arena.bottom_radius;

				if (dist2 > d * d) {
					po.Scale((Glob.arena.corner_radius - Glob.arena.bottom_radius) / Math.sqrt(dist2));
					po.Add(vv);
					danToSphereInnerMin(dan,
						x, y, z,
						po.x, Glob.arena.bottom_radius, po.y,
						Glob.arena.bottom_radius);
				}
			}
		}

		if (DEBUG_CD) Perf.change("cd.part5");

		// Ceiling corners
		if (y > Glob.arena.height - Glob.arena.top_radius) {
			//if (Debug.on && Glob.test1) Debug.echo("0");

			// Side x
			if (x > (Glob.arena.width / 2) - Glob.arena.top_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					(Glob.arena.width / 2) - Glob.arena.top_radius,
					Glob.arena.height - Glob.arena.top_radius,
					z,
					Glob.arena.top_radius);
			}

			// Side z
			if (z > (Glob.arena.depth / 2) - Glob.arena.top_radius) {
				danToSphereInnerMin(dan,
					x, y, z,
					x,
					Glob.arena.height - Glob.arena.top_radius,
					(Glob.arena.depth / 2) - Glob.arena.top_radius,
					Glob.arena.top_radius);
			}

			// Corner
			if (x > (Glob.arena.width / 2) - Glob.arena.corner_radius && z > (Glob.arena.depth / 2) - Glob.arena.corner_radius) {
				p(vv,
					(Glob.arena.width / 2) - Glob.arena.corner_radius,
					(Glob.arena.depth / 2) - Glob.arena.corner_radius
				);

				p(po, x, z);
				po.Sub(vv);
				double d = Glob.arena.corner_radius - Glob.arena.top_radius;

				if (po.len2() > d * d) {
					po = po.normalize();
					double da = Glob.arena.corner_radius - Glob.arena.top_radius;
					vv = vv.add(po.x * da, po.y * da);

					danToSphereInnerMin(dan, x, y, z, vv.x, Glob.arena.height - Glob.arena.top_radius, vv.y, Glob.arena.top_radius);
				}
			}
		}

		if (DEBUG_CD) Perf.stop("cd.part5");
	}

	private static Point p(Point pnt, double x, double y) {
		if (pnt == null) {
			pnt = new Point(x, y);
		} else {
			pnt.x = x;
			pnt.y = y;
		}

		return pnt;
	}

	public static void danToArena(Result result, double x, double y, double z, double rad) {
		boolean negate_x = x < 0;
		boolean negate_z = z < 0;

		if (negate_x) {
			x = -x;
		}
		if (negate_z) {
			z = -z;
		}

		danToArenaQuarter(result, x, y, z, rad);
		if (negate_x) {
			result.x = -result.x;
		}
		if (negate_z) {
			result.z = -result.z;
		}
	}

	public static class Result {
		double distance;
		double x;
		double y;
		double z;

		public Result(double distance, double x_, double y_, double z_) {
			this.distance = distance;
			this.x = x_;
			this.y = y_;
			this.z = z_;
		}

		public void set(double distance_, double x_, double y_, double z_) {
			distance = distance_;
			x = x_;
			y = y_;
			z = z_;
		}
	}
}
