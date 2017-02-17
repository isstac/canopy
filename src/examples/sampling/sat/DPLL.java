package sampling.sat;

import java.util.*;

import gov.nasa.jpf.symbc.Debug;

/**
 * Based on the implementation found here:
 * http://giudoku.sourceforge.net/codes.html
 */
public class DPLL
{
//  public static void main(String[] args)
//  {
//    try
//    {
//      List<Clausola> formula = new LinkedList<Clausola>();
//      int n = Integer.parseInt(args[0]);
//
//      parseClauses(formula, args, n);
//
//      boolean[] solution = solveDPLL(n, formula);
//      if (solution != null)
//      {
//        System.out.println("A possible assignation for the variables has been found: ");
//        System.out.println(Arrays.toString(solution));
//      }
//      else
//      {
//        System.out.println("The formula is a contradiction");
//      }
//    }
//    catch (NumberFormatException e)
//    {
//      System.out.println("Wrong format of an argument");
//      System.out.println();
//      helpMessage();
//    }
//    catch (ArrayIndexOutOfBoundsException e)
//    {
//      System.out.println("Missing arguments");
//      System.out.println();
//      helpMessage();
//    }
//    catch (IndexOutOfBoundsException e)
//    {
//      System.out.println(e.getMessage());
//      System.out.println();
//      helpMessage();
//    }
//  }


  public static void main(String[] args) {

    final int literals = Integer.parseInt(args[0]);
    final int clausesNum =Integer.parseInt(args[1]);

    List<Clause> clauses = new LinkedList<>();

    for (int j = 0; j < clausesNum; j++) {
      Clause clause = new Clause(literals);
      for(int i = 0; i < literals; i++) {
        boolean neg = Debug.makeSymbolicBoolean("neg" + i + "_" + j + "_" + (i+1));
        int lit = i + 1;
        if (neg) {
          clause.addLiteral(lit);
        } else {
          clause.addLiteral(-lit);
        }
      }
      clauses.add(clause);
    }

    boolean[] solution = solveDPLL(literals, clauses);
  }

  /*
   * Parses clauses on command line and puts them in the formula
   * Throws exceptions if the format isn't correct
   */
  public static void parseClauses(List<Clause> F, String[] args, int N)
  {
    for (int i = 1; i < args.length; i++)
    {
      /*
       * Brackets not present: this usually means the user has entered some whitespaces
       * between the characters in the clause
       */
      if ((args[i]).charAt(0) != '[' || (args[i]).charAt(args[i].length() -1) != ']')
      {
        throw new NumberFormatException("Brackets");
      }
      
      String clause = args[i].substring(1, args[i].length() - 1); /* removes brackets */
      clause += ","; /* necessary for the last literals */
      
      Clause parsed = new Clause(N);
      
      String[] literals = clause.split(","); /* single literals */
      for (int j = 0; j < literals.length; j++)
      {
        parsed.addLiteral(Integer.parseInt(literals[j]));
      }
      F.add(parsed);
    }
  }
  
  /*
   * Prints usage message
   */
  public static void helpMessage()
  {
    System.out.println("Usage: $ java DPLL N <clause1> ... <clauseN>");
    System.out.print("Each clause is in the form (x1,x2, ...,xN) where x(i) is an integer in [-N; N] (without 0)");
    System.out.println(" and represents a literal (direct if positive, with NOT if negative). ");
    System.out.println("Clauses must not contain whitespaces");
    System.out.println("You can't have more than N literals in total (and in a single clause of course)");
  }
  
  public static boolean[] solveDPLL(int n_literals, List<Clause> F)
  {
    boolean[] assign = new boolean[n_literals]; /* will contain the assignments if possible */
    
    if (solveDPLL2(n_literals, F, assign))
    {
      return assign;
    }
    
    return null;
  }

