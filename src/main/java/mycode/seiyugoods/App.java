package mycode.seiyugoods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) throws Exception {
        new SpringApplication(App.class).run();
        Thread.sleep(Long.MAX_VALUE);
    }
}
