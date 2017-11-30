package jwgs.blob

import android.app.Application
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context

class BlobApplication: Application() {

    private val userService = UserService()

    /**
     * The live data for the local users, and specific users.
     * These are separate so we can show the problem of
     * different views into the data being out-of-sync.
     * This is similar in function to the problem where an event from
     * one part of iPlayer (watching an episode) can require
     * reloading of other separate data (e.g. the watching list).
     *
     * The re-loading of the user list is dependant on some
     * flags: usersModified and usersLoadedTime as a basic implementation
     * of a 'dirty' flag (when a destructive action is taken in the app
     * and a TTL (to catch changes outside the app).
     */
    private val localUsers: MutableLiveData<Loadable<Users, Int>> = MutableLiveData()
    private val localUserStates = HashMap<String, MutableLiveData<Loadable<User, Int>>>()

    private var usersModified = false
    private var usersLoadedTime: Long = 0

    init {
        localUsers.value = Loadable.Init()
    }

    /**
     * Called to observer the local list of users in the application
     */
    fun observeUsers(owner: LifecycleOwner, observer: (Loadable<Users, Int>) -> Unit ) {
        localUsers.observe(owner, Observer {
            it?.let{ observer(it) }
        })
    }

    /**
     * Called to observe a specific user by ID
     */
    fun observeUser(owner: LifecycleOwner, userId: String, observer:  (Loadable<User, Int>) -> Unit) {
        getUserStateLiveData(userId).observe(owner, Observer{
            it?.let { observer(it) }
        })
    }

    /**
     * Called to indicate a view needs to show the list of users
     * Maybe we should be talking in Activity specifics here, maybe:
     * "onUserListActivityStarted" or something, and then have
     * specific ViewModels for those activities too...
     * UserListActivityViewModel or some such... ActivityViewModels?
     */
    fun namesListWasViewed() {
        if(doesLocalUserListNeedLoading()) {
            localUsers.value = Loadable.Loading()
            val deferredUsers = userService.get()
            deferredUsers.invokeOnCompletion {
                val completed = deferredUsers.getCompleted()
                when (completed) {
                    is Result.Success -> onUsersRetrievedSuccessfully(completed)
                    is Result.Error -> localUsers.value = Loadable.Error(completed.error)
                }
            }
        }
    }

    /**
     * Called when a view needs to show info about a specific user
     */
    fun userWasViewed(userId: String) {
        val userStateLiveData = getUserStateLiveData(userId)
        val deferredUser = userService.get(userId)
        deferredUser.invokeOnCompletion {
            val result = deferredUser.getCompleted()
            userStateLiveData.value = when(result) {
                is Result.Success -> Loadable.Loaded(result.payload)
                is Result.Error -> Loadable.Error(result.error)
            }
        }
    }

    /**
     * Called when a user is selected from a list of users
     * to reveal the details
     */
    fun userWasSelected(context: Context, userId: String) {
        UserDetailActivity.start(context, userId)
    }

    /**
     * Called to request a deletion
     * This sets the usersModified flag so that we know
     * to re-fetch the users list when a view is interested
     * in it.
     */
    fun deleteUser(userId: String) {
        usersModified = true
        val userStateLiveData = getUserStateLiveData(userId)
        val oldState = userStateLiveData.value
        userStateLiveData.value = Loadable.Loading()
        val deleteUser = userService.deleteUser(userId)
        deleteUser.invokeOnCompletion {
            val result = deleteUser.getCompleted()
            userStateLiveData.value = when(result) {
                is Result.Success -> Loadable.Error(404)
                is Result.Error -> oldState
            }
        }
    }

    private fun onUsersRetrievedSuccessfully(completed: Result.Success<Users, Int>) {
        usersLoadedTime = System.currentTimeMillis()
        usersModified = false
        localUsers.value = Loadable.Loaded(completed.payload)
    }

    private fun doesLocalUserListNeedLoading(): Boolean {
        val value = localUsers.value
        val isLoading = value is Loadable.Loading
        val needsLoading = value is Loadable.Init || usersModified || areLocalUsersOld()
        return !isLoading && needsLoading
    }

    private fun areLocalUsersOld(): Boolean {
        val now = System.currentTimeMillis()
        return ( now - usersLoadedTime ) > 10000
    }

    private fun getUserStateLiveData(userId: String): MutableLiveData<Loadable<User, Int>> {
        var userStateData = localUserStates[userId]
        if (userStateData == null) {
            userStateData = MutableLiveData()
            localUserStates[userId] = userStateData
            userStateData.value = Loadable.Init()
        }
        return userStateData
    }

}

