package com.metro.afc.blacklist.infrastructure.adapter.in;

import com.metro.afc.blacklist.application.dto.AddToBlacklistRequest;
import com.metro.afc.blacklist.application.dto.BlacklistResponse;
import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistFacade blacklistFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('CARD_BLACKLIST')")
    public ResponseEntity<BlacklistResponse> add(
            @Valid @RequestBody AddToBlacklistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blacklistFacade.add(request, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CARD_BLACKLIST')")
    public ResponseEntity<BlacklistResponse> remove(@PathVariable UUID id) {
        return ResponseEntity.ok(
                blacklistFacade.remove(id, SecurityUtils.getCurrentUserId())
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<List<BlacklistResponse>> findAllActive() {
        return ResponseEntity.ok(blacklistFacade.findAllActive());
    }
}