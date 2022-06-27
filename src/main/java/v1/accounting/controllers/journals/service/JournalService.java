package v1.accounting.controllers.journals.service;

import v1.accounting.controllers.journals.service.payload.JournalData;
import v1.accounting.controllers.journals.service.payload.JournalEntryData;
import v1.accounting.controllers.journals.service.payload.JournalEntryRequest;
import v1.accounting.controllers.journals.service.payload.JournalRequest;
import v1.accounting.domains.Journal;
import v1.accounting.domains.JournalEntry;
import v1.authentication.domains.Business;
import v1.statics.TimeConverter;
import v1.statics._StatusTypes_Enum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class JournalService {

	@Inject
	JournalEntryService service;

	public Journal createJournal(JournalRequest request, Business business) {

		// loop through the journal entries and verify that the credits = debits
		BigDecimal credits = new BigDecimal("0");
		BigDecimal debits = new BigDecimal("0");
		for (JournalEntryRequest entryRequest : request.entries) {
			credits = credits.add(entryRequest.credit);
			debits = debits.add(entryRequest.debit);
		}

		if (credits.compareTo(debits) != 0) {
			throw new WebApplicationException("Total Credits must be equal to Total Debits!", 403);
		}

		// set the auto variables
		BigDecimal amount = credits;

		// check if the journal exists
		Journal exists = Journal.single(amount, Boolean.FALSE, "SYSTEM", request.reference, request.date,
				business);
		if (exists != null) {
			throw new WebApplicationException(
					"A journal with the same details already exists! Are you sure you want proceed?", 409);
		}

		// create a new journal
		Journal journal = new Journal(request.date, Boolean.FALSE, "SYSTEM", null, request.reference,
				request.notes, business);
		journal.amount = amount;
		journal.entries = new ArrayList<>();
		journal.persist();

		// save the journal entries
		for (JournalEntryRequest req : request.entries) {
			req.journalId = journal.id;
			journal.entries.add(service.createJournalEntry(req, journal, Boolean.TRUE, business));
		}

		// check if approval is required
		if (request.approve) {
			approveJournal(journal.id, business);
		}

		// check if publishing is required
		if (request.publish) {
			publishJournal(journal.id, business);
		}

		return journal;
	}

	public Journal updateJournalDetails(Long id, JournalRequest request, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.DRAFT.toString()))
			throw new WebApplicationException("Only draft journals can be approved!", 403);

		BigDecimal credits = new BigDecimal("0");
		BigDecimal debits = new BigDecimal("0");
		for (JournalEntryRequest entryRequest : request.entries) {
			credits = credits.add(entryRequest.credit);
			debits = debits.add(entryRequest.debit);
		}

		if (credits.compareTo(debits) != 0) {
			throw new WebApplicationException("Total Credits must be equal to Total Debits!", 500);
		}

		Journal old = journal;

		journal.date = request.date;
		journal.notes = request.notes;
		journal.reference = request.reference;
		journal.amount = credits;

		List<JournalEntry> newlist = new ArrayList<>();

		for (JournalEntryRequest req : request.entries) {
			if (req.id == null) {
				req.journalId = journal.id;
				JournalEntry e = service.createJournalEntry(req, journal, Boolean.FALSE, business);
				journal.entries.add(e);
				newlist.add(e);
			} else {
				newlist.add(service.updateJournalEntry(req));
			}
		}

		for (JournalEntry e : journal.entries) {
			if (!newlist.contains(e))
				service.deleteJournalEntry(e.id);
		}

		return journal;
	}

	public Journal approveJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.DRAFT.toString()))
			throw new WebApplicationException("Only draft journals can be approved!", 403);

		journal.status = _StatusTypes_Enum.APPROVED.toString();

		return journal;
	}

	public Journal publishJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.APPROVED.toString()))
			throw new WebApplicationException("Only approved journals can be published!", 403);

		if (journal.date > TimeConverter.LocalDate_to_EpochMilli_DayEnd(LocalDate.now()))
			throw new WebApplicationException("Future journals cannot be published!", 403);

		journal.status = _StatusTypes_Enum.PUBLISHED.toString();

		return journal;
	}

	public Journal declineJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.APPROVED.toString()))
			throw new WebApplicationException("Only approved journals can be declined!", 403);

		journal.status = _StatusTypes_Enum.DECLINED.toString();

		return journal;
	}

	public Journal undeclineJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.DECLINED.toString()))
			throw new WebApplicationException("Only declined journals can be undeclined!", 403);

		journal.status = _StatusTypes_Enum.APPROVED.toString();

		return journal;
	}

	public Journal rejectJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.DRAFT.toString()))
			throw new WebApplicationException("Only draft journals can be rejected!", 403);

		journal.status = _StatusTypes_Enum.REJECTED.toString();

		return journal;
	}

	public Journal unrejectJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected!", 404);

		if (!journal.status.equals(_StatusTypes_Enum.REJECTED.toString()))
			throw new WebApplicationException("Only rejected journals can be unrejected!", 403);

		journal.status = _StatusTypes_Enum.DRAFT.toString();

		return journal;
	}

	public JournalData getJournalDetails(Long id) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected", 404);

		JournalData j = new JournalData();
		j.amount = journal.getAmount();
		j.journalNo= journal.journalNo;
		j.journalClass= journal.journalClass;
		j.date= journal.date;
		j.isSystemGenerated=journal.isSystemGenerated;
		j.notes= journal.notes;
		j.reference= journal.reference;
		j.status= journal.status;
		j.type= journal.type;
		j.id= journal.id;

		for(JournalEntry i : journal.entries){
			JournalEntryData d = new JournalEntryData();
			d.id=i.id;
			d.date=i.date;
			d.entryTime=i.entryTime;
			d.credit=i.getCredit();
			d.debit=i.getDebit();
			d.ledger=i.ledger;
			d.notes=i.notes;

			j.entries.add(d);
		}

		return j;
	}

	public List<String> getJournalTypes() {
		List<String> journalTypes = new ArrayList<>();

		journalTypes.add("MANUAL");
		journalTypes.add("SYSTEM");

		return journalTypes;
	}

	public List<String> getJournalStatuses() {
		List<String> journalTypes = new ArrayList<>();

		journalTypes.add("DRAFT");
		journalTypes.add("APPROVED");
		journalTypes.add("PUBLISHED");
		journalTypes.add("DECLINED");
		journalTypes.add("DELETED");
		journalTypes.add("DRAFT");
		journalTypes.add("REJECTED");

		return journalTypes;
	}

	public List<Journal> getJournals(Long startDate, Long endDate, Boolean isSystemGenerated,
			String reference, _StatusTypes_Enum status, Business business) {
		LocalDate today = LocalDate.now();
		if (endDate == null) {
			endDate = TimeConverter.EpochMillis_Now();
		} else {
			endDate = TimeConverter.LocalDate_to_EpochMilli_DayEnd(TimeConverter.EpochMils_to_LocalDate(endDate));
		}
		if (startDate == null) {
			startDate = TimeConverter
					.LocalDate_to_EpochMilli_DayStart(LocalDate.of(today.getYear(), today.getMonth(), 01));
		} else {
			startDate = TimeConverter.LocalDate_to_EpochMilli_DayStart(TimeConverter.EpochMils_to_LocalDate(startDate));
		}
		String stat = null;
		if (status != null)
			stat = status.toString();

		return Journal.search(startDate, endDate, isSystemGenerated, reference, stat, business);
	}

	public Journal deleteJournal(Long id, Business business) {
		Journal journal = Journal.findById(id);
		if (journal == null)
			throw new WebApplicationException("Invalid journal selected", 404);

		if (!journal.status.equals(_StatusTypes_Enum.DRAFT.toString()))
			throw new WebApplicationException("Only draft journals can be deleted", 403);

		journal.status = _StatusTypes_Enum.DELETED.toString();

		return journal;
	}

}
