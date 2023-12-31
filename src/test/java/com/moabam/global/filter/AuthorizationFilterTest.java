package com.moabam.global.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.auth.JwtAuthenticationService;
import com.moabam.api.application.auth.JwtProviderService;
import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.filter.AuthorizationFilter;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.support.fixture.JwtProviderFixture;
import com.moabam.support.fixture.PublicClaimFixture;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class AuthorizationFilterTest {

	@InjectMocks
	AuthorizationFilter authorizationFilter;

	@Mock
	HandlerExceptionResolver handlerExceptionResolver;

	@Mock
	JwtAuthenticationService jwtAuthenticationService;

	@Mock
	AuthorizationService authorizationService;

	@DisplayName("토큰 타입이 Bearer가 아니면 예외 발생")
	@ParameterizedTest
	@ValueSource(strings = {
		"Access", "ID", "Self-signed", "Refresh", "Federated"
	})
	void filter_token_type_mismatch(String tokenType) throws ServletException, IOException {
		// Given
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		httpServletRequest.addHeader("token_type", tokenType);
		MockFilterChain mockFilterChain = new MockFilterChain();

		// When + Then
		authorizationFilter.doFilter(httpServletRequest, httpServletResponse, mockFilterChain);

		verify(handlerExceptionResolver, times(1))
			.resolveException(
				eq(httpServletRequest), eq(httpServletResponse),
				eq(null), any(UnauthorizedException.class));
	}

	@DisplayName("필터가 쿠키가 없다면 예외 발생")
	@Test
	void filter_have_any_cookie_error() throws ServletException, IOException {
		// Given
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockFilterChain mockFilterChain = new MockFilterChain();
		httpServletRequest.addHeader("token_type", "Bearer");

		// when
		authorizationFilter.doFilter(httpServletRequest, httpServletResponse, mockFilterChain);

		// then
		verify(handlerExceptionResolver, times(1))
			.resolveException(
				eq(httpServletRequest), eq(httpServletResponse),
				eq(null), any(UnauthorizedException.class));
	}

	@DisplayName("엑세스 토큰이 없어서 예외 발생")
	@Test
	void filter_have_any_access_token_error() throws ServletException, IOException {
		// given
		JwtProviderService jwtProviderService = JwtProviderFixture.jwtProviderService();
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockFilterChain mockFilterChain = new MockFilterChain();
		httpServletRequest.addHeader("token_type", "Bearer");

		// when
		String token = jwtProviderService.provideRefreshToken(Role.USER);
		httpServletRequest.setCookies(new Cookie("refresh_token", token));

		authorizationFilter.doFilter(httpServletRequest, httpServletResponse, mockFilterChain);

		// then
		verify(handlerExceptionResolver, times(1))
			.resolveException(
				eq(httpServletRequest), eq(httpServletResponse),
				eq(null), any(UnauthorizedException.class));
	}

	@DisplayName("refresh 토큰이 없어서 예외 발생")
	@Test
	void filter_have_any_refresh_token_error() throws ServletException, IOException {
		// given
		JwtProviderService jwtProviderService = JwtProviderFixture.jwtProviderService();
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockFilterChain mockFilterChain = new MockFilterChain();

		// when
		String token = jwtProviderService.provideAccessToken(publicClaim);
		httpServletRequest.setCookies(
			new Cookie("token_type", "Bearer"),
			new Cookie("access_token", token));

		when(jwtAuthenticationService.parseClaim(token)).thenReturn(publicClaim);
		when(jwtAuthenticationService.isTokenExpire(token, Role.USER)).thenReturn(true);

		authorizationFilter.doFilter(httpServletRequest, httpServletResponse, mockFilterChain);

		// then
		verify(handlerExceptionResolver, times(1))
			.resolveException(
				eq(httpServletRequest), eq(httpServletResponse),
				eq(null), any(UnauthorizedException.class));
	}

	@DisplayName("새로운 토큰 발급 성공")
	@Test
	void issue_new_token_success() throws ServletException, IOException {
		// given
		JwtProviderService jwtProviderService = JwtProviderFixture.jwtProviderService();
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockFilterChain mockFilterChain = new MockFilterChain();

		// when
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);
		String refreshToken = jwtProviderService.provideRefreshToken(Role.USER);
		httpServletRequest.setCookies(
			new Cookie("token_type", "Bearer"),
			new Cookie("access_token", accessToken),
			new Cookie("refresh_token", refreshToken));

		when(jwtAuthenticationService.parseClaim(accessToken)).thenReturn(publicClaim);
		when(jwtAuthenticationService.isTokenExpire(accessToken, Role.USER)).thenReturn(true);
		when(jwtAuthenticationService.isTokenExpire(refreshToken, Role.USER)).thenReturn(false);

		authorizationFilter.doFilter(httpServletRequest, httpServletResponse, mockFilterChain);

		// then
		verify(authorizationService, times(1))
			.issueServiceToken(httpServletResponse, publicClaim);

		AuthMember authMember = AuthorizationThreadLocal.getAuthMember();
		assertThat(authMember.id()).isEqualTo(1L);
	}
}
