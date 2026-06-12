package ru.practice.servicedesk.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practice.servicedesk.dto.equipment.EquipmentRequest;
import ru.practice.servicedesk.dto.equipment.EquipmentResponse;
import ru.practice.servicedesk.entity.Department;
import ru.practice.servicedesk.entity.Equipment;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.exception.ResourceNotFoundException;
import ru.practice.servicedesk.repository.DepartmentRepository;
import ru.practice.servicedesk.repository.EquipmentRepository;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.equipmentRepository = equipmentRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return equipmentRepository.findAll(Sort.by("title")).stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long id) {
        return EquipmentResponse.from(getEquipment(id));
    }

    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = getEquipment(id);
        applyRequest(equipment, request);
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Transactional
    public void delete(Long id) {
        Equipment equipment = getEquipment(id);
        equipmentRepository.delete(equipment);
    }

    private Equipment getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Оборудование не найдено: " + id));
    }

    private void applyRequest(Equipment equipment, EquipmentRequest request) {
        equipment.setInventoryNumber(request.inventoryNumber());
        equipment.setTitle(request.title());
        equipment.setType(request.type());
        equipment.setStatus(request.status());
        equipment.setLocation(request.location());
        equipment.setSerialNumber(request.serialNumber());
        equipment.setPurchaseDate(request.purchaseDate());
        equipment.setComment(request.comment());
        equipment.setAssignedTo(request.assignedToId() == null ? null : userService.findById(request.assignedToId()));
        equipment.setDepartment(request.departmentId() == null ? null : getDepartment(request.departmentId()));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Подразделение не найдено: " + id));
    }
}

