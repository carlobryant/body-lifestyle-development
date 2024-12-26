package com.example.bold_app.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.bold_app.MainActivity
import com.example.bold_app.R
import com.example.bold_app.databinding.FragmentFitnessAddBinding
import com.example.bold_app.error_trapping.addDecimalLimiter
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.bold_app.model.FitnessModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar


class FitnessAddFragment(
    private val dataId: String? = null,
    private val updateDate: String? = null,
    private val updateWeight: String? = null,
    private val updateHeight: String? = null,
    private val updateHeartRate: String? = null
    ) : Fragment() {
    private var _binding: FragmentFitnessAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseRef: DatabaseReference

    private val mainActivityViewModel: MainActivityViewModel  by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFitnessAddBinding.inflate(inflater, container, false)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) firebaseRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/fitness")
        else Log.e("FitnessAddFragment", "User not authenticated.")

        binding.etWeight.addDecimalLimiter(2)
        binding.etHeight.addDecimalLimiter(2)

        // New Date
        val calendar = Calendar.getInstance()
        var getYear = calendar.get(Calendar.YEAR)
        var getMonth = calendar.get(Calendar.MONTH)
        var getDay = calendar.get(Calendar.DAY_OF_MONTH)

        binding.etWeight.hint = getString(R.string.wHint, "0")
        binding.etHeight.hint = getString(R.string.hHint, "0")
        binding.etHeartRate.hint = getString(R.string.hrHint, "0")

        // Edit Data
        if (dataId != null && updateDate != null) {
            binding.layoutAddFitness.backgroundTintList = ContextCompat
                .getColorStateList(requireContext(), R.color.bright_orange)

            binding.buttonDelete.visibility = View.VISIBLE

            var (updateYear, updateMonth, updateDay) = updateDate.split("-")
            getYear = updateYear.toInt()
            getMonth = updateMonth.toInt() - 1
            getDay = updateDay.toInt()

            binding.etWeight.hint = getString(R.string.wHint, updateWeight)
            binding.etHeight.hint = getString(R.string.hHint, updateHeight)
            binding.etHeartRate.hint = getString(R.string.hrHint, updateHeartRate)

            val bmi = updateWeight!!.toDouble() /
                    ((updateHeight!!.toDouble()/100)*(updateHeight.toDouble()/100))

            val dpToPx = { dp: Int -> (dp * resources.displayMetrics.density).toInt() }
            val imgClass = binding.imageClass.layoutParams as ViewGroup.MarginLayoutParams
            imgClass.height = dpToPx(85)
            imgClass.width = dpToPx(85)
            imgClass.topMargin = dpToPx(20)
            binding.imageClass.layoutParams = imgClass

            if (bmi < 18.5)
                binding.imageClass.setImageResource(R.drawable.ic_rv_underweight)
            else if (bmi in 18.5..24.99)
                binding.imageClass.setImageResource(R.drawable.ic_rv_normal)
            else if (bmi in 25.0..29.99)
                binding.imageClass.setImageResource(R.drawable.ic_rv_overweight)
            else
                binding.imageClass.setImageResource(R.drawable.ic_rv_obese)
        }

        binding.buttonDate.text = getString(R.string.date_format, getYear, getMonth + 1, getDay)

        binding.etWeight.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val weightVal = binding.etWeight.text.toString().toDoubleOrNull()
                if(weightVal == null || weightVal < 2 || weightVal > 700){
                    if (weightVal != null)
                        Toast.makeText(context, "Invalid weight!", Toast.LENGTH_SHORT).show()
                    binding.etWeight.text.clear()
                }
            }
        }

        binding.etHeight.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val heightVal = binding.etHeight.text.toString().toDoubleOrNull()
                if(heightVal == null || heightVal < 50 || heightVal > 300){
                    if (heightVal != null)
                        Toast.makeText(context, "Invalid height!", Toast.LENGTH_SHORT).show()
                    binding.etHeight.text.clear()
                }
            }
        }

        val layoutParams = binding.layoutAddFitness.layoutParams as ViewGroup.MarginLayoutParams
        binding.etHeartRate.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val heartRateVal = binding.etHeartRate.text.toString().toDoubleOrNull()
                if(heartRateVal == null || heartRateVal < 20 || heartRateVal > 600){
                    if (heartRateVal != null)
                        Toast.makeText(context, "Invalid heart rate!", Toast.LENGTH_SHORT).show()
                    binding.etHeartRate.text.clear()
                }
                layoutParams.topMargin = 30
                binding.layoutAddFitness.layoutParams = layoutParams
            } else {
                layoutParams.topMargin = -150
                binding.layoutAddFitness.layoutParams = layoutParams
            }
        }

        binding.buttonReturn.setOnClickListener {
            mainActivityViewModel.setFitnessView(true)
            (activity as? MainActivity)?.replaceFragment(FitnessFragment())
        }

        binding.buttonDelete.setOnClickListener {
            if (uid != null && dataId != null && updateDate != null)
                deleteFitnessData(dataId)
        }

        binding.buttonSave.setOnClickListener {
            var (year, month, day) = binding.buttonDate.text.split("-")
            if(month.toInt() in 1..9 && month.length == 1) month = "0$month"
            if(day.toInt() in 1..9 && day.length == 1) day = "0$day"
            binding.buttonDate.text = getString(R.string.date_format_split, year, month, day)

            if (binding.etWeight.text.toString().toDoubleOrNull() != null &&
                binding.etWeight.text.toString().toDouble() !in 2.0..700.0) {
                Toast.makeText(context, "Invalid weight!", Toast.LENGTH_SHORT).show()
                binding.etWeight.text.clear()
                return@setOnClickListener
            } else if (binding.etHeight.text.toString().toDoubleOrNull() != null &&
                binding.etHeight.text.toString().toDouble() !in 50.0..300.0){
                Toast.makeText(context, "Invalid height!", Toast.LENGTH_SHORT).show()
                binding.etHeight.text.clear()
                return@setOnClickListener
            } else if (binding.etHeartRate.text.toString().toDoubleOrNull() != null &&
                binding.etHeartRate.text.toString().toInt() !in 20..600){
                Toast.makeText(context, "Invalid heart rate!", Toast.LENGTH_SHORT).show()
                binding.etHeartRate.text.clear()
                return@setOnClickListener
            }

            if (uid != null && dataId != null && updateDate != null){
                if(binding.etWeight.text.toString() == "" && binding.etHeight.text.toString() == ""
                    && binding.etHeartRate.text.toString() == ""
                    && binding.buttonDate.text.toString() == updateDate)
                    Toast.makeText(context, "No changes made.", Toast.LENGTH_SHORT).show()
                else
                    updateFitnessData(dataId)
            } else if (uid != null && binding.etWeight.text.isNotEmpty()
                && binding.etHeight.text.isNotEmpty() && binding.etHeartRate.text.isNotEmpty()){
                saveFitnessData()
            } else {
                Log.e("FitnessAddFragment", "User not authenticated and/or values missing." +
                        " Cannot save data.")
                Toast.makeText(context, "Weight, Height, and " +
                        "Heart Rate are all required.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.DialogTheme,
                { _, year, month, day ->
                    binding.buttonDate.text = getString(R.string.date_format, year, month + 1, day)
                },
                getYear,
                getMonth,
                getDay
            )
            datePickerDialog.show()
        }

        return binding.root
    }

    private fun saveFitnessData() {
        val date = binding.buttonDate.text.toString()
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val height = binding.etHeight.text.toString().toDoubleOrNull() ?: 0.0
        val heartRate = binding.etHeartRate.text.toString().toIntOrNull() ?: 0

        val fitnessData = FitnessModel(date, weight, height, heartRate)

        firebaseRef.push().setValue(fitnessData)
            .addOnSuccessListener {
                Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                mainActivityViewModel.setFitnessView(true)
                (activity as? MainActivity)?.replaceFragment(FitnessFragment())
            }
            .addOnFailureListener { error ->
                Log.e("FitnessAddFragment", "Failed to save data: ${error.message}")
                Toast.makeText(context, "Failed to save data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFitnessData(dataId: String) {
        val date = binding.buttonDate.text.toString()
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: updateWeight!!.toDouble()
        val height = binding.etHeight.text.toString().toDoubleOrNull() ?: updateHeight!!.toDouble()
        val heartRate =
            binding.etHeartRate.text.toString().toIntOrNull() ?: updateHeartRate!!.toInt()

        val fitnessData = FitnessModel(date, weight, height, heartRate)
        dataId.let { id ->
            FirebaseDatabase.getInstance().getReference(
                "users/${
                    FirebaseAuth.getInstance()
                        .currentUser?.uid
                }/fitness"
            ).child(id).setValue(fitnessData)
                .addOnSuccessListener {
                    Toast.makeText(
                        context, "Updated Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainActivityViewModel.setFitnessView(true)
                    (activity as? MainActivity)?.replaceFragment(FitnessFragment())
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteFitnessData(dataId: String) {
        MaterialAlertDialogBuilder(ContextThemeWrapper(requireContext(),
            R.style.DialogTheme))
            .setTitle("Delete Permanently")
            .setMessage("Delete ${binding.buttonDate.text} data?")
            .setPositiveButton("OK") { _, _ ->
                dataId.let { id ->
                    FirebaseDatabase.getInstance().getReference(
                        "users/${FirebaseAuth.getInstance().currentUser?.uid}/fitness/$dataId"
                    ).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Deleted Successfully",
                                Toast.LENGTH_SHORT).show()
                            if (isAdded) {
                                mainActivityViewModel.setFitnessView(true)
                                (activity as? MainActivity)?.replaceFragment(FitnessFragment())
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(context, "Error ${error.message}", Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .setNegativeButton("CANCEL"){_, _ -> }.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}