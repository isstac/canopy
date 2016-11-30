package sampling.backtracking;

/**
 * @author Kasper Luckow
 */
public class BacktrackingExample {

  public static void main(String[] args) {
    test(10);
  }

  public static void test(int a) {
    if(a > 10) {
      System.out.println("a > 10");
      if(a <= 10) {
        System.out.println("a <= 10");
      }
    }

  }
}
