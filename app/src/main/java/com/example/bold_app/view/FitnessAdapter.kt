package com.example.bold_app.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.bold_app.MainActivity
import com.example.bold_app.databinding.RvFitnessItemBinding
import com.example.bold_app.model.FitnessModel
import com.example.bold_app.R
import com.example.bold_app.viewmodel.MainActivityViewModel
import java.util.Locale

class FitnessAdapter (
    private  val fitnessList : java.util.ArrayList<FitnessModel>,
    private val owner: ViewModelStoreOwner
):
    RecyclerView.Adapter<FitnessAdapter.ViewHolder>() {

    private val mainActivityViewModel: MainActivityViewModel by lazy {
        ViewModelProvider(owner)[MainActivityViewModel::class.java]
    }

    class ViewHolder(val binding : RvFitnessItemBinding) : RecyclerView.ViewHolder(binding.root) {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return  ViewHolder(RvFitnessItemBinding.inflate(LayoutInflater
            .from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return fitnessList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = fitnessList[position]

        var cardioHealth = "Unknown"
        val (year, month, day) = currentItem.date.split("-")
        val bmi = currentItem.weight / ((currentItem.height/100)*(currentItem.height/100))

        holder.apply {
            binding.apply {
                tvMonthDayItem.text = monthDay(month, day)
                tvYearItem.text = year

                tvBMIItem.text = String.format(Locale.getDefault(), "%s %.2f",
                    "BMI:", bmi)
                if (bmi < 18.5)
                    imgItem.setImageResource(R.drawable.ic_rv_underweight)
                else if (bmi in 18.5..24.99)
                    imgItem.setImageResource(R.drawable.ic_rv_normal)
                else if (bmi in 25.0..29.99)
                    imgItem.setImageResource(R.drawable.ic_rv_overweight)
                else
                    imgItem.setImageResource(R.drawable.ic_rv_obese)

                cardioHealth = if (currentItem.heartRate > 83)
                    "Poor"
                else if (currentItem.heartRate in 75..83)
                    "Below Average"
                else if (currentItem.heartRate in 67..74)
                    "Average"
                else if (currentItem.heartRate in 60..66)
                    "Above Average"
                else if (currentItem.heartRate in 55..59)
                    "Very Good"
                else
                    "Athlete"

                tvCardioItem.text = String.format(Locale.getDefault(),
                    "%s %s", "Cardio Health:", cardioHealth)

                rvContainer.setOnClickListener {
                    mainActivityViewModel.setFitnessView(false)
                    (owner as? MainActivity)?.replaceFragment(FitnessAddFragment(currentItem.dataId,
                        currentItem.date, currentItem.weight.toString(),
                        currentItem.height.toString(), currentItem.heartRate.toString()))
                }
            }
        }
    }

    private fun monthDay(month: String, day: String): String {
        return when (month) {
            "01" -> "January $day"
            "02" -> "February $day"
            "03" -> "March $day"
            "04" -> "April $day"
            "05" -> "May $day"
            "06" -> "June $day"
            "07" -> "July $day"
            "08" -> "August $day"
            "09" -> "September $day"
            "10" -> "October $day"
            "11" -> "November $day"
            "12" -> "December $day"
            else -> ""
        }
    }
}