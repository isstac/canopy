/*
 * Decompiled with CFR 0_114.
 */
package sampling.evaluation.gabfeed5.sort;

public class ArrayIndex {
    private static final int INVALID_MID = -1;
    private int startIndex;
    private int endIndex;
    private int midpoint;

    public static ArrayIndex partition(int startIndex, int endIndex) {
        return new ArrayIndex(startIndex, -1, endIndex);
    }

    public static ArrayIndex merge(int startIndex, int midpoint, int endIndex) {
        return new ArrayIndex(startIndex, midpoint, endIndex);
    }

    private ArrayIndex(int startIndex, int midpoint, int endIndex) {
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

