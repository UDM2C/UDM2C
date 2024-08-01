package com.example.livealone.broadcast.mapper;

import com.example.livealone.broadcast.dto.BroadcastResponseDto;
import com.example.livealone.broadcast.dto.CreateBroadcastResponseDto;
import com.example.livealone.broadcast.dto.StreamKeyResponseDto;
import com.example.livealone.broadcast.entity.Broadcast;
import com.example.livealone.broadcast.entity.BroadcastCode;
import com.example.livealone.broadcast.entity.BroadcastStatus;
import com.example.livealone.product.entity.Product;
import com.example.livealone.user.entity.User;

public class BroadcastMapper {

  public static Broadcast toBroadcast(String title, User streamer, Product product, BroadcastCode broadcastCode) {
    return Broadcast.builder()
        .title(title)
        .broadcastStatus(BroadcastStatus.ONAIR)
        .broadcastCode(broadcastCode)
        .streamer(streamer)
        .product(product)
        .broadcastCode(broadcastCode)
        .build();
  }

  public static BroadcastResponseDto toBroadcastResponseDto(Broadcast broadcast, Product product) {
    return BroadcastResponseDto.builder()
        .broadcast_title(broadcast.getTitle())
        .product_id(product.getId())
        .product_name(product.getName())
        .product_price(product.getPrice())
        .product_introduction(product.getIntroduction())
        .build();
  }

  public static StreamKeyResponseDto toStreamKeyResponseDto(Boolean isLive, String streamKey) {
    return StreamKeyResponseDto.builder()
        .is_live(isLive)
        .stream_key(streamKey)
        .build();
  }

  public static CreateBroadcastResponseDto toCreateBroadcastResponseDto(Broadcast broadcast) {
    return CreateBroadcastResponseDto.builder()
        .id(broadcast.getId())
        .build();
  }
}
