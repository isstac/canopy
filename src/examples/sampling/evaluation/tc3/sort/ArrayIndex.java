/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// Decompiled by Procyon v0.5.30
// 

package sampling.evaluation.tc3.sort;

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
