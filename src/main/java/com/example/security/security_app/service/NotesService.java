package com.example.security.security_app.service;

import com.example.security.security_app.entity.Notes;
import com.example.security.security_app.models.NoteVO;
import com.example.security.security_app.models.UserContext;
import com.example.security.security_app.repositories.NotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotesService {

    private final NotesRepository notesRepository;

    public List<NoteVO> getNotesForUser() {
        String username = UserContext.get().getUserName();
        return notesRepository.findByOwnerUsername(username)
                .stream()
                .map(this::mapToVO)
                .toList();
    }

    public NoteVO createNoteForUser(String content) {
        String username = UserContext.get().getUserName();
        Notes notes = Notes.builder()
                .ownerUsername(username)
                .content(content)
                .build();

        Notes saved = notesRepository.save(notes);
        return mapToVO(saved);
    }

    public NoteVO updateNoteForUser(UUID noteId, String content) {
        String username = UserContext.get().getUserName();
        Notes notes = notesRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!notes.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to update this note");
        }

        notes.setContent(content);
        Notes updated = notesRepository.save(notes);

        return mapToVO(updated);
    }

    public void deleteNoteForUser(UUID noteId) {
        String username = UserContext.get().getUserName();
        Notes notes = notesRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!notes.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this note");
        }

        notesRepository.delete(notes);
    }

    private NoteVO mapToVO(Notes notes) {
        return NoteVO.builder()
                .id(notes.getId())
                .content(notes.getContent())
                .ownerUsername(notes.getOwnerUsername())
                .build();
    }

    private Notes mapToEntity(NoteVO vo) {
        return Notes.builder()
                .id(vo.getId())
                .content(vo.getContent())
                .ownerUsername(vo.getOwnerUsername())
                .build();
    }

}
