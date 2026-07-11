package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
