package com.moabam.api.dto.room;

import jakarta.validation.constraints.Pattern;

public record EnterRoomRequest(
	@Pattern(regexp = "^(|[0-9]{4,8})$") String password
) {

}
