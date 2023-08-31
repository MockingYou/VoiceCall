package com.telpo.voicecall

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.telpo.voicecall.APIs.GetApartments
import com.telpo.voicecall.APIs.MyApi
import com.telpo.voicecall.apartments.Apartment
import com.telpo.voicecall.apartments.ApartmentsAdapter
import com.telpo.voicecall.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val BASE_URL = "http://192.168.222.68:3000"

    private lateinit var binding: ActivityMainBinding
    private lateinit var apartmentsAdapter: ApartmentsAdapter
    private var selectedAp: Apartment? = null
    private var apartments: List<Apartment> =
        listOf(Apartment(1, "Gigel"), Apartment(2, "Marcel"), Apartment(3, "Purcel"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initApartments()
        GlobalScope.launch {
            fetchData().observe(this@MainActivity) { apartments ->
                Log.e("FetchedApartments", "Fetched apartments: $apartments")
            }
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

        binding.apartmentsShow.setOnClickListener {
            binding.apartmentsRecycler.visibility = View.VISIBLE
        }
    }

    suspend fun fetchData(): LiveData<List<GetApartments>> = withContext(Dispatchers.IO) {
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
            if (responseBody != null) {
                for (apartment in responseBody) {
                    Log.e("ApartmentData", "Apartment: $apartment")
                }
            }
            MutableLiveData(responseBody ?: emptyList())
        } else {
            MutableLiveData(emptyList())
        }
    }


}
