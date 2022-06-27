package v1.accounting.controllers.ledgers.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.agroal.api.AgroalDataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import v1.accounting.domains.Ledger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.WebApplicationException;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import v1.accounting.controllers.ledgers.service.payload.*;
import v1.authentication.domains.Business;
import v1.statics.GeneralPDFMethods;
import v1.statics.TimeConverter;
import v1.statics._StatusTypes_Enum;

@ApplicationScoped
public class LedgerService {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    CommonLedgerQuerries queries;

    /**
     * Save a new ledger If a ledger with the set name already exists, then the new
     * ledger will not be created.
     *
     * @param model    The object containing the required parameters
     * @param business The business saving the ledger
     * @return Saved ledger entity
     */
    public Ledger saveLedger(LedgerRequest model, Business business) {
        Ledger exists = Ledger.checkExists(model.name, business);
        if (exists != null) {
            throw new WebApplicationException("Ledger with the same name already exists", 409);
        }

        Ledger category = Ledger.findById(model.categoryId);
        if (category == null) {
            throw new WebApplicationException("Invalid ledger category selected", 404);
        }

        Ledger parentLedger = null;
        if (model.parentLedgerId != null) {
            parentLedger = Ledger.findById(model.parentLedgerId);
            if (parentLedger == null) {
                throw new WebApplicationException("Invalid parent ledger selected", 404);
            }
        } else {
            parentLedger = category;
        }
        parentLedger.hasSubAccounts = Boolean.TRUE;
        parentLedger.isPostable = Boolean.FALSE;

        Ledger ledger = new Ledger(model.name, model.description, parentLedger, business);
        ledger.persist();

        return ledger;
    }

    /**
     * Update the ledger with the specified id and also update the ledger
     *
     * @param id       The id of the ledger to be updated
     * @param model    The details of the ledger to be updated
     * @param business The business updating the ledger
     * @return Updated ledger entity
     */
    public Ledger updateLedger(Long id, LedgerRequest model, Business business) {

        Ledger exists = Ledger.checkExists(model.name, business, id);
        if (exists != null) {
            throw new WebApplicationException("Ledger with the same name already exists", 409);
        }

        Ledger entity = Ledger.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Invalid ledger selected!", 404);
        }

        Ledger olddata = entity;

        entity.name = model.name;
        entity.description = model.description;

