/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycode.seiyugoods.source.callable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import mycode.seiyugoods.source.CallableSource;
import mycode.seiyugoods.source.polling.Amiami;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AmiamiTitles extends CallableSource {

    @Autowired
    Amiami amiami;

    @Override
    public Map<String, Object> call(Map<Class, Long> map) throws Exception {
        Optional amiamiCache = amiami.getCache(map, "titleSet");
        if (amiamiCache.isPresent()) {
            updateTimeStamp(map, Amiami.class);
            Map<String, Object> result = new HashMap<>();

                result.put (
                "titleSet", amiamiCache.get());
                return result ;
            }
            return null;
        }
    }
