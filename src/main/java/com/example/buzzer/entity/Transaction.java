package com.example.buzzer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    
    @Column(name = "product_code")
    private String productCode;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "transaction_type")
    private String transactionType; // "PURCHASE" or "RETURN"
    
    @Column(name = "source_file")
    private String sourceFile;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
}