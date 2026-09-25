package com.anjar.portfolio.entity;

/**
 * Role user dalam sistem multi-tenant.
 *
 * OWNER       — User biasa, punya portfolio sendiri, bisa CRUD data sendiri
 * SUPER_ADMIN — Admin platform, bisa CRUD semua user & semua data
 */
public enum UserRole {

    /**
     * Owner portfolio — CRUD data sendiri (projects, blog, profile, dll)
     */
    OWNER,

    /**
     * Super admin — CRUD semua user + semua portfolio
     */
    SUPER_ADMIN
}