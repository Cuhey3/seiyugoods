package mycode.seiyugoods;

import org.apache.camel.builder.RouteBuilder;

public class DataSourceRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        from("direct:jdbc_sql").to("jdbc:ds");
    }
}
