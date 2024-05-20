@file:Suppress("DEPRECATION", "UnusedImport", "RedundantNullableReturnType",
    "RedundantSamConstructor", "UNNECESSARY_SAFE_CALL", "ReplaceGetOrSet",
    "MemberVisibilityCanBePrivate"
)

package com.nikhil.guardian.guardiandetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.nikhil.guardian.R
import com.nikhil.guardian.databinding.FragmentGuardianInfoBinding
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Toast


class GuardianInfo : Fragment() {

    private lateinit var binding: FragmentGuardianInfoBinding
    private lateinit var model: GuardianInfoViewModel


    @SuppressLint("UseRequireInsteadOfGet", "FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_guardian_info,container,false)

        // Get the view model
        model = ViewModelProviders.of(this).get(GuardianInfoViewModel::class.java)

        // Specify layout for recycler view
        val linearLayoutManager = LinearLayoutManager(
            activity!!, RecyclerView.VERTICAL,false)
        binding.guardianList.layoutManager = linearLayoutManager

        // Observe the model
        model.allGuardians.observe(this, Observer{ guardians->
            // Data bind the recycler view
            binding.guardianList.adapter = GuardianAdapter(guardians)
        })

        binding.addGuardian.setOnClickListener { openAddGuardian() }

        model.showSnackBarEvent.observe(this, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_LONG
                ).show()
                model.doneShowingSnackbar()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the FAB
        val fabAddGuardian: FloatingActionButton = view.findViewById(R.id.fab)

        // Set click listener for the FAB
        fabAddGuardian.setOnClickListener {
            openAddGuardian()
        }
    }

    fun openAddGuardian(){
        findNavController().navigate(GuardianInfoDirections.actionGuardianInfoToAddGuardian())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_item -> showDeleteConfirmationDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete all guardians?")
        builder.setPositiveButton("Yes") { _, _ ->
            // User clicked Yes, perform deletion
            model.onClear()
            Toast.makeText(requireContext(), "Guardians deleted", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { _, _ ->
            // User clicked No, do nothing or handle as needed
            Toast.makeText(requireContext(), "Deletion canceled", Toast.LENGTH_SHORT).show()
        }

        val dialog = builder.create()

        // Set the dialog's view alpha to 0 to start with
        dialog.setOnShowListener {
            val view = dialog.findViewById<View>(android.R.id.content)
            view?.alpha = 0f

            // Animate the view's alpha to 1
            val fadeInAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            fadeInAnimator.duration = 1000 // Set the duration of the fade-in animation
            fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    view?.alpha = 0f
                }
            })
            fadeInAnimator.start()
        }

        dialog.show()
    }
}
