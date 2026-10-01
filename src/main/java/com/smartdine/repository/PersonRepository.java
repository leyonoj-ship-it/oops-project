package com.smartdine.repository;

import com.smartdine.model.Person;
import com.smartdine.model.UserRole;
import com.smartdine.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByEmail(String email);
    Optional<Person> findByGoogleId(String googleId);
    List<Person> findByRole(UserRole role);
    List<Person> findByStatus(UserStatus status);
    List<Person> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
