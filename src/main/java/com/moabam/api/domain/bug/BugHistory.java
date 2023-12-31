package com.moabam.api.domain.bug;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import com.moabam.api.domain.payment.Payment;
import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "bug_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BugHistory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "bug_type", nullable = false)
	private BugType bugType;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "action_type", nullable = false)
	private BugActionType actionType;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Builder
	private BugHistory(Long memberId, Payment payment, BugType bugType, BugActionType actionType, int quantity) {
		this.memberId = requireNonNull(memberId);
		this.payment = payment;
		this.bugType = requireNonNull(bugType);
		this.actionType = requireNonNull(actionType);
		this.quantity = validateQuantity(quantity);
	}

	private int validateQuantity(int quantity) {
		if (quantity < 0) {
			throw new BadRequestException(INVALID_QUANTITY);
		}

		return quantity;
	}
}
