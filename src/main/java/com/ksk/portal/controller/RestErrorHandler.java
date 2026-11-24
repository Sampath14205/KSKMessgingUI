package com.ksk.portal.controller;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class RestErrorHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> bad(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage() == null ? "Invalid request" : e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> state(IllegalStateException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage() == null ? "Request cannot be completed" : e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> error(Exception e) {
        return ResponseEntity.status(500).body(Map.of("success", false, "error", "Request could not be completed."));
    }
}
