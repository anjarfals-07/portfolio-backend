package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long totalOwners;
    private long totalSuperAdmins;
    private long activeUsers;
    private long totalProjects;
    private long totalBlogPosts;
    private long totalSkills;
    private long totalExperiences;
    private long totalTechStacks;
    private long totalMessages;
}