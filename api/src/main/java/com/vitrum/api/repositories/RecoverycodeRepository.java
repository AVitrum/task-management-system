package com.vitrum.api.repositories;

import com.vitrum.api.data.models.User;
import com.vitrum.api.data.submodels.Recoverycode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecoverycodeRepository extends JpaRepository<Recoverycode, Long> {

    Optional<Recoverycode> findByUser(User user);
}
