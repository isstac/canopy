package sampling.BasicSAT;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Stack;

public class Solver {

	private ArrayList<Clause> clauses, learnts;
	private ArrayList<Assign> assigns;
	private ArrayList<Clause> reason;
	private ArrayList<Integer> level;
	private ArrayList<ArrayList<Clause>> watches;
	private ArrayDeque<Lit> propQ;
	private Stack<Integer> trail, trailLim;

	private boolean ok;

	public int getNVars() {
		return assigns.size();
	}

	public int getNAssigns() {
		return trail.size();
	}

	public int getDecisionLevel() {
		return this.trailLim.size();
	}

	public Solver() {
		this.ok = true;

		this.clauses = new ArrayList<Clause>();
		this.learnts = new ArrayList<Clause>();
		this.assigns = new ArrayList<Assign>();
		this.reason = new ArrayList<Clause>();
		this.level = new ArrayList<Integer>();
		this.watches = new ArrayList<ArrayList<Clause>>();
		this.propQ = new ArrayDeque<Lit>();
		this.trail = new Stack<Integer>();
		this.trailLim = new Stack<Integer>();
	}

	public void setVarCount(int numVars) {
		for (int i = 0; i < 3 * numVars; ++i) {
			this.assigns.add(null);
			this.reason.add(null);
			this.level.add(null);
			this.watches.add(null);
		}

		int i, j;
		for (i = 1; i <= numVars; i++) {
			j = i << 1;
			this.assigns.set(i, Assign.UNDEF);
			this.watches.set(j, new ArrayList<Clause>());
			this.watches.set(j + 1, new ArrayList<Clause>());
		}
	}

	public void addClause(Clause c) {
		this.clauses.add(c);
	}

	private Clause propogate() {
		while (propQ.size() > 0) {
			Lit p = propQ.remove();
			ArrayList<Clause> tmp = this.watches.get(p.getVal());
			this.watches.set(p.getVal(), new ArrayList<Clause>());

			for (int i = 0; i < tmp.size(); i++) {
				if (!tmp.get(i).Propogate(this, p)) {
					for (int j = i + 1; j < tmp.size(); j++) {
						this.watches.get(p.getVal()).add(tmp.get(j));
					}

					this.propQ.clear();
					return tmp.get(i);
				}
			}
		}

		return null;
	}

	public Assign value(Lit p) {
		if (this.assigns.get(p.getVar()) == null) {
			return Assign.UNDEF;
		}

		Assign x = this.assigns.get(p.getVar());
		if (!p.getSign()) {
			return x;
		} else {
			if (x == Assign.TRUE) {
				return Assign.FALSE;
			} else if (x == Assign.FALSE) {
				return Assign.TRUE;
			} else {
				return Assign.UNDEF;
			}
		}
	}

	public void addWatch(Lit p, Clause c) {
		try {
			if (this.watches.get(p.getVal()) == null) {
				this.watches.set(p.getVal(), new ArrayList<Clause>());
			}
		} catch (IndexOutOfBoundsException ex) {
			this.watches.set(p.getVal(), new ArrayList<Clause>());
		}

		this.watches.get(p.getVal()).add(c);
	}

	public void addNegWatch(Lit p, Clause c) {
		try {
			if (this.watches.get(p.getNegVal()) == null) {
				this.watches.set(p.getNegVal(), new ArrayList<Clause>());
			}
		} catch (IndexOutOfBoundsException ex) {
			this.watches.set(p.getNegVal(), new ArrayList<Clause>());
		}

		this.watches.get(p.getNegVal()).add(c);
	}

	public boolean enqueue(Lit p, Clause from) {
		if (this.value(p) != Assign.UNDEF) {
			if (this.value(p) == Assign.FALSE) {
				return false;
			}
			else {
				return true;
			}
		}
		else { // FIX: use p.Var for everything
			int x = p.getVar();
			this.assigns.set(x, ((p.getSign()) ? Assign.FALSE : Assign.TRUE));
			this.reason.set(x, from);
			this.trail.push(p.getVal());
			this.assignLevel(x);
			this.propQ.add(p);
			return true;
		}
	}

	private void assignLevel(int x) {
		try {
			if (this.level.get(x) == null) {
				this.level.set(x, this.getDecisionLevel());
			}
		} catch (IndexOutOfBoundsException ex) {
			this.level.set(x, this.getDecisionLevel());
		}

		this.level.set(x, this.getDecisionLevel());
	}

