package com.example.bold_app.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat.getColor
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.bold_app.model.ChecklistItem
import com.example.bold_app.R
import com.example.bold_app.viewmodel.MainActivityViewModel


class GoalAdapter(private val goalList: List<ChecklistItem>,
                  private val viewModel: MainActivityViewModel
): RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {


    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loading: RelativeLayout = itemView.findViewById<RelativeLayout>(R.id.loadingPanel)
        val completion: ImageView = itemView.findViewById<ImageView>(R.id.completion)
        val title: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val reps: TextView = itemView.findViewById(R.id.tvTaskReps)
        val description: TextView = itemView.findViewById(R.id.tvTaskDesc)
        val link: WebView = itemView.findViewById<WebView>(R.id.webLink)
        val done: ImageButton = itemView.findViewById<ImageButton>(R.id.imageDone)
        val info: ImageButton = itemView.findViewById<ImageButton>(R.id.imageInfo)
        val rv: ConstraintLayout = itemView.findViewById<ConstraintLayout>(R.id.rvTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_goal_item, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val item = goalList[position]

        holder.title.text = item.title
        holder.reps.text = item.reps
        holder.description.text = item.description

        if (item.isDone) {
            holder.done.setImageResource(R.drawable.ic_btn_restore)
            holder.info.visibility = View.GONE
            holder.reps.visibility = View.GONE
            holder.description.visibility = View.GONE
            holder.link.visibility = View.GONE
            holder.completion.visibility = View.VISIBLE
            holder.title.setTextColor(ContextCompat.getColor(holder.title.context, R.color.blue))
            holder.rv.setBackgroundColor(ContextCompat.getColor(holder.title.context, R.color.white))
        } else {
            holder.done.setImageResource(R.drawable.ic_btn_done)
            holder.info.visibility = View.VISIBLE
            holder.reps.visibility = View.VISIBLE
            holder.description.visibility = View.GONE
            holder.completion.visibility = View.GONE
            holder.link.visibility = View.GONE
            holder.title.setTextColor(ContextCompat.getColor(holder.title.context, R.color.black))
            holder.rv.setBackgroundColor(ContextCompat.getColor(holder.title.context, R.color.light_orange))
        }

        holder.done.setOnClickListener {
            item.isDone = !item.isDone
            notifyItemChanged(position)
            viewModel.updateChecklistItem(item.id, item.isDone)
        }

        holder.info.setOnClickListener {
            if(holder.description.visibility == View.GONE){
                holder.loading.visibility = View.VISIBLE
                if (item.link != null && item.link.isNotEmpty() && item.link != "") {
                    holder.link.setBackgroundColor(ContextCompat
                        .getColor(holder.link.context, R.color.orange))
                    holder.link.apply {
                        settings.javaScriptEnabled = true
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                holder.loading.visibility = View.GONE
                            }
                        }
                    }
                    holder.link.loadData(item.link, "text/html", "UTF-8")
                    holder.link.visibility = View.VISIBLE
                } else {
                    holder.link.visibility = View.GONE
                    holder.loading.visibility = View.GONE
                }
                holder.description.visibility = View.VISIBLE
                holder.info.setImageResource(R.drawable.ic_btn_minimize)
            } else {
                holder.description.visibility = View.GONE
                holder.link.visibility = View.GONE
                holder.info.setImageResource(R.drawable.ic_btn_info)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = goalList.size
}