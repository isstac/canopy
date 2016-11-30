package sampling;

/**
 * @author Kasper Luckow
 */
public class Local {


  public static void main(String[] args) {
//    test(2,2);
    //test2(2,true);
    test3(2);
  }

  public static void test(int a, int b) {
    int i = 0;
    int j = 0;
    if(b > a) {
      while (i < a) {
        i++;
      }
      if (i < 50) {
        while (j < b) {
          j++;
        }
      }
    }
  }

  public static void test2(int a, boolean c) {
    int i = 0;
    if(c) {
      while (i < a) {
        i++;
      }
    } else {
      while (i < 2*a) {
        i++;
      }
    }
  }

  public static void test3(int a) {
    int i = 0;
    while (i < a) {
      i++;
    }
  }
}
