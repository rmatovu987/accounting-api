package v1.accounting.controllers.ledgers.service.payload;

import java.math.BigDecimal;

public class OpeningBalanceRequest {
    public Long ledgerId;
    public BigDecimal amount;

    public OpeningBalanceRequest() {
    }

    public OpeningBalanceRequest(Long ledgerId, BigDecimal amount) {
        this.ledgerId = ledgerId;
        this.amount = amount;
    }
}
