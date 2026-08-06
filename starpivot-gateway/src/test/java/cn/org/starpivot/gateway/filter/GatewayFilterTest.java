package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.security.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GatewayFilterTest {

    @Nested
    class InternalPathBlockFilterTest {

        private InternalPathBlockFilter filter;
        private GatewayFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new InternalPathBlockFilter(new ObjectMapper());
            chain = mock(GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @Test
        void shouldBlockInternalPath() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/internal/users").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();

            assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(any());
        }

        @Test
        void shouldAllowNonInternalPath() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/sys/users").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();

            verify(chain).filter(any());
        }

        @Test
        void shouldBlockPathWithInternalInMiddle() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/internal/order/list").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();

            assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        }

        @Test
        void orderShouldBeHighestPlus50() {
            assertEquals(Ordered.HIGHEST_PRECEDENCE + 50, filter.getOrder());
        }
    }

    @Nested
    class StripUserHeadersFilterTest {

        private StripUserHeadersFilter filter;
        private GatewayFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new StripUserHeadersFilter();
            chain = mock(GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @Test
        void shouldStripUserHeadersBeforeForwarding() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/sys/users")
                    .header(SecurityConstants.USER_ID_HEADER, "999")
                    .header(SecurityConstants.USER_NAME_HEADER, "hacker")
                    .header(SecurityConstants.ROLES_HEADER, "admin")
                    .header(SecurityConstants.PERMISSIONS_HEADER, "*:*:*")
                    .header("X-Custom-Header", "keep-me")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();

            verify(chain).filter(argThat(ex -> {
                HttpHeaders headers = ex.getRequest().getHeaders();
                return !headers.containsKey(SecurityConstants.USER_ID_HEADER)
                        && !headers.containsKey(SecurityConstants.USER_NAME_HEADER)
                        && !headers.containsKey(SecurityConstants.ROLES_HEADER)
                        && !headers.containsKey(SecurityConstants.PERMISSIONS_HEADER)
                        && headers.containsKey("X-Custom-Header");
            }));
        }

        @Test
        void shouldPassThroughWhenNoUserHeaders() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/sys/users")
                    .header("Authorization", "Bearer token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();

            verify(chain).filter(any());
        }

        @Test
        void orderShouldBeHighestPlus50() {
            assertEquals(Ordered.HIGHEST_PRECEDENCE + 50, filter.getOrder());
        }
    }
}
