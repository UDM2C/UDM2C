package com.example.livealone.broadcast.entity;

import com.example.livealone.global.entity.Timestamp;
import com.example.livealone.product.entity.Product;
import com.example.livealone.reservation.entity.Reservations;
import com.example.livealone.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "broadcasts")
public class Broadcast extends Timestamp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	private BroadcastStatus broadcastStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User streamer;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "air_time", referencedColumnName = "air_time", nullable = false, unique = true)
	private Reservations reservation;

	@Builder
	public Broadcast(String title, BroadcastStatus broadcastStatus, User streamer, Product product, Reservations reservation) {
		this.title = title;
		this.broadcastStatus = broadcastStatus;
		this.streamer = streamer;
		this.product = product;
		this.reservation = reservation;
	}

	public Broadcast updateBroadcast(String title, User streamer, Product product) {
		this.title = title;
		this.broadcastStatus = BroadcastStatus.ONAIR;
		this.streamer = streamer;
		this.product = product;
		return this;
	}

	public Broadcast closeBroadcast() {
		this.broadcastStatus = BroadcastStatus.CLOSE;
		return this;
	}

}
