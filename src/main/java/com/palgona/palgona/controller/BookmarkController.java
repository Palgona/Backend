package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "북마크 추가 api", description = "상품id를 받아서 북마크 추가를 진행한다.")
    public ResponseEntity<Void> createBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.createBookmark(productId, member);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "북마크 삭제 api", description = "상품id를 받아서 북마크 삭제를 진행한다.")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.deleteBookmark(productId, member);

        return ResponseEntity.ok().build();
    }
}
