package com.example.bold_app.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bold_app.MainActivity
import com.example.bold_app.databinding.FragmentFitnessBinding
import com.example.bold_app.model.FitnessModel
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FitnessFragment : Fragment() {
    private var _binding: FragmentFitnessBinding? = null
    private val binding get() = _binding!!

    private lateinit var fitnessList: ArrayList<FitnessModel>
    private lateinit var firebaseRef: DatabaseReference

    private var mainActivityViewModel: MainActivityViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFitnessBinding.inflate(inflater, container, false)

        mainActivityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firebaseRef = FirebaseDatabase.getInstance().getReference("users/$uid/fitness")
            fitnessList = arrayListOf()

            fetchData()

            _binding?.rvFitness?.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
            }
        }

        binding.floatingActionButton.setOnClickListener {
            mainActivityViewModel!!.setFitnessView(false)
            (activity as? MainActivity)?.replaceFragment(FitnessAddFragment())
        }
        return binding.root
    }

    private fun fetchData(){
        binding.floatingActionButton.visibility = View.GONE
        binding.loadingPanel.visibility = View.VISIBLE

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fitnessList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val fitnessData = child.getValue(FitnessModel::class.java)?.apply {
                            dataId = child.key
                        }

                        if (fitnessData != null) {
                            fitnessList.add(fitnessData)
                        } else {
                            Log.e("FitnessFragment", "Invalid fitness data")
                        }
                    }
                } else
                    _binding?.noData?.visibility = View.VISIBLE

                _binding?.let {
                    val rvAdapter = FitnessAdapter(fitnessList, requireActivity())
                    it.rvFitness.adapter = rvAdapter
                }
                fitnessList.sortByDescending { it.date }
                val latestFitnessData = fitnessList.firstOrNull()

                latestFitnessData?.let { latestEntry ->
                    mainActivityViewModel!!.updateLatestBMI((latestEntry.weight /
                            ((latestEntry.height/100)*(latestEntry.height/100))))
                }


                _binding?.floatingActionButton?.visibility = View.VISIBLE
                _binding?.loadingPanel?.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                activity?.let {
                    Toast.makeText(it, "Failed to read data: $error", Toast.LENGTH_SHORT).show()
                }
                Log.e("Fitness", "Failed to read data: $error")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}