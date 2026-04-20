package com.example.security.security_app.repositories;

import com.example.security.security_app.entity.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotesRepository extends JpaRepository<Notes, UUID>{

    List<Notes> findByOwnerUsername(String ownerUsername);

}
