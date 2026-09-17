package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Sort by created_at DESC (terbaru dulu)
    List<Message> findAllByOrderByCreatedAtDesc();

    // Filter by read status
    List<Message> findByReadOrderByCreatedAtDesc(Boolean read);

    // Count unread (buat badge di sidebar admin)
    long countByReadFalse();
}