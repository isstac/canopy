package sampling.safetugs;

public class Grid {
	// This code is to represent the Grid of Tug and Obstacle positions and a
	// position is locked if a Tug is there. The Tug number indicates
	// which Tug has the lock, i.e. is at that position	
	
	Lock[][] grid; 
	
	int X;
	int Y;
	
	public Grid(int X, int Y) {
		this.X = X;
		this.Y = Y;
		grid = new Lock[X][Y];
		for (int i = 0; i < X; i++) 
			for (int j = 0; j < Y; j++) 
				grid[i][j] = new Lock();
	}

	public void setLock(int x, int y, int id) {
		grid[x][y].setLock(id);
	}

	public boolean isLocked(int x, int y) {
		return grid[x][y].isLocked();
	}

	public int getLockId(int x, int y) {
		return grid[x][y].getLockId();
	}

	public void clearLock(int x, int y) {
		grid[x][y].clearLock();
	}		
	
	
	
}
