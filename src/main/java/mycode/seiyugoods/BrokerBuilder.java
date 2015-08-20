package mycode.seiyugoods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mycode.seiyugoods.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class BrokerBuilder {

    private final Map<Class, Set> needMap;
    private final Map<Class, Set> needForMap;
    private Class<? extends Source> choiceClass;
    @Autowired
    DefaultListableBeanFactory factory;

    public BrokerBuilder() {
        this.needMap = new HashMap<>();
        this.needForMap = new HashMap<>();
    }

    public BrokerBuilder from(Class<? extends Source> clazz) {
        this.choiceClass = clazz;
        return this;
    }

    public BrokerBuilder to(Class<? extends Source> clazz) {
        Set get = this.needMap.get(clazz);
        if (get == null) {
            get = new HashSet<>();

        }
        get.add(choiceClass);
        this.needMap.put(clazz, get);
        Set get1 = this.needForMap.get(this.choiceClass);
        if (get1 == null) {
            get1 = new HashSet<>();
        }
        get1.add(clazz);
        this.needForMap.put(this.choiceClass, get1);
        return this;
    }
    
    public Set<Class> getNeedFor(Class clazz){
        return this.needForMap.get(clazz);
    }
}
