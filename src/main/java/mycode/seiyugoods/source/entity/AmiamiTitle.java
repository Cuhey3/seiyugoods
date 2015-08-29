/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycode.seiyugoods.source.entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity // (1)
@Table(name = "amiami_title") // (2)
public class AmiamiTitle {

    public AmiamiTitle(Long id, String amiamiTitle) {
        this.id = id;
        this.amiamiTitle = amiamiTitle;
    }

    public AmiamiTitle() {
    }
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amiami_title_id_seq")
    @SequenceGenerator(name = "amiami_title_id_seq", sequenceName = "amiami_title_id_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false)
    private String amiamiTitle;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "amiami_wiki", joinColumns = @JoinColumn(name = "amiami_title_id"),
            inverseJoinColumns = @JoinColumn(name = "wiki_title_id"))
    private Set<WikiTitle> wikiTitles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmiamiTitle() {
        return amiamiTitle;
    }

    public void setAmiamiTitle(String amiamiTitle) {
        this.amiamiTitle = amiamiTitle;
    }

    public Set<WikiTitle> getWikiTitles() {
        return wikiTitles;
    }

    public void setWikiTitles(Set<WikiTitle> wikiTitles) {
        this.wikiTitles = wikiTitles;
    }
}
