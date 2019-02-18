import Lama.Point;

import Lama.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class DecisionMaker {
	public static final int ACCURATES_LIMIT = 5;
	final static boolean DEBUG = false;
	static int GENERATIONS_NUM = 10;
	static int FIRST_GENERATION_GENES_NUM;
	final static int ACTIONS_PER_GENE = 40;
	final static int SURVIVORS_NUM = 10;
	final static int MUTANTS_PER_GENERATION = 9;
	static int SIMULATE_BALL_AFTER_SEQ_TICKS;
	static int SIMULATE_BALL_AFTER_SEQ_TICKS_DEFENDER;

	final static double MUT_MIN_DIFF = 0.2;

	public static int actions_per_gene_current = ACTIONS_PER_GENE;
	public static boolean no_jumps = false;

	public static int G_PARAM_MOVE = 0;
	public static int G_PARAM_JUMP = 1;
	public static int G_PARAM_NITRO = 2;
	public static int G_PARAM_REPEATS = 10;

	public static int debug_evaluated_times = 0;
	public static Genetics.Genotype<Act>[] best_genotypes = null;

	private static int createGene(Dude dude, Genetics.Genotype<Act> genotype, Double action_val, int actions_per_gene, int actions_num) {
		boolean early_round = !Glob.TEST_DEFENCE_ONLY && !Glob.goal_scored && Glob.last_round_started_tick + 40 > Glob.tick;
		Genetics.GeneDouble action;

		if (action_val != null) {
			action = new Genetics.GeneDouble(action_val, G_PARAM_MOVE, true, -Math.PI, Math.PI, HMath.ONE_DEGREE * 15.0, MUT_MIN_DIFF);
		} else {
			if (early_round) {
				double angle = dude.pointFromXZ().getAngleTo(Glob.ball.pointFromXZ());
				angle += -HMath.TEN_DEGREES + Genetics.randNextDouble() * HMath.TEN_DEGREES * 2.0;

				action = new Genetics.GeneDouble(angle, G_PARAM_MOVE, true, angle - HMath.TEN_DEGREES, angle + HMath.TEN_DEGREES, HMath.ONE_DEGREE * 15.0, MUT_MIN_DIFF);
			} else {
				action = new Genetics.GeneDouble(G_PARAM_MOVE, true, -Math.PI, Math.PI, HMath.ONE_DEGREE * 15.0, MUT_MIN_DIFF);
			}
		}

		Genetics.GeneDouble jump;
		if (action_val != null || no_jumps) {
			jump = new Genetics.GeneDouble(0, G_PARAM_JUMP, true, 0.0, 1.0, 1.0, MUT_MIN_DIFF);
		} else {
			jump = new Genetics.GeneDouble(G_PARAM_JUMP, true, 0.0, 1.0, 1.0, MUT_MIN_DIFF);
		}

		Genetics.GeneInt repeats = new Genetics.GeneInt(actions_num == 0 ? 1 : 5, G_PARAM_REPEATS, false, 1, 10, 5, MUT_MIN_DIFF);

		if (actions_num + repeats.value > actions_per_gene) repeats.value = actions_per_gene - actions_num;
		actions_num += repeats.value;

		CBGene pair = new CBGene(action, jump, repeats);

		genotype.genes.add(pair);

		return actions_num;
	}

	public static void findOptimalActionSequence(Dude dude, boolean is_attacker, int num_dudes_to_act, boolean is_closest_attacker_to_ball) {
		Perf.start("find_action");
		debug_evaluated_times = 0;

		Scores.reset();

		{
			Perf.start("genotype.init");

			G<Act> genetics = new G<>();
			boolean do_genetics = dude.touch
								  && (!is_attacker || Glob.ball_next_landing_target[dude.dude_id] != null
									  || dude.distanceLess(Glob.ball, 25.0));

			HashSet<Dude> dudes_to_sim_hs = new HashSet<>();
			HashSet<Dude> skipped_enemies_hs = new HashSet<>();

			if (CFG.HAS_NITRO && dude.nitro > 0 && !dude.touch && dude.distanceMore(Glob.ball, 4.0)) {
				Dude test = dude.copy();
				int t = 0;
				Scores.nitro_in_air_can_touch_ball = false;
				while (test.nitro > 0) {
					TheBall b = Glob.trajectory[t].copy();
					b.touch = false;

					Act a = new Act(test.dirTo(b).NormalizeFast().Scale(100), 0, true);
					test.Sim(a, b);

					if (b.touch || test.distanceLessEq(b, 3.15)) {
						Scores.nitro_in_air_can_touch_ball = true;
						break;
					}
					t++;
					if (t >= Glob.trajectory.length) break;
				}
			}

			double genetics_mult = (1.0 - ((dude.distanceTo(Glob.ball) - 5.0)) / 40.0) * (is_attacker ? (Glob.tick % 2 == 1 || num_dudes_to_act > 1 ? 0.5 : 1.0) : 1.0);

			if (Glob.trajectory[20] != null) {
				double genetics_mult2 = (1.0 - ((dude.distanceTo(Glob.trajectory[20]) - 5.0)) / 40.0) * (is_attacker ? (Glob.tick % 2 == 1 || num_dudes_to_act > 1 ? 0.5 : 1.0) : 1.0);
				if (genetics_mult2 > genetics_mult) genetics_mult = genetics_mult2;
			}

			int gen_num = HMath.clamp((int) Math.round((GENERATIONS_NUM + 1) * genetics_mult), is_attacker ? 3 : 5, GENERATIONS_NUM);
			int surv_num = HMath.clamp((int) Math.round((SURVIVORS_NUM + 2) * genetics_mult), is_attacker ? 3 : 5, SURVIVORS_NUM);
			int muts_num = HMath.clamp((int) Math.round((MUTANTS_PER_GENERATION + 2) * genetics_mult), is_attacker ? 3 : 5, MUTANTS_PER_GENERATION);
			actions_per_gene_current = ACTIONS_PER_GENE;
			no_jumps = false;

			if (is_attacker && dude.distanceMore(Glob.ball, 25)) {
				actions_per_gene_current = 75;
				no_jumps = true;
				gen_num = Math.min(gen_num, 5);
			}

			for (Dude d : Glob.dudes) {
				Dude dc = d; //.copy();

				if (dc.id == dude.id) {
					dudes_to_sim_hs.add(dude);
					continue;
				}

				if ((!dc.touch || dc.touch_ball) && (dc.distanceLess(dude, 20) || dc.distanceLess(Glob.ball, 15)) || dc.distanceLess(Glob.trajectory[15], 10)) {
					dudes_to_sim_hs.add(dc);
					continue;
				}

				if (!dc.is_mate) {
					if (dc.distanceLess(dude, 5.0) || dc.distanceLess(Glob.ball, 5.0)) {
						dudes_to_sim_hs.add(dc);
					} else {
						skipped_enemies_hs.add(dc);
					}
					continue;
				}
			}

			for (Dude d : dudes_to_sim_hs) {
				if (d.is_mate) continue;
				if (Glob.dudes_movement[d.id - 1] != null) continue;

				predictEnemyMovement(d);
			}

			if (Debug.on) {
				for (Dude d : dudes_to_sim_hs) Debug.square3(d.add(0, -0.9, 0), 2.0, Color.red, true);
			}

			Dude[] dudes_to_sim = dudes_to_sim_hs.toArray(new Dude[0]);
			Dude[] skipped_enemies = skipped_enemies_hs.toArray(new Dude[0]);

			if (is_attacker) calcEnemiesToMeTrajectories(dude, Glob.enemy_team);

			// Add best genotype from previous tick

			if (best_genotypes[dude.dude_id] != null && best_genotypes[dude.dude_id].genes.size() > 0) {
				addPreviousBestGenotype(dude, is_attacker, do_genetics, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball);

				if (best_genotypes[dude.dude_id] != null) {
					genetics.pushGenotype(best_genotypes[dude.dude_id], surv_num);
				}
			}

			best_genotypes[dude.dude_id] = null;

			// Add straight-forward movement genotype
			genetics.pushGenotype(getStraightForwardGenotype(dude, is_attacker, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball), surv_num);

			// Add do-nothing genotype
			genetics.pushGenotype(getDoNothingGenotype(dude, is_attacker, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball), surv_num);

			if (dude.touch || dude.y < CFG.ROBOT_MIN_RADIUS * 1.2) {
				genetics.pushAllGenotypes(getGenotypesAround(dude, 10, is_attacker, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball), surv_num);
			}

			// ------
			// First generation
			// ------

			if (Debug.on && !Glob.goal_scored && Glob.last_round_started_tick + 40 > Glob.tick) Debug.echo("Round beginning. Focusing on a ball");

			// maybe if do_genetics only?
			int limit = do_genetics ? FIRST_GENERATION_GENES_NUM : 10;
			for (int i = 0; i < limit; i++) {
				Genetics.Genotype<Act> genotype = new Genetics.Genotype<>();
				genotype.nitro_gene = createNitroGene(dude);

				int total_acts = 0;
				while (total_acts < actions_per_gene_current) {
					total_acts = createGene(dude, genotype, null, actions_per_gene_current, total_acts);
				}

				genotype.score = Scores.evaluate(actions_per_gene_current, dude.copy(), genotype, 0, is_attacker, !do_genetics, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball);
				genetics.pushGenotype(genotype, surv_num);
				if (Debug.on && DEBUG) Debug.echo("Adding first-gen genotype #" + i + " :: " + genotype);
			}

			Perf.stop("genotype.init");
			Perf.start("genotype.mutate");

			// ------
			// Mutations
			// ------

			// don't calc if in air
			if (do_genetics) {
				StringBuilder str_gen = new StringBuilder();

				if (Debug.on) {
					Debug.echo("Gens num: " + gen_num + ", surv: " + surv_num + ", muts: " + muts_num + ", apg: " + actions_per_gene_current);
					Debug.text(new Point(Debug.form.width * 0.68, 17 + 20 * dude.dude_id), "Gens: " + gen_num + ", survs: " + surv_num + ", muts: " + muts_num + ", apg: " + actions_per_gene_current, Color.white, 1, 0.4);
				}

				if (Debug.on) Debug.echo("Pre-Survived Genotypes: " + genetics.genotypes.size());

				int gid = 0;
				double ts = 0;
				if (DEBUG) {
					for (Genetics.Genotype g : genetics.genotypes) {
						if (Debug.on) Debug.echo("gid: " + gid + " :: " + g.toString());
						ts += g.score;
						gid++;
					}
					if (Debug.on) Debug.echo("Total: " + ts + "\n");
				}

				for (int generation = 1; generation <= gen_num; generation++) {
					genetics.naturalSelection(surv_num);

					if (DEBUG) {
						gid = 0;
						ts = 0;
						for (Genetics.Genotype g : genetics.genotypes) {
							if (Debug.on) Debug.echo("Gen " + generation + ", gid: " + gid + " :: " + g.toString());
							ts += g.score;
							gid++;
						}
						if (Debug.on) Debug.echo("Total: " + ts + "\n");
					}

					G<Act> gen = new G<>();
					for (Genetics.Genotype<Act> g : genetics.genotypes) {
						Genetics.Genotype<Act> mutant;

						for (int copies = 0; copies < muts_num; copies++) {
							mutant = CBGene.getMutant(g, 1.0, 0.1, 0.1, 0.1, dude.nitro > 0 && generation < 5 ? 0.3 : 0, false);

							mutant.score = Scores.evaluate(actions_per_gene_current, dude.copy(), mutant, generation, is_attacker, generation == gen_num, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball);

							gen.pushGenotype(mutant, surv_num);
						}
					}

					gen.pushAllGenotypes(genetics.genotypes, surv_num);
					genetics = gen;
					if (Debug.on) str_gen.append(" gen -> ").append(generation).append(", num: ").append(genetics.genotypes.size());

					if (!Glob.TEST_DEFENCE_ONLY && generation >= 5 && genetics.genotypes.size() > 0 && genetics.genotypes.get(0).score > 0) {
						if (Debug.on) Debug.echo("Already found good score, interrupting mutations at generation " + generation);
						break;
					}

					if (false && generation > 5 && !Scores.any_touched_ball && (is_attacker || Glob.ball.distanceMore(dude, 30))) {
						if (Debug.on) Debug.echo("Can't touch ball, interrupting mutations at generation " + generation);
						break;
					}
				}
				if (Debug.on) Debug.echo("Genotypes :: " + str_gen);
			}

			Perf.stop("genotype.mutate");
			Perf.start("genotype.process");

			double best_score = -Double.MAX_VALUE;

			best_genotypes[dude.dude_id] = null;

			for (Genetics.Genotype<Act> g : genetics.genotypes) {
				if (best_genotypes[dude.dude_id] == null || g.score > best_score) {
					best_score = g.score;
					best_genotypes[dude.dude_id] = g;
				}
			}

			if (best_genotypes[dude.dude_id] != null && best_genotypes[dude.dude_id].genes.size() > 0) {
				CBGene gene = (CBGene) best_genotypes[dude.dude_id].genes.get(0);
				Act best_act = gene.sequence != null && gene.sequence.length > 0 ? gene.sequence[0] : null;

				if (best_genotypes[dude.dude_id].ball_defender_kick_to != null) {
					Glob.ball_defender_kick_to = best_genotypes[dude.dude_id].ball_defender_kick_to.copy();
				}

				if (Debug.on) {
					Debug.echo("Best act for " + dude + " -> " + best_act + ", genotype_id: " + best_genotypes[dude.dude_id].id + ", score: " + best_genotypes[dude.dude_id].score);

					if (Scores.DEBUG && Debug.on) {
						Debug.echo(best_genotypes[dude.dude_id].debug_score.getDebugInfo("\n", 0) + "\n");
						Debug.appendMemo1(is_attacker ? "Attacker" : "Defender");
						Debug.appendMemo1(best_genotypes[dude.dude_id].debug_score.getDebugInfo("\n", 0));
					}

					V3 last_pos = dude.pos();
					V3 last_ball = Glob.ball.pos();
					int t = 0;
					Glob.dudes_movement[dude.id - 1] = new Dude[ACTIONS_PER_GENE];

					for (Genetics.RepeatableActionGene gn : best_genotypes[dude.dude_id].genes) {
						CBGene g = (CBGene) gn;
						if (g.sequence == null) break;

						for (Act a : g.sequence) {
							if (a.dude_applied == null) break;

							int clr = 50;
							int clr_added = 255 - Math.min(clr + (int) (a.dude_applied.y * 10.0), 255);
							if (Debug.on) Debug.line(last_pos, a.dude_applied, 3, new Color(clr_added, clr_added, clr_added), 1);

							if (t < Glob.dudes_movement.length) {
								Glob.dudes_movement[dude.id - 1][t] = a.dude_applied.copyFull();
							}

							last_pos = a.dude_applied.pos();

							if (a.ball_applied == null) break;

							if (Debug.on) {
								if (is_closest_attacker_to_ball) Debug.line(last_ball, a.ball_applied, 2, new Color(255 - (int) Math.min(0 + a.ball_applied.y * 20, 255), 50, 50, 255), 1);
								else Debug.line(last_ball, a.ball_applied, 2, new Color(50, 255 - (int) Math.min(0 + a.ball_applied.y * 20, 255), 50, 255), 1);

								if (t % 2 == 0) Debug.sphere(a.dude_applied, a.dude_applied.radius, new Color(1, 1, 1, 20), 1);

								last_ball = a.ball_applied.pos();
							}

							if (Debug.on && a.accurate) {
								Debug.square3(a.dude_applied, a.dude_applied.radius * 2.5, new Color(76, 255, 255, 150), true);
								Debug.square3(a.ball_applied, CFG.BALL_RADIUS * 2.5, new Color(97, 126, 255, 150), true);
							}

							t++;
						}
					}

					// To draw full best genotype path
					if (Debug.on) {
						Scores.evaluate(actions_per_gene_current, dude.copy(), best_genotypes[dude.dude_id], -1, is_attacker, true, dudes_to_sim, skipped_enemies, is_closest_attacker_to_ball);
					}
				}

				dude.Act(best_act);
			}

			Perf.stop("genotype.process");
		}

		Perf.stop("find_action");
		if (Debug.on) Debug.echo("Evaluated times: " + debug_evaluated_times);
	}

	private static Genetics.GeneDouble createNitroGene(Dude dude) {
		Genetics.GeneDouble result = new Genetics.GeneDouble(G_PARAM_NITRO, true, 0.0, 1.0, 1.0, 0.25);
		return result;
	}

	private static void calcEnemiesToMeTrajectories(Dude dude, Dude[] enemies) {
		for (int eid = 0; eid < enemies.length; eid++) {
			Dude e = enemies[eid].copy();
			for (int i = 0; i < Glob.enemies_to_me_traj[eid].length; i++) {
				Act a = new Act(e.dirTo(dude).normalize().scaleXZ(100.0));
				e.Sim(a, null);
				Glob.enemies_to_me_traj[eid][i] = e.pos();

				if (Debug.on) Debug.sphere(e.pos(), 0.1, new Color(179, 179, 179, 50), 1);
			}
		}
	}

	private static Genetics.Genotype<Act> getStraightForwardGenotype(Dude dude, boolean is_attacker, Dude[] dudes_to_sim, Dude[] enemies_to_skip, boolean is_closest_attacker_to_ball) {
		int sf_acts = 0;
		double angle_to_ball = dude.pointFromXZ().getAngleTo(
			Glob.ball_next_landing_target[dude.dude_id] != null ? Glob.ball_next_landing_target[dude.dude_id].pointFromXZ() : Glob.ball.pointFromXZ()
		);

		Genetics.Genotype<Act> sf_genotype = new Genetics.Genotype<>();
		sf_genotype.nitro_gene = createNitroGene(dude);

		for (int i = 0; i < Math.ceil(actions_per_gene_current / 5); i++) {
			sf_acts = createGene(dude, sf_genotype, angle_to_ball, actions_per_gene_current, sf_acts);
		}

		sf_genotype.score = Scores.evaluate(actions_per_gene_current, dude.copy(), sf_genotype, 0, is_attacker, false, dudes_to_sim, enemies_to_skip, is_closest_attacker_to_ball);
		if (DEBUG && Debug.on) Debug.echo("Adding straight-forward genotype: " + sf_genotype);
		return sf_genotype;
	}

	private static Genetics.Genotype<Act> getDoNothingGenotype(Dude dude, boolean is_attacker, Dude[] dudes_to_sim, Dude[] enemies_to_skip, boolean is_closest_attacker_to_ball) {
		int sf_acts = 0;
		Genetics.Genotype<Act> sf_genotype = new Genetics.Genotype<>();
		sf_genotype.nitro_gene = createNitroGene(dude);

		for (int i = 0; i < Math.ceil(actions_per_gene_current / 5); i++) {
			sf_acts = createGene(dude, sf_genotype, -10000.0, actions_per_gene_current, sf_acts);
		}

		sf_genotype.score = Scores.evaluate(actions_per_gene_current, dude.copy(), sf_genotype, 0, is_attacker, false, dudes_to_sim, enemies_to_skip, is_closest_attacker_to_ball);
		if (DEBUG && Debug.on) Debug.echo("Adding do-nothing genotype: " + sf_genotype);
		return sf_genotype;
	}

	private static ArrayList<Genetics.Genotype<Act>> getGenotypesAround(Dude dude, double genotypes_num, boolean is_attacker, Dude[] dudes_to_sim, Dude[] enemies_to_skip, boolean is_closest_attacker_to_ball) {
		ArrayList<Genetics.Genotype<Act>> result = new ArrayList<>();

		for (double a = 0; a < genotypes_num; a++) {

			double val = -Math.PI + (Math.PI * 2.0 * (a / genotypes_num));
			int sf_acts = 0;
			Genetics.Genotype<Act> sf_genotype = new Genetics.Genotype<>();
			sf_genotype.nitro_gene = createNitroGene(dude);

			for (int i = 0; i < Math.ceil(actions_per_gene_current / 5); i++) {
				sf_acts = createGene(dude, sf_genotype, val, actions_per_gene_current, sf_acts);
			}

			sf_genotype.score = Scores.evaluate(actions_per_gene_current, dude.copy(), sf_genotype, 0, is_attacker, false, dudes_to_sim, enemies_to_skip, is_closest_attacker_to_ball);

			result.add(sf_genotype);
			if (DEBUG && Debug.on) Debug.echo("Adding 360-degree genotype: " + sf_genotype);
		}

		return result;
	}

	private static void addPreviousBestGenotype(Dude dude, boolean is_attacker, boolean do_genetics, Dude[] dudes_to_sim, Dude[] enemies_to_skip, boolean is_closest_attacker_to_ball) {
		CBGene first_gene = (CBGene) best_genotypes[dude.dude_id].genes.get(0);
		first_gene.repeats_gene.value -= 1;

		if (first_gene.repeats_gene.value <= 0) {
			best_genotypes[dude.dude_id].genes.remove(0);
			if (Debug.on) Debug.echo("Previous gene sequence is over");
		}

		if (best_genotypes[dude.dude_id].genes.size() > 0) {
			for (Genetics.RepeatableActionGene gn : best_genotypes[dude.dude_id].genes) {
				gn.snapshot = null;
			}

			if (Debug.on) {
				if (Debug.on) Debug.echo("Adding best genotype from previous tick: " + best_genotypes[dude.dude_id]);

				V3 last_pos = dude.pos();
				for (Genetics.RepeatableActionGene gn : best_genotypes[dude.dude_id].genes) {
					CBGene g = (CBGene) gn;

					if (g.sequence == null) break;

					for (Act a : g.sequence) {
						if (a.dude_applied == null) break;

						if (Debug.on) {
							int clr = 0;
							int clr_added = 255 - Math.min(clr + (int) (a.dude_applied.y * 7.0), 255);
							if (Debug.on) Debug.line(last_pos, a.dude_applied, 1, new Color(clr_added, 50, clr_added), 1);
						}

						last_pos = a.dude_applied.pos();

					}
				}

			}

			best_genotypes[dude.dude_id].score = Scores.evaluate(actions_per_gene_current, dude.copy(), best_genotypes[dude.dude_id], 0, is_attacker, !do_genetics, dudes_to_sim, enemies_to_skip, is_closest_attacker_to_ball);
		} else {
			best_genotypes[dude.dude_id] = null;
		}
	}

	public static int calcActionSequenceFromGene(CBGene gene, int actions_count, boolean last_gene) {
		Perf.start("calcActionSequenceFromGenotype");

		Act a;
		if (gene.action_gene.param == G_PARAM_MOVE) {
			if (gene.action_gene.value == -10000) {
				a = new Act(V3.zero(), 0, false);
			} else {
				a = new Act(new Point(1, 0).getPointAhead(gene.action_gene.value, 100.0).toXZ(CFG.ROBOT_MIN_RADIUS));
			}

			if (gene.jump_gene.value > 0.7) {
				a.Jump(CFG.ROBOT_MAX_JUMP_SPEED);
			}

		} else {
			if (Debug.on) Debug.echo("!! unknown action: " + gene.action_gene.param);
			Perf.stop("calcActionSequenceFromGenotype");
			return actions_count;
		}


		int len = Math.min(gene.repeats_gene.value, actions_count);
		if (len < gene.repeats_gene.value) gene.repeats_gene.value = len;

		gene.sequence = new Act[last_gene ? actions_count : len];

		for (int act_num = 0; act_num < len; act_num++) {
			Act ac = a.copy();
			gene.sequence[act_num] = ac;
		}

		actions_count -= len;

		if (last_gene && actions_count > 0) {
			for (int act_num = len; act_num < gene.sequence.length; act_num++) {
				gene.sequence[act_num] = new Act();
			}
		}

		Perf.stop("calcActionSequenceFromGenotype");

		return actions_count;
	}

	public static void findDefendingAction(Dude dude, TheBall ball) {
		if (CFG.HAS_NITRO && dude.nitro < 60 && ball.z > 0 && ball.vel.z > 0) {
			ArrayList<Nitro> close_nitros = new ArrayList<>();
			for (Nitro n : Glob.nitro) {
				if (n.z > 0 != dude.z > 0 || n.respawn != null) continue;

				close_nitros.add(n);
			}

			if (close_nitros.size() > 0) {
				Nitro n = Utils.getClosest(dude, close_nitros);

				Act result = new Act(dude.dirTo(n).NormalizeFast().Scale(dude.distanceMore(n, 5) ? 100.0 : 1.0).replace(null, 1.0, null));
				dude.Act(result);

				if (Debug.on) Debug.arrow(dude, n, 0.2, 1.0, 1.0, Color.yellow, 1);

				return;
			}
		}

		if (dude.z > (Glob.arena.depth / 2.0) && Glob.point_for_defence.distanceLess(dude, Glob.arena.goal_width * 0.05)) {
			return;
		}

		V3 desired_vel = Glob.point_for_defence.sub(dude);
		Act result = new Act(desired_vel.scale(dude.distanceMore(Glob.point_for_defence, 3.0) ? 10.0 : 1.0).replace(null, CFG.ROBOT_MIN_RADIUS, null));

		if (dude.vel.len() >= 10 && dude.distanceMore(Glob.point_for_defence, Glob.arena.goal_width) && Math.abs(dude.vel.getAngleToXZ(Glob.point_for_defence.sub(dude))) <= HMath.TEN_DEGREES) {
			result.Jump();
		}

		dude.Act(result);

	}

	public static void findSemiAttackerAction(Dude dude, TheBall ball) {
		V3 target = Glob.dudes_targets[dude.id - 1];
		if (target == null) target = ball.projectXZ();

		if (target.distanceLess(dude, 3.0)) {
			return;
		}

		V3 desired_vel = dude.dirTo(target);
		Act result = new Act(desired_vel.scale(dude.distanceMore(target, 5.0) ? 100.0 : 1.0).replace(null, CFG.ROBOT_MIN_RADIUS, null));

		dude.Act(result);

		if (Debug.on) {
			Debug.arrow(dude.pos(), target.pos(), 0.5, 1.0, 1.0, new Color(19, 19, 19, 150), 1);
			Debug.echo("Playing semi-attacker action for " + dude);
		}
	}

	public static class G<Genotype> extends Genetics2<Genotype> {
	}

	public static class CBGene extends Genetics.RepeatableActionGene<Genetics.GeneDouble> {
		public Genetics.GeneDouble jump_gene;
		public Act[] sequence = null;
//		public GeneSnapshot snapshot;
//		Genetics.GeneDouble nitro_gene;

		//		public CBGene(Genetics.GeneDouble action_gene, Genetics.GeneDouble jump, Genetics.GeneDouble nitro, Genetics.GeneInt repeats_gene) {
		public CBGene(Genetics.GeneDouble action_gene, Genetics.GeneDouble jump, Genetics.GeneInt repeats_gene) {
			super(action_gene, repeats_gene);
			jump_gene = jump;
//			nitro_gene = nitro;
		}

		public static Genetics.Genotype<Act> getMutant(Genetics.Genotype<Act> from_, double factor, double mutate_action_chance, double mutate_jump_chance, double mutate_repeats_chance, double mutate_nitro_chance, boolean no_snapshots) {
			Genetics.Genotype<Act> result = from_.copy();
			boolean mutated = false;
			boolean nitro_mutated = false;
			Genetics.Genotype.last_id++;
			result.id = Genetics.Genotype.last_id;

			if (factor > 1.0) factor = 1.0;
			if (factor <= 0.1) factor = 0.1;

			if (CFG.HAS_NITRO && Genetics.randNextDouble() < mutate_nitro_chance) {
				result.nitro_gene.mutate(factor);
				nitro_mutated = true;
			}

			while (!mutated) {
				for (Genetics.RepeatableActionGene gn : result.genes) {
					CBGene g = (CBGene) gn;

					if (Genetics.randNextDouble() < mutate_action_chance) {
						g.action_gene.mutate(factor);
						mutated = true;
					}
					if (!no_jumps && Genetics.randNextDouble() < mutate_jump_chance) {
						g.jump_gene.mutate(factor);
						mutated = true;
					}
					if (Genetics.randNextDouble() < mutate_repeats_chance) {
						g.repeats_gene.mutate(factor);
						mutated = true;
					}

					if (mutated || nitro_mutated || no_snapshots) {
						g.snapshot = null;
					}

				}
			}

			return result;
		}

		@Override
		public CBGene copy() {
			CBGene result = new CBGene((Genetics.GeneDouble) action_gene.copy(), (Genetics.GeneDouble) jump_gene.copy(), (Genetics.GeneInt) repeats_gene.copy());
			if (sequence != null) {
				result.sequence = new Act[sequence.length];
				for (int i = 0; i < sequence.length; i++) result.sequence[i] = sequence[i].copy();
			}

			if (snapshot != null) {
				result.snapshot = (GeneSnapshot) snapshot.copy();
			}

			return result;
		}
	}

	public static Dude[] findEnemyActionSequence(Dude dude, boolean is_attacker, int actions_num) {
		G<Act> genetics = new G<>();

		int surv_num = 5;

		for (int i = 0; i < 40; i++) {
			Genetics.Genotype<Act> genotype = new Genetics.Genotype<>();
			genotype.nitro_gene = createNitroGene(dude);
			int actions = actions_num;

			int total_acts = 0;
			while (total_acts < actions) {
				total_acts = createGene(dude, genotype, null, actions, total_acts);
			}

			genotype.score = Scores.evaluateEnemy(actions_num, dude.copy(), genotype, is_attacker);
			genetics.pushGenotype(genotype, surv_num);
		}

		for (int generation = 1; generation <= 5; generation++) {
			G<Act> gen = new G<>();
			for (Genetics.Genotype<Act> g : genetics.genotypes) {
				Genetics.Genotype<Act> mutant;

				for (int copies = 0; copies < 5; copies++) {
					mutant = CBGene.getMutant(g, 1.0, 0.1, 0.1, 0.1, dude.nitro > 0 ? 0.3 : 0, true);
					mutant.score = Scores.evaluateEnemy(actions_num, dude.copy(), mutant, is_attacker);
					gen.pushGenotype(mutant, surv_num);
				}
			}

			gen.pushAllGenotypes(genetics.genotypes, surv_num);
			genetics = gen;
		}

		Genetics.Genotype<Act> bg = null;

		for (Genetics.Genotype<Act> g : genetics.genotypes) {
			if (bg == null || g.score > bg.score) {
				bg = g;
			}
		}

		if (bg == null) return null;

		Dude[] result = new Dude[actions_num];

		int idx = 0;
		for (Genetics.RepeatableActionGene g : bg.genes) {
			CBGene gene = (CBGene) g;
			if (gene.sequence == null) break;

			for (Act a : gene.sequence) {
				if (a.dude_applied == null) return result;

				result[idx] = a.dude_applied.copyFull();
				if (Debug.on) {
					Debug.sphere(result[idx].pos(), 0.2, Color.green, 1);
					Debug.sphere(a.ball_applied.pos(), 2.0, new Color(255, 0, 243, 20), 1);
				}

				idx++;
				if (idx >= actions_num) return result;
			}
		}

		return result;
	}

	private static void predictEnemyMovement(Dude dude) {
		Glob.dudes_movement[dude.id - 1] = DecisionMaker.findEnemyActionSequence(dude, dude.id != Glob.enemy_defender.id, DecisionMaker.ACTIONS_PER_GENE);
	}

}
