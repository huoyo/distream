package cn.langpy.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapFrame<K, V> extends HashMap<K, V> {

    /*MapFrame<Object, ListFrame> agesGroup = lines.groupBy("年龄");*/
    public MapFrame<K, MapFrame<Object,ListFrame>> groupBy(String column) {
        MapFrame<K,MapFrame<Object,ListFrame>> mapFrames = new MapFrame();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            ListFrame v = (ListFrame) get(k);
            MapFrame<Object,ListFrame> mapFrame = v.groupBy(column);
            mapFrames.put(k,mapFrame);
        }
        return mapFrames;
    }

    public <T> Map<K, T> count() {
        Map<K, T> mapInt = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Integer size = vFrame.size();
                mapInt.put(k, (T)size);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                Map<Object, Integer> count = vFrame.count();
                mapInt.put(k,(T)count);
            }

        }
        return mapInt;
    }

    public <T> Map<K, T> sum(String sumColumn) {
        Map<K, T> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Double sum = vFrame.get(sumColumn).sum();
                map.put(k, (T)sum);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                Map<Object, Double> sum = vFrame.sum(sumColumn);
                map.put(k,(T)sum);
            }
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

    public <T> Map<K, T> avg(String sumColumn) {
        Map<K, T> map = new HashMap<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Double avg = vFrame.get(sumColumn).avg();
                map.put(k, (T)avg);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                Map<Object, Double> avg = vFrame.avg(sumColumn);
                map.put(k,(T)avg);
            }
        }
        return map;
    }

    public Map<K, Double> toHashMap() {
        return (HashMap) this;
    }

}
