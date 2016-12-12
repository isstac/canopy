package sampling.batch;

/**
 * @author Kasper Luckow
 */
public class SortList {
  private int x;
  private SortList next;

  private static final int SENTINEL = Integer.MAX_VALUE;

  private SortList(int x, SortList next) {
    this.x = x;
    this.next = next;
  }

  public SortList() {
    this(SENTINEL, null);
  }

  public void insertMask(int data) {
    if (data > this.x) {
      next.insertMask(data);
    } else {
      next = new SortList(x, next);
      x = data;
    }
  }

  public void insert(int data) {
    if (data > this.x) {
      next.insert(data);
    } else {
      next = new SortList(x, next);
      x = data;
    }
  }
}
