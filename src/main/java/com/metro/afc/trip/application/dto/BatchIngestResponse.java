package com.metro.afc.trip.application.dto;

import java.util.List;

public record BatchIngestResponse(
        int total,
        int success,
        int skipped,
        int failed,
        List<String> errors
) {}