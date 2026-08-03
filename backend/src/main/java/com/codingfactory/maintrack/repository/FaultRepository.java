package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaultRepository extends JpaRepository<Fault, Long> {

    List<Fault> findByStatus(FaultStatus status);

    List<Fault> findByMachineId(Long machineId);

    // Xrisimopoieitai apo to Excel import gia na entopisoume vlaves pou exoun
    // idi eisaxthei (idios arithmos gnostopoiisis) kai na tis prosperasoume.
    Optional<Fault> findByExternalRef(String externalRef);
}
