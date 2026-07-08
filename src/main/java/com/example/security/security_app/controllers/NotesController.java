package com.example.security.security_app.controllers;

import com.example.security.security_app.models.NoteRequest;
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

    @PostMapping("/save")
    public NoteVO createNote(@RequestBody NoteRequest noteRequest) {
        return noteService.createNoteForUser(noteRequest);
    }

    @GetMapping("get/all")
    public List<NoteVO> getUserNotes() {
        return noteService.getNotesForUser();
    }

    @PutMapping("update")
    public NoteVO updateNote(@RequestBody NoteRequest noteRequest) {
        return noteService.updateNoteForUser(noteRequest);
    }

    @DeleteMapping("delete/{noteId}")
    public void deleteNote(@PathVariable UUID noteId) {
        noteService.deleteNoteForUser(noteId);
    }
}