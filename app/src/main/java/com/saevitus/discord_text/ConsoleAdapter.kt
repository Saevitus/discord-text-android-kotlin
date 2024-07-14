package com.saevitus.discord_text

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class ConsoleAdapter(private val consoleLines: MutableList<ConsoleLine>) :
    RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder>() {

  class ConsoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.consoleLineText)
    val timestampView: TextView = itemView.findViewById(R.id.consoleLineTimestamp) // Optional
    val levelView: TextView = itemView.findViewById(R.id.consoleLineLevel) // Optional
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
    val itemView =
        LayoutInflater.from(parent.context).inflate(R.layout.console_line_item, parent, false)
    return ConsoleViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
    val line = consoleLines[position]
    holder.textView.text = line.text

    // Optional: Display timestamp and level
    holder.timestampView.text =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(line.timestamp))
    holder.levelView.text = line.level
  }

  override fun getItemCount(): Int = consoleLines.size

  fun addLine(line: ConsoleLine) {
    consoleLines.add(line)
    notifyItemInserted(consoleLines.size - 1)
  }

  fun clearLines() {
    consoleLines.clear()
    notifyDataSetChanged() // Notify the adapter that the data set has changed
  }
}
