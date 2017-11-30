package jwgs.blob

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.user_detail.*

class UserDetailActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, userId: String) {
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra("USER_ID", userId)
            context.startActivity(intent)
        }
    }

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail)
        this.userId = intent.getStringExtra("USER_ID")
        viewModel().observe(this, userId) {
            updateViewState(it)
        }
    }

    private fun updateViewState(it: Loadable<User, Int>) {
        when (it) {
            is Loadable.Loading -> showLoading()
            is Loadable.Error -> {
                hideLoading()
                name.text = "NOT FOUND!"
            }
            is Loadable.Loaded -> {
                hideLoading()
                name.text = it.value.name
            }
        }
    }

    private fun hideLoading() {
        loadingSpinner.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        loadingSpinner.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        blob().userWasViewed(userId)
    }

    private fun viewModel() = ViewModelProviders.of(this).get(UserDetailViewModel::class.java)

    @Suppress("UNUSED_PARAMETER")
    fun deleteUser(view: View) {
        blob().deleteUser(userId)
    }

    private fun blob() = (application as BlobApplication)

}

class UserDetailViewModel(application: Application): AndroidViewModel(application) {

    private val blobApplication = application as BlobApplication

    fun observe(owner: LifecycleOwner, userId: String, observer: (Loadable<User,Int>)->Unit) {
        blobApplication.observeUser(owner, userId, observer)
    }

}
