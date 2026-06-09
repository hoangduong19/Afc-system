package com.metro.afc.ticket.application.scheduler;

import com.metro.afc.ticket.application.port.out.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketExpirationScheduler {

    private final TicketRepository ticketRepository;

    @Scheduled(cron = "0 0 0 * * *") // chạy lúc 00:00 mỗi ngày
    @Transactional
    public void expireTickets() {
        int count = ticketRepository.expireOverdueTickets(LocalDate.now());
        log.info("Expired {} tickets", count);
    }
}