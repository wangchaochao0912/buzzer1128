package com.example.buzzer.repository;

import com.example.buzzer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySourceFile(String sourceFile);
    
    List<Transaction> findByTransactionType(String transactionType);
    
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.productCode = :productCode")
    List<Transaction> findByProductCode(@Param("productCode") String productCode);
    
    @Query("SELECT t FROM Transaction t WHERE t.productName LIKE %:productName%")
    List<Transaction> findByProductNameContaining(@Param("productName") String productName);
}