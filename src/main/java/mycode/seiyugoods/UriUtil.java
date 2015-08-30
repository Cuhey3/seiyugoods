package mycode.seiyugoods;

import mycode.seiyugoods.source.polling.ApiSeiyuName;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UriUtil extends RouteBuilder {

    @Autowired
    ApiSeiyuName apiSeiyuName;


    @Override
    public void configure() throws Exception {
        from("direct:allSeiyuName")
                .bean(this, "getAllSeiyuName");
    }

    public Object getAllSeiyuName() {
        return apiSeiyuName.getJson();
    }
    
    
}
