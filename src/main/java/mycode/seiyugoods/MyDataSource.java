package mycode.seiyugoods;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MyDataSource {

    final Pattern p = Pattern.compile("^postgres://(.*?):(.*?)@(.*?):(.*?)/(.*?)$");

    @Bean(name = "ds", destroyMethod = "close")
    public DataSource getDataSource() throws URISyntaxException {
        PGPoolingDataSource source = new PGPoolingDataSource();
        Matcher m = p.matcher(System.getenv("DATABASE_URL"));
        m.find();
        source.setTcpKeepAlive(true);
        source.setUser(m.group(1));
        source.setPassword(m.group(2));
        source.setServerName(m.group(3));
        source.setPortNumber(Integer.parseInt(m.group(4)));
        source.setDatabaseName(m.group(5));
        source.setSsl(true);
        source.setSslfactory("org.postgresql.ssl.NonValidatingFactory");
        source.setMaxConnections(10);
        return source;
    }
}
