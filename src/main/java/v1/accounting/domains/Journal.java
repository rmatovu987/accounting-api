package v1.accounting.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import v1.authentication.domains.Business;
import v1.statics.TimeConverter;
import v1.statics._StatusTypes_Enum;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Journal extends PanacheEntity {
    @Column(nullable = false)
    public Long date;

    @Column(nullable = false)
    public String journalNo;

    public Boolean isSystemGenerated;

    @Column(nullable = false)
    public String type; //manual, integrated, system

    @Column(nullable = true)
    public String journalClass;

    @Column(nullable = false)
    public String reference;

    @Column(nullable = false)
    public String status;

    @Transient
    public BigDecimal amount;

    public String notes;

    public Long entryTime = TimeConverter.EpochMillis_Now();

    /// mappers
    @OneToMany(mappedBy = "journal")
    @JsonbTransient
    @LazyCollection(LazyCollectionOption.FALSE)
    public List<JournalEntry> entries = new ArrayList<>();

    // relationships
    @ManyToOne
    @JsonbTransient
    @JoinColumn(nullable = false)
    public Business business;

    public Journal() {
    }

    public Journal(Long date, Boolean isSystemGenerated, String type, String journalClass, String reference, String notes,
                   Business business) {
        this.date = date;
        this.journalNo = generateJournalNo(business);
        this.isSystemGenerated = isSystemGenerated;
        this.type = type;
        this.journalClass = journalClass;
        this.reference = reference;
        this.notes = notes;
        this.business = business;
        this.entryTime = TimeConverter.EpochMillis_Now();
        this.status = _StatusTypes_Enum.DRAFT.toString();
        this.entries = new ArrayList<>();
    }

    public BigDecimal getAmount() {
        BigDecimal amount1 = BigDecimal.ZERO;
        for(JournalEntry entry : entries){
            amount1 = amount1.add(entry.credit);
        }
        return amount1;
    }

    public static Journal single(BigDecimal amount, Boolean isSystemGenerated, String type, String reference, Long date,
                                 Business business) {
        Optional<Journal> journal = Journal.find(
                "(?1 is null or amount=?1) and (?2 is null or isSystemGenerated=?2) and (?3 is null or type=?3) and (?4 is null or reference=?4) and (?5 is null or date=?5) and business=?6",
                amount, isSystemGenerated, type, reference, date, business).firstResultOptional();

        return journal.orElse(null);

    }

    public static List<Journal> search(Long startDate, Long endDate, Boolean isSystemGenerated,
                                       String reference, String status, Business business) {
        return list(
                "(date between ?1 and ?2) and (?3 is null or status=?3) and (?4 is null or isSystemGenerated=?4) and (?5 is null or reference=?5) and business=?6",
                startDate, endDate, status, isSystemGenerated, reference, business);
    }

    public static String generateJournalNo(Business business) {
        Optional<Journal> T = Journal.find("business", Sort.descending("id"), business).firstResultOptional();

        if (T.isPresent()) {
            String lastInv = T.get().journalNo;
            int lastInvInt = Integer.parseInt(lastInv) + 1;

            return String.format("%03d", lastInvInt);
        } else {

            return String.format("%03d", 1);
        }
    }
}
