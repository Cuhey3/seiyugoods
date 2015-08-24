package mycode.seiyugoods.source.callable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.CallableSource;
import mycode.seiyugoods.source.polling.SeiyuCategoryMembers;
import mycode.seiyugoods.source.instant.SeiyuTemplateIncludePages;
import mycode.seiyugoods.Utility;
import mycode.seiyugoods.source.instant.AdultSeiyuCategoryMembers;
import org.apache.camel.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryAndTemplateSeiyu extends CallableSource {

    @Autowired
    SeiyuTemplateIncludePages stip;
    @Autowired
    AdultSeiyuCategoryMembers ascm;
    @Autowired
    SeiyuCategoryMembers scm;

    @Override
    public Map<String, Object> call(@Body Map<Class, Long> map) throws IOException {
        stip.ready();
        Set<String> attrSet1 = new Utility().getAttrSet(stip.getMapList(), "title");
        ascm.ready();
        Set<String> attrSet2 = new Utility().getAttrSet(ascm.getMapList(), "title");
        System.out.println(attrSet2);

        Optional scmCache = scm.getCache(map, "mapList");
        if (scmCache.isPresent()) {
            updateTimeStamp(map, SeiyuCategoryMembers.class);
            Map<String, Object> result = new HashMap<>();
            result.put("mapList", ((List<Map<String, String>>) scmCache.get()).stream()
                    .filter((m)
                            -> attrSet1.contains(m.get("title")))
                    .filter((m)
                            -> !attrSet2.contains(m.get("title")))
                    .map((m) -> {
                        System.out.println(this.getClass().getSimpleName() + " added: " + m);
                        return new HashMap<>(m);
                    })
                    .collect(Collectors.toList()));
            return result;
        }
        return null;
    }
}
