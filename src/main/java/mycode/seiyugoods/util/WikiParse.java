package mycode.seiyugoods.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WikiParse {

    private final String url = "https://ja.wikipedia.org/w/api.php";
    private String param;
    private String list;
    private String map;
    private String continueElement;

    public List<Map<String, String>> getMapList() throws IOException {
        String requestUrl = url + "?" + param;
        Document get = Jsoup.connect(requestUrl).timeout(Integer.MAX_VALUE).get();
        ArrayList<Map<String, String>> resultList = new ArrayList<>();
        addElementsAsMap(resultList, get.select(list).select(map));
        if (continueElement != null) {

            while (true) {
                Elements els = get.select("continue[" + continueElement + "]");
                if (els.isEmpty()) {
                    break;
                } else {
                    String value = els.first().attr(continueElement);
                    get = Jsoup.connect(requestUrl + "&" + continueElement + "=" + value).timeout(Integer.MAX_VALUE).get();
                    addElementsAsMap(resultList, get.select(list).select(map));
                }
            }
        }
        return resultList;
    }

    public void addElementsAsMap(List l, Elements elements) {
        elements.stream()
                .map((element) -> {
                    Map<String, String> m = new HashMap<>();
                    StreamSupport.stream(element.attributes().spliterator(), false)
                    .forEach((entry) -> {
                        m.put(entry.getKey(), entry.getValue());
                    });
                    return m;
                })
                .forEach(l::add);
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setList(String list) {
        this.list = list;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public void setContinueElement(String continueElement) {
        this.continueElement = continueElement;
    }
}
