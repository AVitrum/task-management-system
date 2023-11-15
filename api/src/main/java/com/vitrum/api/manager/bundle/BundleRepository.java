package com.vitrum.api.manager.bundle;

import com.vitrum.api.manager.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BundleRepository extends JpaRepository<Bundle, Long> {

    Optional<Bundle> findByCreatorAndPerformer(Member creator, Member performer);
    Optional<Bundle> findByCreatorAndTitle(Member creator, String title);
    Optional<Bundle> findByPerformerAndTitle(Member performer, String title);

    Boolean existsByCreatorAndTitle(Member creator, String title);
    Boolean existsByPerformerAndTitle(Member performer, String title);
    Boolean existsByCreatorAndPerformer(Member creator, Member performer);
}
