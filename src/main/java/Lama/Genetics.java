package Lama;

import java.util.*;

public class Genetics<T> {
	public static Random rand = new Random(42);
	public ArrayList<Genotype<T>> genotypes;

	public Genetics() {
		genotypes = new ArrayList<>();
	}

	public static double randNextDouble() {
		double d = rand.nextDouble();
//		System.out.println(" ND " + d + " ");
		return d;
	}

	public static int randNextInt() {
		int d = rand.nextInt();
//		System.out.println(" NI " + d + " ");
		return d;
	}

	public void naturalSelection(int survivors) {
		genotypes = getTopGenotypes(survivors);
	}

	public ArrayList<Genotype<T>> getTopGenotypes(int num) {
		ArrayList<Genotype<T>> map = new ArrayList<>();
		double min_value = Double.MAX_VALUE;

		for (Genotype<T> g : genotypes) {
			if (map.size() < num) {
				map.add(g);

				if (g.score <= min_value) {
					min_value = g.score;
				}
				continue;
			}

			if (g.score <= min_value) continue;

			//			double new_min_value = 1000000;
			for (Genotype<T> gn : map) {
				if (gn.score == min_value) {
					map.remove(gn);
					break;
				}
			}

			map.add(g);

			min_value = Double.MAX_VALUE;
			for (Genotype<T> gn : map) {
				if (gn.score < min_value) {
					min_value = gn.score;
				}
				//				if (gn.score < new_min_value) {
				//					new_min_value = gn.score;
				//				}
			}
		}

		return map;
	}


	public void pushAllGenotypes(ArrayList<Genotype<T>> genotypes, int survivors_num) {
		for (Genotype g : genotypes) {
			pushGenotype(g, survivors_num);
		}
	}

	public void pushGenotype(Genotype<T> genotype, int survivors_num) {
		genotypes.add(genotype);
	}

	public static abstract class BaseGene<T> implements Copiable<BaseGene<T>> {
		public int param;
		public boolean looped = false;
		public T value;
		public T min;
		public T max;
		public T mutation_delta;
		public double min_diff;

		public BaseGene(int param, boolean looped, T min, T max, T mutation_delta, double min_diff) {
			this.param = param;
			this.looped = looped;
			this.min = min;
			this.max = max;
			this.mutation_delta = mutation_delta;
			this.min_diff = min_diff;
		}

		@Override
		public abstract BaseGene<T> copy();

		public abstract void mutate(double factor);

		@Override
		public String toString() {
			return "BaseGene{" +
				   " value=" + value +
				   ", min=" + min +
				   ", max=" + max +
				   ", param=" + param +
				   ", looped=" + looped +
				   ", mutation_delta=" + mutation_delta +
				   ", min_diff=" + min_diff +
				   '}';
		}
	}

	public static class GeneInt extends BaseGene<Integer> {
		public GeneInt(int param_, boolean looped_, Integer min_, Integer max_, Integer mutation_delta_, double min_diff) {
			super(param_, looped_, min_, max_, mutation_delta_, min_diff);
			value = min_ + (int) Math.round(randNextDouble() * (max_ - min_));
		}

		public GeneInt(int value_, int param_, boolean looped_, Integer min_, Integer max_, Integer mutation_delta_, double min_diff) {
			super(param_, looped_, min_, max_, mutation_delta_, min_diff);
			value = value_;
		}

/*		@Override
		public void populate(int param_, double min_, double max_, double delta_, boolean looped_) {
			param = param_;
			min = (int)min_;
			max = (int)max_;
			mutation_delta = (int)delta_;
			looped = looped_;
		}*/

		@Override
		public void mutate(double factor) {
			int v;
			double mdf = mutation_delta * factor;

			do {
				v = value + (int) Math.round(randNextDouble() * 2 * mdf - mdf);
			} while (Math.abs(v - value) < min_diff * mdf);


			if (v < min) {
				v = looped ? max - (min - v) : min;
			}
			if (v > max) {
				v = looped ? min + (v - max) : max;
			}
			value = v;
		}

