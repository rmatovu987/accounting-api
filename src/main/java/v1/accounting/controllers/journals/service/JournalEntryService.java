package v1.accounting.controllers.journals.service;

import v1.accounting.controllers.journals.DebitCreditSigns;
import v1.accounting.controllers.journals.service.payload.JournalEntryRequest;
import v1.accounting.domains.Journal;
import v1.accounting.domains.JournalEntry;
import v1.accounting.domains.Ledger;
import v1.authentication.domains.Business;
import v1.statics._StatusTypes_Enum;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import java.util.List;

@ApplicationScoped
public class JournalEntryService {

    /**
     * Creates a new journal entry
     *
     * @param request
     * @param newJournal
     * @param business
     * @return Created journal entry
     */
    public JournalEntry createJournalEntry(JournalEntryRequest request, Journal journal, Boolean newJournal, Business business) {
        Ledger ledger = Ledger.findById(request.ledgerId);
        if (ledger == null)
            throw new WebApplicationException("Invalid ledger selected!", 404);

        if (!newJournal && journal.status.equals(_StatusTypes_Enum.PUBLISHED.toString()))
            throw new WebApplicationException("Only journals that are not yet published can receive new journal entries!", 403);

        if (!newJournal && !request.confirmation) {
            Boolean exists = Boolean.FALSE;
            for (JournalEntry entry : journal.entries) {
                if (entry.ledger == ledger
                        && entry.credit.compareTo(DebitCreditSigns.credit(request.credit, ledger.type)) == 0
                        && entry.debit.compareTo(DebitCreditSigns.debit(request.debit, ledger.type)) == 0)
                    exists = Boolean.TRUE;
            }

            if (exists)
                throw new WebApplicationException(
                        "Journal entry already exists. Are you sure you want to create the entry?", 409);
        }

        JournalEntry journalEntry = new JournalEntry(journal.date, request.notes,
                DebitCreditSigns.debit(request.debit, ledger.type),
                DebitCreditSigns.credit(request.credit, ledger.type), journal, ledger, business);
        journalEntry.persist();

        return journalEntry;
    }

    /**
     * Updates a journal entry.
     *
     * @param request
     * @return Updated journal entry
     */
    public JournalEntry updateJournalEntry(JournalEntryRequest request) {
        JournalEntry journalEntry1 = JournalEntry.findById(request.id);
        if (journalEntry1 == null)
            throw new WebApplicationException("Invalid journal selected!", 404);

        Ledger ledger = Ledger.findById(request.ledgerId);
        if (ledger == null)
            throw new WebApplicationException("Invalid ledger selected!");

        JournalEntry journalEntry = journalEntry1;
        journalEntry.debit = DebitCreditSigns.debit(request.debit, ledger.type);
        journalEntry.credit = DebitCreditSigns.credit(request.credit, ledger.type);
        journalEntry.ledger = ledger;
        journalEntry.notes = request.notes;

        return journalEntry;
    }

    /**
     * Fetch journal entry details
     * 
     * @param id
     * @return Journal Entry
     */
    public JournalEntry getJournalEntryDetails(Long id) {
        JournalEntry journalEntry = JournalEntry.findById(id);
        if (journalEntry == null)
            throw new WebApplicationException("Invalid journalEntry selected", 404);

        return journalEntry;
    }

    /**
     * Fetch journal entries based on query parameters.
     * 
     * @param date1
     * @param date2
     * @return List of journal entries
     */
    public List<JournalEntry> getJournalEntries(Long date1, Long date2, Long ledgerId, Long journalId, Long branchId,
            Business business) {
        Journal journal = Journal.findById(journalId);
        if (journal == null)
            throw new WebApplicationException("Invalid journal selected!", 404);

        Ledger ledger = Ledger.findById(ledgerId);
        if (ledger == null)
            throw new WebApplicationException("Invalid ledger selected!");

        return JournalEntry.search(date1, date2, ledger, journal, business);
    }

    /**
     * Delete a journal entry
     * 
     * @param id
     * @return Deleted journal entry
     */
    public JournalEntry deleteJournalEntry(Long id) {

        JournalEntry journalEntry = JournalEntry.findById(id);
        if (journalEntry == null)
            throw new WebApplicationException("Invalid journalEntry selected", 404);

        JournalEntry journalEntry1 = journalEntry;

        journalEntry.delete();

        return journalEntry1;
    }

}
