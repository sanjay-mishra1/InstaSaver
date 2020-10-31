package com.example.instasaver;

import java.util.HashMap;
import java.util.Map;

public class KMap<K, V> extends HashMap<K, V> {

    public KMap() {
        super();
    }

    @Override
    public V get(Object key) {
        if (this.containsKey(key)) {
            return super.get(key);
        } else {
            Map<K, V> value = new KMap<K, V>();
            super.put((K)key, (V)value);
            return (V)value;
        }
    }
}
