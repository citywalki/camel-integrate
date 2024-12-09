package pro.walkin.camel.integrate;

import jakarta.inject.Singleton;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.https;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.imaps;

@Singleton
public class ReceiveBillToFastApiRoute extends RouteBuilder {
    @ConfigProperty(name = "mail.username")
    String username;

    @ConfigProperty(name = "mail.password")
    String password;

    @ConfigProperty(name = "feishu.api")
    String feisuApi;

    @Override
    public void configure() throws Exception {
        from(imaps("imap.qq.com")
                .username(username)
                .password(password).unseen(true).delay(6000).advanced()).convertBodyTo(String.class)
                .transform().body(o -> Map.of("content", o.toString().trim())).marshal().json()
                .log("${body}").to(https(feisuApi).skipRequestHeaders(true).httpMethod(jakarta.ws.rs.HttpMethod.POST));
    }
}
