package sampling.safetugs;

public class Lock {
		boolean set = false;
		int id = -1;
		
		public Lock() {
			this.id = -1;
			set = false;
		}
		public void setLock(int id) {
			set = true;
			this.id = id;
		}
		public void clearLock() {
			set = false;
			id = -1;
		}
		public boolean isLocked() {
			return set;
		}
		public int getLockId() {
			return id;
		}
}
