package com.metro.afc.issuance;

import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issuance")
@RequiredArgsConstructor
public class CardIssuanceController {

    private final CardIssuanceUseCase cardIssuanceUseCase;

    @PostMapping("/cards")
    public ResponseEntity<IssueWithTicketResponse> issue(
            @Valid @RequestBody IssueWithTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardIssuanceUseCase.issue(
                        request, SecurityUtils.getCurrentUserId()
                ));
    }
}