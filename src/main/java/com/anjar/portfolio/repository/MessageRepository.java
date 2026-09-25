package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ===== MULTI-TENANT (BARU) =====
    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Message> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, Boolean read);

    long countByUserIdAndReadFalse(Long userId);

    // ===== LAMA =====
    List<Message> findAllByOrderByCreatedAtDesc();
    List<Message> findByReadOrderByCreatedAtDesc(Boolean read);
    long countByReadFalse();
}