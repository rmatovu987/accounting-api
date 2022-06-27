package v1.accounting.controllers.ledgers.service.payload;

import java.math.BigDecimal;

public class COAbalance {
    public BigDecimal realBalance;
    public BigDecimal feckBalance;

    public COAbalance() {
    }

    public COAbalance(BigDecimal realBalance, BigDecimal feckBalance) {
        this.realBalance = realBalance;
        this.feckBalance = feckBalance;
    }
}
