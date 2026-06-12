package ru.practice.servicedesk.dto.report;

import java.util.Map;

public record DashboardReport(
        long totalTickets,
        long openTickets,
        long resolvedTickets,
        long equipmentTotal,
        long equipmentInRepair,
        Map<String, Long> ticketsByStatus,
        Map<String, Long> equipmentByStatus
) {
}

