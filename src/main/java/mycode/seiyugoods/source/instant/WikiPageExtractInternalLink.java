package mycode.seiyugoods.source.instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mycode.seiyugoods.source.InstantSource;
import mycode.seiyugoods.source.entity.AmiamiTitle;
import mycode.seiyugoods.source.entity.WikiTitle;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class WikiPageExtractInternalLink implements InstantSource {

    String title;
    Set<String> linkTitles = new HashSet<>();

    public Set<String> getLinkTitles() {
        return linkTitles;
    }
    String wikitext;
    Pattern p = Pattern.compile("[\\(（]([^\\(（）\\)]+?)[）\\)][ 　]*$");
    Pattern p1 = Pattern.compile("<!--.+?-->", Pattern.DOTALL);
    Pattern p2 = Pattern.compile("<ref.*?(/>|>.*?</ref>)", Pattern.DOTALL);

    public WikiPageExtractInternalLink(String title) {
        this.title = title;
    }

    @Override
    public void ready() throws Exception {
        Connection.Response resp = Jsoup.connect("https://ja.wikipedia.org/w/api.php?action=parse&prop=wikitext|links&format=json&redirects=&page=" + URLEncoder.encode(title, "UTF-8")).maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE).ignoreContentType(true).execute();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map> readValue = mapper.readValue(resp.body(), Map.class);
        try {
            wikitext = ((Map<String, String>) readValue.get("parse").get("wikitext")).get("*");
        } catch (Throwable t) {
            wikitext = "";
        }
        wikitext = p1.matcher(wikitext).replaceAll("");
        wikitext = p2.matcher(wikitext).replaceAll("");
        List<Map> links;
        try {
            links = (List<Map>) readValue.get("parse").get("links");
        } catch (Throwable t) {
            links = new ArrayList<>();
        }

        links.stream()
                .filter((m) -> m.get("ns").equals(0) && m.containsKey("exists"))
                .map((m) -> (String) m.get("*"))
                .filter((linkTitle) -> wikitext.contains(linkTitle) || wikitext.contains(linkTitle.replace(" ", "_")))
                .forEach(linkTitles::add);
    }

    public Map<String, Set<String>> getCharacterName(List<WikiTitle> wikiTitles) {
        String[] split = wikitext.split("\r\n|\n|\r");
        HashMap<String, Set<String>> charNameSetMap = new HashMap<>();
        Matcher m;
        for (String s : split) {
            s = s.replace("_", " ");
            m = p.matcher(s);
            if (m.find()) {
                for (WikiTitle wikiTitle : wikiTitles) {
                    if (s.contains(wikiTitle.getWikiTitle())) {
                        String group = m.group(1);
                        group = group.replaceAll("'''(.+?)'''", "$1");
                        group = group.replaceAll("\\[\\[(.+?)\\]\\]", "$1");
                        String[] sp = group.split("( ?/ ?|、|\\|)");
                        for (AmiamiTitle amiamiTitle : wikiTitle.getAmiamiTitles()) {
                            String key = amiamiTitle.getAmiamiTitle();
                            Set<String> get = charNameSetMap.get(key);
                            if (get == null) {
                                get = new HashSet<>();
                            }
                            get.add(group);
                            get.addAll(Arrays.asList(sp));
                            charNameSetMap.put(key, get);
                        }
                    }
                }
            }
        }
        return charNameSetMap;
    }
}
