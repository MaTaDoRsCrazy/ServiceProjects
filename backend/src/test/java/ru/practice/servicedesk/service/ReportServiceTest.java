package ru.practice.servicedesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.dto.report.DashboardReport;
import ru.practice.servicedesk.repository.EquipmentRepository;
import ru.practice.servicedesk.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void dashboardAggregatesTicketsAndEquipment() {
        when(ticketRepository.count()).thenReturn(5L);
        when(ticketRepository.countByStatus(TicketStatus.NEW)).thenReturn(2L);
        when(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)).thenReturn(1L);
        when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(1L);
        when(ticketRepository.countByStatus(TicketStatus.CLOSED)).thenReturn(1L);
        when(equipmentRepository.count()).thenReturn(10L);
        when(equipmentRepository.countByStatus(EquipmentStatus.REPAIR)).thenReturn(3L);

        DashboardReport report = reportService.dashboard();

        assertThat(report.totalTickets()).isEqualTo(5);
        assertThat(report.openTickets()).isEqualTo(3);
        assertThat(report.resolvedTickets()).isEqualTo(2);
        assertThat(report.equipmentTotal()).isEqualTo(10);
        assertThat(report.equipmentInRepair()).isEqualTo(3);
    }
}

