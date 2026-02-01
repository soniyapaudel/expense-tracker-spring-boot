package com.soniya.expense_tracker.service;

import com.soniya.expense_tracker.model.Expense;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExpenseCsvService {

    public String generateCsv(List<Expense> expenses) {

        String filePath = "expense_report.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            // csv header
            writer.println("Id, Amount, Category, Description, ExpenseDate");
            // csv rows
            for (Expense e : expenses) {
                String category = e.getCategory() != null ? e.getCategory() : "";
                String description = e.getDescription() != null ? e.getDescription().replaceAll(",", " ") : "";
                String date = e.getExpenseDate() != null ? e.getExpenseDate().toString() : "";

                writer.printf("%d,%.2f,%s,%s,%s%n",
                        e.getId(),
                        e.getAmount(),
                        category,
                        description,
                        date);
            }
            writer.flush();
            return filePath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }

    }

}
