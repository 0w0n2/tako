package com.bukadong.tcg.api.delivery.repository;

import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Delivery d set d.status = :to where d.status = :from")
    int bulkUpdateStatus(@Param("from") DeliveryStatus from, @Param("to") DeliveryStatus to);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByStatusAndUpdatedAtBefore(DeliveryStatus status, LocalDateTime before);
}
