package com.bobooi.watch.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author bobo
 * @date 2021/3/31
 */

public interface DataRepository<Entity, Id> extends JpaRepository<Entity, Id> {
}
