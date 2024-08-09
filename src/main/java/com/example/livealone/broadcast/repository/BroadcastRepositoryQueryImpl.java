package com.example.livealone.broadcast.repository;

import static com.example.livealone.broadcast.entity.QBroadcast.broadcast;

import com.example.livealone.broadcast.dto.QUserBroadcastResponseDto;
import com.example.livealone.broadcast.dto.UserBroadcastResponseDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BroadcastRepositoryQueryImpl implements BroadcastRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<UserBroadcastResponseDto> findAllByUserId(Long userId, int page, int size) {

    PageRequest pageRequest = PageRequest.of(page,size);

    return queryFactory.select(new QUserBroadcastResponseDto(
          broadcast.title,
          broadcast.broadcastStatus,
          broadcast.product.name,
          broadcast.reservation.airTime
        ))
        .from(broadcast)
        .where(broadcast.streamer.id.eq(userId))
        .offset(pageRequest.getOffset())
        .limit(pageRequest.getPageSize())
        .orderBy(new OrderSpecifier<>(Order.DESC, broadcast.id))
        .fetch();

  }

}
