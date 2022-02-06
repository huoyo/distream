package cn.langpy.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapFrame<K, V> extends HashMap<K, V> {

    public Map<K, Integer> count() {
        Map<K, Integer> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            ListFrame v = (ListFrame) get(k);
            int size = v.size();
            map.put(k, size);
        }
        return map;
    }

    public Map<K, Double> sum(String sumColumn) {
        Map<K, Double> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            ListFrame v = (ListFrame) get(k);
            map.put(k, v.get(sumColumn).sum());
        }
        return map;
    }

    public Map<K, ListFrame> concat(String sumColumn) {
        Map<K, ListFrame> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            ListFrame v = (ListFrame) get(k);
            map.put(k, v.get(sumColumn));
        }
        return map;
    }

    public Map<K, Double> avg(String sumColumn) {
        Map<K, Double> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            ListFrame v = (ListFrame) get(k);
            map.put(k, v.get(sumColumn).avg());
        }
        return map;
    }

    public Map<K, Double> toHashMap() {
        return (HashMap) this;
    }

}
