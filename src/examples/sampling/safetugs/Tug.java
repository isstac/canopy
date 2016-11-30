package sampling.safetugs;
/**
 * 
 */

/**
 * @author Corina Pasareanu and Willem Visser
 *
 */
public class Tug {

	Location current; // this has an x and y coordinate
	Location nextPos; 
	int id;	 // tug has an id
	Plan plan; // all the locations in order that tug will visit
	Grid grid;
	double stayProbability;
	
	int state; //the state
	//states of each node
	static int INITS = 0;
	static int NEXT = 1;
	static int REQUEST = 2;
	static int WAITING = 3;
	static int MOVE = 4;
	 
	public Tug(int id, Plan p, Grid g, double prob) {
		this.id=id;
		assert id < 100; // obstacles are greater equal 100
		plan = p;
		grid = g;
		stayProbability = prob;
		current = plan.getStep();
	}
	
	public Tug(int id, Plan p, Grid g) {
		this.id=id;
		assert id < 100; // obstacles are greater equal 100
		plan = p;
		grid = g;
		stayProbability = 0.0;
		current = plan.getStep();
	}
	
	public int getState() {
		return state;
	}
	
	public boolean atDestination() {
		return plan.isEnd();
	}
	
	public Location getFinalLocation() {
		return plan.finalLocation();
	}	
	
	public boolean moved() {
		return MOVED;
	}
	
	public int whyIsTugStuck() {
		if (state == REQUEST) {
			int id = grid.getLockId(nextPos.x, nextPos.y);
			//System.out.println("REQUEST " + id);
			return id;
		}
		return -1;
	}
	
	boolean MOVED = false;
	int stayedCounter = 0;;
	int limit = 3;
	
	// this is the main activity of each tug
	void move(boolean force) {
		int x = current.getX();
		int y = current.getY();
		int xp = (nextPos != null) ? nextPos.getX() : -1;
		int yp = (nextPos != null) ? nextPos.getY() : -1;
		if (state == INITS) {
			grid.setLock(x,y,id);
			state = NEXT;
		} else if (state == NEXT) {
			MOVED = false;
			// compute next point on route
			if (plan.isEnd())
				return;
			if (!force) {
				if (stayedCounter < limit && stayProbability != 0.0) {
					if (Probabilistic.bernoulli(stayProbability)) {
						//System.out.println("Tug " + this.id + " is staying");
						stayedCounter++;
						return;
					}
					else {
						stayedCounter = 0;
						//System.out.println((1-stayProbability)*100.0 + " move");
					}
				}
				else {
					stayedCounter = 0;
					//System.out.println("100% move");
				}
			} else {
				stayedCounter = 0;
				//System.out.println("Forced to move");
			}
			nextPos = plan.getNextStep();
			state = REQUEST;
		} else if (state == REQUEST) {
			// request the lock but only if it is free
			// if(EXISTS_LOWER(idp,lock[xp][yp]@idp != 0)) return;
			if (grid.isLocked(xp,yp))
				return;
			grid.setLock(xp,yp,id);
			state = WAITING;
		} else if (state == WAITING) {
			// grab the lock if we are the highest
			// id node to request or hold the lock
			// if(EXISTS_HIGHER(idp, lock[xp][yp]@idp != 0)) return;
			int lockId = grid.getLockId(xp,yp);
			if (lockId > id)
				return;
			assert lockId == id;
			state = MOVE;
		} else if (state == MOVE) {
			grid.clearLock(x,y);
			current = plan.takeStep();
			state = NEXT;
			MOVED = true;
		}
	}
}
