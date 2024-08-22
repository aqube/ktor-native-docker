import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import kotlinx.html.body
import kotlinx.html.h1
import org.kotlincrypto.hash.sha3.SHA3_256

fun Route.indexRoute() = get("/") {
    call.respondHtml {
        body {
            h1 { +"Hello from Ktor 👋" }
        }
    }
}

/**
 * A platform specific function.
 * @return name of the current platform
 */
expect fun platform(): String

fun Route.platformRoute() = get("/platform") {
    call.respond(PlatformResponse(platform()))
}

@OptIn(ExperimentalStdlibApi::class)
fun Route.hash(limit: Int = 20_000_000) = post("/hash") {
    if ((call.request.contentLength() ?: 0) > limit) {
        call.respond(HttpStatusCode.PayloadTooLarge)
    } else {
        val bytes = call.receiveChannel().toByteArray(limit)
        val hash = SHA3_256().digest(bytes).toHexString()
        call.respond(hash)
    }
}