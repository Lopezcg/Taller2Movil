package com.example.taller2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imagebutton.setOnClickListener{
            startActivity(Intent(this,ContactosActivity::class.java))
        }
        binding.imagebutton2.setOnClickListener{
            startActivity(Intent(this,CamaraActivity::class.java))
        }
        binding.imagebutton3.setOnClickListener{
            startActivity(Intent(this,MapsActivity::class.java))
        }

    }
}