@file:Suppress("RedundantNullableReturnType", "UselessCallOnNotNull", "OVERRIDE_DEPRECATION",
    "DEPRECATION", "RedundantSamConstructor", "RemoveRedundantQualifierName", "NAME_SHADOWING",
    "RemoveSingleExpressionStringTemplate", "PrivatePropertyName", "ForEachParameterNotUsed",
    "RemoveEmptyParenthesesFromLambdaCall", "RedundantLambdaArrow"
)

package com.nikhil.guardian

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.room.Room
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.nikhil.guardian.FirebaseAuth.LoginViewModel
import com.nikhil.guardian.database.Guardian
import com.nikhil.guardian.database.GuardianDatabase
import com.nikhil.guardian.databinding.NewFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashBoardFragment : Fragment() {

    private lateinit var binding: NewFragmentBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var Latitude: String = ""
    private var Longitude: String = ""

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val LOCATION_PERMISSION_REQUEST_CODE = 123



    companion object {
        const val TAG = "DashBoardFragment"
        const val SIGN_IN_RESULT_CODE = 1001
        private const val PERMISSION_SEND_SMS = 2
    }

    private val viewModel by viewModels<LoginViewModel>()

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.new_fragment, container, false
        )

        getLocation()

        binding.guardianButton.setOnClickListener { view: View ->
            view.findNavController()
                .navigate(DashBoardFragmentDirections.actionDashBoardFragmentToGuardianInfo())
        }
        binding.callbutton?.setOnClickListener { view: View ->
            view.findNavController()
                .navigate(DashBoardFragmentDirections.actionDashBoardFragmentToCall())
        }

        binding.locButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_mapsActivity)
        }

        binding.emerButton.setOnClickListener {
            getLocation()
            if (Longitude.isNullOrBlank() || Longitude.isNullOrEmpty()) {
                Toast.makeText(
                    activity!!,
                    "Click on Location button and try again",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_SEND_SMS)
                } else {
                    uiScope.launch {
                        withContext(Dispatchers.IO) {
                            emergencyFun()
                        }
                    }
                }
            }
        }
        binding.emerButton2.setOnClickListener {
            getLocation()
            if (Longitude.isNullOrBlank() || Longitude.isNullOrEmpty()) {
                Toast.makeText(
                    activity!!,
                    "Click on Location button and try again",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_SEND_SMS)
                } else {
                    uiScope.launch {
                        withContext(Dispatchers.IO) {
                            emergencyFun2()
                        }
                    }
                }
            }
        }
        binding.emerButton3.setOnClickListener {
            getLocation()
            if (Longitude.isNullOrBlank() || Longitude.isNullOrEmpty()) {
                Toast.makeText(
                    activity!!,
                    "Click on Location button and try again",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_SEND_SMS)
                } else {
                    uiScope.launch {
                        withContext(Dispatchers.IO) {
                            emergencyFun3()
                        }
                    }
                }
            }
        }
        binding.button18.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_sosActivity)
        }
        binding.trainingbutton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_youtubeActivity)
        }
        binding.nearhos.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_nearhospital)
        }
        binding.nearpol.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_nearps)
        }
        binding.videobutton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_videoactivity)
        }
        binding.spbutton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_dashBoardFragment_to_sp)
        }
        //binding.table.setOnClickListener { /* your click listener code */ }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()
    }

    @SuppressLint("LogNotTimber")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                //User successfully signed in
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.textView.text =
                        ("Welcome, " + FirebaseAuth.getInstance().currentUser?.displayName)
                }
                else -> {
                    launchSignInFlow()
                }
            }
        })
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email
        // If users choose to register with their email,
        // they will need to create a password as well
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
            //
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(), DashBoardFragment.SIGN_IN_RESULT_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try to get the location again
                getLocation()
            } else {
                // Permission denied, handle it accordingly (e.g., show a message to the user)
                Toast.makeText(activity, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                    Latitude = (location.latitude).toString()
                    Longitude = (location.longitude).toString()
                }
            }
        } else {
            // If the permission has not been granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }



    @SuppressLint("UseRequireInsteadOfGet")
    private fun emergencyFun() {
        val db =
            Room.databaseBuilder(activity!!, GuardianDatabase::class.java, "GuardianDB").build()
        val emailList: List<Guardian> = db.guardianDatabaseDao().getEmail()

        var maillist: String = ""
        val subject: String = "From Women Safety App"
        val text: String = resources.getString(R.string.problem)
        val text1 =
            text.plus("https://www.google.com/maps/search/?api=1&query=$Latitude,$Longitude")

        emailList.forEach() {
            maillist = emailList.joinToString(separator = ",") { it -> "${it.guardianEmail}" }
        }
        emailList.forEach() {
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("${it.guardianPhoneNo}", null, "$text1", null, null)
        }

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.setType("message/rfc822")
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(maillist))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text1)
        startActivity(Intent.createChooser(shareIntent, "Send mail using.."))
    }
    @SuppressLint("UseRequireInsteadOfGet")
    private fun emergencyFun2() {
        val db =
            Room.databaseBuilder(activity!!, GuardianDatabase::class.java, "GuardianDB").build()
        val emailList: List<Guardian> = db.guardianDatabaseDao().getEmail()

        var maillist: String = ""
        val subject: String = "From Women Safety App"
        val text: String = resources.getString(R.string.problem2)
        val text1 =
            text.plus("https://www.google.com/maps/search/?api=1&query=$Latitude,$Longitude")

        emailList.forEach() {
            maillist = emailList.joinToString(separator = ",") { it -> "${it.guardianEmail}" }
        }
        emailList.forEach() {
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("${it.guardianPhoneNo}", null, "$text1", null, null)
        }

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.setType("message/rfc822")
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(maillist))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text1)
        startActivity(Intent.createChooser(shareIntent, "Send mail using.."))
    }
    @SuppressLint("UseRequireInsteadOfGet")
    private fun emergencyFun3() {
        val db =
            Room.databaseBuilder(activity!!, GuardianDatabase::class.java, "GuardianDB").build()
        val emailList: List<Guardian> = db.guardianDatabaseDao().getEmail()

        var maillist: String = ""
        val subject: String = "From Women Safety App"
        val text: String = resources.getString(R.string.problem3)
        val text1 =
            text.plus("https://www.google.com/maps/search/?api=1&query=$Latitude,$Longitude")

        emailList.forEach() {
            maillist = emailList.joinToString(separator = ",") { it -> "${it.guardianEmail}" }
        }
        emailList.forEach() {
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("${it.guardianPhoneNo}", null, "$text1", null, null)
        }

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.setType("message/rfc822")
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(maillist))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text1)
        startActivity(Intent.createChooser(shareIntent, "Send mail using.."))
    }
}
