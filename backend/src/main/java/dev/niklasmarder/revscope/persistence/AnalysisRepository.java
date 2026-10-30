package dev.niklasmarder.revscope.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for stored analyses. */
public interface AnalysisRepository extends JpaRepository<AnalysisEntity, String> {
}
