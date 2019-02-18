package Lama;

import Lama.Renderer.DebugBase;

import java.util.*;

/**
 * Created by lamik on 18.11.2016.
 */
public class Perf {
	public static final boolean enabled = DebugBase.on;
//	public static final boolean on = false;

	public static class PerfItem {
		String name;
		int times;
		int last_turn;
		int turns;
		long total_time;
		long started_at;	
		boolean running;

		public PerfItem(String name) {
			this.name = name;
			this.running = true;
			this.started_at = System.currentTimeMillis();
			this.times = 1;
			this.total_time = 0;
			this.last_turn = DebugBase.tick;
			this.turns = 1;
		}

		public void start() {
			if (this.running) this.stop();

			if (last_turn != DebugBase.tick) this.turns++;
			this.running = true;
			this.started_at = System.currentTimeMillis();
			this.times++;
			this.last_turn = DebugBase.tick;
		}

		public void stop() {
			this.running = false;
			this.total_time += System.currentTimeMillis() - this.started_at;
		}

		@Override
		public String toString() {
//			return "PerfItem[" + name + "] x\t" + times + "\tavg_per_run:\t" + (int)Math.round(total_time/times) + "\tavg_per_turn:\t" + (int)Math.round(total_time/turns) + "\ttotal_time:\t" + total_time;
			return " times:\t" + times + "\tavg_per_run:\t" + (int)Math.round(total_time/times) + "\tavg_per_turn:\t" + (int)Math.round(total_time/turns) + "\ttotal_time:\t" + total_time;
		}
	}

	public static HashMap<String, PerfItem> counter = new HashMap<>();
	public static String last_name = "";

	public static void start(String name) {
		if (!enabled || !DebugBase.on) return;

		last_name = name;

		PerfItem pi = counter.get(name);
		if (pi == null) {
			pi = new PerfItem(name);
			counter.put(name, pi);
			return;
		}

		pi.start();
	}

	public static void stop(String name) {
		if (!enabled || !DebugBase.on) return;

		last_name = "";

		PerfItem pi = counter.get(name);
		pi.stop();
	}

	public static void change(String name) {
		if (!enabled || !DebugBase.on) return;

		if (!last_name.equals("")) stop(last_name);
		start(name);
	}

	public static void print() {
		if (!DebugBase.on || !enabled) return;

		Map<String,ArrayList<String>> output = new HashMap<>();
		int max_width = 0;

		for (PerfItem pi : counter.values()) {
			String name = pi.name;
			if (name.length() > max_width) max_width = name.length();

			String prefix = name.contains(".") ? name.substring(0, name.indexOf(".")) : name;
			String suffix = name.contains(".") ? name.substring(name.indexOf(".") + 1) : "";

			ArrayList<String> list = output.containsKey(prefix) ? output.get(prefix) : new ArrayList<>();
			list.add(suffix.equals("") ? "VALUE" : suffix);

			if (!output.containsKey(prefix)) output.put(prefix, list);
		}

		max_width += 4;

		DebugBase.echo(" \nPerformance:");

		for (Map.Entry<String,ArrayList<String>> entry : output.entrySet()) {
			String n = entry.getKey();

			System.out.print( String.format("%1$-" + (max_width) + "s", n) + " :: ");
			ArrayList<String> strings = entry.getValue();

			if (strings.contains("VALUE")) {
				System.out.println(counter.get(entry.getKey()));
				strings.remove("VALUE");
			} else {
				System.out.println();
			}

			for (String param : strings) {
				String name = entry.getKey() + (param.equals("VALUE") ? "" : "." + param);

//				Debug.echo(() -> "     " + param + " => " + counter.get(name));
				System.out.println( "    " + String.format("%1$-" + (max_width - 4) + "s", param) + " :: " + counter.get(name));
			}
		}
	}


}
