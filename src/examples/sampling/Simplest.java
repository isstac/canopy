package sampling;

/**
 * @author Kasper Luckow
 */
public class Simplest {

  public static void main(String[] args) {
    test(2);
  }

  public static void test(int a) {

    if (a > 99) {
      System.out.println("a > 85");
//      if (a > 90) {
//
//        System.out.println("a > 85 && a > 90");
//
//      } else {
//        System.out.println("a > 85 && a <= 90");
//
//      }
    }
    else {
      System.out.println("a <= 85");
    }

  }
}
