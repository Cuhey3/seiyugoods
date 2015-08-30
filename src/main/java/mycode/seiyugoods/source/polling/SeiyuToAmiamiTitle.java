package mycode.seiyugoods.source.polling;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.PollingSource;
import mycode.seiyugoods.source.entity.Seiyu;
import mycode.seiyugoods.source.entity.WikiTitle;
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
        period = 3;
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
            List<WikiTitle> collect = wikiTitleRepository.findAll().stream()
                    .filter((wikiTitle) -> seiyuToWikiTitle.contains(wikiTitle.getWikiTitle()))
                    .map((wikiTitle) -> wikiTitleRepository.findOneWithFetchAmiamiTitles(wikiTitle.getId()).get(0))
                    .collect(Collectors.toList());
            Map<String, Set<String>> characterName = wpeil.getCharacterName(collect);
            seiyu.setAmiamiTitlesJson(mapper.writeValueAsString(characterName.keySet()));
            seiyu.setAmiamiCharJson(mapper.writeValueAsString(characterName));
            seiyu.setAmiamiTitlesTimestamp(System.currentTimeMillis());
            System.out.println(seiyu.getName() + " has " + characterName.keySet().size() + " amiami titles.");
            characterName.entrySet().stream().forEach(System.out::println);
            repository.save(seiyu);
        }
        return null;
    }

}
