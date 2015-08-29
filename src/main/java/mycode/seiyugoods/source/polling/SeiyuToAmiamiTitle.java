package mycode.seiyugoods.source.polling;

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
        period = 10;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        List<Seiyu> resultSet = repository.findSeiyuOrderbyAmiamiTitlesTimestamp(new PageRequest(0, 1));
        if (!resultSet.isEmpty()) {
            Seiyu seiyu = repository.findOneWithJoinFetch(resultSet.get(0).getId()).get(0);
            WikiPageExtractInternalLink wpeil = new WikiPageExtractInternalLink(seiyu.getName());
            wpeil.ready();
            Set<String> seiyuToWikiTitle = wpeil.getLinkTitles();
            Set<AmiamiTitle> amiamiTitles = seiyu.getAmiamiTitles();
            wikiTitleRepository.findAll().stream()
                    .filter((wikiTitle) -> seiyuToWikiTitle.contains(wikiTitle.getWikiTitle()))
                    .map((wikiTitle)->wikiTitleRepository.findOneWithFetchAmiamiTitles(wikiTitle.getId()).get(0))
                    .forEach((wikiTitle) -> {
                        wikiTitle.getAmiamiTitles()
                        .forEach(amiamiTitles::add);
                    });
            seiyu.setAmiamiTitles(amiamiTitles);
            seiyu.setAmiamiTitlesTimestamp(System.currentTimeMillis());
            System.out.println(seiyu.getName() + " has " + seiyu.getAmiamiTitles().size() + " amiami titles.");
            seiyu.getAmiamiTitles().forEach((ami)->System.out.println(ami.getAmiamiTitle()));
            repository.save(seiyu);
        }
        return null;
    }

}
