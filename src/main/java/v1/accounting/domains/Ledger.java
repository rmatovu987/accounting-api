package v1.accounting.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import v1.authentication.domains.Business;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
public class Ledger extends PanacheEntity {
    @Column(nullable = false)
    public String name;

    @Column(nullable = true)
    public String systemName;

    @Column(nullable = false)
    public String code;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public Boolean isCategory;

    @Column(nullable = false)
    public Boolean isSystemGenerated;

    @Column(nullable = false)
    public Boolean isSupaAccount;

    @Column(nullable = false)
    public Boolean hasSubAccounts;

    @Column(nullable = false)
    public Boolean showOnChart;

    @Column(nullable = false)
    public Boolean isPostable;

    @Column(nullable = false)
    public String type;

    public Boolean isContra;

    // mappersString
    // relationships
    @ManyToOne
    @JoinColumn(nullable = true)
    public Ledger contraLedger;

    @ManyToOne
    @JoinColumn(nullable = true)
    public Ledger category;

    @ManyToOne
    @JoinColumn(nullable = true)
    public Ledger parentLedger;

    @ManyToOne
    @JsonbTransient
    @JoinColumn(nullable = false)
    public Business business;

    public Ledger() {
    }

    /**
     * Common Ledger // normally created via the create ledger function
     *
     * @param name
     * @param description
     * @param parentLedger
     * @param business
     */
    public Ledger(String name, String description, Ledger parentLedger, Business business) {
        this.name = name;
        this.description = description;
        this.parentLedger = parentLedger;
        this.business = business;
        this.systemName=System.currentTimeMillis()+"";

        /// set
        this.isSystemGenerated = Boolean.FALSE;
        this.showOnChart = Boolean.TRUE;
        this.type = parentLedger.type;
        this.isContra = Boolean.FALSE;
        this.isPostable = Boolean.TRUE;
        this.hasSubAccounts = Boolean.FALSE;
        this.isSupaAccount = Boolean.FALSE;
        this.isCategory = Boolean.FALSE;
        this.code = generateCode(parentLedger, business);

        // get
        if (parentLedger.isCategory) {
            this.category = parentLedger;
        } else {
            this.category = parentLedger.category;
        }
    }

    /**
     * For first time chart populating // only used when initializing the business
     *
     * @param code
     * @param name
     * @param systemName
     * @param description
     * @param isCategory
     * @param isSystemGenerated
     * @param isSupaAccount
     * @param hasSubAccounts
     * @param showOnChart
     * @param isPostable
     * @param type
     * @param isContra
     * @param contraLedger
     * @param parentLedger
     * @param category
     * @param business
     */
    public Ledger(String code, String name, String systemName, String description, Boolean isCategory,
                  Boolean isSystemGenerated, Boolean isSupaAccount, Boolean hasSubAccounts, Boolean showOnChart, Boolean isPostable,
                  String type, Boolean isContra, Ledger contraLedger, Ledger parentLedger, Ledger category, Business business) {
        this.name = name;
        this.systemName = systemName;
        this.description = description;
        this.isCategory = isCategory;
        this.isSystemGenerated = isSystemGenerated;
        this.isSupaAccount = isSupaAccount;
        this.hasSubAccounts = hasSubAccounts;
        this.showOnChart = showOnChart;
        this.isPostable = isPostable;
        this.type = type;
        this.isContra = isContra;
        this.contraLedger = contraLedger;
        this.parentLedger = parentLedger;
        this.category = category;
        this.business = business;
        this.code = code;
    }

    public static Ledger getLedgerBySystemName(String systemName, Business business) {
        return find("systemName=?1 and business=?2", systemName, business).firstResult();
    }

    public static Ledger getLedgerByCode(String code, Business business) {
        return find("code=?1 and business=?2", code, business).firstResult();
    }

    public static Ledger checkExists(String name, Business business) {
        return find("name =?1 and business=?2", name, business).firstResult();
    }

    public static Ledger checkExists(String name, Business business, Long id) {
        return find("name =?1 and business=?2 and id!=?3", name, business, id).firstResult();
    }

    public static List<Ledger> findPostableLedgers(Business business, String categoryCode) {

        String searchInput = null;
        if (categoryCode != null) {
            searchInput = categoryCode + "%";
        }
        return find("business=?1 and isPostable=?2 and (?3 is null or code like ?3)", business, Boolean.TRUE, searchInput)
                .list();
    }

    public static List<Ledger> search(Business business) {
        return list("business=?1", business);
    }

    public static List<Ledger> findSubLedgers(Ledger ledger) {

        return find("parentLedger=?1", ledger).list();
    }

    public static List<Ledger> findChartOfAccountsLedgers(Business business, String categoryCode) {
        String searchInput = null;
        if (categoryCode != null) {
            searchInput = categoryCode + "%";
        }

        return list("business=?1 and showOnChart=?2 and (?3 is null or code like ?3)", business, Boolean.TRUE, searchInput);
    }

    public static String generateCode(Ledger parent, Business business) {
        List<Ledger> ledgerList = find("business=?1 and parentLedger=?2", business, parent).list();

        int latestAccount = 0;
        for (Ledger ledger : ledgerList) {
            String ledgerT = ledger.code.replace(parent.code + ".", "");
            int late = Integer.parseInt(ledgerT);

            if (late >= latestAccount) {
                latestAccount = late;
            }
        }

        return parent.code + "." + (latestAccount + 1);
    }
}
