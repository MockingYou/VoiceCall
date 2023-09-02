package com.telpo.voicecall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.telpo.voicecall.APIs.GetApartments
import com.telpo.voicecall.APIs.MyApi
import com.telpo.voicecall.apartments.Apartment
import com.telpo.voicecall.apartments.ApartmentsAdapter
import com.telpo.voicecall.databinding.ActivityMainBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val BASE_URL = "http://10.0.2.2:3000"

    private lateinit var binding: ActivityMainBinding
    private lateinit var apartmentsAdapter: ApartmentsAdapter
    private var selectedAp: Apartment? = null
    private var apartments: List<Apartment> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initApartments()

        binding.apartmentsShow.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val fetchedApartments = fetchData().await()
                Log.e("FetchedApartments", "Fetched apartments: $fetchedApartments")
                apartments = fetchedApartments.map {
                    Apartment(it.apnumber, it.owner)
                }
                apartmentsAdapter.submitList(apartments)
            }
            binding.apartmentsRecycler.visibility = View.VISIBLE
        }
        binding.phoneCall.setOnClickListener {
            val intent = Intent(this, VoiceCallActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initApartments() {
        apartmentsAdapter = ApartmentsAdapter { apartment ->
            selectedAp = apartment
            binding.chooseApartmentText.text = apartment.apartment.toString()
            binding.apartmentsRecycler.visibility = View.GONE
        }
        binding.apartmentsRecycler.adapter = apartmentsAdapter
        apartmentsAdapter.submitList(apartments)

//        binding.apartmentsShow.setOnClickListener {
//        }
    }

    suspend fun fetchData(): Deferred<List<GetApartments>> = GlobalScope.async(Dispatchers.IO) {
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val apiService = retrofit.create(MyApi::class.java)

            val response = apiService.getApartments()
            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Handle the exception here, such as logging or showing an error message
            Log.e("FetchDataException", "Error fetching data: ${e.message}")
            emptyList()
        }
    }




}
