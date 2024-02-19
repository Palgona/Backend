package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> createBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.createBookmark(productId, member);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.deleteBookmark(productId, member);

        return ResponseEntity.ok().build();
    }
}