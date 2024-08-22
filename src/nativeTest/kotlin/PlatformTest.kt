import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformTest {

    @Test
    fun testPlatform() =  testApplication {
        application {
            module()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        client.get("/platform").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(PlatformResponse("Native"), body())
        }
    }

}
