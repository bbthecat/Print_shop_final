package com.printflow.repository;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only queries for the admin report. Each query takes a time range [start, end).
 */
public interface ReportRepository extends Repository<PrintOrder, Long> {

    interface StatusCount {
        OrderStatus getStatus();

        Long getTotal();
    }

    interface ServiceUsage {
        String getServiceName();

        Long getTotalQuantity();

        BigDecimal getTotalSales();
    }

    @Query("""
            select o.status as status, count(o) as total
            from PrintOrder o
            where o.createdAt >= :start and o.createdAt < :end
            group by o.status
            """)
    List<StatusCount> countOrdersByStatus(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("""
            select sum(p.amount)
            from Payment p
            where p.paymentStatus = :status and p.paidAt >= :start and p.paidAt < :end
            """)
    BigDecimal sumPaymentAmount(@Param("status") PaymentStatus status,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("""
            select s.name as serviceName, sum(i.quantity) as totalQuantity, sum(i.subtotal) as totalSales
            from PrintItem i
            join PrintService s on s.id = i.serviceId
            where i.order.status <> :excluded
              and i.order.createdAt >= :start and i.order.createdAt < :end
            group by s.id, s.name
            order by sum(i.quantity) desc
            """)
    List<ServiceUsage> findTopServices(@Param("excluded") OrderStatus excluded,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);
}
