import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Content()
        }
    }
}

@Composable
private fun Content() {
    var usersState by remember { mutableStateOf<List<User>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(usersState) {
        scope.launch {
            usersState = getAllUsers()
        }
    }

    UserList(usersState)

}

@Composable
fun UserList(users: List<User>?) {
    if (users == null) return

    LazyColumn {
        items(users) { user ->
            UserCard(user)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun UserCard(user: User) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource("man.png"),
            contentDescription = "User picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "${user.firstname} ${user.lastname}",
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
                Text(
                    text = user.username,
                    modifier = Modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

suspend fun getAllUsers(): List<User> {
    return client.get("http://localhost:9500/users").body()
}

suspend fun getUserById(): User {
    return client.get("http://localhost:9500/users/1").body()
}

val client = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                useAlternativeNames = false
            },
        )
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTP
            host = "localhost"
            port = 9500
        }
        headers.appendIfNameAbsent("Content-Type", "application/json")
    }
    install(Logging) {
        logger = Logger.DEFAULT
    }
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens("123", "")
            }
            refreshTokens {
                TODO("Not implemented yet!")
            }
            sendWithoutRequest { request ->
                when (request.url.pathSegments.last()) {
                    "login" -> false
                    else -> true
                }
            }
        }
    }
}

@Serializable
data class User(
    val id: Long,
    val username: String,
    val birthDate: LocalDate,
    val firstname: String,
    val lastname: String
)