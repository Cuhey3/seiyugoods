package mycode.seiyugoods.source.instant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mycode.seiyugoods.source.InstantSource;
import mycode.seiyugoods.util.WikiParse;
import org.springframework.stereotype.Component;

@Component
public class AdultSeiyuCategoryMembers implements InstantSource {

    private final List<Map<String, String>> mapList = Collections.synchronizedList(new ArrayList<>());

    public List<Map<String, String>> getMapList() {
        return mapList;
    }

    @Override
    public void ready() throws IOException {
        WikiParse parse = new WikiParse();
        parse.setParam("action=query&list=categorymembers&cmtitle=Category:%E3%82%A2%E3%83%80%E3%83%AB%E3%83%88%E3%82%B2%E3%83%BC%E3%83%A0%E5%A3%B0%E5%84%AA&cmlimit=500&cmnamespace=0&format=xml&continue=&cmprop=title|sortkeyprefix");
        parse.setList("categorymembers");
        parse.setMap("cm");
        parse.setContinueElement("cmcontinue");
        mapList.clear();
        mapList.addAll(parse.getMapList());
    }
}
