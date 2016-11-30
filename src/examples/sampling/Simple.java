package sampling;

/**
 * @author Kasper Luckow
 *
 */
public class Simple {

  
  public static void main(String[] args) {
    test(2);
  }
  
  public static void test(int a) {
    if(a < 10) {
      if(a < 9) {
        if(a < 5) {
          if(a < 3) {
            System.out.println(a);
          }
        } else {
          if(a > 5) {
            if(a > 6) {
              System.out.println(a);              
            }
          }
        }
      }
//      for(int i = 0; i < a; i++) {
//        if(i == 9) {
//          assert false;
//        }
//      }
      System.out.println(a);
    } else {   
      System.out.println(a);
    }
  }
}
