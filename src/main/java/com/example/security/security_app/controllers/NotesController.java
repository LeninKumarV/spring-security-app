package com.example.security.security_app.controllers;

import com.example.security.security_app.models.NoteVO;
import com.example.security.security_app.service.NotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NotesController {

    private final NotesService noteService;

    @PostMapping
    public NoteVO createNote(@RequestBody String content) {
        return noteService.createNoteForUser(content);
    }

    @GetMapping
    public List<NoteVO> getUserNotes() {
        return noteService.getNotesForUser();
    }

    @PutMapping("/{noteId}")
    public NoteVO updateNote(@PathVariable UUID noteId,
                           @RequestBody String content) {
        return noteService.updateNoteForUser(noteId, content);
    }

    @DeleteMapping("/{noteId}")
    public void deleteNote(@PathVariable UUID noteId) {
        noteService.deleteNoteForUser(noteId);
    }
}