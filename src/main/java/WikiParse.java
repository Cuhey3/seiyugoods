
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nanashi
 */
public class WikiParse extends RouteBuilder {

    static boolean resourceReady = false;

    @Override
    public void configure() throws Exception {
        final LinkedHashMap<String, String> jsons = new LinkedHashMap<>();
        final LinkedHashMap<String, String> resource = new LinkedHashMap<>();
        String port = System.getenv("PORT");
        from("file:resource/wiki?noop=true").process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                String file_name = exchange.getIn().getHeader(Exchange.FILE_NAME_ONLY, String.class);
                resource.put(file_name, exchange.getIn().getBody(String.class));
            }
        }).choice().when(property("CamelBatchComplete")).process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                resourceReady = true;
            }
        });
        from("jetty:http://0.0.0.0:" + port + "/wiki/").process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                if (resourceReady) {
                    exchange.getIn().setBody(resource.get("top.html"));
                } else {
                    exchange.getIn().setBody("<script>document.write('now loading...');setTimeout(function(){document.location.reload();},3000)</script>");
                }
            }
        });
        from("jetty:http://0.0.0.0:" + port + "/resource/wiki/?matchOnUriPrefix=true").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getOut().setBody(resource.get(exchange.getIn().getHeader("CamelHttpPath", String.class)));
            }

        });
        from("jetty:http://0.0.0.0:" + port + "/parse").choice().when(new Predicate() {

            @Override
            public boolean matches(Exchange exchange) {
                String page = exchange.getIn().getHeader("page", String.class);
                if (jsons.containsKey(page)) {
                    exchange.getIn().setBody(jsons.get(page));
                    return true;
                } else {
                    return false;
                }
            }
        }).otherwise().process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                String page = exchange.getIn().getHeader("page", String.class);
                Connection.Response execute = null;
                while (execute == null) {
                    try {
                        execute = Jsoup.connect("http://ja.wikipedia.org/w/api.php?action=parse&page=" + URLEncoder.encode(page, "UTF-8") + "&prop=wikitext|links&format=json").ignoreContentType(true).method(Connection.Method.GET).execute();
                    } catch (Throwable t) {
                    }
                }
                exchange.getIn().setBody(execute.body());
            }
        })
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ArrayList<LinkedHashMap<String, String>> tokens = new ArrayList<>();
                        Map json = exchange.getIn().getBody(Map.class);
                        Map cursor = (Map) json.get("parse");
                        cursor = (Map) cursor.get("wikitext");
                        String wikitext = (String) cursor.get("*");
                        wikitext = Pattern.compile("<!--.+?-->", Pattern.DOTALL).matcher(wikitext).replaceAll("");
                        wikitext = Pattern.compile("<ref.*?(/>|>.*?</ref>)", Pattern.DOTALL).matcher(wikitext).replaceAll("");
                        Pattern p = Pattern.compile("^([:;]|! |'+|\\*+|=+|\\{\\||\\|-|\\|\\}|\\}+|\\| ?)");
                        Pattern year1 = Pattern.compile("^'''\\s*(\\d{4}年|時期未定)\\s*'''\\s*$");
                        Pattern year2 = Pattern.compile("^!.*(\\d{4}年|時期未定)\\s*$");
                        StringBuilder buffer = new StringBuilder();
                        LinkedHashMap<String, String> section = new LinkedHashMap<>();
                        for (String s : wikitext.split("\r\n|\n|\r")) {
                            Matcher m = p.matcher(s);
                            if (m.find()) {
                                switch (m.group(1)) {
                                    case "| ":
                                    case "|":
                                        buffer.append("\n").append(s);
                                        break;
                                    case "}}":
                                        buffer.append("\n").append(s);
                                        output(tokens, section, buffer);
                                        break;
                                    case "'''":
                                        Matcher m_year1 = year1.matcher(s);
                                        if (m_year1.find()) {
                                            output(tokens, section, buffer);
                                            buffer = new StringBuilder();
                                            section.put("year", m_year1.group(1));
                                        } else {
                                            buffer = new StringBuilder(s);
                                        }
                                        break;
                                    case "==":
                                        section = new LinkedHashMap<>();
                                        buffer = new StringBuilder();
                                        s = s.replaceFirst("^==\\s*(.+?)\\s*==\\s*$", "$1");
                                        section.put("h2", s);
                                        section.remove("h3");
                                        section.remove("h4");
                                        section.remove("h5");
                                        section.remove("year");
                                        break;
                                    case "===":
                                        s = s.replaceFirst("^===\\s*(.+?)\\s*===\\s*$", "$1");
                                        buffer = new StringBuilder();
                                        section.put("h3", s);
                                        section.remove("h4");
                                        section.remove("year");
                                        break;
                                    case "====":
                                        s = s.replaceFirst("^====\\s*(.+?)\\s*====\\s*$", "$1");
                                        buffer = new StringBuilder();
                                        section.put("h4", s);
                                        section.remove("h5");
                                        section.remove("year");
                                        break;
                                    case ";":
                                        s = s.replaceFirst("^;\\s*(.+?)\\s*$", "$1");
                                        buffer = new StringBuilder();
                                        section.put("h5", s);
                                        section.remove("year");
                                        break;
                                    case "*":
                                    case ":":
                                        output(tokens, section, buffer);
                                        buffer = new StringBuilder(s);
                                        break;
                                    case "**":
                                        buffer.append("\n").append(s);
                                        break;
                                    case "{|":
                                        break;
                                    case "!":
                                    case "! ":
                                        Matcher m_year2 = year2.matcher(s);
                                        if (m_year2.find()) {
                                            section.put("year", m_year2.group(1));
                                        }
                                        break;
                                    case "|-":
                                        output(tokens, section, buffer);
                                        buffer = new StringBuilder();
                                        break;
                                }
                            } else {
                                if (s.isEmpty() || s.matches("^\\s+$")) {
                                    output(tokens, section, buffer);
                                    buffer = new StringBuilder();
                                } else {
                                    if (buffer.length() > 0) {
                                        buffer.append("\n").append(s);
                                    } else {
                                        buffer.append(s);
                                    }
                                }
                            }
                        }
                        output(tokens, section, buffer);
                        cursor = (Map) json.get("parse");
                        List<Map<String, Object>> links = (List) cursor.get("links");
                        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                        LinkedHashMap<String, Object> linksToToken = new LinkedHashMap<>();
                        ArrayList<LinkedHashMap<String, String>> tokenArray = new ArrayList<>();
                        for (Map<String, Object> link : links) {
                            if ((int) link.get("ns") == 0) {
                                String title = (String) link.get("*");
                                if (title.length() < 2) {
                                    continue;
                                }
                                ArrayList<Integer> ex = new ArrayList<>();
                                for (int i = 0; i < tokenArray.size(); i++) {
                                    if (tokenArray.get(i).get("s").contains(title)) {
                                        ex.add(i);
                                    }
                                }
                                for (LinkedHashMap<String, String> section2 : tokens) {
                                    String s = section2.get("s");
                                    if (s.contains(title)) {
                                        ex.add(tokenArray.size());
                                        tokenArray.add(new LinkedHashMap<>(section2));
                                        section2.put("s", "");
                                    }
                                }
                                if (!ex.isEmpty()) {
                                    LinkedHashMap<String, Object> r = new LinkedHashMap<>();
                                    if (link.containsKey("exists")) {
                                        r.put("e", 1);
                                    }
                                    r.put("t", ex);
                                    linksToToken.put(title, r);
                                }
                            }
                        }
                        result.put("links", linksToToken);
                        result.put("tokens", tokenArray);
                        exchange.getIn().setBody(result);
                    }
                }).marshal().json(JsonLibrary.Jackson).process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println(exchange.getIn().getHeader("page", String.class) + "新規取得しました");
                        jsons.put(exchange.getIn().getHeader("page", String.class), exchange.getIn().getBody(String.class));
                    }
                });
    }

    public static void output(ArrayList<LinkedHashMap<String, String>> tokens, LinkedHashMap<String, String> section, StringBuilder buffer) {
        if (buffer.length() > 0) {
            LinkedHashMap<String, String> section2 = new LinkedHashMap<>(section);
            section2.put("s", new String(buffer));
            section.put("s", new String(buffer));
            tokens.add(section2);
        }

    }
}
