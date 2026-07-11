package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// To JpaRepository mas dinei "dorean" ta vasika: save, findById, findAll, delete...
// Emeis prosthetoume mono ta "eidika" queries pou xreiazomaste.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
