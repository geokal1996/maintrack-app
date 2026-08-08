package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.FaultStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FaultStatusChangeRepository extends JpaRepository<FaultStatusChange, Long> {

    List<FaultStatusChange> findByFaultIdOrderByChangedAtAsc(Long faultId);

    // Ta id ton vlavon pou EXOUN idi istoriko. To xrisimopoiei o DataSeeder gia na
    // symplirosei me MIA erotisi tis palies vlaves pou dimiourgithikan prin yparxei
    // to istoriko - anti gia mia erotisi ana vlavi.
    @Query("select distinct c.fault.id from FaultStatusChange c")
    List<Long> findFaultIdsWithHistory();

    void deleteByFaultId(Long faultId);
}
