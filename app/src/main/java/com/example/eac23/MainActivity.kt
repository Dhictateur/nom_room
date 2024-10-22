package com.example.roomnameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 1. Defineix l'entitat User
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0,
    val name: String
)

// 2. Implementa el DAO UserDao
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): User?
}

// 3. Crea la base de dades Room AppDatabase
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// 4. Defineix la classe MainActivity
class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialitza la base de dades
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "user-database"
        ).build()

        setContent {
            // Pantalla principal
            UserNameScreen()
        }
    }

    @Composable
    fun UserNameScreen() {
        var userName by remember { mutableStateOf(TextFieldValue("Escriu el teu nom")) }

        LaunchedEffect(Unit) {
            // Carrega el nom emmagatzemat al iniciar
            val user = db.userDao().getUser()
            userName = if (user != null) {
                TextFieldValue(user.name)
            } else {
                TextFieldValue("Escriu el teu nom")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Nom") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // Guarda el nom a la base de dades
                CoroutineScope(Dispatchers.IO).launch {
                    db.userDao().insert(User(name = userName.text))
                }
            }) {
                Text("Guardar")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        UserNameScreen()
    }
}
