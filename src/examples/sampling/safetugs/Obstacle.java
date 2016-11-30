package sampling.safetugs;


/**
 * @author Willem Visser
 *
 */
public class Obstacle {

	Location current; // this has an x and y coordinate
	Location nextPos; 
	int id;	 // tug has an id
	Plan plan; // all the locations in order that tug will visit
	Grid grid;
	
	int state; //the state
	//states of each node
	static int INITS = 0;
	static int NEXT = 1;
	static int REQUEST = 2;
	static int WAITING = 3;
	static int MOVE = 4;
	 
	public Obstacle(int id, Plan p, Grid g) {
		this.id=id+100; // used to check if it is a Obstacle or Tug
		plan = p;
		grid = g;
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
	
	public Location getCurrentLocation() {
		return current;
	}
	
	public boolean moved() {
		return MOVED;
	}
	
	boolean MOVED = false;
	
	// this is the main activity of each tug
	void move(boolean force) {
		int x = current.getX();
		int y = current.getY();
		int xp = (nextPos != null) ? nextPos.getX() : -1;
		int yp = (nextPos != null) ? nextPos.getY() : -1;
		System.out.println(state + " Obstacle at " + current + " then " + nextPos);
		if (state == INITS) {
			grid.setLock(x,y,id);
			state = NEXT;
		} else if (state == NEXT) {
			MOVED = false;
			// compute next point on route
			if (plan.isEnd())
				return;
			if (!force)
				if (Probabilistic.bernoulli(0.9))
				   return;
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
