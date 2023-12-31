package com.moabam.api.domain.coupon;

import static com.moabam.global.common.util.GlobalConstant.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import java.time.LocalDate;
import java.util.Optional;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 20)
	private String name;

	@ColumnDefault("1")
	@Column(name = "point", nullable = false)
	private int point;

	@ColumnDefault("1")
	@Column(name = "max_count", nullable = false)
	private int maxCount;

	@ColumnDefault("''")
	@Column(name = "description", length = 50)
	private String description;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "type", nullable = false)
	private CouponType type;

	@Column(name = "start_at", unique = true, nullable = false)
	private LocalDate startAt;

	@Column(name = "open_at", nullable = false)
	private LocalDate openAt;

	@Column(name = "admin_id", updatable = false, nullable = false)
	private Long adminId;

	@Builder
	private Coupon(String name, String description, int point, int maxCount, CouponType type, LocalDate startAt,
		LocalDate openAt, Long adminId) {
		this.name = requireNonNull(name);
		this.point = validatePoint(point);
		this.maxCount = validateStock(maxCount);
		this.description = Optional.ofNullable(description).orElse(BLANK);
		this.type = requireNonNull(type);
		this.startAt = requireNonNull(startAt);
		this.openAt = requireNonNull(openAt);
		this.adminId = requireNonNull(adminId);
	}

	@Override
	public String toString() {
		return String.format("Coupon{startAt=%s, openAt=%s}", startAt, openAt);
	}

	private int validatePoint(int point) {
		if (point < 1) {
			throw new BadRequestException(INVALID_COUPON_POINT);
		}

		return point;
	}

	private int validateStock(int stock) {
		if (stock < 1) {
			throw new BadRequestException(INVALID_COUPON_STOCK);
		}

		return stock;
	}
}
