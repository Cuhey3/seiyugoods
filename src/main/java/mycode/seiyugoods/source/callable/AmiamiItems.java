package mycode.seiyugoods.source.callable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mycode.seiyugoods.source.CallableSource;
import mycode.seiyugoods.source.polling.Amiami;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AmiamiItems extends CallableSource {

    @Autowired
    Amiami amiami;

    @Override
    public Map<String, Object> call(Map<Class, Long> map) throws Exception {
        Optional amiamiCache = amiami.getCache(map, "mapList");
        if (amiamiCache.isPresent()) {
            updateTimeStamp(map, Amiami.class);
            List<Map<String, String>> mapList = (List<Map<String, String>>) amiamiCache.get();
            Map<String, List<Map<String, String>>> listMap = new LinkedHashMap<>();
            mapList.stream().forEach((m) -> {
                String key = m.get("orig");
                List<Map<String, String>> list = listMap.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(m);
                listMap.put(key, list);
            });
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("listMap", listMap);
            return result;
        }
        return null;
    }

}
