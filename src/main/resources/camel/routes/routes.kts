package camel.routes

import jakarta.mail.internet.MimeMultipart
import org.apache.camel.http.common.HttpMethods

from(
    imaps("imap.qq.com")
        .username("{{mail.username}}")
        .password("{{mail.password}}")
        .unseen(true)
        .delay(6000)
        .peek(true)
        .advanced()
)
    .setBody {
        val mimeMultipart = it.getIn().getBody(MimeMultipart::class.java)
        try {
            if (mimeMultipart.count > 1) {
                val bodyPart = mimeMultipart.getBodyPart(0)
                val content = bodyPart.content.toString()

                val regex = Regex("您账户4248.*")
                val matched = regex.find(content)

                matched?.value?.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }
    }
    .choice()
    .`when`(body().isNotNull)
    .convertBodyTo(String::class.java)
    .transform().body { body ->
        mapOf("content" to body.toString().trim())
    }
    .marshal().json()
    .log("\${body}")
    .to(
        https("{{feishu.api}}")
            .skipRequestHeaders(true)
            .httpMethod(HttpMethods.POST)
    )
    .end()
