package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.notification.NotificationService;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final FcmService fcmService;

	@GetMapping("/rooms/{roomId}/members/{memberId}")
	@ResponseStatus(HttpStatus.OK)
	public void sendKnock(
		@PathVariable("roomId") Long roomId,
		@PathVariable("memberId") Long memberId,
		@Auth AuthMember authMember
	) {
		notificationService.sendKnock(roomId, memberId, authMember.id());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public void createFcmToken(@RequestParam("fcmToken") String fcmToken, @Auth AuthMember authMember) {
		fcmService.createToken(fcmToken, authMember.id());
	}
}