  /*
   * The core DPLL method
   * return True if a contains a valid assignation, False if F is a contradiction
   */
  public static boolean solveDPLL2(int n_literals, List<Clause> F, boolean[] a)
  {
    /*
     * Two "tricks" to improve performances avoiding to try
     * all possible assignations
     */
    UnitPropagate(F, a);
    PureLiteralAssign(F, a, n_literals);
    
    if (F.isEmpty()) /* every clause has been removed */
    {
      return true;
    }
    if (anEmptyClause(F)) /* a clause has been emptied */
    {
      return false;
    }
    
    int nextLiteral = ChooseLiteral(F);
    
    /*
     * Creates two new formulas adding to F literal and !literal respectively
     */
    List<Clause> F1 = new LinkedList<Clause>(F);
    List<Clause> F2 = new LinkedList<Clause>(F);
    
    Clause new1 = new Clause(n_literals);
    new1.addLiteral(nextLiteral);
    Clause new2 = new Clause(n_literals);
    new2.addLiteral(-nextLiteral);
    
    F1.add(new1);
    F2.add(new2);
    
    /*
     * Recursive step! Tries the two possible assignations of nextLiteral (False and True)
     * and finds out if it creates a valid assignation or a contradiction
     */
    return solveDPLL2(n_literals, F1, a) || solveDPLL2(n_literals, F2, a);
  }
  
  public static void PureLiteralAssign(List<Clause> F, boolean[] assign, int n)
  {
    int pos = 0;
    int neg = 0;
    
    for (int i = 1; i <= n; i++)
    {
      ListIterator<Clause> it = F.listIterator();
      pos = neg = 0;
      while (it.hasNext())
      {
        Clause c = (Clause)it.next();
        if (c.hasLetterale(i))
        {
          pos++; /* "positive" form */
        }
        else if (c.hasLetterale(-i))
        {
          neg++; /* "negative" form */
        }
      }
      
      if (pos == n) /* the literal appears in every clause in positive form */
      {
        assign[i-1] = true; /* the variable gets assigned True */
        
        /*
         * Updates the formula by unit propagating
         */
        Clause new1 = new Clause(3);
        new1.addLiteral(i);
        F.add(new1);
        UnitPropagate(F, assign);
      }
      else if (neg == n) /* the literal appears in every clause in negative form */
      {
        assign[i-1] = false;
        
        /*
         * Same updating as before
         */
        Clause new1 = new Clause(3);
        new1.addLiteral(-i);
        F.add(new1);
        UnitPropagate(F, assign);
      }
    }
  }
  
  public static void UnitPropagate(List<Clause> F, boolean[] assign)
  {    
    for (int i = 0; i < F.size(); i++)
    {
      Clause cu = F.get(i);
      if (cu.clausolaUnaria())
      {
        int propagate = cu.getLetterale();
        
        if (propagate > 0)
        {
          assign[propagate-1] = true;
        }
        else
        {
          assign[-propagate-1] = false;
        }
        
        /*
         * Updates all the formula removing the instances of !propagate
         * and removing the clauses which contains propagate
         */
        for (int j = 0; j < F.size(); j++)
        {
          Clause c1 = F.get(j);
          if (c1.hasLetterale(propagate))
          {
            F.remove(j);
            j--; /* if you remove this the program will get drunk and start dancing mambo on tables */
          }
          else if (c1.hasLetterale(-propagate))
          {
            F.remove(j);
            c1.rmvLetterale(-propagate);
            F.add(j, c1);
          }
        }
        i = 0; /* resets the search from the beginning to reflect the recent changes */
      }
    }
  }
  
  /*
   * Checks if the formula contains an empty clause
   */
  public static boolean anEmptyClause(List<Clause> F)
  {
    ListIterator<Clause> it = F.listIterator();
    
    while (it.hasNext())
    {
      Clause c = (Clause)it.next();
      if (c.clausolaVuota())
      {
        return true;
      }
    }
    
    return false;
  }

  /*
   * Randomly returns the first literal it can get
   */
  public static int ChooseLiteral(List<Clause> F)
  {
    Clause first = F.get(0);
    return first.getLetterale();
  }
  
}
