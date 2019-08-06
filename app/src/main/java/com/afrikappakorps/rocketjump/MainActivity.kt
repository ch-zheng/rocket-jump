package com.afrikappakorps.rocketjump

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //findViewById<Button>(R.id.play_button).setOnClickListener { startActivity(Intent(this, GameActivity::class.java)) }
        val intent = Intent(this, LeaderActivity::class.java)
        intent.putExtra("name", "Bob")
        intent.putExtra("score", 12L)
        findViewById<Button>(R.id.play_button).setOnClickListener { startActivity(intent) }
    }
}