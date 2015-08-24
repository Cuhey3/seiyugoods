package mycode.seiyugoods.source;

import java.util.Map;
import java.util.Objects;
import org.apache.camel.Body;

public abstract class CallableSource extends Source {

    @Override
    public void configure() throws Exception {
        fromF("seda:%s", this.getClass().getName())
                .bean(this, "call")
                .choice().when().method(this,"checkNotate")
                .setBody(constant(this.getClass()))
                .to("seda:broker.notate");
    }

    public abstract Map<String, Object> call(@Body Map<Class, Long> map) throws Exception;

    @Override
    public boolean isUpToDate(Map<Class, Long> brokerMap) {
        return usingSourceTimeStamp.keySet().stream()
                .allMatch((key)
                        -> Objects.equals(usingSourceTimeStamp.get(key), brokerMap.get(key)));
    }
}
