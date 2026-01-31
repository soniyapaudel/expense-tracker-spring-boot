package com.soniya.expense_tracker.controller;

import com.soniya.expense_tracker.model.Expense;
import com.soniya.expense_tracker.repository.ExpenseRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import java.io.StringWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.PrintWriter;
import java.io.FileWriter;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired

    private ExpenseRepository expenseRepository;

    // create Post

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        return expenseRepository.save(expense);
    }

    // Read all (Get)
    @GetMapping
    public List<Expense> getAllExpenses(@RequestParam(required = false) String category) {
        if (category != null) {
            return expenseRepository.findByCategory(category);
        }
        return expenseRepository.findAll();
    }

    // Read by id(GET)

    @GetMapping("/{id}")
    public Expense getExpenseById(@PathVariable Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    // Update(PUT)
    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Expense updatedExpense) {
        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense != null) {
            expense.setAmount(updatedExpense.getAmount());
            expense.setCategory(updatedExpense.getCategory());
            expense.setDescription(updatedExpense.getDescription());
            return expenseRepository.save(expense);

        }

        return null;

    }

    // Partial Update
    @PatchMapping("/{id}")
    public Expense patchExpense(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense == null) {
            return null;
        }

        // Update only provided fields
        if (updates.containsKey("amount")) {
            expense.setAmount(Double.valueOf(updates.get("amount").toString()));

        }

        if (updates.containsKey("category")) {
            expense.setCategory(updates.get("category").toString());
        }

        if (updates.containsKey("description")) {
            expense.setDescription(updates.get("description").toString());
        }

        if (updates.containsKey("expenseDate")) {

            expense.setExpenseDate(LocalDate.parse(updates.get("expenseDate").toString()));
        }

        return expenseRepository.save(expense);
    }

    // Delete (DELETE)

    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable Long id) {
        expenseRepository.deleteById(id);
        return "Expense deleted successfully";
    }

    @GetMapping("/by-date")
    public List<Expense> getExpensesByDateRange(@RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);

    }

    // Total expenses by category
    @GetMapping("/report/category")
    public Map<String, Double> getTotalByCategory() {
        List<Expense> expenses = expenseRepository.findAll();
        Map<String, Double> report = new HashMap<>();

        for (Expense e : expenses) {
            report.put(e.getCategory(), report.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }
        return report;
    }

    // Total expenses in date range
    @GetMapping("/report/total")
    public Double getTotalExpenses(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        return total;
    }

    // Generate all report in csv file
    @GetMapping("/report/csv")
    public ResponseEntity<String> generateCsvAndSave() {

        List<Expense> expenses = expenseRepository.findAll();

        String filePath = "expenses_report.csv";
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

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Error generating CSV:" + ex.getMessage());
        }

        return ResponseEntity.ok("CSV file saved successfullly at " + filePath);

    }

}