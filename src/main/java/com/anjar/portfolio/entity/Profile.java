package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== OWNER (Multi-Tenant) =====
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(length = 200)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "short_bio", length = 500)
    private String shortBio;

    @Column(length = 200)
    private String email;

    @Column(length = 200)
    private String location;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    @Column(name = "available_for_work")
    @Builder.Default
    private Boolean availableForWork = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, String>> socials;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}