package ru.practice.servicedesk.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.dto.report.DashboardReport;
import ru.practice.servicedesk.repository.EquipmentRepository;
import ru.practice.servicedesk.repository.TicketRepository;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;
    private final EquipmentRepository equipmentRepository;

    public ReportService(TicketRepository ticketRepository, EquipmentRepository equipmentRepository) {
        this.ticketRepository = ticketRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardReport dashboard() {
        Map<String, Long> ticketsByStatus = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            ticketsByStatus.put(status.name(), ticketRepository.countByStatus(status));
        }

        Map<String, Long> equipmentByStatus = new LinkedHashMap<>();
        for (EquipmentStatus status : EquipmentStatus.values()) {
            equipmentByStatus.put(status.name(), equipmentRepository.countByStatus(status));
        }

        long resolved = ticketsByStatus.get(TicketStatus.RESOLVED.name()) + ticketsByStatus.get(TicketStatus.CLOSED.name());
        long totalTickets = ticketRepository.count();
        long openTickets = totalTickets - resolved;

        return new DashboardReport(
                totalTickets,
                openTickets,
                resolved,
                equipmentRepository.count(),
                equipmentByStatus.get(EquipmentStatus.REPAIR.name()),
                ticketsByStatus,
                equipmentByStatus
        );
    }
}

