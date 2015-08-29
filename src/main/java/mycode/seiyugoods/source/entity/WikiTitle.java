package mycode.seiyugoods.source.entity;

import java.util.Set;
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
@Table(name = "wiki_title") // (2)
public class WikiTitle {

    public WikiTitle() {
    }
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wiki_title_id_seq")
    @SequenceGenerator(name = "wiki_title_id_seq", sequenceName = "wiki_title_id_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false)
    private String wikiTitle;
    @Column(nullable = true)
    private String pageid;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "amiami_wiki", joinColumns = @JoinColumn(name = "wiki_title_id"),
            inverseJoinColumns = @JoinColumn(name = "amiami_title_id"))
    private Set<AmiamiTitle> amiamiTitles;

    public WikiTitle(Long id, String wikiTitle) {
        this.id = id;
        this.wikiTitle = wikiTitle;
    }

    public Set<AmiamiTitle> getAmiamiTitles() {
        return amiamiTitles;
    }

    public void setAmiamiTitles(Set<AmiamiTitle> amiamiTitles) {
        this.amiamiTitles = amiamiTitles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWikiTitle() {
        return wikiTitle;
    }

    public void setWikiTitle(String wikiTitle) {
        this.wikiTitle = wikiTitle;
    }

    public String getPageid() {
        return pageid;
    }

    public void setPageid(String pageid) {
        this.pageid = pageid;
    }
}
