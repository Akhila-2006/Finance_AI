package com.example.financeai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
        Integer userId, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserIdAndTypeAndDateBetweenOrderByDateDesc(
        Integer userId, String type, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.date BETWEEN :startDate AND :endDate")
    Double sumAmountByUserIdAndTypeAndDateBetween(
        Integer userId, String type, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'INCOME'")
    Double sumAllIncomeByUserId(Integer userId);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE'")
    Double sumAllExpensesByUserId(Integer userId);
}