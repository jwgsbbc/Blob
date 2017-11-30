package jwgs.blob

import android.os.Handler
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

class UserService {

    private val handler = Handler()

    private var remoteUsers = listOf(
            User("1", "James"),
            User("2", "Jill"),
            User("3", "Ez"),
            User("4", "Keith"))


    fun get(): Deferred<Result<Users, Int>> {
        val deferred = CompletableDeferred<Result<Users, Int>>()
        handler.postDelayed({
            deferred.complete(Result.Success(remoteUsers))
        }, 3000)
        return deferred
    }

    fun get(userId: String): Deferred<Result<User, Int>> {
        val deferred = CompletableDeferred<Result<User, Int>>()
        handler.postDelayed({
            val user = remoteUsers.find { it.id == userId }
            if(user!=null) {
                deferred.complete(Result.Success(user))
            }
            else {
                deferred.complete(Result.Error(404))
            }
        }, 3000)
        return deferred
    }

    fun deleteUser(userId: String): Deferred<Result<Boolean, Int>> {
        val deferred = CompletableDeferred<Result<Boolean, Int>>()
        handler.postDelayed({
            remoteUsers = remoteUsers.filter { it.id != userId }
            deferred.complete(Result.Success(true))
        },3000)
        return deferred
    }

}