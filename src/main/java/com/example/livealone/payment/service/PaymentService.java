package com.example.livealone.payment.service;

import java.util.HashMap;
import java.util.Map;

import com.example.livealone.global.config.URIConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.livealone.order.entity.Order;
import com.example.livealone.order.repository.OrderRepository;
import com.example.livealone.payment.dto.PaymentRequestDto;
import com.example.livealone.payment.dto.PaymentResponseDto;
import com.example.livealone.payment.entity.Payment;
import com.example.livealone.payment.entity.PaymentMethod;
import com.example.livealone.payment.entity.PaymentStatus;
import com.example.livealone.payment.repository.PaymentRepository;
import com.example.livealone.user.entity.User;
import com.example.livealone.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	private final URIConfig uriConfig;

	@Value("${payment.kakao.cid}")
	private String cid;

	@Value("${payment.kakao.secret-key}")
	private String secretKey;

	@Value("${payment.kakao.approval-url}")
	private String approvalUrl;

	@Value("${payment.kakao.cancel-url}")
	private String cancelUrl;

	@Value("${payment.kakao.fail-url}")
	private String failUrl;

	@Value("${payment.toss.client-key}")
	private String tossClientKey;

	@Value("${payment.toss.secret-key}")
	private String tossSecretKey;

	@Value("${payment.toss.ret-url}")
	private String tossRetUrl;

	@Value("${payment.toss.ret-cancel-url}")
	private String tossRetCancelUrl;

	@Value("${payment.toss.result-callback}")
	private String tossResultCallback;

	public PaymentResponseDto createKakaoPayReady(PaymentRequestDto requestDto) {
		// Ready API -> 성공 시 next url 리턴 -> 프론트에서 결제 진행 -> 사용자가 결제 수단 선택 후 비밀번호 인증까지 마치면 결제 대기 화면은 결제 준비 API 요청시
		// 전달 받은 approval_url에 pg_token 파라미터를 붙여 대기화면을 approval_url로 redirect
		// 인증완료 시 응답받은 pg_token과 tid로 최종 승인요청 -> online/v1/payment/approve

		String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

		log.info("Create Kakao pay ready 진입 URI :{} ",url);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "SECRET_KEY " + secretKey);
		headers.set("Content-type", "application/json"); // 500 에러 해결 지점

		HashMap<String, String> params = new HashMap<>();
		params.put("cid", "TC0ONETIME");
		params.put("partner_order_id", String.valueOf(requestDto.getOrderId()));
		params.put("partner_user_id", String.valueOf(requestDto.getUserId()));
		params.put("item_name", requestDto.getItemName());
		params.put("quantity", String.valueOf(requestDto.getOrderQuantity()));
		int totalAmount = requestDto.getAmount() * requestDto.getOrderQuantity();
		params.put("total_amount", String.valueOf(totalAmount));
		params.put("vat_amount", "0");
		params.put("tax_free_amount", "0");
		params.put("approval_url", String.format("http://%s/payment/kakao/complete?order_id=%d&user_id=%d",uriConfig.getServerHost() ,requestDto.getOrderId(), requestDto.getUserId()));
		params.put("cancel_url", cancelUrl);
		params.put("fail_url", failUrl);

		HttpEntity<HashMap<String, String>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

			JsonNode jsonNode = objectMapper.readTree(response.getBody());

			User user = userRepository.findById(requestDto.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + requestDto.getUserId()));

			Order order = orderRepository.findById(requestDto.getOrderId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid order ID: " + requestDto.getOrderId()));

			String tid = jsonNode.get("tid").asText();

			if (paymentRepository.existsByTid(tid)) {
				return PaymentResponseDto.builder()
					.status("FAILED")
					.message("결제 준비 실패: 중복된 TID")
					.build();
			}

			Payment payment = Payment.builder()
				.user(user)
				.order(order)
				.amount(requestDto.getAmount())
				.paymentMethod(PaymentMethod.KAKAO_PAY)
				.status(PaymentStatus.REQUESTED)
				.tid(tid)
				.orderQuantity(requestDto.getOrderQuantity())
				.shippingAddress(requestDto.getShippingAddress())
				.deliveryRequest(requestDto.getDeliveryRequest())
				.build();

			paymentRepository.save(payment);

			return PaymentResponseDto.builder()
				.status("READY")
				.message("결제 준비 완료")
				.paymentId(payment.getId())
				.userId(requestDto.getUserId())
				.orderId(requestDto.getOrderId())
				.amount(requestDto.getAmount())
				.paymentMethod(requestDto.getPaymentMethod())
				.createdAt(payment.getCreatedAt().toString())
				.nextRedirectUrl(jsonNode.get("next_redirect_pc_url").asText())
				.build();

		} catch (Exception e) {
			e.printStackTrace();
			return PaymentResponseDto.builder()
				.status("FAILED")
				.message("결제 준비 실패")
				.build();
		}
	}


	/**
	 * 카카오페이 결제 승인
	 *
	 * @param pgToken 결제 승인 토큰
	 * @param orderId 주문 ID
	 * @param userId  사용자 ID
	 * @return 결제 응답 DTO
	 */

	@Transactional
	public PaymentResponseDto approveKakaoPayPayment(String pgToken, Long orderId, Long userId) {
		String url = "https://open-api.kakaopay.com/online/v1/payment/approve";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "SECRET_KEY " + secretKey);
		headers.set("Content-type", "application/json");

		Payment payment = paymentRepository.findByOrder_Id(orderId);
		if (payment == null) {
			return PaymentResponseDto.builder()
				.status("FAILED")
				.message("Invalid order ID: " + orderId)
				.build();
		}

		Map<String, String> params = new HashMap<>();
		params.put("cid", cid);
		params.put("tid",  payment.getTid());
		params.put("partner_order_id", orderId.toString());
		params.put("partner_user_id", userId.toString());
		params.put("pg_token", pgToken);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			JsonNode jsonNode = objectMapper.readTree(response.getBody());

			payment.updateStatus(PaymentStatus.COMPLETED);

			return PaymentResponseDto.builder()
				.status("COMPLETED")
				.message("결제 완료")
				.paymentId(orderId)
				.userId(userId)
				.orderId(orderId)
				.amount(payment.getAmount())
				.paymentMethod(payment.getPaymentMethod().name())
				.createdAt(jsonNode.get("created_at").asText())
				.updateAt(jsonNode.get("approved_at").asText())
				.build();

		} catch (Exception e) {
			e.printStackTrace();
			return PaymentResponseDto.builder()
				.status("FAILED")
				.message("결제 승인 실패")
				.build();
		}
	}

	/**
	 * 토스페이 결제 준비(생성)
	 *
	 * @param requestDto 결제 요청 DTO
	 * @return 결제 응답 DTO
	 */
	public PaymentResponseDto createTossPayReady(PaymentRequestDto requestDto) {
		String url = "https://pay.toss.im/api/v2/payments";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> params = new HashMap<>();
		params.put("orderNo", requestDto.getOrderId().toString());
		params.put("amount", requestDto.getAmount());
		params.put("amountTaxFree", "0"); // requestDto에서 받아옴
		params.put("productDesc", requestDto.getItemName()); // requestDto에서 받아옴
		params.put("apiKey", tossClientKey);
		params.put("autoExecute", true);
		params.put("callbackVersion", "V2");
		params.put("resultCallback", tossResultCallback);
		params.put("retUrl", tossRetUrl);
		params.put("retCancelUrl", tossRetCancelUrl);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

			JsonNode jsonNode = objectMapper.readTree(response.getBody());

			// 필드 존재 여부 체크
			if (jsonNode.has("payToken") && jsonNode.has("checkoutPage")) {
				User user = userRepository.findById(requestDto.getUserId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + requestDto.getUserId()));

				Order order = orderRepository.findById(requestDto.getOrderId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid order ID: " + requestDto.getOrderId()));

				Payment payment = Payment.builder()
					.user(user)
					.order(order)
					.amount(requestDto.getAmount())
					.paymentMethod(PaymentMethod.TOSS_PAY)
					.status(PaymentStatus.REQUESTED)
					.tid(jsonNode.get("payToken").asText())
					.orderQuantity(requestDto.getOrderQuantity())
					.shippingAddress(requestDto.getShippingAddress())
					.deliveryRequest(requestDto.getDeliveryRequest())
					.build();

				paymentRepository.save(payment);

				return PaymentResponseDto.builder()
					.status("READY")
					.message("결제 준비 완료")
					.paymentId(payment.getId())
					.userId(requestDto.getUserId())
					.orderId(requestDto.getOrderId())
					.amount(requestDto.getAmount())
					.paymentMethod(requestDto.getPaymentMethod())
					.createdAt(payment.getCreatedAt().toString())
					.nextRedirectUrl(jsonNode.get("checkoutPage").asText())
					.build();
			} else {
				return PaymentResponseDto.builder()
					.status("FAILED")
					.message("결제 준비 실패: 필요한 필드가 응답에 없습니다.")
					.build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return PaymentResponseDto.builder()
				.status("FAILED")
				.message("결제 준비 실패")
				.build();
		}
	}

	/**
	 * 토스페이 결제 승인
	 *
	 * @param payToken 결제 고유 토큰
	 * @return 결제 응답 DTO
	 */
	@Transactional
	public PaymentResponseDto approveTossPayPayment(String payToken) {
		String url = "https://pay.toss.im/api/v2/execute";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, String> params = new HashMap<>();
		params.put("apiKey", tossSecretKey);
		params.put("payToken", payToken);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			JsonNode jsonNode = objectMapper.readTree(response.getBody());

			Payment payment = paymentRepository.findByTid(payToken);
			if (payment == null) {
				throw new IllegalArgumentException("Invalid payToken: " + payToken);
			}

			payment.updateStatus(PaymentStatus.COMPLETED);

			return PaymentResponseDto.builder()
				.status("COMPLETED")
				.message("결제 완료")
				.paymentId(payment.getId())
				.userId(payment.getUser().getId())
				.orderId(payment.getOrder().getId())
				.amount(payment.getAmount())
				.paymentMethod(payment.getPaymentMethod().name())
				.createdAt(payment.getCreatedAt().toString())
				.updateAt(jsonNode.get("approved_at").asText())
				.build();

		} catch (Exception e) {
			e.printStackTrace();
			return PaymentResponseDto.builder()
				.status("FAILED")
				.message("결제 승인 실패")
				.build();
		}
	}


	/**
	 * 주문 ID로 tid 조회
	 *
	 * @param orderId 주문 ID
	 * @return tid
	 */
	private String getTidByOrderId(Long orderId) {
		Payment payment = paymentRepository.findByOrder_Id(orderId);
		return payment.getTid();
	}
}