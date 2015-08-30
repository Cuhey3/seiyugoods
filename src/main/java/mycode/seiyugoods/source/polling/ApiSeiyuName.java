package mycode.seiyugoods.source.polling;

import java.util.Map;
import mycode.seiyugoods.source.PollingSource;
import org.apache.camel.Body;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ApiSeiyuName extends PollingSource {

    String json = "{}";

    public ApiSeiyuName() {
        period = 600;
    }

    @Override
    public void configure() {
        fromF("timer:%s?period=%ss", this.getClass().getName(), period)
                .to("sql:select id,name,sort_key from seiyu where amiami_titles_json <>'[]' and trends like '20%' order by id?dataSource=ds")
                .marshal().json(JsonLibrary.Jackson)
                .bean(this, "setJson");
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        return null;
    }

    public void setJson(@Body String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

}
