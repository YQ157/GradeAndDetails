package com.example.gradeanddetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GradeAdapter(var data: List<Grade>): RecyclerView.Adapter<GradeAdapter.ViewHolder>() {
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val name: TextView = view.findViewById(R.id.name)
        val score: TextView = view.findViewById(R.id.score)
        val detail: TextView = view.findViewById(R.id.detail)
        val credits: TextView = view.findViewById(R.id.credits)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_grade,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grade = data[position]
        holder.name.text = grade.name
        holder.score.text = grade.score
        if ((grade.detail != null) and grade.detail!!.isNotEmpty()){
            holder.detail.text = grade.detail!!.split(";").joinToString("\n")
        }else{
            holder.detail.text = "-"
        }
        holder.credits.text = grade.credits
    }
}