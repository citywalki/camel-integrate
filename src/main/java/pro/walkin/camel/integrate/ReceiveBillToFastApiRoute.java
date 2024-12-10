package pro.walkin.camel.integrate;

import jakarta.inject.Singleton;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.ws.rs.HttpMethod;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                .password(password)
                .unseen(true)
                .delay(6000)
                .peek(true)
                .advanced())
                .setBody(exchange -> {
                    MimeMultipart mimeMultipart = exchange.getIn().getBody(MimeMultipart.class);
                    try {
                        if (mimeMultipart.getCount() > 1) {
                            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
                            String content = String.valueOf(bodyPart.getContent());

                            // 正则表达式：匹配以"您账户4248"开头，并在遇到换行符之前结束的所有字符
                            String regex = "您账户4248.*";

                            // 编译正则表达式
                            Pattern pattern = Pattern.compile(regex);
                            // 创建匹配器
                            Matcher matcher = pattern.matcher(content);

                            // 查找匹配项
                            if (matcher.find()) {
                                String matchedString = matcher.group();
                                return matchedString.trim();
                            }

                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    return null;
                })
                .choice()
                .when(body().isNotNull())
                .convertBodyTo(String.class)
                .transform().body(o -> Map.of("content", o.toString().trim()))
                .marshal().json()
                .log("${body}").to(https(feisuApi).skipRequestHeaders(true).httpMethod(HttpMethod.POST))
                .end();
    }
}
