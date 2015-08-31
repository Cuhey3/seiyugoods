package mycode.seiyugoods;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileRoute extends RouteBuilder {

    String body = "";

    @Override
    public void configure() throws Exception {
        from("file:public?noop=true&idempotent=true&idempotentKey=${file:name}-${file:modified}&readLock=none")
                .process((Exchange exchange) -> {
                    setBody(exchange.getIn().getBody(String.class));
                });
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
