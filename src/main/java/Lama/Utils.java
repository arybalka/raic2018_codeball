package Lama;

import java.util.ArrayList;

public class Utils {
	public static ArrayList<? extends Copiable> copyArrayList(ArrayList<? extends Copiable> from_) {
		ArrayList<Copiable> result = new ArrayList<>();

		for (Copiable cp : from_) {
			result.add((Copiable) cp.copy());
		}

		return result;
	}

	public static <T extends Copiable> T[] copyArray(T[] from_) {
		T[] result = (T[]) new Copiable[from_.length];

		for (int i = 0; i < from_.length; i++) {
			Copiable cp = from_[i];
			result[i] = (T) cp.copy();
		}

		return result;
	}

	public static <T extends V3> T getClosest(V3 target, ArrayList<T> list) {
		double min_dist = Double.MAX_VALUE;
		T result = null;

		for (T v : list) {
			double d2 = v.distance2(target);
			if (d2 < min_dist) {
				min_dist = d2;
				result = v;
			}
		}

		return result;
	}

	public static <T extends V3> T getClosest(V3 target, T[] list) {
		double min_dist = Double.MAX_VALUE;
		T result = null;

		for (T v : list) {
			double d2 = v.distance2(target);
			if (d2 < min_dist) {
				min_dist = d2;
				result = v;
			}
		}

		return result;
	}
}
