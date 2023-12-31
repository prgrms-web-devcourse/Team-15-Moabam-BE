package com.moabam.api.domain.room.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.global.config.JpaConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({JpaConfig.class, ParticipantSearchRepository.class})
class ParticipantSearchRepositoryTest {

	@Autowired
	private ParticipantSearchRepository participantSearchRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private RoomRepository roomRepository;

	@DisplayName("인증 시간에 따른 참여자 조회를 성공적으로 했을 때, - List<Participant>")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideRoomAndParticipants")
	@ParameterizedTest
	void participantSearchRepository_findAllByRoomCertifyTime(Room room, List<Participant> participants) {
		// Given
		roomRepository.save(room);
		participantRepository.saveAll(participants);

		// When
		List<Participant> actual = participantSearchRepository.findAllByRoomCertifyTime(10);

		// Then
		assertThat(actual).hasSize(5);
	}
}
