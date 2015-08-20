package mycode.seiyugoods.source.polling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mycode.seiyugoods.source.PollingSource;
import mycode.seiyugoods.util.WikiParse;
import org.springframework.stereotype.Component;

@Component
public class SeiyuCategoryMembers extends PollingSource {

    public SeiyuCategoryMembers() {
        period = 60 * 60;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        Map<String, Object> sourceFields = new HashMap<>();
        /*sourceFields.put("mapList", WikiParse.builder()
                .param("action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&cmnamespace=0&format=xml&continue=&cmprop=title|sortkeyprefix")
                .list("categorymembers")
                .map("cm")
                .continueElement("cmcontinue")
                .build().getMapList());*/
        sourceFields.put("mapList", new ArrayList<>());
        return sourceFields;
    }
}
