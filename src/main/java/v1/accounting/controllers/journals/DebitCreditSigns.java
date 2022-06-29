package v1.accounting.controllers.journals;

import java.math.BigDecimal;

public class DebitCreditSigns {

    public static BigDecimal debit(BigDecimal amount, String type) {
        if (type.equals("LIABILITIES") || type.equals("EQUITY")
                || type.equals("INCOMES")) {
            amount = amount.negate();
        }

        return amount;
    }

    public static BigDecimal credit(BigDecimal amount, String type) {
        if (type.equals("ASSETS") || type.equals("EXPENSES")) {
            amount = amount.negate();
        }

        return amount;
    }
}
