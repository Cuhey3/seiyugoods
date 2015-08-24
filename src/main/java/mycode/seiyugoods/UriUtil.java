package mycode.seiyugoods;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import mycode.seiyugoods.source.callable.CategoryAndTemplateSeiyu;
import mycode.seiyugoods.source.polling.SeiyuCategoryMembers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UriUtil extends RouteBuilder {

    @Autowired
    CategoryAndTemplateSeiyu cats;


    @Override
    public void configure() throws Exception {
        from("direct:allSeiyuName")
                .bean(this, "getAllSeiyuName")
                .marshal().json(JsonLibrary.Jackson);
    }

    public Object getAllSeiyuName() {
        return cats.getCache(Broker.allSourceTimeStamp, "mapList").get();
    }
    
    
}
