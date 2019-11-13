package de.adorsys.aspsp.xs2a.connector.oauth;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.rest.client.AuthRequestInterceptor;
import de.adorsys.ledgers.rest.client.UserMgmtRestClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenValidationServiceTest {
    @Mock
    private AuthRequestInterceptor authRequestInterceptor;
    @Mock
    private UserMgmtRestClient userMgmtRestClient;

    @InjectMocks
    private TokenValidationService tokenValidationService;

    @Test
    public void validate_withValidToken_shouldReturnValidToken() {
        // Given
        String token = "some token";
        BearerTokenTO expected = new BearerTokenTO();
        when(userMgmtRestClient.validate(token))
                .thenReturn(ResponseEntity.ok(expected));

        // When
        BearerTokenTO actual = tokenValidationService.validate(token);

        // Then
        assertEquals(expected, actual);

        InOrder inOrder = Mockito.inOrder(authRequestInterceptor, userMgmtRestClient);
        inOrder.verify(authRequestInterceptor).setAccessToken(token);
        inOrder.verify(userMgmtRestClient).validate(token);
        inOrder.verify(authRequestInterceptor).setAccessToken(null);
    }

    @Test
    public void validate_onFeignException_shouldReturnNull() {
        // Given
        String token = "some token";
        Response feignResponse = Response.builder()
                                         .status(HttpStatus.BAD_REQUEST.value())
                                         .request(Request.create(Request.HttpMethod.GET, "", Collections.emptyMap(), null))
                                         .headers(Collections.emptyMap())
                                         .build();
        FeignException feignException = FeignException.errorStatus("some message", feignResponse);
        when(userMgmtRestClient.validate(token))
                .thenThrow(feignException);

        // When
        BearerTokenTO actual = tokenValidationService.validate(token);

        // Then
        assertNull(actual);

        InOrder inOrder = Mockito.inOrder(authRequestInterceptor, userMgmtRestClient);
        inOrder.verify(authRequestInterceptor).setAccessToken(token);
        inOrder.verify(userMgmtRestClient).validate(token);
        inOrder.verify(authRequestInterceptor).setAccessToken(null);
    }
}