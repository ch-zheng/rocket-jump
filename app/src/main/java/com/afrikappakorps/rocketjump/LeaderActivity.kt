package com.afrikappakorps.rocketjump

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.leaderboard_entry.view.*

class LeaderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val data: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader)

        viewManager = LinearLayoutManager(this)
        viewAdapter = LeaderAdapter(data)

        recyclerView = findViewById<RecyclerView>(R.id.leader_board_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onResume() {
        super.onResume()

    }
}

class LeaderAdapter(private val data: ArrayList<String>) : RecyclerView.Adapter<LeaderAdapter.LeaderViewHolder>() {
    class LeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameText = view.name_text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_entry, parent, false)
        return LeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderViewHolder, position: Int) {
        holder.nameText.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
