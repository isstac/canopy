package sampling.safetugs;

/**
 * @author Corina Pasareanu and Willem Visser
 *
 */

public class Main {
	
	static Tug[] t; // the tugs
	static Obstacle obstacle; // the obstacle
	
	  //path for each tug. path is represented as
	  //-- path 0 : 
	  //   (4,2),(4,3), (4,4), (5,4), (6,4), (6,3), (6,2)
	  //-- path 1 : 
	  //   (4,3), (4,4), (5,4), (6,4), (7,4), (7,3)
	  //-- path 2 : 
	  //   (4,4), (5,4), (6,4), (6,5), (6,4)
	  //-- path 3 : 
	  //   (4,5), (5,5), (6,5), (7,5)
	  //-- saves the next waypoint in (xp,yp)

	static void setupTugPlans(Grid g) {
		t = new Tug[4]; oldState = new int[4];
		Plan p0 = new Plan(7);
		Plan p1 = new Plan(6);
		Plan p2 = new Plan(5);
		Plan p3 = new Plan(4);
		p0.add(4,2);p0.add(4,3);p0.add(4,4);p0.add(5,4);p0.add(6,4);p0.add(6,3);p0.add(6,2);
		p1.add(4,3);p1.add(4,4);p1.add(5,4);p1.add(6,4);p1.add(7,4);p1.add(7,3);
		p2.add(4,4);p2.add(5,4);p2.add(6,4);p2.add(6,5);p2.add(6,4);
		p3.add(4,5);p3.add(5,5);p3.add(6,5);p3.add(7,5);		
		t[0] = new Tug(0,p0,g);
		t[1] = new Tug(1,p1,g);
		t[2] = new Tug(2,p2,g);
		t[3] = new Tug(3,p3,g);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
		oldState[2] = t[2].getState();
		oldState[3] = t[3].getState();
	}
	
	/*
	 * On a 4,4 grid these are the tug definitions
	 * 2
	 * 1,0,1,3,0.1
	 * 0,1,3,1,0.8
	 * 
	 * these are the plans as given
	 * 13 14 15 16 
	 * 8 7 13 19 25 26 
	 * 
	 * which corresponds to this grid
	 * ... 7   8   9  10
	 * ...13  14  15  16
	 * ...19  20  21  22
	 * ...25  26  27  28
	 * 
	 *  which means the plans are for us
	 *  1,0 -> 1,1 -> 1,2 -> 1,3 with a staying probability of 0.1
	 *  0,1 -> 0,0 -> 1,0 -> 2,0 -> 3,0 -> 3,1 with a staying probability 0.8 
	 */
	static void setupTugPlansProbability(Grid g) {
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(5);
		Plan p1 = new Plan(6);
		p0.add(1,0);p0.add(1,1);p0.add(1,2);p0.add(1,3);p0.add(1,4);
		p1.add(0,1);p1.add(0,0);p1.add(1,0);p1.add(2,0);p1.add(3,0);p1.add(3,1);
		t[0] = new Tug(0,p0,g,0.8);
		t[1] = new Tug(1,p1,g,0.1);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
	}
	
	static void setupTugPlansProbability(Grid g, Double[] probs) {
		assert probs.length == 2;
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(4);
		Plan p1 = new Plan(6);
		p0.add(1,0);p0.add(1,1);p0.add(1,2);p0.add(1,3);
		p1.add(0,1);p1.add(0,0);p1.add(1,0);p1.add(2,0);p1.add(3,0);p1.add(3,1);
		t[0] = new Tug(0,p0,g,probs[0]);
		t[1] = new Tug(1,p1,g,probs[1]);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
	}
	
	static void setupTugPlansProbabilistic(Grid g) {
		t = new Tug[4]; oldState = new int[4];
		Plan p0 = new Plan(7);
		Plan p1 = new Plan(6);
		Plan p2 = new Plan(5);
		Plan p3 = new Plan(4);
		p0.add(4,2);p0.add(4,3);p0.add(4,4);p0.add(5,4);p0.add(6,4);p0.add(6,3);p0.add(6,2);
		p1.add(4,3);p1.add(4,4);p1.add(5,4);p1.add(6,4);p1.add(7,4);p1.add(7,3);
		p2.add(4,4);p2.add(5,4);p2.add(6,4);p2.add(6,5);p2.add(6,4);
		p3.add(4,5);p3.add(5,5);p3.add(6,5);p3.add(7,5);		
		t[0] = new Tug(0,p0,g,0.9);
		t[1] = new Tug(1,p1,g,1.0);
		t[2] = new Tug(2,p2,g,1.0);
		t[3] = new Tug(3,p3,g,1.0);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
		oldState[2] = t[2].getState();
		oldState[3] = t[3].getState();
	}
	
