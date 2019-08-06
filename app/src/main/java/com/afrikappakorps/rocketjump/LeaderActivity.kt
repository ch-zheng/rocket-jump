package com.afrikappakorps.rocketjump

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.leaderboard_entry.view.*

class LeaderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FirestoreRecyclerAdapter<LeaderEntry, LeaderAdapter.LeaderViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var data: ArrayList<Pair<String, Long>> = ArrayList()
    private lateinit var db: FirebaseFirestore
    private val TAG: String = "LEADER_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader)

        val latestScore = intent.getLongExtra("score", 0)
        Log.d(TAG, "SCORE IS $latestScore")
        val sharedPreferences = getSharedPreferences("com.afrikappakorps.rocketjump.USER_DATA_KEY", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val name = sharedPreferences.getString("NAME_KEY", "unnamed")
        val prevHighScore = sharedPreferences.getLong("SCORE_KEY", 0)

        db = FirebaseFirestore.getInstance()

        if (latestScore > 0 && latestScore > prevHighScore ) {
            editor.putLong("SCORE_KEY", latestScore)
            editor.apply()
            updateScores(name as String, latestScore)
        }


        val query = db.
            collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(20)

        val options = FirestoreRecyclerOptions.Builder<LeaderEntry>()
            .setQuery(query, LeaderEntry::class.java)
            .build()

        viewAdapter = LeaderAdapter(options)
        viewAdapter.startListening()
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.leader_board_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

    override fun onStart() {
        super.onStart()
        viewAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewAdapter.stopListening()
    }

    private fun updateScores(name: String, score: Long) {
        Log.d(TAG, "UPDATING SCORES")
        db.collection("scores")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val data = hashMapOf("name" to name,
                        "score" to score)
                    db.collection("scores").add(data)
                } else {
                    for (d in docs) {
                        db.collection("scores")
                            .document(d.id)
                            .update("score", score)
                    }
                }
            }
    }
}

class LeaderAdapter(options: FirestoreRecyclerOptions<LeaderEntry>)
    : FirestoreRecyclerAdapter<LeaderEntry, LeaderAdapter.LeaderViewHolder>(options) {
    class LeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.name_text
        val scoreText: TextView = view.score_text
        val posText: TextView = view.pos_text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_entry, parent, false)
        return LeaderViewHolder(view)
    }

    override fun onBindViewHolder(p0: LeaderViewHolder, p1: Int, p2: LeaderEntry) {
        val p4 = p1 + 1
        p0.scoreText.text = p2.score.toString()
        p0.posText.text = "#${p4}"
        p0.nameText.text = p2.name

    }

}
