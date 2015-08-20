package mycode.seiyugoods;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Broker extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        String port = System.getenv("PORT");
        if (port == null) {
            port = "80";
        }
        String siteTopUrl = "http://0.0.0.0:" + port;
        from("jetty:" + siteTopUrl).setBody().constant("Hello seiyugoods!");
        
        from("jetty:" + siteTopUrl + "/api/seiyu_name").to("direct:allSeiyuName");
    }
}
