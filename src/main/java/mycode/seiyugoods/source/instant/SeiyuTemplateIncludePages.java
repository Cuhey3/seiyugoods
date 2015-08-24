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
public class SeiyuTemplateIncludePages implements InstantSource {

    private final List<Map<String, String>> mapList = Collections.synchronizedList(new ArrayList<>());

    public List<Map<String, String>> getMapList() {
        return mapList;
    }

    @Override
    public void ready() throws IOException {
        WikiParse parse = new WikiParse();
        parse.setParam("action=query&list=backlinks&bltitle=Template:%E5%A3%B0%E5%84%AA&format=xml&bllimit=500&blnamespace=0&continue=");
        parse.setList("backlinks");
        parse.setMap("bl");
        parse.setContinueElement("blcontinue");
        mapList.clear();
        mapList.addAll(parse.getMapList());
    }
}
