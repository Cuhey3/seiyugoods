package mycode.seiyugoods.source.instant;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.InstantSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GoogleWikiTitle implements InstantSource {

    String keyword;
    Set<String> wikiTitleSet = new HashSet<>();

    public Set<String> getWikiTitleSet() {
        return wikiTitleSet;
    }

    public GoogleWikiTitle(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void ready() throws Exception {
        Document get = Jsoup.connect("https://www.google.co.jp/search?ie=utf-8&oe=utf-8&hl=ja&q=" + URLEncoder.encode(keyword.replace("-", "－"), "UTF-8") + " wikipedia").userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36").get();
        get.select("table").remove();
        Elements select = get.select("h3 a[href^=https://ja.wikipedia.org/wiki/]");
        Set<String> collect = select.stream()
                .map((e) -> e.attr("href").replace("https://ja.wikipedia.org/wiki/", "").replaceFirst("#.+", ""))
                .map((String t) -> {
                    try {
                        return URLDecoder.decode(t, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        return "";
                    }
                }).collect(Collectors.toSet());
        wikiTitleSet.addAll(collect);
    }
}
