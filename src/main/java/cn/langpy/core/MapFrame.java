package cn.langpy.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapFrame<K, V> extends LinkedHashMap<K, V> {

    public MapFrame<K, MapFrame<Object,ListFrame>> groupBy(String column) {
        MapFrame<K,MapFrame<Object,ListFrame>> mapFrames = new MapFrame();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame) v;
                MapFrame<Object,ListFrame> mapFrame = vFrame.groupBy(column);
                mapFrames.put(k,mapFrame);
            } else if (v instanceof MapFrame) {
                MapFrame vFrame = (MapFrame) v;
                MapFrame mapFrame = vFrame.groupBy(column);
                mapFrames.put(k,mapFrame);
            }

        }
        return mapFrames;
    }

    public <T> MapFrame<K, T> count() {
        MapFrame<K, T> mapInt = new MapFrame<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Integer size = vFrame.size();
                mapInt.put(k, (T)size);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame<Object, Integer> count = vFrame.count();
                mapInt.put(k,(T)count);
            }

        }
        return mapInt;
    }

    public <T> MapFrame<K, T> sum(String sumColumn) {
        MapFrame<K, T> map = new MapFrame<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Double sum = vFrame.get(sumColumn).sum();
                map.put(k, (T)sum);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame<Object, Double> sum = vFrame.sum(sumColumn);
                map.put(k,(T)sum);
            }
        }
        return map;
    }

    public <T> MapFrame<K, T> max(String maxColumn) {
        MapFrame<K, T> map = new MapFrame<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Object max = vFrame.get(maxColumn).max();
                map.put(k, (T)max);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame max = vFrame.max(maxColumn);
                map.put(k,(T)max);
            }
        }
        return map;
    }

    public <T> MapFrame<K, T> min(String minColumn) {
        MapFrame<K, T> map = new MapFrame<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Object min = vFrame.get(minColumn).min();
                map.put(k, (T)min);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame min = vFrame.min(minColumn);
                map.put(k,(T)min);
            }
        }
        return map;
    }

    public <T> MapFrame<K, T> concat(String concatColumn) {
        MapFrame<K, T> map = new MapFrame<K,T>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                map.put(k, (T)vFrame.get(concatColumn));
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame<Object, ListFrame> concat = vFrame.concat(concatColumn);
                map.put(k,(T)concat);
            }
        }
        return map;
    }

    public <T> MapFrame<K, T> avg(String avgColumn) {
        MapFrame<K, T> map = new MapFrame<>();
        Set<K> ks = this.keySet();
        for (K k : ks) {
            V v = get(k);
            if (v instanceof ListFrame) {
                ListFrame vFrame = (ListFrame)v ;
                Double avg = vFrame.get(avgColumn).avg();
                map.put(k, (T)avg);
            } else if (v instanceof MapFrame) {
                MapFrame<Object,ListFrame> vFrame = (MapFrame<Object,ListFrame>)v ;
                MapFrame<Object, Double> avg = vFrame.avg(avgColumn);
                map.put(k,(T)avg);
            }
        }
        return map;
    }

    public Map<K, Double> toHashMap() {
        return (HashMap) this;
    }

}
