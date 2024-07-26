package com.example.livealone.delivery.controller;

import com.example.livealone.delivery.dto.DeliveryHistoryResponseDto;
import com.example.livealone.delivery.service.DeliveryService;
import com.example.livealone.global.dto.CommonResponseDto;
import com.example.livealone.global.security.UserDetailsImpl;
import com.example.livealone.user.dto.UserInfoResponseDto;
import com.example.livealone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/user/delivery")
    public ResponseEntity<CommonResponseDto<List<DeliveryHistoryResponseDto>>> getUserDeliveryHistory(/*@AuthenticationPrincipal UserDetailsImpl userDetails*/) {

        //User user = userDetails.getUser();

        List<DeliveryHistoryResponseDto> deliveryHistoryResponseDto = deliveryService.getUserDeliveryHistory(/*,user*/);
        CommonResponseDto<List<DeliveryHistoryResponseDto>> commonResponseDto = CommonResponseDto.<List<DeliveryHistoryResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("User Delivery History inquiry successfully")
                .data(deliveryHistoryResponseDto)
                .build();

        return ResponseEntity.ok().body(commonResponseDto);
    }
}
