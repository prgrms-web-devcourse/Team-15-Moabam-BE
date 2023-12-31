package com.moabam.api.domain.item.repository;

import static com.moabam.api.domain.item.QInventory.*;
import static com.moabam.api.domain.item.QItem.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventorySearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Inventory> findOne(Long memberId, Long itemId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(inventory)
			.where(
				DynamicQuery.generateEq(memberId, inventory.memberId::eq),
				DynamicQuery.generateEq(itemId, inventory.item.id::eq))
			.fetchOne()
		);
	}

	public Optional<Inventory> findDefault(Long memberId, ItemType type) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(inventory)
			.where(
				DynamicQuery.generateEq(memberId, inventory.memberId::eq),
				DynamicQuery.generateEq(type, inventory.item.type::eq),
				inventory.isDefault.isTrue())
			.fetchOne()
		);
	}

	public List<Item> findItems(Long memberId, ItemType type) {
		return jpaQueryFactory.selectFrom(inventory)
			.join(inventory.item, item)
			.where(
				DynamicQuery.generateEq(memberId, inventory.memberId::eq),
				DynamicQuery.generateEq(type, inventory.item.type::eq))
			.orderBy(inventory.createdAt.desc())
			.select(item)
			.fetch();
	}

	public List<Inventory> findDefaultSkin(Long memberId) {
		return jpaQueryFactory.selectFrom(inventory)
			.join(inventory.item)
			.on(inventory.item.id.eq(item.id))
			.where(
				inventory.memberId.eq(memberId),
				inventory.isDefault.isTrue()
			).fetch();
	}

	public List<Inventory> findDefaultInventories(List<Long> memberId, String roomType) {
		return jpaQueryFactory.selectFrom(inventory)
			.join(inventory.item, item).fetchJoin()
			.where(
				inventory.memberId.in(memberId),
				inventory.isDefault.isTrue(),
				inventory.item.type.eq(ItemType.valueOf(roomType))
			)
			.fetch();
	}
}
