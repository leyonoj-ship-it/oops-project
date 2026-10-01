package com.smartdine.repository;

import com.smartdine.model.Establishment;
import com.smartdine.model.EstablishmentType;
import com.smartdine.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {

    List<Establishment> findByType(EstablishmentType type);

    List<Establishment> findByStatus(UserStatus status);

    List<Establishment> findByTypeAndStatus(EstablishmentType type, UserStatus status);

    @Query("SELECT e FROM Establishment e WHERE e.status = :status AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.locationAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Establishment> searchEstablishments(@Param("query") String query, @Param("status") UserStatus status);

    Optional<Establishment> findByOwnerId(Long ownerId);

    long countByType(EstablishmentType type);

    long countByStatus(UserStatus status);
}
