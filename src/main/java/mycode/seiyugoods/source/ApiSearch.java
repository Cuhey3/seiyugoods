package mycode.seiyugoods.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mycode.seiyugoods.Broker;
import mycode.seiyugoods.source.callable.AmiamiItems;
import mycode.seiyugoods.source.entity.Seiyu;
import mycode.seiyugoods.source.repository.SeiyuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApiSearch {

    @Autowired
    SeiyuRepository repository;
    @Autowired
    AmiamiItems amiamiItems;

    public Object search(String name) throws IOException {
        List<Seiyu> findOneByName = repository.findOneByName(name);
        if (findOneByName.isEmpty()) {
            return new HashMap<>();
        } else {
            String amiamiCharJson = findOneByName.get(0).getAmiamiCharJson();
            if (amiamiCharJson.equals("{}")) {
                return new HashMap<>();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> readValue = mapper.readValue(amiamiCharJson, Map.class);
                Optional amiamiItemsCache = amiamiItems.getCache(Broker.allSourceTimeStamp, "listMap");
                if (amiamiItemsCache.isPresent()) {
                    Map<String, List> listMap = (Map<String, List>) amiamiItemsCache.get();
                    ArrayList hitItems = new ArrayList();
                    readValue.keySet().stream()
                            .filter((key) -> (listMap.containsKey(key)))
                            .forEach((key) -> {
                                hitItems.addAll(listMap.get(key));
                            });
                    if(hitItems.isEmpty()){
                        return new HashMap<>();
                    }else{
                        HashMap<String,Object> result = new HashMap<>();
                        result.put("character", readValue);
                        result.put("items", hitItems);
                        return result;
                    }
                } else {
                    return new HashMap<>();
                }
            }
        }
    }
}
