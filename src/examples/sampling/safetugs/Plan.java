package sampling.safetugs;

public class Plan {
	int total;
	int currentStep;
	Location[] steps;
	
	private int stepIdx;
	
	public Plan(int t) {
		total = t;
		steps = new Location[total];
		currentStep = 0;
		stepIdx = 0;
	}
	
	public void add(int x, int y) {
		steps[stepIdx++] = new Location(x,y);
	}
	
	// return the next step, but don't advance yet
	public Location getNextStep() {
		return steps[currentStep+1];
	}
	
	public Location takeStep() {
		currentStep++;
		return steps[currentStep];
	}
	
	public Location getStep() {
		return steps[currentStep];
	}
	
	public boolean isEnd() {
		return (currentStep == total-1);
	}
	
	public Location finalLocation() {
		return steps[total-1];
	}
}
