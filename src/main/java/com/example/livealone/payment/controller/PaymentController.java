package com.example.livealone.payment.controller;

import com.example.livealone.global.config.URIConfig;
import com.example.livealone.payment.dto.PaymentRequestDto;
import com.example.livealone.payment.dto.PaymentResponseDto;
import com.example.livealone.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://livealone.shop:3000")
@Slf4j
public class PaymentController {

	private final PaymentService paymentService;
	private final URIConfig uriConfig;

	/**
	 * 카카오페이 결제 준비
	 *
	 * @param requestDto 결제 요청 DTO
	 * @return 결제 응답 DTO
	 */
	@PostMapping("/kakao/process")
	public ResponseEntity<PaymentResponseDto> createKakaoPayReady(@RequestBody PaymentRequestDto requestDto) {
		log.info("Get kakao API : {}",requestDto.getItemName());
		PaymentResponseDto response = paymentService.createKakaoPayReady(requestDto);
		if (response.getStatus().equals("FAILED")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 카카오페이 결제 승인
	 *
	 * @param pgToken 결제 승인 토큰
	 * @param orderId 주문 ID
	 * @param userId  사용자 ID
	 * @return 결제 응답 DTO
	 */
	@PostMapping("/kakao/approve")
	public ResponseEntity<PaymentResponseDto> approveKakaoPayPayment(@RequestParam String pgToken, @RequestParam Long orderId, @RequestParam Long userId) {
		PaymentResponseDto response = paymentService.approveKakaoPayPayment(pgToken, orderId, userId);
		if (response.getStatus().equals("FAILED")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 카카오페이 결제 완료 처리
	 *
	 * @param pgToken 결제 승인 토큰
	 * @param orderId 주문 ID
	 * @param userId  사용자 ID
	 * @return 결제 응답 DTO
	 */
	@GetMapping("/kakao/complete")
	public RedirectView completeKakaoPayment(@RequestParam("pg_token") String pgToken,
											@RequestParam("order_id") Long orderId,
											@RequestParam("user_id") Long userId) {
		PaymentResponseDto response = paymentService.approveKakaoPayPayment(pgToken, orderId, userId);
		RedirectView redirectView = new RedirectView();
		if (response.getStatus().equals("FAILED")) {
			String url = "http://livealone.shop:3000/payment";
			redirectView.setUrl(url);
		} else {
			String url = "http://livealone.shop:3000/completepayment";
			redirectView.setUrl(url);
		}
		return redirectView;
	}

	/**
	 * 결제 상태 조회
	 *
	 * @param paymentId 결제 ID
	 * @return 결제 응답 DTO
	 */
	@GetMapping("/{paymentId}/status")
	public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}


	/**
	 * 토스페이 결제 준비
	 *
	 * @param requestDto 결제 요청 DTO
	 * @return 결제 응답 DTO
	 */
	@PostMapping("/toss/process")
	public ResponseEntity<PaymentResponseDto> createTossPayReady(@RequestBody PaymentRequestDto requestDto) {
		PaymentResponseDto response = paymentService.createTossPayReady(requestDto);
		if (response.getStatus().equals("FAILED")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 토스페이 결제 승인
	 *
	 * @param payToken 결제 고유 토큰
	 * @return 결제 응답 DTO
	 */
	@PostMapping("/toss/approve")
	public ResponseEntity<PaymentResponseDto> approveTossPayPayment(@RequestParam String payToken) {
		PaymentResponseDto response = paymentService.approveTossPayPayment(payToken);
		if (response.getStatus().equals("FAILED")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		return ResponseEntity.ok(response);
	}

	/**
	 * 토스페이 결제 완료 처리
	 *
	 * @param payToken 결제 승인 토큰
	 * @param orderId 주문 ID
	 * @param userId 사용자 ID
	 * @return 결제 응답 DTO
	 */
	@GetMapping("/toss/complete")
	public RedirectView completeTossPayment(@RequestParam("payToken") String payToken,
											@RequestParam("order_id") Long orderId,
											@RequestParam("user_id") Long userId) {
		PaymentResponseDto response = paymentService.approveTossPayPayment(payToken);
		RedirectView redirectView = new RedirectView();
		if (response.getStatus().equals("FAILED")) {
			redirectView.setUrl("http://livealone.shop:3000/payment");
		} else {
			redirectView.setUrl("http://livealone.shop:3000/completepayment");
		}
		return redirectView;
	}

	/**
	 * 사용자 결제 내역 조회
	 *
	 * @param userId 사용자 ID
	 * @return 결제 내역 리스트
	 */
	@GetMapping("/user/{userId}/payment")
	public ResponseEntity<?> getUserPaymentHistory(@PathVariable Long userId) {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}
}
