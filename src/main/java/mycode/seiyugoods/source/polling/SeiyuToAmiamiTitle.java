package mycode.seiyugoods.source.polling;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mycode.seiyugoods.source.PollingSource;
import mycode.seiyugoods.source.entity.AmiamiTitle;
import mycode.seiyugoods.source.entity.Seiyu;
import mycode.seiyugoods.source.instant.WikiPageExtractInternalLink;
import mycode.seiyugoods.source.repository.SeiyuRepository;
import mycode.seiyugoods.source.repository.WikiTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SeiyuToAmiamiTitle extends PollingSource {

    @Autowired
    SeiyuRepository repository;
    @Autowired
    WikiTitleRepository wikiTitleRepository;

    public SeiyuToAmiamiTitle() {
        period = 60;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Seiyu> resultSet = repository.findSeiyuOrderbyAmiamiTitlesTimestamp(new PageRequest(0, 1));
        if (!resultSet.isEmpty()) {
            Seiyu seiyu = resultSet.get(0);
            WikiPageExtractInternalLink wpeil = new WikiPageExtractInternalLink(seiyu.getName());
            wpeil.ready();
            Set<String> seiyuToWikiTitle = wpeil.getLinkTitles();
            String amiamiTitlesJson = seiyu.getAmiamiTitlesJson();
            if (amiamiTitlesJson == null) {
                amiamiTitlesJson = "[]";
            }
            Set<String> amiamiTitles = mapper.readValue(amiamiTitlesJson, Set.class);
            wikiTitleRepository.findAll().stream()
                    .filter((wikiTitle) -> seiyuToWikiTitle.contains(wikiTitle.getWikiTitle()))
                    .map((wikiTitle) -> wikiTitleRepository.findOneWithFetchAmiamiTitles(wikiTitle.getId()).get(0))
                    .forEach((wikiTitle) -> {
                        wikiTitle.getAmiamiTitles()
                        .stream().map((amiamiTitle) -> amiamiTitle.getAmiamiTitle())
                        .forEach(amiamiTitles::add);
                    });

            seiyu.setAmiamiTitlesJson(mapper.writeValueAsString(amiamiTitles));
            seiyu.setAmiamiTitlesTimestamp(System.currentTimeMillis());
            System.out.println(seiyu.getName() + " has " + amiamiTitles.size() + " amiami titles.");
            amiamiTitles.forEach(System.out::println);
            repository.save(seiyu);
        }
        return null;
    }

}
