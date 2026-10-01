package com.smartdine.repository;

import com.smartdine.model.EstablishmentOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstablishmentOwnerRepository extends JpaRepository<EstablishmentOwner, Long> {
    Optional<EstablishmentOwner> findByEmail(String email);
}
