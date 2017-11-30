package jwgs.blob

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val namesAdapter: UsersAdapter = UsersAdapter()

    init {
        namesAdapter.setOnUserClickedListener {
            blob().userWasSelected(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUsersListView()
        viewModel().observe(this) {
            updateViewState(it)
        }
    }

    private fun initUsersListView() {
        usersView.layoutManager = LinearLayoutManager(this)
        usersView.adapter = namesAdapter
    }

    private fun updateViewState(it: Loadable<Users, Int>?) {
        when (it) {
            is Loadable.Loading -> {
                showLoading()
            }
            is Loadable.Loaded -> {
                hideLoading()
                namesAdapter.setUsers(it.value)
            }
            else -> {
                hideLoading()
                namesAdapter.setUsers(emptyList())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        blob().namesListWasViewed()
    }

    private fun viewModel() = ViewModelProviders.of(this).get(MainViewModel::class.java)

    private fun hideLoading() {
        loadingSpinner.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        loadingSpinner.visibility = View.VISIBLE
    }

    private fun blob() = (application as BlobApplication)

}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    fun observe(owner: LifecycleOwner, observer: (Loadable<Users, Int>?) -> Unit ) {
        getApplication<BlobApplication>().observeUsers(owner, {
            observer(it)
        })
    }
}