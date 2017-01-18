package sampling.BasicSAT;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Character;

import gov.nasa.jpf.symbc.Debug;

public class BasicSAT {

	private String filename;
	private Solver solver;

	public BasicSAT() {
		this.solver = new Solver();
	}

	public BasicSAT(String filename) {
		this.filename = filename;
		this.solver = new Solver();
	}

	public boolean solve() {
		this.readFile();
		return this.solver.search();
	}

	public ArrayList<?> getSolution() {
		return this.solver.getSolution();
	}

	private void readFile() {
		try (BufferedReader sr = new BufferedReader(new FileReader(filename))) {
			String s = "";
			ArrayList<Lit> lits = new ArrayList<Lit>();
			String delims = " ";

			while ((s = sr.readLine()) != null) {
				s = s.trim();
				String[] pieces = s.split(delims);

				if (pieces.length > 1) {
					if (pieces[0].equals("c")) {
						continue;
					} else if (pieces[0].equals("p")) {
						this.solver.setVarCount(Integer.parseInt(pieces[2]));
					} else { // It's a clause
						lits.clear();

						for (String p : pieces) {
							if (p.length() == 0 || Character.isWhitespace(p.charAt(0))) {
								continue;
							}

							int x = Integer.parseInt(p);
							if (x == 0) {
								break;
							}

							lits.add(new Lit(Math.abs(x), (x < 0) ? true : false));
						}

						Clause[] clause = new Clause[] { null };
						Clause.clauseNew(this.solver, lits, false, clause);
						this.solver.addClause(clause[0]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readArray(int[][] arr, int vars) {
		ArrayList<Lit> lits = new ArrayList<Lit>();
		this.solver.setVarCount(vars);
		for (int[] pieces : arr) {
			lits.clear();
			for (int x : pieces) {

				lits.add(new Lit(Math.abs(x), (x < 0) ? true : false));

				Clause[] clause = new Clause[] { null };
				Clause.clauseNew(this.solver, lits, false, clause);
				this.solver.addClause(clause[0]);
			}
		}

		boolean soln = this.solver.search();
		this.printSoln();
		System.out.println(soln ? "SAT" : "UNSAT");
	}

	public void printSoln() {
		this.solver.printSudoSolution();

	}

	public static void doSingleFile(String filename) {
		BasicSAT solver = new BasicSAT(filename);
		boolean soln = solver.solve();
		System.out.println(filename + " : " + (soln ? "SAT" : "UNSAT"));
	}

	public static void doDirectory(String dirName) {
		File di = new File(dirName);
		System.out.println(di);

		FilenameFilter cnfs = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".cnf");
			}
		};

		File[] files = di.listFiles(cnfs);
		for (File f : files) {
			doSingleFile(dirName + "\\" + f.getName());
		}
	}

  public static void doSymbolic() {
    BasicSAT solver = new BasicSAT();
    final int VARS = 10;

    int[][] D = new int[VARS][1];
    for (int i = 0; i < VARS; i++) {
      boolean neg = Debug.makeSymbolicBoolean("neg"+i+1);
      int lit = i + 1;
      if(neg) {
        D[i][0] = - lit;
      } else {
        D[i][0] = lit;
      }

    }
    solver.readArray(D, VARS);
  }

	public static void usage() {
		System.out.println("Usage: java BasicSAT.BasicSAT <input_file.cnf>");
	}

	public static void main(String[] args) {
    doSymbolic();
    /*if (args.length < 1) {
			usage();
			return;
		}

		if (args[0].endsWith(".cnf")) {
			doSingleFile(args[0]);
		} else {
			doDirectory(args[0]);
		}*/
	}
}