	static void setupTugPlans2(Grid g) {
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(3);
		Plan p1 = new Plan(3);
		p0.add(4,2);p0.add(5,2);p0.add(6,2);
		p1.add(4,3);p1.add(5,3);p1.add(6,3);
		t[0] = new Tug(0,p0,g);
		t[1] = new Tug(1,p1,g);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
	}
	
	static void setupTugPlans2WithObstacle(Grid g) {
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(3);
		Plan p1 = new Plan(3);
		p0.add(4,2);p0.add(5,2);p0.add(6,2);
		p1.add(4,3);p1.add(5,3);p1.add(6,3);
		t[0] = new Tug(0,p0,g);
		t[1] = new Tug(1,p1,g);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
		Plan plane = new Plan(4);
		plane.add(5, 1);plane.add(5, 2);plane.add(5, 3);plane.add(5, 4);
		obstacle = new Obstacle(0,plane,g);
	}
	
	static void setupTugPlansSimple(Grid g) {
		int numTugs = 1;
		t = new Tug[numTugs]; oldState = new int[numTugs];
		Plan p0 = new Plan(4);
		p0.add(2,0);p0.add(2,1);p0.add(2,2);p0.add(2,3);
		t[0] = new Tug(0,p0,g);
		oldState[0] = t[0].getState();
		Plan plane = new Plan(3);
		plane.add(1, 2);plane.add(2, 2);plane.add(3, 2);
		obstacle = new Obstacle(0,plane,g);
	}
	
	static void setupTugPlansSimple2(Grid g) {
		int numTugs = 1;
		t = new Tug[numTugs]; oldState = new int[numTugs];
		Plan p0 = new Plan(4);
		/*p0.add(2,0);*/p0.add(2,1);p0.add(2,2);p0.add(2,3);p0.add(2,4);
		t[0] = new Tug(0,p0,g);
		oldState[0] = t[0].getState();
		Plan plane = new Plan(3);
		plane.add(1, 3);plane.add(2, 3);plane.add(3, 3);
		obstacle = new Obstacle(0,plane,g);
	}
	
	static void setupProbablisticTugs(Grid g) {
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(3);
		Plan p1 = new Plan(3);
		p0.add(4,2);p0.add(5,2);p0.add(6,2);
		p1.add(2,2);p1.add(3,2);p1.add(4,2);
		t[0] = new Tug(0,p0,g,0.5);
		t[1] = new Tug(1,p1,g,0.0);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
	}
	
	// two move right, one move left
	static void setupProbablisticTugs2(Grid g) {
		t = new Tug[2]; oldState = new int[2];
		Plan p0 = new Plan(3);
		//Plan p1 = new Plan(3);
		Plan p2 = new Plan(3);
		p0.add(3,2);p0.add(4,2);p0.add(5,2);
		//p1.add(2,2);p1.add(3,2);p1.add(4,2);
		p2.add(6,2);p2.add(5,2);p2.add(5,1);//p2.add(4,1);
		t[0] = new Tug(0,p0,g,0.9);
		//t[1] = new Tug(1,p1,g,1.0);
		t[1] = new Tug(1,p2,g,0.1);
		oldState[0] = t[0].getState();
		//oldState[1] = t[1].getState();
		oldState[1] = t[1].getState();
	}
	
	static void setupTugPlans3Deadlock(Grid g) {
		t = new Tug[3]; oldState = new int[3];
		Plan p0 = new Plan(3);
		Plan p1 = new Plan(3);
		Plan p2 = new Plan(1);		
		p0.add(4,2);p0.add(5,2);p0.add(6,2);
		p1.add(4,3);p1.add(5,3);p1.add(6,3);
		p2.add(5, 2);
		t[0] = new Tug(0,p0,g);
		t[1] = new Tug(1,p1,g);
		t[2] = new Tug(2,p2,g);
		oldState[0] = t[0].getState();
		oldState[1] = t[1].getState();
		oldState[2] = t[2].getState();
	}
	
