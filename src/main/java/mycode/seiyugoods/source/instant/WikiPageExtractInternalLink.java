package mycode.seiyugoods.source.instant;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import mycode.seiyugoods.source.InstantSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WikiPageExtractInternalLink implements InstantSource {

    String title;
    Set<String> linkTitles = new HashSet<>();

    public Set<String> getLinkTitles() {
        return linkTitles;
    }
    String wikitext;

    public WikiPageExtractInternalLink(String title) {
        this.title = title;
    }

    @Override
    public void ready() throws Exception {
        Document doc = Jsoup.connect("https://ja.wikipedia.org/w/api.php?action=parse&prop=wikitext|links&format=xml&redirects=&page=" + URLEncoder.encode(title, "UTF-8")).maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE).get();
        wikitext = doc.select("wikitext").text();
        doc.select("parse links pl").stream()
                .filter((e) -> e.attr("ns").equals("0") && e.hasAttr("exists"))
                .map((e) -> e.text())
                .filter((linkTitle) -> wikitext.contains(linkTitle))
                .forEach(linkTitles::add);
    }

}
