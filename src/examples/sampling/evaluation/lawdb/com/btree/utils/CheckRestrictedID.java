package sampling.evaluation.lawdb.com.btree.utils;

import java.util.*;

public class CheckRestrictedID
{
    ArrayList<Integer> ids;
    
    public CheckRestrictedID() {
        this.ids = (ArrayList<Integer>)new ArrayList();
    }
    
    public void add(final int id) {
        this.ids.add(id);
    }
    
    public boolean isRestricted(final int id) {
        return this.ids.contains((Object)id);
    }
    
    public boolean remove(final Integer key) {
        return this.ids.remove((Object)key);
    }
}
