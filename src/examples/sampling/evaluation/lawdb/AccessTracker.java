/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sampling.evaluation.lawdb;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessTracker
{
    public int loc;
    public int[] ids;
    
    AccessTracker() {
        this.loc = 0;
        this.ids = new int[10];
    }
    
    void add(final String lastaccessinfolog, final String toString, final int id) {
        try {
            this.ids[this.loc] = id;
            ++this.loc;
        }
        catch (ArrayIndexOutOfBoundsException ae) {
            this.loc = 0;
        }
    }
    
    void clean() {
        try {
            final DSystemHandle sys = new DSystemHandle("127.0.0.1", 6669);
            // final DFileHandle fhlog = new DFileHandle("lastaccess.log", sys);
            // fhlog.setContents(this.ids.toString());
            // fhlog.storefast(null, null);
        }
        // catch (IOException ex) {
        catch (Exception ex) {
            Logger.getLogger(AccessTracker.class.getName()).log(Level.SEVERE, (String)null, (Throwable)ex);
        }
    }
}
