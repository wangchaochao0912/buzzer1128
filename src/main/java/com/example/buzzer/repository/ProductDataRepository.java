package com.example.buzzer.repository;

import com.example.buzzer.entity.ProductData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductDataRepository extends JpaRepository<ProductData, Long> {
    List<ProductData> findByIsReturn(Boolean isReturn);
    List<ProductData> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<ProductData> findByProductCode(String productCode);
}