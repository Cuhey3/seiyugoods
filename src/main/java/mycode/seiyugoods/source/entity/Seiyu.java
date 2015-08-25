/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycode.seiyugoods.source.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity // (1)
@Table(name = "seiyu") // (2)
public class Seiyu {

    public Seiyu(Long id, String name, String pageid, String sortKey) {
        this.id = id;
        this.name = name;
        this.pageid = pageid;
        this.sortKey = sortKey;
    }

    public Seiyu() {
    }
    @Id // (5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seiyu_id_seq")
    @SequenceGenerator(name = "seiyu_id_seq", sequenceName = "seiyu_id_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String trends;
    @Column(nullable = true)
    private Long trendsTimestamp;
    @Column(nullable = false)
    private String pageid;
    @Column(nullable = false)
    private String sortKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrends() {
        return trends;
    }

    public void setTrends(String trends) {
        this.trends = trends;
    }

    public Long getTrendsTimestamp() {
        return trendsTimestamp;
    }

    public void setTrendsTimestamp(Long trendsTimestamp) {
        this.trendsTimestamp = trendsTimestamp;
    }

    public String getPageid() {
        return pageid;
    }

    public void setPageid(String pageid) {
        this.pageid = pageid;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

}
