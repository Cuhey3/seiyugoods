/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycode.seiyugoods.source.repository;

import java.util.List;
import mycode.seiyugoods.source.entity.Seiyu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SeiyuRepository extends JpaRepository<Seiyu, Long> {

    @Query("SELECT x FROM Seiyu x ORDER BY x.trendsTimestamp NULLS FIRST")
    List<Seiyu> findSeiyuOrderbyTimestamp(Pageable pageable);
}
