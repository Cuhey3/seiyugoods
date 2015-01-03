
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
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
    public static String json = "<script>document.write('now loading...');setTimeout(function(){document.location.reload();},5000)</script>";

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
                                LinkedHashSet<Object> female = new LinkedHashSet<>();
                                for (String name : female_seiyus.keySet()) {
                                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                                    map.put("name", name);
                                    map.put("eventids", female_seiyus.get(name));
                                    map.put("count", getUniqueSize(female_seiyus.get(name)));
                                    map.put("count2", female_seiyus.get(name).size());
                                    female.add(map);
                                }
                                LinkedHashSet<Object> male = new LinkedHashSet<>();
                                for (String name : male_seiyus.keySet()) {
                                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                                    map.put("name", name);
                                    map.put("eventids", male_seiyus.get(name));
                                    map.put("count", getUniqueSize(male_seiyus.get(name)));
                                    map.put("count2", male_seiyus.get(name).size());
                                    male.add(map);
                                }
                                result.put("female_seiyu", female);
                                result.put("male_seiyu", male);
                                result.put("eventids", eventIds);
                                result.put("event", events);
                                exchange.getOut().setBody(result);
                            }
                        }).marshal().json(JsonLibrary.Jackson).process(new Processor() {

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                json = "<!DOCTYPE html>\n"
                                + "<html lang='ja' ng-app='MyApp' id='my'>\n"
                                + "    <head>\n"
                                + "        <meta charset='utf-8'>\n"
                                + "        <meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
                                + "        <title>こえぽたブラウザ</title>\n"
                                + "        <meta name='description' content=''>\n"
                                + "        <meta name='viewport' content='width=device-width, initial-scale=1'>\n"
                                + "        <link rel='stylesheet' href='http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css'>\n"
                                + "        <style>\n"
                                + "            .app-modal-window .modal-dialog{width:1000px;}\n"
                                + "            .no{width:40px;text-align:center;}\n"
                                + "            .name{cursor:pointer;width:90px;}\n"
                                + "            .count{width:40px;text-align:center;}\n"
                                + "            .ev{width:600px;}\n"
                                + "            .name:hover{color:blue;font-weight:bold;}\n"
                                + "        </style>\n"
                                + "        <script src='http://code.jquery.com/jquery-1.11.0.min.js'></script>\n"
                                + "        <script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.2.0/angular.min.js'></script>\n"
                                + "        <script src='http://cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.10.0/ui-bootstrap-tpls.min.js'></script>\n"
                                + "        <script src='https://code.jquery.com/jquery-2.1.1.min.js'></script>\n"
                                + "        <script src='http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js'></script>\n"
                                + "        <script src='seiyu.js'></script>\n"
                                + "        <script>"
                                + "	var seiyu_obj=" + exchange.getIn().getBody(String.class) + ";            angular.module('MyApp', ['ui.bootstrap'])\n"
                                + "                    .controller('MyController', ['$scope', '$modal', function($scope, $modal) {\n"
                                + "                            $scope.now = seiyu_obj.female_seiyu;\n"
                                + "                            $scope.gender = 'female';\n"
                                + "                            $scope.eventIds = seiyu_obj.eventIds;\n"
                                + "                            $scope.events = seiyu_obj.event;\n"
                                + "                            $scope.sort = function(a) {\n"
                                + "                                return -a.count;\n"
                                + "                            };\n"
                                + "                            $scope.sort = function(a) {\n"
                                + "                                return $scope.events[a.eventids[0]].b;\n"
                                + "                            };\n"
                                + "                            $scope.modalOpen = function(seiyu) {\n"
                                + "                                $scope.seiyu = seiyu;\n"
                                + "                                $modal.open({\n"
                                + "                                    templateUrl: 'myModal', controller: ModalInstanceCtrl,\n"
                                + "                                    scope: $scope, windowClass: 'app-modal-window'\n"
                                + "                                });\n"
                                + "                            };\n"
                                + "                            $scope.changeGender = function(){\n"
                                + "                                if($scope.gender === 'female'){\n"
                                + "                                    $scope.now = seiyu_obj.male_seiyu;\n"
                                + "                                    $scope.gender = 'male';\n"
                                + "                                }else{\n"
                                + "                                    $scope.now = seiyu_obj.female_seiyu;\n"
                                + "                                    $scope.gender = 'female';\n"
                                + "                                   \n"
                                + "                                }\n"
                                + "                            };\n"
                                + "                        }]);\n"
                                + "            var ModalInstanceCtrl = function($scope, $modalInstance) {\n"
                                + "            };\n"
                                + "        </script>\n"
                                + "        <script  type='text/ng-template' id='myModal'>\n"
                                + "            <style>\n"
                                + "            .date{width:80px;}\n"
                                + "            .evname{width:380px;}\n"
                                + "            .place{width:120px;}\n"
                                + "            .member{width:220px;}\n"
                                + "            </style>\n"
                                + "            <div class='modal-header'>\n"
                                + "            <h3 class='modal-title'><a href='http://ja.wikipedia.org/wiki/{{seiyu.name}}' target='_blank'>{{seiyu.name}}</a></h3>\n"
                                + "            {{seiyu.count}}会場 {{seiyu.count2}}ステージ\n"
                                + "            </div>\n"
                                + "            <div class='modal-body'>\n"
                                + "            <table class='table'>\n"
                                + "            <thead><tr><th>日時</th><th>イベント名</th><th>場所</th><th>出演者</th></tr></thead>\n"
                                + "            <tbody>\n"
                                + "            <tr ng-repeat='id in seiyu.eventids'>\n"
                                + "            <td class='date'>{{events[id].a}}</td>\n"
                                + "            <td class='evname'><a href='http://www.koepota.jp/eventschedule/{{id.substr(0, 4)}}/{{id.substr(4, 2)}}/{{id.substr(6, 2)}}/{{id}}.html' target='_blank'>{{events[id].b}}</a></td>\n"
                                + "            <td class='place'>{{events[id].c}}</td>\n"
                                + "            <td class='member'>{{events[id].d}}</td>\n"
                                + "            </tr>\n"
                                + "    </tbody>\n"
                                + "            </table>\n"
                                + "    </div>\n"
                                + "    <div class='modal-footer'></div>\n"
                                + "            </body>\n"
                                + "        </script>\n"
                                + "\n"
                                + "    <body ng-controller='MyController'>\n"
                                + "        <div class='container'>\n"
                                + "            <h2 style='float:left;'>こえぽたイベントスケジュールブラウザ</h2><div style='float:right;padding-top:20px;'><input type='button' ng-click='changeGender();' value='性別を切り替え'></div> <div style='clear:both;'><a href='http://www.koepota.jp/eventschedule/'>本家</a></div>\n"
                                + "            <table class='table table-hover'>\n"
                                + "                <thead>\n"
                                + "                    <tr>\n"
                                + "                        <th>No</th>\n"
                                + "                        <th>氏名</th>\n"
                                + "                        <th>件数</th>\n"
                                + "                        <th class='ev'>直近のイベント</th>\n"
                                + "                    </tr>\n"
                                + "                </thead>\n"
                                + "                <tbody>\n"
                                + "                    <tr ng-repeat=\"seiyu in now|orderBy:['-count', sort]\">\n"
                                + "                        <td class='no'>{{$index + 1}}</td>\n"
                                + "                        <td ng-click='modalOpen(seiyu)' class='name'>{{seiyu.name}}</td>\n"
                                + "                        <td class='count'>{{seiyu.count}}</td>\n"
                                + "                        <td class='ev'><a href='http://www.koepota.jp/eventschedule/{{seiyu.eventids[0].substr(0, 4)}}/{{seiyu.eventids[0].substr(4, 2)}}/{{seiyu.eventids[0].substr(6, 2)}}/{{seiyu.eventids[0]}}.html' target='_blank'>{{events[seiyu.eventids[0]].b}}</a></td>\n"
                                + "                    </tr>\n"
                                + "                </tbody>\n"
                                + "            </table>\n"
                                + "        </div>\n"
                                + "    </body>\n"
                                + "</html>";
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
    }

    public static int getUniqueSize(Set<String> set) {
        LinkedHashSet<String> newSet = new LinkedHashSet<>();
        for (String s : set) {
            s = s.replaceFirst("\\d+$", "");
            newSet.add(s);
        }
        return newSet.size();
    }
}