	// Page 15
	private void analyze(Clause confl, Object[] out_learnt, int[] btlevel)
	{
		int counter = 0;
		Lit p = Lit.LIT_UNDEF;
		ArrayList<Lit> p_reason;
		ArrayList<Integer> seen = new ArrayList<Integer>(); // May need to optimize this

		btlevel[0] = 0;
		out_learnt[0] = new ArrayList<>();
		((ArrayList<Lit>) out_learnt[0]).add(Lit.LIT_UNDEF); // Reserve space for lits[0]

		do {
			p_reason = confl.CalcReason(this, p);
			for (Lit q : p_reason) {
				if (!seen.contains(q.getVar())) {
					seen.add(q.getVar());
					if (this.level.get(q.getVar()) == this.getDecisionLevel()) {
						++counter;
					} else if (this.level.get(q.getVar()) > 0){
						//out_learnt.Add(Lit.LitFromVal(q.Val));
						((ArrayList<Lit>) out_learnt[0]).add(Lit.NegativeLit(q));
						btlevel[0] = Math.max(btlevel[0], this.level.get(q.getVar()));
					}
				}
			}

			do {
				p = Lit.LitFromVal(this.trail.peek());
				confl = this.reason.get(p.getVar());
				this.undoOne();
			} while (!seen.contains(p.getVar()));

			--counter;
		} while(counter > 0);

		((ArrayList<Lit>) out_learnt[0]).set(0, Lit.NegativeLit(p));
	}

	private void undoOne() {
		Lit l = Lit.LitFromVal(this.trail.peek());
		int x = l.getVar();
		this.assigns.set(x, Assign.UNDEF);
		this.reason.set(x, null);
		this.level.set(x, -1);
		this.trail.pop();
	}

	public boolean search() {
		while (true) {
			Clause confl = this.propogate();
			if (confl != null) {
				if (this.getDecisionLevel() == 0) {
					return false;
				}

				int[] backtrack_level = new int[] { 0 };
				Object[] learnt_clause = new Object[] { null };
				this.analyze(confl, learnt_clause, backtrack_level);

				if (backtrack_level[0] == 0) {
					int y = 0;
				}

				this.cancelUntil(Math.max(backtrack_level[0], 0));  // 0 for root level?
				this.record((ArrayList<Lit>) learnt_clause[0]);
			} else {

				if ((this.getNAssigns() * 3) == this.getNVars()) {
					return true;
				}
				else { // Pick new variable
					for (int i = 0; i < this.assigns.size(); ++i) {
						Assign x = this.assigns.get(i);
						if (x == null) {
							continue;
						}

						if (x == Assign.UNDEF) {
							this.assume(Lit.LitFromVal((i * 2) + 1));
							break;
						}
					}
				}
			}
		}
	}

	private void cancelUntil(int level) {
		while (this.getDecisionLevel() > level) {
			this.cancel();
		}
	}

	private void cancel() {
		int c = this.trail.size() - this.trailLim.peek();
		for (; c != 0; c--) {
			this.undoOne();
		}

		this.trailLim.pop();
	}

	private boolean assume(Lit p) {
		this.trailLim.push(this.trail.size());
		return this.enqueue(p, null);
	}

	private void record(ArrayList<Lit> clause) {
		Clause[] c = new Clause[] { null };
		Clause.clauseNew(this, clause, true, c);
		this.enqueue(clause.get(0), c[0]);
		if (c != null) {
			learnts.add(c[0]);
		}
	}

	public void printClauses() {
		for (Clause c : this.clauses) {
			c.PrintClause();
		}
	}

	public ArrayList<?> getSolution() {
		return this.assigns;
	}

	public void printSudoSolution() {
		int count = 0;
		int index = 0;
		int offset = 81;

		for (Assign a : this.assigns) {
			if (a == Assign.TRUE) {
				count++;
				int x = index / offset;
				int y = (index - (x * offset) - 1) / 9;
				int z = (index - (x * offset)) - (9 * y);

				System.out.println("index = " + index + " " + x + "," + y + " = " + z);
			}

			index++;
		}

		System.out.println("Total true: " + count);
	}

	public void printLearnts() {
		System.out.println("Learnts\n----------");
		for (Clause c : this.learnts) {
			if (c != null) {
				c.PrintClause();
			}
		}

		System.out.println();
	}
}
