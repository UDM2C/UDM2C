package com.example.livealone.reservation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponseDto {
  private String email;
  private LocalDateTime airTime;
  private String code;
}
