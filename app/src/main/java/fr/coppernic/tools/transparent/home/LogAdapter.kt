package fr.coppernic.tools.transparent.home

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.coppernic.tools.transparent.R

class LogAdapter(private val dataSet: ArrayList<String>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
    class LogViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val tvLog = LayoutInflater.from(parent.context)
                .inflate(R.layout.log_view, parent, false) as View

        return LogViewHolder(tvLog)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.tvLog).text = dataSet[position]
    }
}