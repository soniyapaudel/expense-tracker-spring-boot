package com.soniya.expense_tracker.service;

import com.soniya.expense_tracker.model.Expense;
import org.springframework.stereotype.Service;

import java.util.List;
import java.io.FileOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.util.stream.Stream;

@Service
public class ExpensePdfService {

    public void generateExpensePdf(List<Expense> expenses) {

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("expenses_report.pdf"));

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Expense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Table (4 column)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            addTableHeader(table);
            addRows(table, expenses);

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Amount", "Category", "Description", "Date").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setPhrase(new Phrase(columnTitle));
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(header);
        });
    }

    private void addRows(PdfPTable table, List<Expense> expenses) {
        for (Expense expense : expenses) {
            table.addCell(String.valueOf(expense.getAmount()));
            table.addCell(expense.getCategory());
            table.addCell(expense.getDescription());
            table.addCell(expense.getExpenseDate() != null ? expense.getExpenseDate().toString() : "");

        }
    }

}
