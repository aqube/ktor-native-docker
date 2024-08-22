import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.utils.io.core.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class HealthTest {
    @Test
    fun testHealth() = testApplication {
        application {
            module()
        }

        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)

        }
    }
}

class GreetingTest {

    @Test
    fun testGreeting() = testApplication {
        application {
            module()
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertContains(bodyAsText(), "Hello from Ktor 👋")
        }
    }

}

class HashTest {


    private fun hashTest(
        body: Any,
        expected: String = "",
        expectedStatus: HttpStatusCode = HttpStatusCode.OK,
    ) = testApplication {
        application {
            routing {
                hash(20_000)
            }
        }
        client.post("/hash") {
            setBody(body)
        }.apply {
            assertEquals(expectedStatus, status)
            assertEquals(expected, bodyAsText())
        }
    }

    @Test
    fun `hashes string`() =
        hashTest("Hello", "8ca66ee6b2fe4bb928a8e3cd2f508de4119c0895f22e011117e22cf9b13de7ef")

    @Test
    fun `hashes byte array`() =
        hashTest("Hello".toByteArray(), "8ca66ee6b2fe4bb928a8e3cd2f508de4119c0895f22e011117e22cf9b13de7ef")

    @Test
    fun `Rejects long request body`() =
        hashTest("0".repeat(20001).toByteArray(), expectedStatus = HttpStatusCode.PayloadTooLarge)

}