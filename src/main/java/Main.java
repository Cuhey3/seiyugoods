
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    public static String regex = null;
    public static String regex2 = null;
    public static String json = "{}";
    public static void main(String[] args) throws Exception {
        org.apache.camel.main.Main main = new org.apache.camel.main.Main();
        main.addRouteBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                final String env = System.getenv("PORT");

                from("jetty:http://0.0.0.0:" + env).process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setBody(json);
                    }
                });
                from("timer:foo?period=5m")
                        .process(new Processor() {

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                while (regex == null) {
                                    regex();
                                }
                            }
                        })
                        .process(new Processor() {

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                while (regex2 == null) {
                                    regex2();
                                }
                            }
                        })
                        .to("jetty:http://www.koepota.jp/eventschedule/").process(new Processor() {

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Pattern p = Pattern.compile(regex);
                                Pattern p2 = Pattern.compile(regex2);
                                TreeMap<String, TreeSet<String>> female_seiyus = new TreeMap<>();
                                TreeMap<String, TreeSet<String>> male_seiyus = new TreeMap<>();
                                TreeMap<String, LinkedHashMap<String, String>> events = new TreeMap<>();
                                TreeSet<String> eventIds = new TreeSet<>();
                                String body = exchange.getIn().getBody(String.class);
                                Document doc = Jsoup.parse(body);
                                Element table = doc.select("#eventschedule").first();
                                if (table != null) {
                                    Elements trs = table.select("tr");
                                    for (Element tr : trs) {
                                        Matcher m = p.matcher(tr.select("td:eq(3)").text());
                                        Matcher m2 = p2.matcher(tr.select("td:eq(3)").text());
                                        String eventId = tr.select("td:eq(1) a").attr("href").replaceAll("http://www.koepota.jp/eventschedule/.+/(.+)\\.html", "$1");
                                        boolean flag = false;
                                        while (m.find()) {
                                            String seiyu = m.group(0);
                                            TreeSet<String> set = female_seiyus.get(seiyu);
                                            if (set == null) {
                                                set = new TreeSet<>();
                                            }
                                            set.add(eventId);
                                            female_seiyus.put(seiyu, set);
                                            flag = true;
                                        }
                                        while (m2.find()) {
                                            String seiyu = m2.group(0);
                                            TreeSet<String> set = male_seiyus.get(seiyu);
                                            if (set == null) {
                                                set = new TreeSet<>();
                                            }
                                            set.add(eventId);
                                            male_seiyus.put(seiyu, set);
                                            flag = true;
                                        }
                                        if (flag) {
                                            eventIds.add(eventId);
                                            LinkedHashMap<String, String> event = new LinkedHashMap<>();
                                            event.put("a", tr.select("td:eq(0)").text());
                                            event.put("b", tr.select("td:eq(1)").text());
                                            event.put("c", tr.select("td:eq(2)").text());
                                            event.put("d", tr.select("td:eq(3)").text());
                                            event.put("e", tr.select("td:eq(4)").text());
                                            events.put(eventId, event);
                                        }
                                    }
                                }
                                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                                result.put("female_seiyu", female_seiyus);
                                result.put("male_seiyu", male_seiyus);
                                result.put("eventids", eventIds);
                                result.put("event", events);
                                exchange.getOut().setBody(result);
                            }
                        }).marshal().json(JsonLibrary.Jackson).process(new Processor() {

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                json = exchange.getIn().getBody(String.class);
                            }
                        });
                from("timer:foo?period=24h").process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        regex();
                    }
                });
            }
        });
        main.run();
    }

    public static void regex() throws IOException {
        String url = "http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0";
        Document doc = Jsoup.connect(url).get();
        StringBuilder sbb = new StringBuilder("(");
        while (true) {
            for (Element e : doc.select("categorymembers cm[title]")) {
                sbb.append(e.attr("title").replaceFirst(" \\(.+\\)$", "")).append("|");
            }
            if (doc.select("categorymembers[cmcontinue]").isEmpty()) {
                break;
            } else {
                doc = Jsoup.connect(url + "&cmcontinue=" + doc.select("categorymembers[cmcontinue]").get(0).attr("cmcontinue")).get();
            }
        }
        String wao = new String(sbb).replaceFirst("\\|$", ")");
        Pattern p = Pattern.compile("\\([^\\[\\]\\(\\)]+\\|[^\\[\\]\\(\\)]+(?<!\\|)\\)");
        while (true) {
            int gap = 0;
            Matcher m = p.matcher(wao);
            StringBuilder sb = new StringBuilder(wao);
            while (m.find()) {
                String replace = getReplace(m.group(0));
                sb.replace(m.start() + gap, m.end() + gap, replace);
                gap += replace.length() - m.group().length();
            }
            wao = new String(sb);
            if (!p.matcher(wao).find()) {
                break;
            }
        }
        Pattern p0 = Pattern.compile("\\(([^\\[\\]\\(\\)\\|]*)\\)");
        wao = p0.matcher(wao).replaceAll("$1");
        regex = wao;
    }

    public static void regex2() throws IOException {
        String url = "http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E7%94%B7%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0";
        Document doc = Jsoup.connect(url).get();
        StringBuilder sbb = new StringBuilder("(");
        while (true) {
            for (Element e : doc.select("categorymembers cm[title]")) {
                sbb.append(e.attr("title").replaceFirst(" \\(.+\\)$", "")).append("|");
            }
            if (doc.select("categorymembers[cmcontinue]").isEmpty()) {
                break;
            } else {
                doc = Jsoup.connect(url + "&cmcontinue=" + doc.select("categorymembers[cmcontinue]").get(0).attr("cmcontinue")).get();
            }
        }
        String wao = new String(sbb).replaceFirst("\\|$", ")");
        Pattern p = Pattern.compile("\\([^\\[\\]\\(\\)]+\\|[^\\[\\]\\(\\)]+(?<!\\|)\\)");
        while (true) {
            int gap = 0;
            Matcher m = p.matcher(wao);
            StringBuilder sb = new StringBuilder(wao);
            while (m.find()) {
                String replace = getReplace(m.group(0));
                sb.replace(m.start() + gap, m.end() + gap, replace);
                gap += replace.length() - m.group().length();
            }
            wao = new String(sb);
            if (!p.matcher(wao).find()) {
                break;
            }
        }
        Pattern p0 = Pattern.compile("\\(([^\\[\\]\\(\\)\\|]*)\\)");
        wao = p0.matcher(wao).replaceAll("$1");
        regex2 = wao;
    }

    static String getReplace(String match) {
        match = match.replaceFirst("^\\(", "");
        match = match.replaceFirst("\\)$", "");
        TreeSet<String> ts = new TreeSet<>(new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                int l1 = ((String) o1).length();
                int l2 = ((String) o2).length();
                if (l1 == l2) {
                    return ((String) o1).compareTo((String) o2);
                } else {
                    return l2 - l1;
                }
            }
        });
        ts.addAll(Arrays.asList(match.split("\\|")));
        TreeSet<Character> top = new TreeSet<>();
        for (String s : ts) {
            top.add(s.charAt(0));
        }
        if (top.size() == 1) {
            StringBuilder sb = new StringBuilder(top.first() + "(");
            for (String s : ts) {
                sb.append(s.substring(1)).append("|");
            }
            return new String(sb).replaceFirst("\\|$", ")");
        }
        TreeSet<Character> bottom = new TreeSet<>();
        for (String s : ts) {
            bottom.add(s.charAt(s.length() - 1));
        }
        if (bottom.size() == 1) {
            StringBuilder sb = new StringBuilder("(");
            for (String s : ts) {
                sb.append(s.substring(0, s.length() - 1)).append("|");
            }
            return new String(sb).replaceFirst("\\|$", ")" + bottom.first());
        }
        if (true) {
            StringBuilder sb = new StringBuilder("(");
            for (Character c : top) {
                sb.append(c).append("(");
                for (String s : ts) {
                    if (s.charAt(0) == c) {
                        sb.append(s.substring(1)).append("|");
                    }
                }
                sb = new StringBuilder(new String(sb).replaceFirst("\\|$", ")|"));
            }
            return new String(sb).replaceFirst("\\|$", ")");
        } else {
            StringBuilder sb = new StringBuilder("(");
            for (Character c : bottom) {
                sb.append("(");
                for (String s : ts) {
                    if (s.endsWith(c + "")) {
                        sb.append(s.substring(0, s.length() - 1)).append("|");
                    }
                }
                sb = new StringBuilder(new String(sb).replaceFirst("\\|$", ")")).append(c).append("|");
            }
            return new String(sb).replaceFirst("\\|$", ")");
        }
    }
}
