package com.moabam.api.application.member;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.domain.member.Badge;
import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.member.BadgeResponse;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.UpdateRanking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberMapper {

	public static Member toMember(Long socialId) {
		return Member.builder()
			.socialId(String.valueOf(socialId))
			.bug(Bug.builder().build())
			.build();
	}

	public static UpdateRanking toUpdateRanking(Member member) {
		return UpdateRanking.builder()
			.rankingInfo(toRankingInfo(member))
			.score(member.getTotalCertifyCount())
			.build();
	}

	public static MemberInfoSearchResponse toMemberInfoSearchResponse(List<MemberInfo> memberInfos) {
		MemberInfo infos = memberInfos.get(0);
		List<BadgeType> badgeTypes = memberInfos.stream()
			.map(MemberInfo::badges)
			.filter(Objects::nonNull)
			.toList();

		return MemberInfoSearchResponse.builder()
			.nickname(infos.nickname())
			.profileImage(infos.profileImage())
			.morningImage(infos.morningImage())
			.nightImage(infos.nightImage())
			.intro(infos.intro())
			.totalCertifyCount(infos.totalCertifyCount())
			.badges(new HashSet<>(badgeTypes))
			.goldenBug(infos.goldenBug())
			.morningBug(infos.morningBug())
			.nightBug(infos.nightBug())
			.build();
	}

	public static MemberInfoResponse toMemberInfoResponse(MemberInfoSearchResponse memberInfoSearchResponse) {
		long certifyCount = memberInfoSearchResponse.totalCertifyCount();

		return MemberInfoResponse.builder()
			.nickname(memberInfoSearchResponse.nickname())
			.profileImage(memberInfoSearchResponse.profileImage())
			.intro(memberInfoSearchResponse.intro())
			.level(certifyCount / LEVEL_DIVISOR)
			.exp(certifyCount % LEVEL_DIVISOR)
			.birds(defaultSkins(memberInfoSearchResponse.morningImage(), memberInfoSearchResponse.nightImage()))
			.badges(badgedNames(memberInfoSearchResponse.badges()))
			.goldenBug(memberInfoSearchResponse.goldenBug())
			.morningBug(memberInfoSearchResponse.morningBug())
			.nightBug(memberInfoSearchResponse.nightBug())
			.build();
	}

	public static Inventory toInventory(Long memberId, Item item) {
		return Inventory.builder()
			.memberId(memberId)
			.item(item)
			.isDefault(true)
			.build();
	}

	public static RankingInfo toRankingInfo(Member member) {
		return RankingInfo.builder()
			.memberId(member.getId())
			.nickname(member.getNickname())
			.image(member.getProfileImage())
			.build();
	}

	public static Badge toBadge(Long memberId, BadgeType badgeType) {
		return Badge.builder()
			.type(badgeType)
			.memberId(memberId)
			.build();
	}

	private static List<BadgeResponse> badgedNames(Set<BadgeType> badgeTypes) {
		return BadgeType.memberBadgeMap(badgeTypes);
	}

	private static Map<String, String> defaultSkins(String morningImage, String nightImage) {
		Map<String, String> birdsSkin = new HashMap<>();
		birdsSkin.put(ItemType.MORNING.name(), morningImage);
		birdsSkin.put(ItemType.NIGHT.name(), nightImage);

		return birdsSkin;
	}
}
