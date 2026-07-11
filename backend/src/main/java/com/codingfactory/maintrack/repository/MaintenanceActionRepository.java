package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.MaintenanceAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceActionRepository extends JpaRepository<MaintenanceAction, Long> {

    List<MaintenanceAction> findByFaultId(Long faultId);
}
