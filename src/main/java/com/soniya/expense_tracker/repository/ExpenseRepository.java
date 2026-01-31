package com.soniya.expense_tracker.repository;

import com.soniya.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(String category);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

}
