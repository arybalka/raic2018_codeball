package Lama;

import java.util.HashMap;
import java.util.Map;

public class Score<T extends Enum> implements Copiable<Score<T>> {
	public int id;
	public HashMap<T, Double> score_map = new HashMap<>();
	public double total = 0;
	public double multiplier = 1.0;
	public double tick_value = 0.0;

	public void add(T type, double value) {
		if (value == 0) return;

		double val = score_map.getOrDefault(type, 0.0);

		value *= multiplier;
		val += value;

		score_map.put(type, val);

		tick_value += value;
		total += value;
	}

	public Score(int genotype_id) {
		this.id = genotype_id;
	}

	public void bonus(T type, double value) {
		add(type, value);
	}

	public String getDebugInfo(String div, int decimals) {
		StringBuilder str = new StringBuilder();

		str.append("ID ").append(id).append("\n");

		for (Map.Entry<T, Double> entry : score_map.entrySet()) {
			if (entry.getValue() == 0) continue;
			str.append("    ").append(entry.getKey()).append(" -> ").append(HMath.round(entry.getValue(), decimals)).append(div);
		}
		str.append("  @ TOTAL: ").append(HMath.round(total, decimals));
		return str.toString();
	}

	public void begin(double score_mult) {
		multiplier = score_mult;
		tick_value = 0.0;
	}

	public double end() {
		return tick_value;
	}

	@Override
	public Score<T> copy() {
		Score<T> s = new Score<>(id);
		s.score_map = new HashMap<>();
		for (Map.Entry<T, Double> entry : this.score_map.entrySet()) {
			s.score_map.put(entry.getKey(), entry.getValue() + 0);
		}
		s.total = total;
		s.multiplier = multiplier;
		s.tick_value = tick_value;

		return s;
	}

	@Override
	public String toString() {
		return "Score{" +
			   "id=" + id +
			   ", total=" + total +
			   ", multiplier=" + multiplier +
			   ", tick_value=" + tick_value +
			   ", stats: " + getDebugInfo("; ", 2) +
			   '}';
	}
}
