// 
// Decompiled by Procyon v0.5.30
// 

package com.cyberpointllc.stac.sort;

public class ArrayIndex
{
    private static final int INVALID_MID = -1;
    private int startIndex;
    private int endIndex;
    private int midpoint;
    
    public static ArrayIndex partition(final int startIndex, final int endIndex) {
        return new ArrayIndex(startIndex, -1, endIndex);
    }
    
    public static ArrayIndex merge(final int startIndex, final int midpoint, final int endIndex) {
        return new ArrayIndex(startIndex, midpoint, endIndex);
    }
    
    private ArrayIndex(final int startIndex, final int midpoint, final int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.midpoint = midpoint;
    }
    
    public int getStart() {
        return this.startIndex;
    }
    
    public int getEnd() {
        return this.endIndex;
    }
    
    public int getMidpoint() {
        return this.midpoint;
    }
    
    public boolean isMerge() {
        return this.midpoint != -1;
    }
    
    public boolean isPartition() {
        return !this.isMerge();
    }
}
