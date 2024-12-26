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
import com.example.bold_app.R
import com.example.bold_app.databinding.FragmentGoalListBinding
import com.example.bold_app.model.ChecklistItem
import com.example.bold_app.model.FitnessModel
import com.example.bold_app.model.GoalModel
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class GoalListFragment : Fragment(){
    private var _binding: FragmentGoalListBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var goalSelected: ArrayList<GoalModel>
    private var selectGoalId: Int? = 0
    private var bmi: Double? = 0.0

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
        _binding = FragmentGoalListBinding.inflate(inflater, container, false)
        binding.checklistStatus.visibility = View.GONE
        binding.loadingPanel.visibility = View.VISIBLE

        mainActivityViewModel.latestBMI.observe(viewLifecycleOwner) { latestBMI -> bmi = latestBMI}

        mainActivityViewModel.latestGoalDataId.observe(viewLifecycleOwner) { latestDataId ->
            if (latestDataId != null) {
                var progress = 0.0
                val firebaseRef = FirebaseDatabase.getInstance()
                    .getReference("users/${FirebaseAuth.getInstance()
                        .currentUser?.uid}/goal/$latestDataId")

                firebaseRef.get().addOnSuccessListener { dataSnapshot ->

                    binding.checklistStatus.visibility = View.VISIBLE
                    binding.loadingPanel.visibility = View.GONE

                    val goalId = dataSnapshot.child("goalId").getValue(Int::class.java)
                    when (goalId) {
                        1 -> binding.tvGoalTitle.text = getString(R.string.improve_cardio_health)
                        2 -> binding.tvGoalTitle.text = getString(R.string.gain_muscle_mass)
                        3 -> binding.tvGoalTitle.text = getString(R.string.lose_weight)
                        4 -> binding.tvGoalTitle.text = getString(R.string.enhance_flexibility)
                        else -> binding.tvGoalTitle.text = getString(R.string.fitness_goal)
                    }
                    val day = dataSnapshot.child("day").getValue(Int::class.java)
                    binding.tvGoalDay.text = getString(R.string.goal_day, day)

                    val calendar = Calendar.getInstance()
                    var getYear = calendar.get(Calendar.YEAR)
                    var getMonth = calendar.get(Calendar.MONTH)
                    var getDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val dateToday = getString(R.string.date_format,
                        getYear, getMonth + 1, getDay)

                    binding.tvChecklistDate.text = dateToday
                    val dateRecorded = dataSnapshot.child("dateToday").getValue(String::class.java)
                    val dateStart = dataSnapshot.child("dateStart").getValue(String::class.java)

                    if (dateToday != dateRecorded) {
                        binding.progressBar.progress = 0
                        binding.tvPercent.text = getString(R.string.progress_format, 0.0)

                        val goalData = GoalModel(dateStart!!, dateToday, goalId!!, day!! + 1,
                            false, false, false, false, false, false, false, false)
                        latestDataId.let { id ->
                            FirebaseDatabase.getInstance().getReference(
                                "users/${FirebaseAuth.getInstance().currentUser?.uid}/goal"
                            ).child(id).setValue(goalData)
                        }
                    }
                    binding.rvGoal.layoutManager = LinearLayoutManager(context)
                    binding.rvGoal.adapter = GoalAdapter(checklistData(
                        dataSnapshot.child("clItem1").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem2").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem3").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem4").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem5").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem6").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem7").getValue(Boolean::class.java)!!,
                        dataSnapshot.child("clItem8").getValue(Boolean::class.java)!!,
                        goalId!!
                    ), mainActivityViewModel)

                    for (i in 1..8) {
                        if(dataSnapshot.child("clItem$i")
                                .getValue(Boolean::class.java) == true)
                            progress = progress + (100.0/8.0)
                    }


                    binding.progressBar.progress = progress.toInt()
                    binding.tvPercent.text = getString(R.string.progress_format, progress)
                }
            }
        }

        mainActivityViewModel.checklistUpdate.observe(viewLifecycleOwner) { update ->
            val (itemId, isDone) = update
            val fieldName = "clItem$itemId"

            val firebaseRef = FirebaseDatabase.getInstance()
                .getReference("users/${FirebaseAuth.getInstance()
                    .currentUser?.uid}/goal/${mainActivityViewModel.latestGoalDataId.value}")

            firebaseRef.child(fieldName).setValue(isDone)
        }
        return binding.root
    }

    private fun checklistData(task1: Boolean, task2: Boolean, task3: Boolean, task4: Boolean,
                              task5: Boolean, task6: Boolean, task7: Boolean, task8: Boolean,
                              goalId: Int
    ): List<ChecklistItem>{
        val description = arrayListOf<String>("Targets chest, shoulders, and triceps, while burning calories. Easier variations recommended if needed. Variations: Standard push-ups, knee push-ups, wall push-ups, wide grip, diamond, decline push-ups.",
            "Targets thighs, glutes, and core, while boosting metabolism. Start with bodyweight before weighted variations. Variations: Bodyweight, goblet, sumo, Bulgarian split, front, jump squats.",
            "Targets glutes, quads, hamstrings, and core, while enhancing stability. Focus on balance and form. Variations: Forward, reverse, walking, lateral, Bulgarian split lunges.",
            "Strengthens core, back, and shoulders, while burning calories. Maintain a straight body line. Variations: Forearm plank, high plank, side plank, plank with leg lift.",
            "Targets triceps, chest, and shoulders. Use a stable surface like parallel bars or a chair. Variations: Bench dips, assisted dips, ring dips, parallel bar dips.",
            "Low-impact cardio targeting endurance and heart health. Start with walking and progress to jogging or running. Variations: Brisk walking, interval jogging, uphill walking.",
            "Great for joint-friendly cardio while strengthening the lower body. Variations: Stationary cycling, outdoor cycling, interval cycling.",
            "A high-intensity workout improving endurance, coordination, and cardiovascular strength. Variations: Basic jump, alternate foot jump, double-unders.",
            "High-intensity cardio targeting legs and core, improving agility and endurance. Variations: Standard high knees, lateral high knees, resistance band high knees.",
            "Full-body cardio workout strengthening the core, arms, and legs while boosting endurance. Variations: Slow climbers, cross-body climbers, resistance band climbers.",
            "Enhances spinal flexibility and relieves lower back tension by alternating between arching and rounding the back. Perform slowly with deep breaths to maximize relaxation and mobility.",
            "Stretches hamstrings, lower back, and calves, improving flexibility in the posterior chain. Keep the back straight and focus on controlled breathing as you reach forward.",
            "Stretches hip flexors and quadriceps, reducing tightness caused by prolonged sitting. Maintain an upright posture and engage the glutes for deeper activation.",
            "Relieves back, hip, and thigh tension while promoting relaxation. Sink hips toward heels, stretch arms forward, and focus on deep breathing to enhance the stretch.",
            "Improves flexibility in the sides of the torso and promotes spinal mobility. Reach one arm overhead while leaning sideways, keeping the hips stable for a full stretch.")

        val video = arrayListOf<String>("mm6_WcoCVTA?si=NGPzTpNHLbaqXpD5", "i7J5h7BJ07g?si=4TSf8LIb9AWnmi3V", "eFWCn5iEbTU?si=d1J4xQwgUt-LzQP0", "pSHjTRCQxIw?si=vXM8176sreobjTU6",
            "4LA1kF7yCGo?si=oyMPO6pJDFS2VtvX", "nmvVfgrExAg?si=vflU5qzMyEjlL_FY", "oyXoWU8j310?si=zO7D2jcNtU2u25bM", "0MwgoHCWgwA?si=8cVFWG5o2gq6KsK20", "IpnHwUwrVVY?si=ociq4Qpk4dCd6N5o",
            "kLh-uczlPLg?si=xFgb2FCdwPhMMM-j", "LIVJZZyZ2qM?si=64KwiWhwTCBs27QX", "_4-dcUi48VM?si=bvLYoKs6mTIZzMsC", "iMQTcZnH4Wg?si=9XMGt8cVdiJa78hc", "KVItAfkdGi0?si=-o_510B3QBFkiDw0",
            "xnY040imnsE?si=LyaCZN9VSS7BHHLc")

        val exercise = arrayListOf<String>("Push-Ups", "Squats", "Lunges", "Plank", "Dips",
            "Walking/Jogging", "Cycling", "Jump Rope", "High Knees", "Mountain Climbers",
            "Cat-Cow Stretch", "Seated-Forward Fold", "Hip Flexor Stretch", "Child's Pose",
            "Side Stretch")

        var exerciseTitle = arrayListOf<String>()
        var exerciseReps = arrayListOf<String>()
        var exerciseDesc = arrayListOf<String>()
        var ytVid = arrayListOf<String>()
        var calQuantity = "1,800-2,400"
        var proQuantity = "1.0-1.6"
        var carbQuantity = "6-8"

        if (bmi!! < 18.5) {
            calQuantity = "2,400-3,200"
            proQuantity = "1.6-2.2"
            carbQuantity = "5-6"
        }
        else if (bmi!! in 18.5..24.99) {
            calQuantity = "2,200-3,000"
            proQuantity = "1.4-2.0"
            carbQuantity = "5-7"
        }
        else if (bmi!! in 25.0..29.99) {
            calQuantity = "2,000-2,600"
            proQuantity = "1.2-1.8"
            carbQuantity = "4-6"
        }

        when (goalId) {
            1 -> {
                for (i in 5..9) {
                    exerciseTitle.add(exercise[i])
                    exerciseDesc.add(description[i])
                    ytVid.add(video[i])
                    when (i) {
                        5 -> {
                            if (bmi!! < 18.5) exerciseReps.add("15-20 min light jogging")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("20-30 min jogging")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("15-20 min jogging")
                            else exerciseReps.add("10-15 min brisk walking")
                        }
                        6 -> {
                            if (bmi!! < 18.5) exerciseReps.add("15-20 min low-intensity")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("25-30 min moderate")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("20-25 min moderate")
                            else exerciseReps.add("15-20 min low-intensity")
                        }
                        7 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (2-3 min light intervals)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (3-5 min intervals)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (2-3 min intervals)")
                            else exerciseReps.add("2 sets (1-2 min intervals)")
                        }
                        8 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (20 sec intervals)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (45 sec intervals)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (30 sec intervals)")
                            else exerciseReps.add("2 sets (20 sec intervals)")
                        }
                        9 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec intervals)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec intervals)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec intervals)")
                            else exerciseReps.add("2 sets (15-20 sec intervals)")
                        }
                    }
                }
            }
            2 -> {
                for (i in 0..4) {
                    exerciseTitle.add(exercise[i])
                    exerciseDesc.add(description[i])
                    ytVid.add(video[i])
                    when (i) {
                        0 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (5-8 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("3 sets (10-15 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (8-12 repetitions)")
                            else exerciseReps.add("3 sets (5-10 repetitions)")
                        }
                        1 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (8-10 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (8-12 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (10-12 repetitions)")
                            else exerciseReps.add("3 sets (8-10 repetitions)")
                        }
                        2 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (8-10 reps per leg)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (10-12 reps per leg)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (10-12 reps per leg)")
                            else exerciseReps.add("2 sets (8-10 reps per leg)")
                        }
                        3 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec)")
                            else exerciseReps.add("2 sets (15-20 sec)")
                        }
                        4 -> {
                            if (bmi!! < 18.5) exerciseReps.add("2 sets (5-8 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (10-12 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (8-10 repetitions)")
                            else exerciseReps.add("2 sets (5-8 repetitions)")
                        }
                    }
                }
            }
            3 -> {
                for (i in 0..9) {
                    if (i in 4..8) continue
                    exerciseTitle.add(exercise[i])
                    exerciseDesc.add(description[i])
                    ytVid.add(video[i])
                    when (i) {
                        0 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (5-8 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("3 sets (10-15 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (8-12 repetitions)")
                            else exerciseReps.add("3 sets (5-10 repetitions)")
                        }
                        1 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (8-10 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (8-12 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (10-12 repetitions)")
                            else exerciseReps.add("3 sets (8-10 repetitions)")
                        }
                        2 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (8-10 reps per leg)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (10-12 reps per leg)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (10-12 reps per leg)")
                            else exerciseReps.add("2 sets (8-10 reps per leg)")
                        }
                        3 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec)")
                            else exerciseReps.add("2 sets (15-20 sec)")
                        }
                        9 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec intervals)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec intervals)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec intervals)")
                            else exerciseReps.add("2 sets (15-20 sec intervals)")
                        }
                    }
                }
                if (bmi!! < 18.5) {
                 calQuantity = "2,200-2,600"
                 proQuantity = "1.6-2.0"
                 carbQuantity = "5-6"
                }
                else if (bmi!! in 18.5..24.99) {
                    calQuantity = "2,000-2,500"
                    proQuantity = "1.5-1.8"
                    carbQuantity = "4-5"
                }
                else if (bmi!! in 25.0..29.99) {
                    calQuantity = "1,800-2,200"
                    proQuantity = "1.4-1.6"
                    carbQuantity = "3-4"
                }
                else {
                    calQuantity = "1,600-2,000"
                    proQuantity = "1.2-1.5"
                    carbQuantity = "2-3"
                }
            }
            4 -> {
                for (i in 10..14) {
                    exerciseTitle.add(exercise[i])
                    exerciseDesc.add(description[i])
                    ytVid.add(video[i])
                    when (i) {
                        10 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (8-10 repetitions)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (10-12 repetitions)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (10-12 repetitions)")
                            else exerciseReps.add("3 sets (8-10 repetitions)")
                        }
                        11 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec hold)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec hold)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec hold)")
                            else exerciseReps.add("2 sets (15-20 sec hold)")
                        }
                        12 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec/side)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec/side)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec/side)")
                            else exerciseReps.add("2 sets (15-20 sec/side)")
                        }
                        13 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (15-20 sec hold)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (30-45 sec hold)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (20-30 sec hold)")
                            else exerciseReps.add("2 sets (15-20 sec hold)")
                        }
                        14 -> {
                            if (bmi!! < 18.5) exerciseReps.add("3 sets (10-12 reps/side)")
                            else if (bmi!! in 18.5..24.99) exerciseReps.add("4 sets (15-20 reps/side)")
                            else if (bmi!! in 25.0..29.99) exerciseReps.add("3 sets (12-15 reps/side)")
                            else exerciseReps.add("2 sets (10-12 reps/side)")
                        }
                    }
                }
            }
        }

        val ytStart = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"
        val ytEnd ="\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; " +
                "autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; " +
                "web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" " +
                "allowfullscreen></iframe>"

        val data = listOf(
            ChecklistItem(1, exerciseTitle[0], exerciseReps[0], exerciseDesc[0], ytStart+ytVid[0]+ytEnd, task1),
            ChecklistItem(2, exerciseTitle[1], exerciseReps[1], exerciseDesc[1], ytStart+ytVid[1]+ytEnd, task2),
            ChecklistItem(3, exerciseTitle[2], exerciseReps[2], exerciseDesc[2], ytStart+ytVid[2]+ytEnd, task3),
            ChecklistItem(4, exerciseTitle[3], exerciseReps[3], exerciseDesc[3], ytStart+ytVid[3]+ytEnd, task4),
            ChecklistItem(5, exerciseTitle[4], exerciseReps[4], exerciseDesc[4], ytStart+ytVid[4]+ytEnd, task5),
            ChecklistItem(6, "Calories", "$calQuantity kcal/day", "Consume nutrient-dense meals " +
                    "such as lean meats, fish (salmon, tuna), whole grains (brown rice, quinoa), " +
                    "healthy fats (avocados, nuts), and a variety of fruits and vegetables to " +
                    "provide essential vitamins and minerals while preserving muscle.", null, task6),
            ChecklistItem(7, "Proteins", "$proQuantity g per kg of body weight", "Supports muscle recovery and " +
                    "maintenance while improving endurance. Includes consuming high-protein foods " +
                    "such as lean meats (chicken breast, turkey), fish (salmon, cod), dairy " +
                    "products (Greek yogurt, cottage cheese), plant-based proteins (tofu, lentils), " +
                    "and protein supplements like whey or plant-based powders for convenience.", null, task7),
            ChecklistItem(8, "Carbohydrates", "$carbQuantity g per kg of body weight", "Total daily carbohydrate " +
                    "intake needed to fuel workouts and replenish energy stores while supporting " +
                    "muscle recovery. Consume complex carbohydrates such as whole grains " +
                    "(brown rice, quinoa), starchy vegetables (sweet potatoes, squash), legumes " +
                    "(beans, lentils), fruits (bananas, berries), and fiber-rich foods like oats " +
                    "and whole-grain bread for sustained energy release.", null, task8)
        )

        exerciseTitle.drop(5)
        exerciseReps.drop(5)
        exerciseDesc.drop(5)
        ytVid.drop(5)

        return data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}