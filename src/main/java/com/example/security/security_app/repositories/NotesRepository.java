package com.example.security.security_app.repositories;

import com.example.security.security_app.entity.Notes;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotesRepository extends JpaRepository<Notes, UUID>{

    @Query("SELECT n FROM Notes n WHERE n.ownerUsername = :ownerUsername AND n.isActive = true")
    List<Notes> findActiveNotesByOwner(@Param("ownerUsername") String ownerUsername);

    @Query("SELECT n FROM Notes n WHERE n.id = :noteId AND n.isActive = true")
    Optional<Notes> findActiveNoteForUpdates(@Param("noteId") UUID noteId);

}
