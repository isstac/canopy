package sampling.evaluation.lawdb.com.client;

import java.io.*;
import java.util.*;

public class CommandLineApp
{
  public static DStoreClient btree;

  //    public static void main(final String[] args) throws IOException {
  //        (CommandLineApp.btree = new DStoreClient()).connect("127.0.0.1", 7689);
  //        final BufferedReader br = new BufferedReader((Reader)new InputStreamReader(System.in));
  //        while (true) {
  //            System.out.print("Enter Command (SEARCH, INSERT, GET, PUT, PUTRECORD,GETRECORD,DONE):");
  //            final String line;
  //            final String cmd = line = br.readLine();
  //            switch (line) {
  //                case "SEARCH": {
  //                    doSearchID(br);
  //                    continue;
  //                }
  //                case "GET": {
  //                    getFilename(br);
  //                    continue;
  //                }
  //                case "PUT": {
  //                    put(br);
  //                    continue;
  //                }
  //                case "INSERT": {
  //                    doInsertID(br);
  //                    continue;
  //                }
  //                case "PUTRECORD": {
  //                    putRecord(br);
  //                    continue;
  //                }
  //                case "GETRECORD": {
  //                    getRecord(br);
  //                    continue;
  //                }
  //                case "DONE": {
  //                    System.exit(1);
  //                    break;
  //                }
  //            }
  //            System.out.println("Unknown command");
  //        }
  //    }


  //Test bed. In order for a vulnerability to be found, the output should be >30s
  public static void main(final String[] args) throws IOException {
    (CommandLineApp.btree = new DStoreClient()).connect("127.0.0.1", 7689);

    int minID = 100000;
    int maxID = 40000000;
    int currID = 100000;//39876345;
    //for(int i = 0; i < 198; i++) {

    //   CommandLineApp.btree.insertnewkeyOverflow(currID+3);
    //}

    long start = System.currentTimeMillis();
    final ArrayList<Integer> search = CommandLineApp.btree.search(38423718, maxID);

    long end = System.currentTimeMillis();

    double elapsedTime = (end-start)/1000.0;
    System.out.println("time taken: " + elapsedTime);
  }

  public static void doInsertID(final BufferedReader br) throws IOException {
    System.out.print("Enter ID to insert:");
    final String id = br.readLine();
    try {
      final int i = Integer.parseInt(id);
      CommandLineApp.btree.insertnewkey(i);
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
  }

  public static void doSearchID(final BufferedReader br) throws IOException {
    System.out.println("Enter ID  range to insert");
    System.out.println("min of range:");
    final String minstr = br.readLine();
    System.out.println("max of range:");
    final String maxstr = br.readLine();
    try {
      final int min = Integer.parseInt(minstr);
      final int max = Integer.parseInt(maxstr);
      final ArrayList<Integer> search = CommandLineApp.btree.search(min, max);
      for (final Integer next : search) {
        System.out.println(new StringBuilder().append("\t").append((Object)next).toString());
      }
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
  }

  public static void getFileNamefromID(final BufferedReader br) throws IOException {
    System.out.print("Enter ID  to get associated File name:");
    System.out.print("ID:");
    final String id = br.readLine();
    try {
      Integer.parseInt(id);
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
  }

  public static void doDeleteID(final BufferedReader br) throws IOException {
    System.out.print("Enter ID  to delete:");
    System.out.print("ID:");
    final String id = br.readLine();
    try {
      final int i = Integer.parseInt(id);
      CommandLineApp.btree.delete(i);
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
  }

  public static String getFilename(final BufferedReader br) throws IOException {
    System.out.print("Enter ID  to get File record for:");
    System.out.print("ID:");
    final String id = br.readLine();
    String val = null;
    try {
      final int i = Integer.parseInt(id);
      val = CommandLineApp.btree.getval(i);
      System.out.println(val);
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
    return val;
  }

  public static String getRecord(final BufferedReader br) throws IOException {
    System.out.print("Enter Record Name:");
    final String rname = br.readLine();
    final String contents = CommandLineApp.btree.getfile(rname);
    System.out.println(contents);
    return rname;
  }

  public static void putRecord(final BufferedReader br) throws IOException {
    System.out.print("Enter Record Name:");
    final String rname = br.readLine();
    System.out.print("Enter Record Contents:");
    final String contents = br.readLine();
    CommandLineApp.btree.storefile(rname, contents);
  }

  public static void put(final BufferedReader br) throws IOException {
    System.out.print("Enter ID to update value of:");
    final String idx = br.readLine();
    try {
      final int id = Integer.parseInt(idx);
      System.out.print("Enter new val:");
      final String contents = br.readLine();
      CommandLineApp.btree.update(id, contents);
    }
    catch (NumberFormatException nfe) {
      System.err.println("Invalid Format!");
    }
  }

  static {
    CommandLineApp.btree = null;
  }
}