        return entity;
    }

    /**
     * Find one ledger with the given id
     *
     * @param id The id of the ledger to be returned
     * @return The ledger's details
     */
    public Ledger getDetails(Long id) {
        Ledger ledger = Ledger.findById(id);
        if (ledger == null) {
            throw new WebApplicationException("Invalid ledger selected", 404);
        }

        return ledger;
    }

    /**
     * Delete an existing category from the database""
     *
     * @param id The id of the category to be deleted
     */
    public void deleteOne(Long id) {
        Ledger ledger = Ledger.findById(id);

        Ledger parent = ledger.parentLedger;

        ledger.delete();

        List<Ledger> ledgerList = Ledger.findSubLedgers(parent);
        if (ledgerList.isEmpty()) {
            parent.hasSubAccounts = Boolean.FALSE;
        }

    }

    public List<Ledger> getChart(Business business, Long categoryId) {
        String categoryCode = null;
        if (categoryId != null) {
            Ledger category = Ledger.findById(categoryId);
            if (category != null) {
                categoryCode = category.code;
            }
        }
        return Ledger.findChartOfAccountsLedgers(business, categoryCode);
    }

    public List<Ledger> getPostable(Business business, Long categoryId) {
        String categoryCode = null;
        if (categoryId != null) {
            Ledger category = Ledger.findById(categoryId);
            if (category != null) {
                categoryCode = category.code;
            }
        }
        return Ledger.findPostableLedgers(business, categoryCode);
    }

    public LedgerHistoryData getLedgerHistory(Long id, Long start, Long end, Business business) throws IOException, DocumentException {

        LocalDate today = LocalDate.now();
        if (start == null) {
            start = TimeConverter.LocalDate_to_EpochMilli_DayEnd(today.withDayOfMonth(01).minusDays(1));
        } else {
            start = TimeConverter.LocalDate_to_EpochMilli_DayEnd(TimeConverter.EpochMils_to_LocalDate(start).minusDays(1));
        }
        if (end == null) {
            end = TimeConverter.LocalDate_to_EpochMilli_DayEnd(
                    LocalDate.of(today.getYear(), today.getMonth(), today.lengthOfMonth()));
        } else {
            end = TimeConverter.LocalDate_to_EpochMilli_DayEnd(TimeConverter.EpochMils_to_LocalDate(end));
        }

        Ledger ledger = Ledger.findById(id);
        if (ledger == null)
            throw new WebApplicationException("No Ledger found", 404);

        /**
         * Excel
         */
        byte[] datar = null;
        String base64 = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] dataPdf = null;
        String base64Pdf = "";
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // START Create PDF document
        Document document = new Document();
        PdfWriter.getInstance(document, pdfStream);
        document.setPageSize(PageSize.A4);

        com.itextpdf.text.Font titleFont = GeneralPDFMethods.pageTitleFont();

        com.itextpdf.text.Font smallBold = GeneralPDFMethods.smallFont();

        com.itextpdf.text.Font tableBold = GeneralPDFMethods.tableHeaderFont();

        Font paperFont = GeneralPDFMethods.tableTextFont();

        document.open();

        GeneralPDFMethods.addBusinessInformation(business, document);

        Paragraph preface = new Paragraph();
        GeneralPDFMethods.addEmptyLine(preface, 1);

        Paragraph titleq = new Paragraph("Ledger History Report", titleFont);
        titleq.setAlignment(Element.ALIGN_CENTER);
        preface.add(titleq);

        GeneralPDFMethods.addEmptyLine(preface, 1);

        // time
        preface.add(new Paragraph("Printed on: "
                + TimeConverter.LocalDateTime_to_String(
                TimeConverter.EpochMils_to_LocalDate(TimeConverter.EpochMillis_Now())
                        .atTime(LocalTime.now())),
                smallBold));

        // period
        preface.add(new Paragraph("For the period starting: "
                + TimeConverter.EpochMils_to_LocalDateTimeString(start)
                + " ending: "
                + TimeConverter.EpochMils_to_LocalDateTimeString(end), paperFont));

        preface.add(new Paragraph("Ledger Name: " + ledger.name, paperFont));
        preface.add(new Paragraph("Ledger Code: " + ledger.code, paperFont));

        GeneralPDFMethods.addEmptyLine(preface, 1);

        document.add(preface);


        // Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        // Create a blank sheet
        XSSFSheet sheet = workbook
                .createSheet("Ledger History for " + ledger.name + " from " + TimeConverter.EpochMils_to_LocalDate(start) + " to " + TimeConverter.EpochMils_to_LocalDate(end));

        XSSFFont headingfont = workbook.createFont();
        headingfont.setBold(true);
        headingfont.setColor(new XSSFColor(Color.RED, new DefaultIndexedColorMap()));

        Row headerrow = sheet.createRow(0);

        Cell heading = headerrow.createCell(0);

        sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
                0, // last row (0-based)
                0, // first column (0-based)
                4 // last column (0-based)
        ));

        XSSFRichTextString title = new XSSFRichTextString(
                "Ledger History for " + ledger.name + " from " + TimeConverter.EpochMils_to_LocalDate(start) + " to " + TimeConverter.EpochMils_to_LocalDate(end));

        XSSFCellStyle tCs = workbook.createCellStyle();
        tCs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tCs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        title.applyFont(headingfont);

        heading.setCellValue(title);

        Row rowheader = sheet.createRow(1);
        rowheader.createCell(0).setCellValue("Date");
        rowheader.createCell(1).setCellValue("Notes");
        rowheader.createCell(2).setCellValue("Branch");
        rowheader.createCell(3).setCellValue("Debit");
        rowheader.createCell(4).setCellValue("Credit");
        rowheader.createCell(5).setCellValue("Balance");

        // START PDF table heading
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{15, 25, 15, 15, 15, 15});

        PdfPCell headerCell = new PdfPCell(new Phrase("Date", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Phrase("Notes", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Phrase("Branch", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Phrase("Debit", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Phrase("Credit", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Phrase("Balance", tableBold));
        headerCell.setBorder(3);
        headerCell.setBackgroundColor(BaseColor.BLACK);
        headerCell.setPadding(4);
        headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(headerCell);

        BigDecimal balance = queries.getBalanceByDateBranch(start, ledger, business);
        List<LedgerHistory> data = new ArrayList<>();


        data.add(new LedgerHistory(Long.parseLong(String.valueOf(0)), BigDecimal.ZERO, BigDecimal.ZERO, start,
                "Balance brought forward", "", "", null, balance));
        saveRowToExcel(sheet, 2, BigDecimal.ZERO, BigDecimal.ZERO, TimeConverter.EpochMils_to_LocalDate(start), "Balance brought forward", "", balance, table, null);

        String qry = "select a.credit, "
                + "a.debit, "
                + "a.date, "
                + "a.notes, "
                + "d.id as journalId "
                + "from JournalEntry a "
                + "inner join Journal d on a.journal_id=d.id "
                + "where a.ledger_id="
                + ledger.id
                + " and a.date >=" + start
                + " and a.date <=" + end
                + " and d.status='"
                + _StatusTypes_Enum.PUBLISHED.toString()
                + "' and a.business_id="
                + business.id
                + " order by a.date, a.id";

        int i = 1;
        try (Connection connection = dataSource.getConnection();
             Statement st = connection.createStatement();
             ResultSet set = st.executeQuery(qry);) {
            while (set.next()) {
                BaseColor backgroundColor = null;
                if (i % 2 != 0) {
                    backgroundColor = new BaseColor(228, 228, 228);
                }

                BigDecimal credit = BigDecimal.ZERO;
                if (set.getBigDecimal("credit") != null) {
                    credit = set.getBigDecimal("credit");
                    balance = balance.add(credit);
                }

                BigDecimal debit = BigDecimal.ZERO;
                if (set.getBigDecimal("debit") != null) {
                    debit = set.getBigDecimal("debit");
                    balance = balance.add(debit);
                }
                data.add(new LedgerHistory(Long.parseLong(String.valueOf(i)), debit.abs(), credit.abs(),
                        set.getLong("date"), set.getString("notes"), set.getString("branchName"), set.getString("currencyName"),
                        set.getLong("journalId"), balance));
                saveRowToExcel(sheet, i + 2, debit.abs(), credit.abs(), TimeConverter.EpochMils_to_LocalDate(set.getLong("date")),
                        set.getString("notes"), set.getString("branchName"), balance, table, backgroundColor);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        document.add(table);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);

        workbook.write(bos);

        workbook.close();
        datar = bos.toByteArray();
        base64 = Base64.encodeBase64String(datar);

        document.close();
        dataPdf = pdfStream.toByteArray();
        base64Pdf = Base64.encodeBase64String(dataPdf);

        LedgerHistoryData dataq = new LedgerHistoryData();
        dataq.data = data;
        dataq.excel = base64;
        dataq.pdf = base64Pdf;

        return dataq;
    }

    private void saveRowToExcel(XSSFSheet sheet, Integer rowNo, BigDecimal debit, BigDecimal credit, LocalDate date, String notes, String branch,
                                BigDecimal balance, PdfPTable table, BaseColor backgroundColor) {
        DecimalFormat df = new DecimalFormat("#,###.##");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Font paperFont = GeneralPDFMethods.tableTextFont();

        Row row = sheet.createRow(rowNo);
        row.createCell(0).setCellValue(formatter.format(date));
        row.createCell(1).setCellValue(notes);
        row.createCell(2).setCellValue(branch);
        row.createCell(3).setCellValue(df.format(debit));
        row.createCell(4).setCellValue(df.format(credit));
        row.createCell(5).setCellValue(df.format(balance));

        PdfPCell d1 = new PdfPCell();
        d1.setPhrase(new Phrase(formatter.format(date), paperFont));
        d1.setBorder(3);
        d1.setPadding(4);
        d1.setBackgroundColor(backgroundColor);
        table.addCell(d1);

        PdfPCell not = new PdfPCell();
        not.setPhrase(new Phrase(notes, paperFont));
        not.setBorder(3);
        not.setPadding(4);
        not.setBackgroundColor(backgroundColor);
        table.addCell(not);

        PdfPCell bran = new PdfPCell();
        bran.setPhrase(new Phrase(branch, paperFont));
        bran.setBorder(3);
        bran.setPadding(4);
        bran.setBackgroundColor(backgroundColor);
        table.addCell(bran);

        PdfPCell deb = new PdfPCell();
        deb.setPhrase(new Phrase(df.format(debit), paperFont));
        deb.setBorder(3);
        deb.setPadding(4);
        deb.setHorizontalAlignment(Element.ALIGN_RIGHT);
        deb.setBackgroundColor(backgroundColor);
        table.addCell(deb);

        PdfPCell cre = new PdfPCell();
        cre.setPhrase(new Phrase(df.format(credit), paperFont));
        cre.setBorder(3);
        cre.setPadding(4);
        cre.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cre.setBackgroundColor(backgroundColor);
        table.addCell(cre);

        PdfPCell bal = new PdfPCell();
        bal.setPhrase(new Phrase(df.format(balance), paperFont));
        bal.setBorder(3);
        bal.setPadding(4);
        bal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        bal.setBackgroundColor(backgroundColor);
        table.addCell(bal);
    }


    public List<Ledger> getChartSubLegders(Business business, Long id) {
        Ledger ledger = Ledger.findById(id);
        if (ledger == null) {
            throw new WebApplicationException("Invalid ledger selected", 404);
        }

        return Ledger.findChartOfAccountsLedgers(business, ledger.code);
    }

}
