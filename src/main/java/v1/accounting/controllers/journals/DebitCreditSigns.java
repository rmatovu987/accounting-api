/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v1.accounting.controllers.journals;

import org.arkounts.coreAccounting.statics._LedgerCategories_Enum;

import java.math.BigDecimal;

public class DebitCreditSigns {

    public static BigDecimal debit(BigDecimal amount, String type) {
        if (_LedgerCategories_Enum.LIABILITIES.toString().equals(type) || _LedgerCategories_Enum.EQUITY.toString().equals(type)
                || _LedgerCategories_Enum.INCOMES.toString().equals(type)) {
            amount = amount.negate();
        }

        return amount;
    }

    public static BigDecimal credit(BigDecimal amount, String type) {
        if (_LedgerCategories_Enum.ASSETS.toString().equals(type) || _LedgerCategories_Enum.EXPENSES.toString().equals(type)) {
            amount = amount.negate();
        }

        return amount;
    }
}
