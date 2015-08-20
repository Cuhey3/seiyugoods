package mycode.seiyugoods.source;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import mycode.seiyugoods.Broker;
import org.apache.camel.Body;
import org.apache.camel.builder.RouteBuilder;

public abstract class Source extends RouteBuilder {

    private int hash = -1;
    protected final Map<Class, Long> usingSourceTimeStamp = new HashMap<>();
    protected final Map<Long, Map<String, Object>> cache = new HashMap<>();

    public boolean isUpToDate(Map<Class, Long> brokerMap) {
        return !cache.isEmpty();
    }

    public boolean checkUpdate(Map<String, Object> sourceFields) {
        int newHash = Arrays.hashCode(sourceFields.values().stream()
                .map((obj) -> {
                    if (obj.getClass().isArray()) {
                        return Arrays.hashCode((Object[]) obj);
                    } else {
                        return obj.hashCode();
                    }
                }).toArray(size -> new Integer[size]));
        if (hash != newHash) {
            hash = newHash;
            return true;
        } else {
            return false;
        }
    }

    public Optional<Long> checkBrokerAndCreateKey() {
        if (Broker.isUpToDate(usingSourceTimeStamp)) {
            long key = System.currentTimeMillis();
            Broker.allSourceTimeStamp.put(this.getClass(), key);
            return Optional.ofNullable(key);
        } else {
            return Optional.ofNullable(null);
        }
    }

    public Optional getCache(Map<Class, Long> map, String kind) {
        Long key = map.get(this.getClass());
        return cache.keySet().stream()
                .filter((k) -> k.equals(key))
                .map((k) -> cache.get(k).get(kind))
                .findFirst();
    }

    public boolean checkNotate(@Body Map<String, Object> body) {
        if (body != null && checkUpdate(body)) {
            Optional<Long> key = checkBrokerAndCreateKey();
            if (key.isPresent()) {
                cache.clear();
                cache.put(key.get(), body);
                return true;
            }
        }
        return false;
    }

    public void updateTimeStamp(Map<Class, Long> map, Class clazz) {
        usingSourceTimeStamp.put(clazz, map.get(clazz));
    }
}
