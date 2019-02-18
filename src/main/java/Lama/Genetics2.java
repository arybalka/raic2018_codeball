package Lama;

import java.util.*;

public class Genetics2<T> {
	public ArrayList<Genetics.Genotype<T>> genotypes;

	public Genetics2() {
		genotypes = new ArrayList<>();
	}

	public void naturalSelection(int survivors) {}


	public void pushAllGenotypes(ArrayList<Genetics.Genotype<T>> genotypes, int survivors_num) {
		for (Genetics.Genotype g : genotypes) {
			pushGenotype(g, survivors_num);
		}
	}
	public void pushGenotype(Genetics.Genotype<T> genotype, int survivors_num) {
		if (genotypes.size() < survivors_num) {
			genotypes.add(genotype);
			return;
		}

		Genetics.Genotype<T> min_genotype = null;
		for (Genetics.Genotype<T> g : genotypes) {
			if (min_genotype == null || g.score < min_genotype.score) {
				min_genotype = g;
			}
		}

		if (min_genotype == null) return;
		if (min_genotype.score > genotype.score) return;

		genotypes.remove(min_genotype);
		genotypes.add(genotype);
	}
}
