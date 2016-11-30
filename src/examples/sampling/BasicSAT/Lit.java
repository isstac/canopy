package sampling.BasicSAT;

public class Lit {

	private int x;

	public int getVar() {
		return this.x >> 1;
	}

	public int getVal() {
		return this.x;
	}

	public int getNegVal() {
		return this.x ^ 1;
	}

	public boolean getSign() {
		return ((this.x & 1) == 0) ? false : true;
	}

	public Lit() { }

	public Lit(int x, boolean sign)
	{
		this.x = x << 1;
		if (sign) {
			this.x++;
		}
	}

	public static Lit LitFromVal(int val)
	{
		Lit n = new Lit();
		n.x = val;
		return n;
	}

	public static Lit NegativeLit(Lit l)
	{
		Lit n = new Lit();
		n.x = l.getNegVal();
		return n;
	}

	@Override
	public String toString()
	{
		String neg = this.getSign() ? "-" : "";
		return neg + this.getVar();
	}

	public static final Lit LIT_UNDEF = new Lit(-1, false);
}
