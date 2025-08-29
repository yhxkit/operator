package com.sample.operator.app.jpa.pgp.repository;

import com.sample.operator.app.jpa.pgp.entity.PgpKeyRing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PgpRepository extends JpaRepository<PgpKeyRing, Integer> {
}
