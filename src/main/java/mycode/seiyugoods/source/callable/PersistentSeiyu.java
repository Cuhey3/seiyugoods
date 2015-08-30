package mycode.seiyugoods.source.callable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.CallableSource;
import mycode.seiyugoods.source.entity.Seiyu;
import mycode.seiyugoods.source.repository.SeiyuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistentSeiyu extends CallableSource {
    
    @Autowired
    SeiyuRepository repository;
    @Autowired
    CategoryAndTemplateSeiyu cats;
    
    @Override
    public Map<String, Object> call(Map<Class, Long> map) throws Exception {
        List<Seiyu> findAll = repository.findAll();
        Optional catsCache = cats.getCache(map, "mapList");
        if (catsCache.isPresent()) {
            updateTimeStamp(map, CategoryAndTemplateSeiyu.class);
            List<Seiyu> collect = ((List<Map<String, String>>) catsCache.get()).stream()
                    .filter((m) -> findAll.stream().noneMatch((seiyu) -> seiyu.getPageid().equals(m.get("pageid"))))
                    .map((m) -> new Seiyu(null, m.get("title"), m.get("pageid"), m.get("sortkeyprefix")))
                    .collect(Collectors.toList());
            if(!collect.isEmpty()){
                repository.save(collect);
            }
        }
        return null;
    }
}
