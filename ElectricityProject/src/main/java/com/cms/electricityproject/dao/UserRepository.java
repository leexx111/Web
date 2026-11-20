package com.cms.electricityproject.dao;

import com.cms.electricityproject.domain.Heidb;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 提供 Heidb 实体的 Spring Data JPA 基础访问能力。
 */
public interface UserRepository extends JpaRepository<Heidb, Integer> {
}