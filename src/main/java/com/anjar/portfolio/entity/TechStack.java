package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tech_stacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== OWNER (Multi-Tenant) =====
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String icon;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}