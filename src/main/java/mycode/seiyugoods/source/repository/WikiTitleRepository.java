package mycode.seiyugoods.source.repository;

import java.util.List;
import mycode.seiyugoods.source.entity.WikiTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WikiTitleRepository extends JpaRepository<WikiTitle, Long> {
    
    @Query("SELECT x FROM WikiTitle x WHERE x.wikiTitle = ?#{[0]}")
    public List<WikiTitle> findByTitle(String title);
    @Query("SELECT x FROM WikiTitle AS x LEFT JOIN FETCH x.amiamiTitles WHERE x.id = ?#{[0]}")
    public List<WikiTitle> findOneWithFetchAmiamiTitles(Long id);
}
