package mycode.seiyugoods.source.callable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.CallableSource;
import mycode.seiyugoods.source.entity.AmiamiTitle;
import mycode.seiyugoods.source.entity.WikiTitle;
import mycode.seiyugoods.source.instant.GoogleWikiTitle;
import mycode.seiyugoods.source.repository.AmiamiTitleRepository;
import mycode.seiyugoods.source.repository.WikiTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistentAmiamiTitles extends CallableSource {

    @Autowired
    AmiamiTitleRepository repository;
    @Autowired
    WikiTitleRepository wikiTitleRepository;
    @Autowired
    AmiamiTitles amiamiTitles;

    @Override
    public Map<String, Object> call(Map<Class, Long> map) throws Exception {
        List<AmiamiTitle> findAll = repository.findAll();
        Optional amiamiTitlesCache = amiamiTitles.getCache(map, "titleSet");
        if (amiamiTitlesCache.isPresent()) {
            updateTimeStamp(map, AmiamiTitles.class);
            Map<String, Object> result = new HashMap<>();
            List<AmiamiTitle> collect = ((Set<String>) amiamiTitlesCache.get()).stream()
                    .filter((title) -> findAll.stream().noneMatch((amiamiTitle) -> amiamiTitle.getAmiamiTitle().equals(title)))
                    .map((String title) -> {
                        GoogleWikiTitle gwt = new GoogleWikiTitle(title);
                        try {
                            gwt.ready();
                        } catch (Exception ex) {
                        }
                        Set<WikiTitle> wikiTitles = gwt.getWikiTitleSet().stream()
                        .map((String wikiTitle) -> {
                            List<WikiTitle> findByTitle = wikiTitleRepository.findByTitle(wikiTitle);
                            if (findByTitle.isEmpty()) {
                                WikiTitle wt = new WikiTitle(null, wikiTitle);
                                wikiTitleRepository.save(wt);
                                return wt;
                            } else {
                                return findByTitle.get(0);
                            }
                        }).collect(Collectors.toSet());
                        if (wikiTitles.isEmpty()) {
                            List<WikiTitle> findByTitle = wikiTitleRepository.findByTitle("none");
                            if (findByTitle.isEmpty()) {
                                WikiTitle wt = new WikiTitle(null, "none");
                                wikiTitleRepository.save(wt);
                                wikiTitles.add(wt);
                            } else {
                                wikiTitles.add(findByTitle.get(0));
                            }
                        }
                        AmiamiTitle amiamiTitle = new AmiamiTitle(null, title);
                        amiamiTitle.setWikiTitles(wikiTitles);
                        return amiamiTitle;
                    })
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                repository.save(collect);
            }
            result.put("findAll", repository.findAll());
            return result;
        }
        return null;
    }
}
