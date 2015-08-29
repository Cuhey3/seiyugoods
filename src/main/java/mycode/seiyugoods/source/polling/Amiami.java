package mycode.seiyugoods.source.polling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mycode.seiyugoods.source.PollingSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class Amiami extends PollingSource {

    public Amiami() {
        period = 2 * 60 * 60;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        Document doc = Jsoup.connect("http://www.amiami.jp/top/page/cal/goods.html").maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE).get();
        doc.select(".listitem:has(.originaltitle:matches(^$))").remove();
        Elements el = doc.select(".listitem");
        Set<String> amiamiTitles = new HashSet<>();
        ArrayList<Map<String, String>> mapList = new ArrayList<>();
        el.stream().map((e) -> {
            Map map = new HashMap<>();
            String title = e.select(".originaltitle").text();
            map.put("img", e.select("img").attr("src").replace("thumbnail", "main"));
            map.put("link", e.select(".name a").attr("href"));
            map.put("name", e.select("ul li").text());
            map.put("release", e.select(".releasedatetext").text());
            map.put("price", e.select(".price").text());
            map.put("orig", title);
            if (!title.isEmpty()) {
                amiamiTitles.add(title);
            }
            return map;
        }).forEach(mapList::add);
        Map<String, Object> result = new HashMap<>();
        result.put("titleSet", amiamiTitles);
        result.put("mapList", mapList);
        return result;
    }

}
