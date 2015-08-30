package mycode.seiyugoods;

import java.io.IOException;
import mycode.seiyugoods.source.ApiSearch;
import mycode.seiyugoods.source.polling.ApiSeiyuName;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UriUtil extends RouteBuilder {

    @Autowired
    ApiSeiyuName apiSeiyuName;
    @Autowired
    ApiSearch apiSearch;

    @Override
    public void configure() throws Exception {
        from("direct:allSeiyuName")
                .bean(this, "getAllSeiyuName");
        from("direct:search")
                .bean(this, "search")
                .marshal().json(JsonLibrary.Jackson);
    }

    public Object getAllSeiyuName() {
        return apiSeiyuName.getJson();
    }

    public Object search(@Header("name") String name) throws IOException {
        return apiSearch.search(name);
    }
}
