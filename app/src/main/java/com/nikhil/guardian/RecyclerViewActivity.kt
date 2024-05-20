@file:Suppress("RedundantSamConstructor")

package com.nikhil.guardian

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class RecyclerViewActivity : AppCompatActivity(), CrimeAdapter.ItemClickListener {

    private lateinit var adapter: CrimeAdapter
    private var crimeList = ArrayList<Crime>()

    // Predefined list of image names
    private val defaultImageNames = listOf("westcott", "clintonsq", "thornden", "destiny", "citymarket", "greenlakes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewrecycler)

        parseJSONData(this) {
            val recyclerView: RecyclerView = findViewById(R.id.recyclerView2)
            val searchView: SearchView = findViewById(R.id.searchView)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.itemAnimator = SlideInUpAnimator()

            adapter = CrimeAdapter(this, crimeList, this, recyclerView)
            recyclerView.adapter = adapter

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.submitList(filterData(newText.orEmpty()))
                    return true
                }
            })
        }
    }

    private fun filterData(query: String): List<Crime> {
        return if (query.isEmpty()) {
            crimeList
        } else {
            crimeList.filter { crime ->
                crime.name.contains(query, ignoreCase = true) || crime.description.contains(query, ignoreCase = true)
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun parseJSONData(context: Context, callback: () -> Unit) {
        val jsonUrl = "https://guardiannikhil.000webhostapp.com"

        // Instantiate the RequestQueue.
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        // Request a JSON response from the provided URL.
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, jsonUrl, null,
            Response.Listener { response ->
                try {
                    // Process the JSON response (assuming it's an array)
                    for (i in 0 until response.length()) {
                        val itemObject = response.getJSONObject(i)
                        val nameValue = itemObject.getString("name")
                        val descriptionValue = itemObject.getString("description")
                        val imageName = defaultImageNames[i % defaultImageNames.size] // Loop through default names if the list is shorter
                        val imageResourceId = getImageResourceId(imageName)
                        val crime = Crime(nameValue, descriptionValue, imageResourceId)
                        crimeList.add(crime)

                        // Print data to Logcat
                        Log.d("CrimeData", "Name: $nameValue, Description: $descriptionValue, ImageName: $imageName, ImageResourceId: $imageResourceId")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Callback after processing the JSON
                    callback.invoke()
                }
            },
            Response.ErrorListener { error ->
                // Handle errors
                error.printStackTrace()
                callback.invoke() // Callback even if there is an error
            }
        )

        // Add the request to the RequestQueue.
        requestQueue.add(jsonArrayRequest)
    }

    @SuppressLint("DiscouragedApi")
    private fun getImageResourceId(imageName: String): Int {
        return resources.getIdentifier(imageName, "drawable", packageName)
    }

    override fun onCrimeClicked(crimeName: String, crimeDescription: String) {
        // Handle click event
    }
}
