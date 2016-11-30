package sampling.safetugs;

public class Location {
	int x;
	int y;
	
	public Location(int a, int b) {
		x = a; y = b;
	}
	
	public Location() {
		x = -1; y = -1;
	}
	
	public void setLocation(int a, int b) {
		x = a; y = b;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean isEqual(Object o) {
		Location l = (Location)o;
		return l.x == x && l.y == y;
	}
	
	public String toString() {
		return "["+x+","+y+"]";
	}
}
