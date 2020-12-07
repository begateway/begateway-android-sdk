package com.begateway.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.Foo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHello.text = Foo().label
    }
}