	static int getTug(int i, int j) {
		int result = -1;
		for (int idx = 0; idx < t.length; idx++) {
			int posX = t[idx].current.getX();
			int posY = t[idx].current.getY();
			if (i == posY && j == posX) {
				if (result != -1)
					System.out.println("------BROKEN------");
				result = idx;
			}
		}
		return result;
	}
	
	static boolean isFinal(int id, int i, int j) {
		Location f = t[id].getFinalLocation();
		return (f.getX() == j && f.getY() == i);
	}
	
	static String getGridSymbol(int x, int y) {
		if (obstacle != null) {
			Location obsLocation = obstacle.getCurrentLocation();
			int obsX = obsLocation.getX();
			int obsY = obsLocation.getY();
			if (y == obsX && x == obsY) {
				return "o|";
			}
		}
		int tugNum = getTug(x,y); // gets the tug at (x,y) or -1 if none there
		for (int i = 0; i < t.length; i++)
			if (isFinal(i,x,y))
				if (tugNum == -1) 
					return Character.toString((char)(i+97))+"|";
				else 
					return Character.toString((char)(i+65))+"|";
		if (tugNum != -1)
			return tugNum+"|";
		else 
			return " |";
	}
	
	static void printGrid(int step, Grid g) {
		System.out.println(" Grid " + step);
		System.out.print(" ");
		for (int i = 0; i < g.X; i++)
			System.out.print(" "+i);
		//System.out.println("  0 1 2 3 4 5 6 7 8 9");
		System.out.println();
		for(int i = 0; i < g.X; i++) {
			System.out.print(i+"|");
			for (int j = 0; j < g.Y; j++)
				System.out.print(getGridSymbol(i,j));
			System.out.println();
		}
	}
	
	static boolean checkObstacleForTug(int i) {
		int id = t[i].whyIsTugStuck(); // returns id of tug/obstacle in its way
		System.out.println(" Tug waiting for id = " + id);
		return (id >= 100);
	}
	
	static boolean isTugStuck(int i) {
		int id = t[i].whyIsTugStuck(); // returns id of tug/obstacle in its way
		//System.out.println(" Tug " + i + " waiting for id = " + id);
		return (id >= 0);
	}
	
	static boolean allDone() {
		for (int i=0;i<t.length;i++) {
			if (!t[i].atDestination())
				return false;
		}
		return true;
	}
	
	static int[] oldState;
	
	static boolean progress() {
		boolean change = false;
		for (int i=0;i<t.length;i++) {
			int newState = t[i].getState();
			if (oldState[i] != newState)
				change = true;
			oldState[i] = newState;
		}
		return change;
	}
	
	public static void countBin(int i) {}
	public static void countWin(int i) {}
	public static void probability(int i) {}
	public static void countTotal(int i) {}
	public static void countLoss(int i) {}
	
	static void runSim(Grid g) {		
		int moves = 0;
		int stuckCount = 0;
		printGrid(moves,g);
		int tugIndex = 0;
		while (!allDone() && moves < 50){
			if (obstacle != null) {
				// first try and move the obstacle
				obstacle.move(false);
			}
			t[tugIndex].move(false);
			if (t[tugIndex].moved()) {
				moves++;
				printGrid(moves,g);
				tugIndex = 0;
			} else {
				// check if tug is stuck because something in its way
				boolean stuck = isTugStuck(tugIndex);
				if (stuck) {
					System.out.println("Tug " + tugIndex + " is stuck");
					printGrid(moves,g);
					stuckCount++;
				}
				if ((tugIndex == t.length-1) && !progress()) {
					if (!allDone()) {	
						System.out.println("Deadlock!");
					}
					break;
				}
				tugIndex = (tugIndex+1) % t.length;
			}
			
		}
		System.out.println("StuckCount " + stuckCount);
	}
	
