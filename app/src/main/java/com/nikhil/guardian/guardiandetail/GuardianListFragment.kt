@file:Suppress("DEPRECATION", "RedundantSamConstructor", "OVERRIDE_DEPRECATION", "ReplaceGetOrSet")

package com.nikhil.guardian.guardiandetail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.guardian.R

class GuardianListFragment : Fragment() {

    private lateinit var viewModel: GuardianInfoViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guardian_list, container, false)
        recyclerView = view.findViewById(R.id.guardianListRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(GuardianInfoViewModel::class.java)

        // Specify layout for recycler view
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager

        // Observe the model
        viewModel.allGuardians.observe(viewLifecycleOwner, Observer { guardians ->
            // Data bind the recycler view
            recyclerView.adapter = GuardianListAdapter(guardians) { guardian ->
                // Handle item click (e.g., make a call)
                makeCall(guardian.guardianPhoneNo)
            }
        })
    }


    private fun makeCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")

        // Check if the CALL_PHONE permission is granted before making the call
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(callIntent)
        } else {
            // You should request the permission at this point
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), CALL_PHONE_PERMISSION_REQUEST_CODE)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the call
                makeCall("your_phone_number")
            } else {
                // Permission denied, you may want to inform the user or handle it accordingly
            }
        }
    }

    companion object {
        private const val CALL_PHONE_PERMISSION_REQUEST_CODE = 123
    }

}
