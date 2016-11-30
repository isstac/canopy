package sampling.BasicSAT;

import java.util.ArrayList;

public class Clause {

	private boolean learnt;
	private ArrayList<Lit> lits;

	public Clause() {
		this.learnt = false;
		this.lits = new ArrayList<Lit>();
	}

	public boolean Propogate(Solver s, Lit p) {
		if (this.lits.get(0).getVal() == p.getNegVal())  // ensure lits[1] is the false lit
		{
			Lit temp = this.lits.get(0);
			this.lits.set(0, this.lits.get(1));
			this.lits.set(1, temp);
		}

		if (s.value(this.lits.get(0)) == Assign.TRUE)
		{
			s.addWatch(p, this);
			return true;
		}

		for (int i = 2; i < this.lits.size(); i++)
		{
			if (s.value(this.lits.get(i)) != Assign.FALSE)
			{
				Lit temp = this.lits.get(1);
				this.lits.set(1, this.lits.get(i));
				this.lits.set(i, temp);
				s.addNegWatch(this.lits.get(1), this);
				return true;
			}
		}

		// DEBUGGING
		//System.Console.WriteLine("BCP: {0} implies {1}", p.ToString(), this.ToString());

		s.addWatch(p, this);
		return s.enqueue(this.lits.get(0), this);
	}

	public ArrayList<Lit> CalcReason(Solver s, Lit p) {
		ArrayList<Lit> x = new ArrayList<Lit>();
		for (int i = (p == Lit.LIT_UNDEF ? 0 : 1); i < this.lits.size(); i++)
		{
			//x.Add(this.lits[i]);
			x.add(Lit.NegativeLit(this.lits.get(i)));
		}

		return x;
	}

	public static boolean clauseNew(Solver s, ArrayList<Lit> ps, boolean learnt, Clause[] out_clause) {
		out_clause[0] = null;

		if (!learnt) {
			for (Lit lit : ps) {
				if (s.value(lit) == Assign.TRUE) {
					return true;
				}
			}
		}

		if (ps.size() == 0) {
			return false;
		}
		else if (ps.size() == 1) {
			return s.enqueue(ps.get(0), null);
		}

		// Else make a new clause
		Clause c = new Clause();
		c.learnt = learnt;
		for (Lit lit : ps) {
			c.lits.add(lit);
		}

		// Add clause to watcher lists (!lits[0], !lits[1])
		s.addNegWatch(c.lits.get(0), c);
		s.addNegWatch(c.lits.get(1), c);

		out_clause[0] = c;

		return true;
	}

	@Override
	public String toString() {
		String s = "";
		for (Lit l : this.lits) {
			s += l.toString() + " ";
		}

		return s;
	}

	public void PrintClause() {
		for (Lit l : this.lits) {
			System.out.print(l.toString() + " ");
		}

		System.out.println();
	}
}