	static void runSim2(Grid g) {		
		int moves = 0;
		int stuckCount = 0;
		printGrid(moves,g);
		int tugIndex = 0;
		while (!allDone() && moves < 10){
			// first try and move the obstacle
			if (obstacle != null)	
				obstacle.move(false);
			t[tugIndex].move(false);
			
			if (t[tugIndex].moved()) {
				moves++;
				printGrid(moves,g);
				tugIndex = 0;
			} else {
				if ((tugIndex == t.length-1) && !progress()) {
					if (!allDone()) {
						for (int i = 0; i < t.length; i++) {
							boolean stuck = checkObstacleForTug(i);
							if (stuck) {
							  System.out.println("Tug " + i + " stuck waiting for Obstacle");
							  printGrid(moves,g);
							  stuckCount++;
							  if (obstacle.atDestination())
								  break;
							  else {
								  int obsTries = 0;
								  while (!obstacle.moved() && !obstacle.atDestination()) {
									  System.out.println("trying to move obstacle");
									  obstacle.move(false);
									  obsTries++;
									  if (obsTries >= 3) { // maybe replan?
										  obstacle.move(true);
									  }
								  }
							  }
							}
						}
						//System.out.println("Deadlock!");
					}
					//break;
				}
				tugIndex = (tugIndex+1) % t.length;
			}	
		}
		if (stuckCount > 0)
			countLoss(0);
		else 
			countWin(0);
		countTotal(0);
	}
	
	static void runSim3(Grid g) {		
		int moves = 0;
		int stuckCount = 0;
		int deadlockCount = 0;
		//printGrid(moves,g);
		int tugIndex = 0;
		int maxMoves = 50;
		while (!allDone() && moves < maxMoves){
			// first try and move the obstacle
			if (obstacle != null)	
				obstacle.move(false);
			t[tugIndex].move(false);
			
			if (t[tugIndex].moved()) {
				moves++;
				//printGrid(moves,g);
				tugIndex = 0;
			} else {
				// check if tug is stuck because something in its way
				if (isTugStuck(tugIndex)) {
					//System.out.println("Tug " + tugIndex + " is stuck");
					//printGrid(moves,g);
					stuckCount++;
				}
				if ((tugIndex == t.length-1) && !progress()) {
					if (!allDone()) {
						//check if all tucks are really stuck?
						//maybe they just don't want to move
						int tug2move = -1;
						for (int i = 0; i < t.length; i++) {
							if (!isTugStuck(i) && !t[i].atDestination()) {
								//System.out.println("Found tug to move " + i);
								tug2move = i;
							}
						}
						if (tug2move == -1) {
							//System.out.println("Deadlock!");
							//printGrid(moves,g);
							deadlockCount++;
							break;
						} else {
							// try to move the tug
							if (t[tug2move].atDestination())
								break;
							else {
								int tries = 0;
								t[tug2move].move(true);
								//printGrid(moves, g);
								/*
								while (!t[tug2move].moved() && 
									   !t[tug2move].atDestination()) {
							       System.out.println("trying to move tug");
								   t[tug2move].move(false);
								   tries++;
								   if (tries >= 3) { // maybe replan?
									  t[tug2move].move(true);
								   }
								}*/
							 }
						}
					}
				}
				tugIndex = (tugIndex+1) % t.length;
			}	
		}
		if (moves >= maxMoves) {
			System.out.println("Stopped Prematurely");
			assert false;
		}
		probability(stuckCount); // probability of each stuckCount, 0, is never stuck
		if (deadlockCount > 0) {
			assert stuckCount > 0;
			probability(99); // 99 special value for deadlock
		}
		//System.out.println("Deadlocks " + deadlockCount + " StuckCount " + stuckCount);
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Double[] tugStayingProb = new Double[2];
		/*
		if (args.length == 2) {
			tugStayingProb[0] = Double.parseDouble(args[0]);
			tugStayingProb[1] = Double.parseDouble(args[1]);
			System.out.println("Tug0StayingProb" + args[0] + " = " + tugStayingProb[0]);
			System.out.println("Tug0StayingProb" + args[0] + " = " + tugStayingProb[1]);
		}
		*/
		Grid grid = new Grid(10,10);
		setupTugPlansProbability(grid);
		runSim3(grid);
		
	}

}
