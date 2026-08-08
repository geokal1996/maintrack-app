package com.codingfactory.maintrack.repository;

import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FaultRepository extends JpaRepository<Fault, Long> {

    List<Fault> findByStatus(FaultStatus status);

    List<Fault> findByMachineId(Long machineId);

    // Xrisimopoieitai apo to Excel import gia na entopisoume vlaves pou exoun
    // idi eisaxthei (idios arithmos gnostopoiisis) kai na tis prosperasoume.
    Optional<Fault> findByExternalRef(String externalRef);

    // Otan to arxeio DEN exei monadiko kodiko (p.x. to xeirokinito Excel tou
    // proistamenou), anagnorizoume tis diples eggrafes apo ton syndyasmo
    // MIHANI + TITLOS + IMEROMINIA. Arketa asfales: i idia vlavi, stin idia mihani,
    // tin idia mera, einai sxedon sigoura i idia katachorisi.
    boolean existsByMachineIdAndTitleAndCreatedAtBetween(
            Long machineId, String title, LocalDateTime from, LocalDateTime to);

    // Anazitisi me selidopoiisi. Kathe kritirio einai PROAIRETIKO: an dothei null,
    // agnoeitai (to "(:x is null or ...)" einai o tropos na to poume sto JPQL).
    //
    // Giati xreiazetai: prin, i lista efernе OLES tis vlaves kai to filtrarisma
    // gino tan sto frontend. Me 1000+ vlaves apo to SAP auto einai adynaton -
    // i selida kollaei kai to diktyo metaferei perittа megabytes.
    @Query("""
            select f from Fault f
            where (:status is null or f.status = :status)
              and (:machineId is null or f.machine.id = :machineId)
              and (:assignedToUserId is null or f.assignedTo.id = :assignedToUserId)
              and (:q is null
                   or lower(f.title) like lower(concat('%', cast(:q as string), '%'))
                   or lower(f.machine.code) like lower(concat('%', cast(:q as string), '%'))
                   or lower(f.machine.name) like lower(concat('%', cast(:q as string), '%')))
            """)
    Page<Fault> search(@Param("status") FaultStatus status,
                        @Param("machineId") Long machineId,
                        @Param("assignedToUserId") Long assignedToUserId,
                        @Param("q") String q,
                        Pageable pageable);

    // I IDIA anazitisi, alla XORIS selidopoiisi - gia tin eksagogi se Excel.
    // To arxeio prepei na exei OLES tis eggrafes pou tairiazoun sta filtra,
    // oxi mono tin selida pou vlepei i othoni.
    @Query("""
            select f from Fault f
            where (:status is null or f.status = :status)
              and (:machineId is null or f.machine.id = :machineId)
              and (:assignedToUserId is null or f.assignedTo.id = :assignedToUserId)
              and (:q is null
                   or lower(f.title) like lower(concat('%', cast(:q as string), '%'))
                   or lower(f.machine.code) like lower(concat('%', cast(:q as string), '%'))
                   or lower(f.machine.name) like lower(concat('%', cast(:q as string), '%')))
            """)
    List<Fault> searchAll(@Param("status") FaultStatus status,
                           @Param("machineId") Long machineId,
                           @Param("assignedToUserId") Long assignedToUserId,
                           @Param("q") String q,
                           Sort sort);
}
