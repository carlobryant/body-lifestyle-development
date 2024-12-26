package com.example.bold_app.view

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bold_app.LogInActivity
import com.example.bold_app.MainActivity
import com.example.bold_app.R
import com.example.bold_app.databinding.FragmentProfileBinding
import com.example.bold_app.google_services.GoogleAuthClient
import com.example.bold_app.viewmodel.LogInViewModel
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.example.bold_app.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.lazy

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }
    private val logInViewModel: LogInViewModel by lazy {
       ViewModelProvider(requireActivity())[LogInViewModel::class.java]
    }

    private val mainActivityViewModel: MainActivityViewModel  by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel.username.observe(viewLifecycleOwner) {
            binding.usernameTextView.text = it
            binding.usernameTextView2.text = it
        }

        profileViewModel.email.observe(viewLifecycleOwner) {
            binding.emailTextView.text = it
        }

        profileViewModel.profilePictureUri.observe(viewLifecycleOwner) { uri ->
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.bold_app_logo)
                .into(binding.profileImageView)
        }

        mainActivityViewModel.latestGoalDataId.observe(viewLifecycleOwner) { latestDataId ->
            if (latestDataId != null) {
                val firebaseRef = FirebaseDatabase.getInstance()
                    .getReference("users/${FirebaseAuth.getInstance()
                        .currentUser?.uid}/goal/$latestDataId")

                firebaseRef.get().addOnSuccessListener { dataSnapshot ->
                    val goalId = dataSnapshot.child("goalId").getValue(Int::class.java)

                    when (goalId) {
                        1 -> binding.goalTextView.text = getString(R.string.improve_cardio_health)
                        2 -> binding.goalTextView.text = getString(R.string.gain_muscle_mass)
                        3 -> binding.goalTextView.text = getString(R.string.lose_weight)
                        4 -> binding.goalTextView.text = getString(R.string.enhance_flexibility)
                    }
                    if (goalId!! > 0) binding.buttonSelectGoal.text = getString(R.string.change_goal)

                    binding.buttonSelectGoal.setOnClickListener {
                        if (goalId > 0) {
                            MaterialAlertDialogBuilder(
                                ContextThemeWrapper(requireContext(), R.style.DialogTheme)
                            )
                                .setTitle("Change Goal")
                                .setMessage("Changing goal will reset your progress, continue?")
                                .setPositiveButton("OK") { _, _ ->
                                    FirebaseDatabase.getInstance()
                                        .getReference("users/${FirebaseAuth.getInstance()
                                            .currentUser?.uid}/goal").orderByKey().limitToLast(1)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    for (child in snapshot.children) {
                                                        for (i in 1..8)
                                                            mainActivityViewModel
                                                            .updateChecklistItem(i, false)
                                                        child.ref.removeValue()
                                                            .addOnSuccessListener {
                                                                if (isAdded) {
                                                                    mainActivityViewModel.setGoalView(true)
                                                                    (activity as? MainActivity)
                                                                        ?.replaceFragment(GoalFragment())
                                                                    (activity as? MainActivity)?.redirectToGoal()
                                                                }
                                                            }
                                                            .addOnFailureListener { error ->
                                                                Toast.makeText(context, "Error ${error.message}",
                                                                    Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "No goal selected",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(context, "Error ${error.message}",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                }
                                .setNegativeButton("CANCEL") { _, _ -> }
                                .show()
                        }
                        else {
                            mainActivityViewModel.setGoalView(true)
                            (activity as? MainActivity)?.replaceFragment(GoalFragment())
                            (activity as? MainActivity)?.redirectToGoal()
                        }
                    }
                }
            }
            else  binding.buttonSelectGoal.setOnClickListener {
                mainActivityViewModel.setGoalView(true)
                (activity as? MainActivity)?.replaceFragment(GoalFragment())
                (activity as? MainActivity)?.redirectToGoal()
            }
        }

        binding.buttonSignOut.setOnClickListener {
            lifecycleScope.launch {
                val intent = Intent(requireActivity(), LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                Toast.makeText(context, "Thank you for using BOLD!", Toast.LENGTH_SHORT).show()
                requireActivity().finish()

                val googleAuthClient = GoogleAuthClient(requireContext())
                googleAuthClient.signOut()
                logInViewModel.updateSignInStatus(false)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
