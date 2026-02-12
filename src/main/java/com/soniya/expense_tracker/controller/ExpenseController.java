package com.soniya.expense_tracker.controller;

import com.soniya.expense_tracker.model.Expense;
import com.soniya.expense_tracker.model.User;
import com.soniya.expense_tracker.repository.ExpenseRepository;
import com.soniya.expense_tracker.repository.UserRepository;
import com.soniya.expense_tracker.security.JwtUtil;
import com.soniya.expense_tracker.service.ExpensePdfService;
import com.soniya.expense_tracker.service.ExpenseCsvService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ExpensePdfService expensePdfService;

    @Autowired
    private ExpenseCsvService expenseCsvService;

    // Helper method to get authenticated user
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String subject = auth.getName(); // This is the subject from JWT
        System.out.println("Debug - Subject from auth: " + subject);

        if (subject == null || subject.isEmpty() || subject.equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        try {
            Long userId = Long.parseLong(subject);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            return user;
        } catch (NumberFormatException e) {
            System.out.println("Debug - Failed to parse userId: " + subject);
            throw new RuntimeException("Invalid user authentication: subject is not a valid Long");
        }
    }

    // CREATE: Add expense for authenticated user
    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        User user = getAuthenticatedUser();
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    // READ ALL: Get all expenses for authenticated user
    @GetMapping
    public List<Expense> getAllExpenses(@RequestParam(required = false) String category) {
        User user = getAuthenticatedUser();

        if (category != null) {
            return expenseRepository.findByUserIdAndCategory(user.getId(), category);
        }
        return expenseRepository.findByUserId(user.getId());
    }

    // READ BY ID: Get single expense (verify ownership)
    @GetMapping("/{id}")
    public Expense getExpenseById(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Verify ownership
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only view your own expenses");
        }
        return expense;
    }

    // UPDATE: Update expense (verify ownership)
    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Expense updatedExpense) {
        User user = getAuthenticatedUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Verify ownership
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only update your own expenses");
        }

        expense.setAmount(updatedExpense.getAmount());
        expense.setCategory(updatedExpense.getCategory());
        expense.setDescription(updatedExpense.getDescription());
        expense.setExpenseDate(updatedExpense.getExpenseDate());

        return expenseRepository.save(expense);
    }

    // PARTIAL UPDATE: Patch expense
    @PatchMapping("/{id}")
    public Expense patchExpense(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        User user = getAuthenticatedUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Verify ownership
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only update your own expenses");
        }

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

    // DELETE: Delete expense (verify ownership)
    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable Long id) {
        User user = getAuthenticatedUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Verify ownership
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only delete your own expenses");
        }

        expenseRepository.deleteById(id);
        return "Expense deleted successfully";
    }

    // Get expenses by date range
    @GetMapping("/by-date")
    public List<Expense> getExpensesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        User user = getAuthenticatedUser();
        return expenseRepository.findByUserIdAndExpenseDateBetween(user.getId(), startDate, endDate);
    }

    // Total expenses by category
    @GetMapping("/report/category")
    public Map<String, Double> getTotalByCategory() {
        User user = getAuthenticatedUser();
        List<Expense> expenses = expenseRepository.findByUserId(user.getId());
        Map<String, Double> report = new HashMap<>();

        for (Expense e : expenses) {
            report.put(e.getCategory(), report.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }
        return report;
    }

    // Total expenses in date range
    @GetMapping("/report/total")
    public Double getTotalExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getAuthenticatedUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(user.getId(), startDate, endDate);
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    // Generate CSV report
    @GetMapping("/report/csv")
    public ResponseEntity<String> generateCsv() {
        User user = getAuthenticatedUser();
        List<Expense> expenses = expenseRepository.findByUserId(user.getId());
        String filePath = expenseCsvService.generateCsv(expenses);
        return ResponseEntity.ok("CSV file saved successfully at " + filePath);
    }

    // Generate PDF report
    @GetMapping("/report/pdf")
    public String generatePdf() {
        User user = getAuthenticatedUser();
        List<Expense> expenses = expenseRepository.findByUserId(user.getId());
        expensePdfService.generateExpensePdf(expenses);
        return "PDF generated successfully!";
    }
}