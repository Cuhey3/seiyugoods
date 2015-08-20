package mycode.seiyugoods.source;

import java.util.Map;

public abstract class PollingSource extends Source {
    
    protected int period;
    
    @Override
    public void configure() {
        fromF("timer:%s?period=%ss", this.getClass().getName(), period)
                .bean(this, "poll")
                .choice().when().method(this,"checkNotate")
                .setBody(constant(this.getClass()))
                .to("seda:broker.notate");
    }
    
    public abstract Map<String, Object> poll() throws Exception;
}
