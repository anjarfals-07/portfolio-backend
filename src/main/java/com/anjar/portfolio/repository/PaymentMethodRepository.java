package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.PaymentMethod;
import com.anjar.portfolio.enums.PaymentMethodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByIsActiveTrueOrderBySortOrderAsc();

    List<PaymentMethod> findAllByOrderBySortOrderAsc();

    List<PaymentMethod> findByTypeAndIsActiveTrueOrderBySortOrderAsc(PaymentMethodType type);

    boolean existsByLabelAndType(String label, PaymentMethodType type);
}