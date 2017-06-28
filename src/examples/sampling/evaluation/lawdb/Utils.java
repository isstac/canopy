package sampling.evaluation.lawdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;

public class Utils
{
    public static String nameFile(final int key) {
        final String fname = new StringBuilder().append(key).append(":").append(System.currentTimeMillis()).toString();
        return fname;
    }
    
    public static void backup(final BTree btree, final String bfile) throws FileNotFoundException {
        final PrintWriter outf = new PrintWriter("./dumps/" + bfile);
        final ArrayList<Integer> allKeys = btree.getRange(0, Integer.MAX_VALUE);
        for (final Integer nextkey : allKeys) {
            outf.println(new StringBuilder().append("ADD:").append((Object)nextkey).append(":").append((Object)nextkey).toString());
            System.out.println(new StringBuilder().append("ADD:").append((Object)nextkey).append(":").append((Object)nextkey).toString());
        }
        outf.flush();
        outf.close();
    }
    
    public static void restore(final BTree btree, final String bfile, final CheckRestrictedID restricted) throws FileNotFoundException, IOException {
        final BufferedReader reader = new BufferedReader((Reader)new FileReader("./dumps/" + bfile));
        String line = null;
        while ((line = reader.readLine()) != null) {
            final int indexOfKey = line.indexOf("ADD:") + "ADD:".length();
            final int indexOfVal = line.indexOf(58, indexOfKey);
            final int indexOfpermission = line.indexOf(45, indexOfKey);
            final String keystr = line.substring(indexOfKey, indexOfVal);
            final int key = Integer.parseInt(keystr);
            final String vstr = line.substring(indexOfVal + 1, indexOfpermission);
            final String perm = line.substring(indexOfpermission + 1, line.length());
            if (key >= UDPServerHandler.IDMIN && key <= UDPServerHandler.IDMAX) {
                final int permv = Integer.parseInt(perm);
                if (permv > 0) {
                    restricted.add(key);
                }
                btree.add(key, vstr, false);
                System.out.println("log-" + line);
            }
            else {
                System.out.println(new StringBuilder().append("ERROR on ").append(key).append(", IDS Must fall in range:").append((Object)UDPServerHandler.IDMIN).append(" to ").append((Object)UDPServerHandler.IDMAX).toString());
            }
        }
        reader.close();
    }
    
    public static void rebalance(final ArrayList<Integer> x) {
    }
}
