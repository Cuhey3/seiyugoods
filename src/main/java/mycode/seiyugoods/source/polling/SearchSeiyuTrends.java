package mycode.seiyugoods.source.polling;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mycode.seiyugoods.source.PollingSource;
import mycode.seiyugoods.source.entity.Seiyu;
import mycode.seiyugoods.source.repository.SeiyuRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SearchSeiyuTrends extends PollingSource {

    @Autowired
    SeiyuRepository repository;

    public SearchSeiyuTrends() {
        period = 60;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Seiyu> findSeiyuOrderbyTimestamp = repository.findSeiyuOrderbyTimestamp(new PageRequest(0, 1));
        if (!findSeiyuOrderbyTimestamp.isEmpty()) {
            Seiyu seiyu = findSeiyuOrderbyTimestamp.get(0);
            Connection.Response get = Jsoup.connect("http://www.google.com/trends/fetchComponent?q=" + URLEncoder.encode(seiyu.getName(), "UTF-8") + "&cid=TIMESERIES_GRAPH_0&export=3&hl=ja").ignoreContentType(true).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36").timeout(Integer.MAX_VALUE).execute();
            if (get.statusCode() == 200) {
                String replaceFirst = get.body().replace("// Data table response", "").replaceFirst(".+setResponse\\((.+)\\);", "$1").replaceAll("\"v\":new Date.+?\\),", "");
                if (!replaceFirst.contains("検索ボリュームが十分でないため結果を表示できません。")) {
                    Map readValue = mapper.readValue(replaceFirst, Map.class);
                    Map table = (Map) readValue.get("table");
                    List<Map> rows = (List<Map>) table.get("rows");
                    Optional<Object> findFirst = rows.stream().map(r -> (List<Map>) r.get("c"))
                            .filter(l -> l.get(1).get("f").equals("100"))
                            .map(l -> l.get(0).get("f"))
                            .findFirst();
                    if (findFirst.isPresent()) {
                        seiyu.setTrends((String) findFirst.get());
                    } else {
                        seiyu.setTrends("error");
                    }
                } else {
                    seiyu.setTrends("検索ボリュームが十分でないため結果を表示できません。");
                }
                seiyu.setTrendsTimestamp(System.currentTimeMillis());
                System.out.println(seiyu.getName() + "\t" + seiyu.getTrends());
                repository.save(seiyu);
            }
        }
        return new HashMap<>();
    }

}
