package v1.accounting.controllers.initializer;

public class LedgerTemplate {

    public String name;

    public String systemName;

    public String code;

    public String description;

    public Boolean isCategory = Boolean.FALSE;

    public Boolean isSupaAccount = Boolean.FALSE;

    public Boolean hasSubAccounts = Boolean.FALSE;

    public Boolean isPostable = Boolean.FALSE;

    public Boolean isContra = Boolean.FALSE;

    public String type;

    public LedgerTemplate contraLedger;

    public LedgerTemplate parentLedger;

    public LedgerTemplate category;

    public LedgerTemplate() {
    }

    public LedgerTemplate(String name, String description, Boolean isCategory, Boolean isPostable, Boolean isContra, LedgerTemplate category) {
        this.name = name;
        this.description = description;
        this.isCategory = isCategory;
        this.isPostable = isPostable;
        this.isContra = isContra;
        this.category = category;
    }

}
