/*
 * Decompiled with CFR 0_114.
 */
package sampling.engagement1.gf4.hashmap;

import java.util.Map;

class Node<K, V>
implements Map.Entry<K, V> {
    final int hash;
    final K key;
    V value;
    Node<K, V> next;

    Node(int hash, K key, V value, Node<K, V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }

    @Override
    public final K getKey() {
        return this.key;
    }

    @Override
    public final V getValue() {
        return this.value;
    }

    public final String toString() {
        return this.key + "=" + this.value;
    }

    @Override
    public final V setValue(V newValue) {
        V oldValue = this.value;
        this.value = newValue;
        return oldValue;
    }

    @Override
    public final boolean equals(Object o) {
        Map.Entry e;
        if (o == this) {
            return true;
        }
        if (o instanceof Map.Entry && this.key.equals((e = (Map.Entry)o).getKey()) && this.value.equals(e.getValue())) {
            return true;
        }
        return false;
    }

    public static int hash(Object key, int capacity) {
        return (key.hashCode() & Integer.MAX_VALUE) % capacity;
    }
}

