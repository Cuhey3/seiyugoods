package mycode.seiyugoods;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Utility {

    public Set<String> getAttrSet(List<Map<String, String>> mapList, String attr) {
        return mapList.stream()
                .map((map) -> map.get(attr))
                .collect(Collectors.toSet());
    }
}
