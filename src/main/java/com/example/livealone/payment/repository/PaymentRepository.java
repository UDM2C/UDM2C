package com.example.livealone.payment.repository;

import com.example.livealone.payment.entity.Payment;
import com.example.livealone.payment.entity.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

	Payment findByOrder_Id(Long orderId);

	boolean existsByTid(String tid);

	Payment findByTid(String paymentKey);
}
