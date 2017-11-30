package jwgs.blob

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class UsersAdapter : RecyclerView.Adapter<NamesViewHolder>() {

    private var users: List<User> = emptyList()
    private var onUserClickedListener: ((String) -> Unit)? = null

    fun setOnUserClickedListener(listener: (String)->Unit) {
        onUserClickedListener = listener
    }

    override fun getItemCount(): Int = users.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.name_row, parent, false)
        return NamesViewHolder(view, view.findViewById(R.id.name))
    }

    override fun onBindViewHolder(holder: NamesViewHolder, position: Int) {
        val user = users[position]
        holder.textView.text = user.name
        holder.itemView.setOnClickListener {
            onUserClickedListener?.invoke(user.id)
        }
    }

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }
}

class NamesViewHolder(view: View, val textView: TextView): RecyclerView.ViewHolder(view)

