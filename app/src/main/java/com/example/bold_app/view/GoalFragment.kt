package com.example.bold_app.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.bold_app.MainActivity
import com.example.bold_app.R
import com.example.bold_app.databinding.FragmentGoalBinding
import com.example.bold_app.model.FitnessModel
import com.example.bold_app.model.GoalModel
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class GoalFragment : Fragment() {
    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!

    private val mainActivityViewModel: MainActivityViewModel  by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGoalBinding.inflate(inflater, container, false)

        mainActivityViewModel.latestBMI.observe(viewLifecycleOwner) { latestBMI ->
            binding.imageCardio.setOnClickListener {
                if(latestBMI != null && latestBMI > 0.0) setGoal(1)
                else Toast.makeText(context, "Fitness data is required to set a goal.",
                    Toast.LENGTH_SHORT).show()
            }

            binding.imageMuscle.setOnClickListener {
                if(latestBMI != null && latestBMI > 0.0) setGoal(2)
                else Toast.makeText(context, "Fitness data is required to set a goal.",
                    Toast.LENGTH_SHORT).show()
            }

            binding.imageWeightLoss.setOnClickListener {
                if(latestBMI != null && latestBMI > 0.0) setGoal(3)
                else Toast.makeText(context, "Fitness data is required to set a goal.",
                    Toast.LENGTH_SHORT).show()
            }

            binding.imageFlexibility.setOnClickListener {
                if(latestBMI != null && latestBMI > 0.0) setGoal(4)
                else Toast.makeText(context, "Fitness data is required to set a goal.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun setGoal(goalId: Int){
        if (goalId > 0){
            val calendar = Calendar.getInstance()
            var getYear = calendar.get(Calendar.YEAR)
            var getMonth = calendar.get(Calendar.MONTH)
            var getDay = calendar.get(Calendar.DAY_OF_MONTH)

            val date = getString(R.string.date_format, getYear, getMonth + 1, getDay)
            val goalSelected = GoalModel(date, date, goalId)

            FirebaseDatabase.getInstance().getReference(
                "users/${
                    FirebaseAuth.getInstance()
                        .currentUser?.uid
                }/goal"
            ).push().setValue(goalSelected)
                .addOnSuccessListener {
                    Toast.makeText(
                        context, "Goal Selected Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainActivityViewModel.setGoalView(false)
                    (activity as? MainActivity)?.replaceFragment(GoalListFragment())
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Selection failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}