package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaultRepository extends JpaRepository<Fault, Long> {

    List<Fault> findByStatus(FaultStatus status);

    List<Fault> findByMachineId(Long machineId);
}
