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

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionStack
{
    Deque<Undo> t_stack;
    BTree btree;
    public boolean is_active;
    public static int INSERT;
    public static int DELETE;
    public static int UPDATE;
    
    public TransactionStack(final BTree btree) {
        this.t_stack = null;
        this.is_active = false;
        this.t_stack = (Deque<Undo>)new ArrayDeque<Undo>(1000);
        this.btree = btree;
    }
    
    void begin() {
        while (this.t_stack.poll() != null) {}
        this.is_active = true;
    }
    
    public void addInsert(final BTree.Node n, final BTree.Node parent, final int key) {
        if (this.is_active) {
            final Undo undo = new Undo(TransactionStack.INSERT, n, parent, key);
            this.t_stack.addLast(undo);
        }
    }
    
    public void rollback() {
        while (this.t_stack.peekLast() != null) {
            final Undo last = (Undo)this.t_stack.removeLast();
            last.undo();
        }
    }
    
    public void commit() {
        while (this.t_stack.poll() != null) {}
    }
    
    public class Undo
    {
        int type;
        int key;
        BTree.Node n;
        BTree.Node nparent;
        BTree.Node newn;
        BTree.Node newnparent;
        
        public Undo(final int type, final BTree.Node n, final BTree.Node nparent, final int key) {
            this.type = type;
            this.n = n;
            this.key = key;
            this.nparent = nparent;
            this.newn = this.newn;
            this.newnparent = this.newnparent;
            this.prepare();
        }
        
        public void prepare() {
            if (this.type == TransactionStack.INSERT && this.n != null) {
                this.newn = this.n.copy(null);
                this.newnparent = this.nparent.copy(null);
            }
        }
        
        public void undo() {
            if (this.type == TransactionStack.INSERT) {
                if (this.n != null) {
                    this.nparent.copyin(this.newnparent);
                    this.n.copyin(this.newn);
                }
                else {
                    TransactionStack.this.btree.delete(this.key);
                }
            }
        }
    }
}