		@Override
		public GeneInt copy() {
			return new GeneInt(value, param, looped, min, max, mutation_delta, min_diff);
		}
	}

	public static class GeneDouble extends BaseGene<Double> {
		public GeneDouble(int param_, boolean looped_, Double min_, Double max_, Double mutation_delta_, double min_diff) {
			super(param_, looped_, min_, max_, mutation_delta_, min_diff);
			value = min_ + randNextDouble() * (max_ - min_);
		}

		public GeneDouble(double value_, int param_, boolean looped_, Double min_, Double max_, Double mutation_delta_, double min_diff) {
			super(param_, looped_, min_, max_, mutation_delta_, min_diff);
			value = value_;
		}

/*
		@Override
		public void populate(int param_, double min_, double max_, double delta_, boolean looped_) {
			param = param_;
			min = min_;
			max = max_;
			mutation_delta = delta_;
			looped = looped_;
			value = min_ + randNextDouble() * (max_ - min_);
		}
*/

		@Override
		public void mutate(double factor) {
			double v;
			double mdf = mutation_delta * factor;

			do {
				v = value + randNextDouble() * 2.0 * mdf - mdf;
			} while (Math.abs(v - value) < min_diff * mdf);

			if (v < min) {
				v = looped ? max - (min - v) : min;
			}
			if (v > max) {
				v = looped ? min + (v - max) : max;
			}
			value = v;
		}

		@Override
		public GeneDouble copy() {
			return new GeneDouble(value, param, looped, min, max, mutation_delta, min_diff);
		}
	}

	public static class RepeatableActionGene<T1 extends BaseGene> implements Copiable<RepeatableActionGene> {
		public T1 action_gene;
		public GeneInt repeats_gene;
		public BaseGeneSnapshot snapshot;

		public RepeatableActionGene(T1 action_gene, GeneInt repeats_gene) {
			this.action_gene = action_gene;
			this.repeats_gene = repeats_gene;
		}

		public RepeatableActionGene(T1 action_gene, GeneInt repeats_gene, BaseGeneSnapshot snapshot) {
			this.action_gene = action_gene;
			this.repeats_gene = repeats_gene;
			this.snapshot = snapshot;
		}

		public void setSnapshot(BaseGeneSnapshot snap) {
			this.snapshot = snap;
		}

		@Override
		public RepeatableActionGene copy() {
			return new RepeatableActionGene(action_gene.copy(), (GeneInt) repeats_gene.copy(), snapshot.copy());
		}
	}

	public static class Genotype<T> implements Copiable<Genotype> {
		public static int last_id = 0;
		public int id;
		public ArrayList<RepeatableActionGene> genes;
		public GeneDouble nitro_gene;
		public V3 ball_defender_kick_to;
		public double score = -Double.MAX_VALUE;
		public Score debug_score;
		public int group_id = -1;

		public Genotype() {
			this.genes = new ArrayList<>();
			last_id++;
			this.id = last_id;
			nitro_gene = null;
			ball_defender_kick_to = null;
		}

		public Genotype(int id_) {
			this.genes = new ArrayList<>();
			this.id = id_;
			nitro_gene = null;
			ball_defender_kick_to = null;
		}

		public Genotype<T> copy() {
			Genotype<T> result = new Genotype<>(id);
			for (RepeatableActionGene g : genes) {
				result.genes.add(g.copy());
			}
			result.nitro_gene = nitro_gene == null ? null : nitro_gene.copy();
			result.ball_defender_kick_to = ball_defender_kick_to == null ? null : ball_defender_kick_to.copy();
			return result;
		}

		@Override
		public String toString() {
			StringBuilder ss = new StringBuilder();
			int len = 0;

			for (RepeatableActionGene g : genes) {
				ss.append(g.repeats_gene.value).append(",");
				len += g.repeats_gene.value;
			}

			return "Genotype{" +
				   "score=" + score +
				   ", debug_score=" + (debug_score != null ? debug_score.total : "-") +
				   ", seq=" + ss + "->" + len +
				   ", kick=" + ball_defender_kick_to +
				   '}';

		}
	}
}
