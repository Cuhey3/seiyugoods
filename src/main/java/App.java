
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class App {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.addRouteBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                //from("timer:foo").setBody(constant("wao")).to("websocket://0.0.0.0:5000/?sendToAll=true");
                from("jetty:0.0.0.0:" +System.getenv("PORT") + "/websocket").setBody(constant("<script>  var ws = new WebSocket('ws://localhost:5000/'); ws.onopen =function(){ console.log('wao');} </script>"));
            }
        });
        main.run();
    }
}