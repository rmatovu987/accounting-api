package v1.accounting.controllers.initializer;

import v1.accounting.domains.Ledger;
import v1.authentication.domains.Business;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AccountingInitializer {

    public void initializeAccounting(Business business) {

        List<Ledger> checkForLedger = Ledger.search(business);
        if (checkForLedger.isEmpty()) {
            List<LedgerTemplate> data = initiateParentLedgers();
            data.forEach(template -> {
                saveLedger(business, template);
            });

            data.forEach(template -> {
                setParents(business, template);
            });
        }
    }

    @Transactional
    public void saveLedger(Business business, LedgerTemplate item) {

        String systemName;
        if(item.systemName == null) systemName = item.name.toUpperCase();
        else systemName = item.systemName.toUpperCase();

        System.out.println("sysname "+systemName);
        Ledger parentLedger = null;
        if (item.parentLedger != null) {
            parentLedger = Ledger.getLedgerBySystemName(item.parentLedger.name.toUpperCase(), business);
            parentLedger.hasSubAccounts = Boolean.TRUE;
            parentLedger.isPostable = Boolean.FALSE;
        }

        Ledger contraLedger = null;
        if (item.isContra) {
            contraLedger = Ledger.getLedgerBySystemName(item.parentLedger.name.toUpperCase(), business);
        }

        Ledger category = null;
        if (item.category != null) {
            category = Ledger.getLedgerBySystemName(item.category.name.toUpperCase(), business);
        }

        Ledger ledger = new Ledger(
                item.code,
                item.name,
                systemName,
                item.description,
                item.isCategory,
                Boolean.TRUE,
                item.isSupaAccount,
                item.hasSubAccounts,
                Boolean.TRUE,
                item.isPostable,
                item.type,
                item.isContra,
                contraLedger, //contra ledger
                parentLedger, //parent ledger
                category,
                business);

        ledger.persist();
        System.out.println(ledger.name);
    }

    @Transactional
    public void setParents(Business business, LedgerTemplate item) {

        Ledger parentLedger = null;
        if (item.parentLedger != null) {
            parentLedger = Ledger.getLedgerBySystemName(item.parentLedger.name.toUpperCase(), business);
            parentLedger.hasSubAccounts = Boolean.TRUE;
            parentLedger.isPostable = Boolean.FALSE;
        }

        Ledger contraLedger = null;
        if (item.isContra) {
            contraLedger = Ledger.getLedgerBySystemName(item.parentLedger.name.toUpperCase(), business);
        }

        Ledger category = null;
        if (item.category != null) {
            category = Ledger.getLedgerBySystemName(item.category.name.toUpperCase(), business);
        }

        Ledger getLedger = Ledger.getLedgerBySystemName(item.name.toUpperCase(), business);
        getLedger.parentLedger = parentLedger;
        getLedger.contraLedger = contraLedger;
        getLedger.category = category;
    }

    public List<LedgerTemplate> initiateParentLedgers() {

        List<LedgerTemplate> data = new ArrayList<>();

        // assets
        LedgerTemplate assets = new LedgerTemplate("Assets", "Assets", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        assets.code = "1";
        assets.isSupaAccount = Boolean.TRUE;
        assets.isContra = Boolean.FALSE;
        assets.hasSubAccounts = Boolean.FALSE;
        assets.systemName = "ASSETS";
        assets.type = "ASSETS";
        data.add(assets);

        // current assets
        LedgerTemplate currentAssets = new LedgerTemplate();
        currentAssets.name = "Current Assets";
        currentAssets.description = "Current Assets";
        currentAssets.isCategory = Boolean.TRUE;
        currentAssets.isPostable = Boolean.FALSE;
        currentAssets.isContra = Boolean.FALSE;
        currentAssets.parentLedger = assets;
        data.add(currentAssets);

        // cash and cash equivalents
        LedgerTemplate cashEquivalents = new LedgerTemplate();
        cashEquivalents.name = "Cash and Cash Equivalents";
        cashEquivalents.description = "Cash and Cash Equivalents";
        cashEquivalents.isCategory = Boolean.TRUE;
        cashEquivalents.isPostable = Boolean.FALSE;
        cashEquivalents.isContra = Boolean.FALSE;
        cashEquivalents.parentLedger = currentAssets;
        data.add(cashEquivalents);

        // Cash at hand
        LedgerTemplate cashHand = new LedgerTemplate();
        cashHand.name = "Cash at hand";
        cashHand.description = "Cash at hand";
        cashHand.isCategory = Boolean.FALSE;
        cashHand.isPostable = Boolean.TRUE;
        cashHand.isContra = Boolean.FALSE;
        cashHand.parentLedger = cashEquivalents;
        data.add(cashHand);

        // Cash at bank
        LedgerTemplate bankCash = new LedgerTemplate();
        bankCash.name = "Cash at bank";
        bankCash.description = "Cash at bank";
        bankCash.isCategory = Boolean.FALSE;
        bankCash.isPostable = Boolean.TRUE;
        bankCash.isContra = Boolean.FALSE;
        bankCash.parentLedger = cashEquivalents;
        data.add(bankCash);

        // account receivables
        LedgerTemplate accountsReceivables = new LedgerTemplate();
        accountsReceivables.name = "Account Receivables";
        accountsReceivables.description = "Account Receivables";
        accountsReceivables.isCategory = Boolean.TRUE;
        accountsReceivables.isPostable = Boolean.TRUE;
        accountsReceivables.isContra = Boolean.FALSE;
        accountsReceivables.parentLedger = currentAssets;
        data.add(accountsReceivables);

        // allowance for bad debt
        LedgerTemplate allowanceForBadDebt = new LedgerTemplate();
        allowanceForBadDebt.name = "Allowance for Bad Debt";
        allowanceForBadDebt.description = "Allowance for Bad Debt";
        allowanceForBadDebt.isCategory = Boolean.TRUE;
        allowanceForBadDebt.isPostable = Boolean.FALSE;
        allowanceForBadDebt.isContra = Boolean.TRUE;
        allowanceForBadDebt.parentLedger = accountsReceivables;
        data.add(allowanceForBadDebt);

        // inventory
        LedgerTemplate inventory = new LedgerTemplate();
        inventory.name = "Inventory";
        inventory.description = "Inventory";
        inventory.isCategory = Boolean.TRUE;
        inventory.isPostable = Boolean.TRUE;
        inventory.isContra = Boolean.FALSE;
        inventory.parentLedger = currentAssets;
        data.add(inventory);

        // prepaid expenses
        LedgerTemplate prepaidexpenses = new LedgerTemplate();
        prepaidexpenses.name = "Prepaid Expenses";
        prepaidexpenses.description = "Prepaid Expenses";
        prepaidexpenses.isCategory = Boolean.TRUE;
        prepaidexpenses.isPostable = Boolean.TRUE;
        prepaidexpenses.isContra = Boolean.FALSE;
        prepaidexpenses.parentLedger = currentAssets;
        data.add(prepaidexpenses);

        // short term investments
        LedgerTemplate stInvestment = new LedgerTemplate();
        stInvestment.name = "Short Term Investment";
        stInvestment.description = "Short Term Investment";
        stInvestment.isCategory = Boolean.TRUE;
        stInvestment.isPostable = Boolean.TRUE;
        stInvestment.isContra = Boolean.FALSE;
        stInvestment.parentLedger = currentAssets;
        data.add(stInvestment);

        // non current assets
        LedgerTemplate nonCurrentAssets = new LedgerTemplate();
        nonCurrentAssets.name = "Non Current Assets";
        nonCurrentAssets.description = "Non Current Assets";
        nonCurrentAssets.isCategory = Boolean.TRUE;
        nonCurrentAssets.isPostable = Boolean.TRUE;
        nonCurrentAssets.isContra = Boolean.FALSE;
        nonCurrentAssets.parentLedger = assets;
        data.add(nonCurrentAssets);

        // PPE
        LedgerTemplate PPE = new LedgerTemplate();
        PPE.name = "Property, Plant and Equipment";
        PPE.description = "Property, Plant and Equipment";
        PPE.isCategory = Boolean.TRUE;
        PPE.isPostable = Boolean.TRUE;
        PPE.isContra = Boolean.FALSE;
        PPE.parentLedger = nonCurrentAssets;
        data.add(PPE);

        // Long term investments
        LedgerTemplate longInvestment = new LedgerTemplate();
        longInvestment.name = "Long Term Investments";
        longInvestment.description = "Long Term Investments";
        longInvestment.isCategory = Boolean.TRUE;
        longInvestment.isPostable = Boolean.TRUE;
        longInvestment.isContra = Boolean.FALSE;
        longInvestment.parentLedger = nonCurrentAssets;
        data.add(longInvestment);


        /// liability
        LedgerTemplate liability = new LedgerTemplate("Liabilities", "Liabilities", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        liability.code = "2";
        liability.isSupaAccount = Boolean.TRUE;
        liability.isContra = Boolean.FALSE;
        liability.hasSubAccounts = Boolean.FALSE;
        liability.systemName = "LIABILITIES";
        liability.type = "LIABILITIES";
        data.add(liability);

        // current liabilities
        LedgerTemplate currentLiabilities = new LedgerTemplate();
        currentLiabilities.name = "Current Liabilities";
        currentLiabilities.description = "Current Liabilities";
        currentLiabilities.isCategory = Boolean.TRUE;
        currentLiabilities.isPostable = Boolean.TRUE;
        currentLiabilities.isContra = Boolean.FALSE;
        currentLiabilities.parentLedger = liability;
        data.add(currentLiabilities);

        // account payable
        LedgerTemplate accountPayable = new LedgerTemplate();
        accountPayable.name = "Accounts Payable";
        accountPayable.description = "Accounts Payable";
        accountPayable.isCategory = Boolean.TRUE;
        accountPayable.isPostable = Boolean.TRUE;
        accountPayable.isContra = Boolean.FALSE;
        accountPayable.parentLedger = currentLiabilities;
        data.add(accountPayable);

        // dividends
        LedgerTemplate dividends = new LedgerTemplate();
        dividends.name = "Dividends Payable";
        dividends.description = "Dividends Payable";
        dividends.isCategory = Boolean.TRUE;
        dividends.isPostable = Boolean.TRUE;
        dividends.isContra = Boolean.FALSE;
        dividends.parentLedger = currentLiabilities;
        data.add(dividends);

        // short term loans
        LedgerTemplate shorttemloans = new LedgerTemplate();
        shorttemloans.name = "Short Term Loans";
        shorttemloans.description = "Short Term Loans";
        shorttemloans.isCategory = Boolean.TRUE;
        shorttemloans.isPostable = Boolean.TRUE;
        shorttemloans.isContra = Boolean.FALSE;
        shorttemloans.parentLedger = currentLiabilities;
        data.add(shorttemloans);

        // interest payable
        LedgerTemplate interestPayable = new LedgerTemplate();
        interestPayable.name = "Interest Payable";
        interestPayable.description = "Interest Payable";
        interestPayable.isCategory = Boolean.TRUE;
        interestPayable.isPostable = Boolean.TRUE;
        interestPayable.isContra = Boolean.FALSE;
        interestPayable.parentLedger = currentLiabilities;
        data.add(interestPayable);

        // bills payable
        LedgerTemplate billspayable = new LedgerTemplate();
        billspayable.name = "Bills Payable";
        billspayable.description = "Bills Payable";
        billspayable.isCategory = Boolean.TRUE;
        billspayable.isPostable = Boolean.TRUE;
        billspayable.isContra = Boolean.FALSE;
        billspayable.parentLedger = currentLiabilities;
        data.add(billspayable);

        // bank overdraft
        LedgerTemplate overdraft = new LedgerTemplate();
        overdraft.name = "Bank Overdraft";
        overdraft.description = "Bank Overdraft";
        overdraft.isCategory = Boolean.TRUE;
        overdraft.isPostable = Boolean.TRUE;
        overdraft.isContra = Boolean.FALSE;
        overdraft.parentLedger = currentLiabilities;
        data.add(overdraft);

        // accrued expenses
        LedgerTemplate accruExpens = new LedgerTemplate();
        accruExpens.name = "Accrued Expenses";
        accruExpens.description = "Accrued Expenses";
        accruExpens.isCategory = Boolean.TRUE;
        accruExpens.isPostable = Boolean.TRUE;
        accruExpens.isContra = Boolean.FALSE;
        accruExpens.parentLedger = currentLiabilities;
        data.add(accruExpens);

        // bank loan Payables
        LedgerTemplate bankloan = new LedgerTemplate();
        bankloan.name = "Bank Loans Payable";
        bankloan.description = "Bank Loans Payable";
        bankloan.isCategory = Boolean.TRUE;
        bankloan.isPostable = Boolean.TRUE;
        bankloan.isContra = Boolean.FALSE;
        bankloan.parentLedger = currentLiabilities;
        data.add(bankloan);

        // non current liabilities
        LedgerTemplate nonCurrentLiabilities = new LedgerTemplate();
        nonCurrentLiabilities.name = "Non Current Liabilities";
        nonCurrentLiabilities.description = "Non Current Liabilities";
        nonCurrentLiabilities.isCategory = Boolean.TRUE;
        nonCurrentLiabilities.isPostable = Boolean.TRUE;
        nonCurrentLiabilities.isContra = Boolean.FALSE;
        nonCurrentLiabilities.parentLedger = liability;
        data.add(nonCurrentLiabilities);

        // long term loans
        LedgerTemplate longtermloans = new LedgerTemplate();
        longtermloans.name = "Long Term Loans";
        longtermloans.description = "Long Term Loans";
        longtermloans.isCategory = Boolean.TRUE;
        longtermloans.isPostable = Boolean.TRUE;
        longtermloans.isContra = Boolean.FALSE;
        longtermloans.parentLedger = nonCurrentLiabilities;
        data.add(longtermloans);

        // mortgages
        LedgerTemplate mortgages = new LedgerTemplate();
        mortgages.name = "Mortgages";
        mortgages.description = "Mortgages";
        mortgages.isCategory = Boolean.TRUE;
        mortgages.isPostable = Boolean.TRUE;
        mortgages.isContra = Boolean.FALSE;
        mortgages.parentLedger = nonCurrentLiabilities;
        data.add(mortgages);

        // bonds payable
        LedgerTemplate bondpayable = new LedgerTemplate();
        bondpayable.name = "Bonds Payable";
        bondpayable.description = "Bonds Payable";
        bondpayable.isCategory = Boolean.TRUE;
        bondpayable.isPostable = Boolean.TRUE;
        bondpayable.isContra = Boolean.FALSE;
        bondpayable.parentLedger = nonCurrentLiabilities;
        data.add(bondpayable);

        // Deferred capital
        LedgerTemplate defferedTaxes = new LedgerTemplate();
        defferedTaxes.name = "Deffered Taxes";
        defferedTaxes.description = "Deffered Taxes";
        defferedTaxes.isCategory = Boolean.TRUE;
        defferedTaxes.isPostable = Boolean.TRUE;
        defferedTaxes.isContra = Boolean.FALSE;
        defferedTaxes.parentLedger = nonCurrentLiabilities;
        data.add(defferedTaxes);

        // captial leases
        LedgerTemplate capitalLeases = new LedgerTemplate();
        capitalLeases.name = "Capital Leases";
        capitalLeases.description = "Capital Leases";
        capitalLeases.isCategory = Boolean.TRUE;
        capitalLeases.isPostable = Boolean.TRUE;
        capitalLeases.isContra = Boolean.FALSE;
        capitalLeases.parentLedger = nonCurrentLiabilities;
        data.add(capitalLeases);

        // contigent liabilities
        LedgerTemplate contigentLiabilities = new LedgerTemplate();
        contigentLiabilities.name = "Contigent Liabilities";
        contigentLiabilities.description = "Contigent Liabilities";
        contigentLiabilities.isCategory = Boolean.TRUE;
        contigentLiabilities.isPostable = Boolean.FALSE;
        contigentLiabilities.isContra = Boolean.FALSE;
        contigentLiabilities.parentLedger = liability;
        data.add(contigentLiabilities);

        // Lawsiut settlement
        LedgerTemplate lawsuit = new LedgerTemplate();
        lawsuit.name = "Lawsuit Settlements";
        lawsuit.description = "Lawsuit Settlements";
        lawsuit.isCategory = Boolean.TRUE;
        lawsuit.isPostable = Boolean.TRUE;
        lawsuit.isContra = Boolean.FALSE;
        lawsuit.parentLedger = contigentLiabilities;
        data.add(lawsuit);

        // Refunds
        LedgerTemplate refunds = new LedgerTemplate();
        refunds.name = "Refunds";
        refunds.description = "Refunds";
        refunds.isCategory = Boolean.TRUE;
        refunds.isPostable = Boolean.TRUE;
        refunds.isContra = Boolean.FALSE;
        refunds.parentLedger = contigentLiabilities;
        data.add(refunds);

        // product warranty
        LedgerTemplate warranty = new LedgerTemplate();
        warranty.name = "Product Warranties";
        warranty.description = "Product Warranties";
        warranty.isCategory = Boolean.TRUE;
        warranty.isPostable = Boolean.TRUE;
        warranty.isContra = Boolean.FALSE;
        warranty.parentLedger = contigentLiabilities;
        data.add(warranty);

        /// Equity
        LedgerTemplate equity = new LedgerTemplate("Equity", "Equity", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        equity.code = "3";
        equity.isSupaAccount = Boolean.TRUE;
        equity.isContra = Boolean.FALSE;
        equity.hasSubAccounts = Boolean.FALSE;
        equity.systemName = "EQUITY";
        equity.type = "EQUITY";
        data.add(equity);

        // retained earnings
        LedgerTemplate retainedEarnings = new LedgerTemplate();
        retainedEarnings.name = "Retained Earnings";
        retainedEarnings.description = "Retained Earnings";
        retainedEarnings.isCategory = Boolean.TRUE;
        retainedEarnings.isPostable = Boolean.TRUE;
        retainedEarnings.isContra = Boolean.FALSE;
        retainedEarnings.parentLedger = equity;
        data.add(retainedEarnings);

        // Share Capital
        LedgerTemplate shareCapital = new LedgerTemplate();
        shareCapital.name = "Share Capital";
        shareCapital.description = "Share Capital";
        shareCapital.isCategory = Boolean.TRUE;
        shareCapital.isPostable = Boolean.TRUE;
        shareCapital.isContra = Boolean.FALSE;
        shareCapital.parentLedger = equity;
        data.add(shareCapital);

        // Share Premium
        LedgerTemplate sharePremium = new LedgerTemplate();
        sharePremium.name = "Share Premium";
        sharePremium.description = "Share Premium";
        sharePremium.isCategory = Boolean.TRUE;
        sharePremium.isPostable = Boolean.TRUE;
        sharePremium.isContra = Boolean.FALSE;
        sharePremium.parentLedger = equity;
        data.add(sharePremium);

        // Treasury Stock
        LedgerTemplate treasury = new LedgerTemplate();
        treasury.name = "Treasury Stock";
        treasury.description = "Treasury Stock";
        treasury.isCategory = Boolean.TRUE;
        treasury.isPostable = Boolean.TRUE;
        treasury.isContra = Boolean.FALSE;
        treasury.parentLedger = equity;
        data.add(treasury);

        // Revaluation reserve
        LedgerTemplate rreserve = new LedgerTemplate();
        rreserve.name = "Revaluation Reserve";
        rreserve.description = "Revaluation Reserve";
        rreserve.isCategory = Boolean.TRUE;
        rreserve.isPostable = Boolean.TRUE;
        rreserve.isContra = Boolean.FALSE;
        rreserve.parentLedger = equity;
        data.add(rreserve);

        // Opening equity
        LedgerTemplate openingEquity = new LedgerTemplate();
        openingEquity.name = "Opening Equity";
        openingEquity.description = "Opening Equity";
        openingEquity.isCategory = Boolean.TRUE;
        openingEquity.isPostable = Boolean.TRUE;
        openingEquity.isContra = Boolean.FALSE;
        openingEquity.parentLedger = equity;
        data.add(openingEquity);

        /// Incomes
        LedgerTemplate income = new LedgerTemplate("Incomes", "Incomes", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        income.code = "4";
        income.isSupaAccount = Boolean.TRUE;
        income.isContra = Boolean.FALSE;
        income.hasSubAccounts = Boolean.FALSE;
        income.systemName = "INCOMES";
        income.type = "INCOMES";
        data.add(income);

        // operating incomes
        LedgerTemplate operatingIncomes = new LedgerTemplate();
        operatingIncomes.name = "Operating Incomes";
        operatingIncomes.description = "Operating Incomes";
        operatingIncomes.isCategory = Boolean.TRUE;
        operatingIncomes.isPostable = Boolean.FALSE;
        operatingIncomes.isContra = Boolean.FALSE;
        operatingIncomes.parentLedger = income;
        data.add(operatingIncomes);

        // sales
        LedgerTemplate sales = new LedgerTemplate();
        sales.name = "Sales";
        sales.description = "Sales";
        sales.isCategory = Boolean.TRUE;
        sales.isPostable = Boolean.TRUE;
        sales.isContra = Boolean.FALSE;
        sales.parentLedger = operatingIncomes;
        data.add(sales);

        // interest earned
        LedgerTemplate interestEraned = new LedgerTemplate();
        interestEraned.name = "Interest Earned";
        interestEraned.description = "Interest Earned";
        interestEraned.isCategory = Boolean.TRUE;
        interestEraned.isPostable = Boolean.TRUE;
        interestEraned.isContra = Boolean.FALSE;
        interestEraned.parentLedger = operatingIncomes;
        data.add(interestEraned);

        // charges levied
        LedgerTemplate chargesLevied = new LedgerTemplate();
        chargesLevied.name = "Charges Levied";
        chargesLevied.description = "Charges Levied";
        chargesLevied.isCategory = Boolean.TRUE;
        chargesLevied.isPostable = Boolean.TRUE;
        chargesLevied.isContra = Boolean.FALSE;
        chargesLevied.parentLedger = operatingIncomes;
        data.add(chargesLevied);

        // non operating incomes
        LedgerTemplate nonOperatingIncomes = new LedgerTemplate();
        nonOperatingIncomes.name = "Non Operating Incomes";
        nonOperatingIncomes.description = "Non Operating Incomes";
        nonOperatingIncomes.isCategory = Boolean.TRUE;
        nonOperatingIncomes.isPostable = Boolean.FALSE;
        nonOperatingIncomes.isContra = Boolean.FALSE;
        nonOperatingIncomes.parentLedger = income;
        data.add(nonOperatingIncomes);

        // Investment Income
        LedgerTemplate investmentIncome = new LedgerTemplate();
        investmentIncome.name = "Investment Income";
        investmentIncome.description = "Investment Income";
        investmentIncome.isCategory = Boolean.TRUE;
        investmentIncome.isPostable = Boolean.TRUE;
        investmentIncome.isContra = Boolean.FALSE;
        investmentIncome.parentLedger = nonOperatingIncomes;
        data.add(investmentIncome);

        // income from sale of assets
        LedgerTemplate saleOfAssets = new LedgerTemplate();
        saleOfAssets.name = "Income from Sale of Assets";
        saleOfAssets.description = "Income from Sale of Assets";
        saleOfAssets.isCategory = Boolean.TRUE;
        saleOfAssets.isPostable = Boolean.TRUE;
        saleOfAssets.isContra = Boolean.FALSE;
        saleOfAssets.parentLedger = nonOperatingIncomes;
        data.add(saleOfAssets);

        /// Expenses
        LedgerTemplate expenses = new LedgerTemplate("Expenses", "Expenses", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        expenses.code = "5";
        expenses.isSupaAccount = Boolean.TRUE;
        expenses.isContra = Boolean.FALSE;
        expenses.hasSubAccounts = Boolean.FALSE;
        expenses.systemName = "EXPENSES";
        expenses.type = "EXPENSES";
        data.add(expenses);

        // operating expenses
        LedgerTemplate operatingExpenses = new LedgerTemplate();
        operatingExpenses.name = "Operating Expenses";
        operatingExpenses.description = "Operating Expenses";
        operatingExpenses.isCategory = Boolean.TRUE;
        operatingExpenses.isPostable = Boolean.FALSE;
        operatingExpenses.isContra = Boolean.FALSE;
        operatingExpenses.parentLedger = expenses;
        data.add(operatingExpenses);

        // payroll
        LedgerTemplate payroll = new LedgerTemplate();
        payroll.name = "Payroll";
        payroll.description = "Payroll";
        payroll.isCategory = Boolean.TRUE;
        payroll.isPostable = Boolean.TRUE;
        payroll.isContra = Boolean.FALSE;
        payroll.parentLedger = operatingExpenses;
        data.add(payroll);

        LedgerTemplate bills = new LedgerTemplate();
        bills.name = "Bills";
        bills.description = "Bills";
        bills.isCategory = Boolean.TRUE;
        bills.isPostable = Boolean.TRUE;
        bills.isContra = Boolean.FALSE;
        bills.parentLedger = operatingExpenses;
        data.add(bills);

        LedgerTemplate purchases = new LedgerTemplate();
        purchases.name = "Purchases";
        purchases.description = "Purchases";
        purchases.isCategory = Boolean.TRUE;
        purchases.isPostable = Boolean.TRUE;
        purchases.isContra = Boolean.FALSE;
        purchases.parentLedger = operatingExpenses;
        data.add(purchases);

        // costs on account receivables
        LedgerTemplate costss = new LedgerTemplate();
        costss.name = "Costs on Accounts Receivables";
        costss.description = "Costs on Accounts Receivables";
        costss.isCategory = Boolean.TRUE;
        costss.isPostable = Boolean.TRUE;
        costss.isContra = Boolean.FALSE;
        costss.parentLedger = operatingExpenses;
        data.add(costss);

        LedgerTemplate costssq = new LedgerTemplate();
        costssq.name = "Cost of Sales";
        costssq.description = "Cost of Sales";
        costssq.isCategory = Boolean.TRUE;
        costssq.isPostable = Boolean.TRUE;
        costssq.isContra = Boolean.FALSE;
        costssq.parentLedger = operatingExpenses;
        data.add(costssq);

        LedgerTemplate badDebtExpense = new LedgerTemplate();
        badDebtExpense.name = "Bad Debt Expenses";
        badDebtExpense.description = "Bad Debt Expenses";
        badDebtExpense.isCategory = Boolean.TRUE;
        badDebtExpense.isPostable = Boolean.TRUE;
        badDebtExpense.isContra = Boolean.FALSE;
        badDebtExpense.parentLedger = operatingExpenses;
        data.add(badDebtExpense);

        LedgerTemplate chargesIncured = new LedgerTemplate();
        chargesIncured.name = "Charges Incurred";
        chargesIncured.description = "Charges Incurred";
        chargesIncured.isCategory = Boolean.TRUE;
        chargesIncured.isPostable = Boolean.TRUE;
        chargesIncured.isContra = Boolean.FALSE;
        chargesIncured.parentLedger = operatingExpenses;
        data.add(chargesIncured);

        // non operating expenses
        LedgerTemplate nonOperatingExpenses = new LedgerTemplate();
        nonOperatingExpenses.name = "Non Operating Expenses";
        nonOperatingExpenses.description = "Non Operating Expenses";
        nonOperatingExpenses.isCategory = Boolean.TRUE;
        nonOperatingExpenses.isPostable = Boolean.FALSE;
        nonOperatingExpenses.isContra = Boolean.FALSE;
        nonOperatingExpenses.parentLedger = expenses;
        data.add(nonOperatingExpenses);

        // losses from sale of assets
        LedgerTemplate lossesFromSaleOfAssets = new LedgerTemplate();
        lossesFromSaleOfAssets.name = "Losses from Sale of Assets";
        lossesFromSaleOfAssets.description = "Losses from Sale of Assets";
        lossesFromSaleOfAssets.isCategory = Boolean.TRUE;
        lossesFromSaleOfAssets.isPostable = Boolean.TRUE;
        lossesFromSaleOfAssets.isContra = Boolean.FALSE;
        lossesFromSaleOfAssets.parentLedger = nonOperatingExpenses;
        data.add(lossesFromSaleOfAssets);

        return data;
    }

}
