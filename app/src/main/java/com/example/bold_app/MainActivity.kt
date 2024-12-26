package com.example.bold_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.bold_app.databinding.ActivityMainBinding
import com.example.bold_app.view.FitnessAddFragment
import com.example.bold_app.view.FitnessFragment
import com.example.bold_app.view.GoalFragment
import com.example.bold_app.view.GoalListFragment
import com.example.bold_app.view.ProfileFragment
import com.example.bold_app.viewmodel.MainActivityViewModel
import com.example.bold_app.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    val profileViewModel: ProfileViewModel by viewModels()
    val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFragment(FitnessFragment())
        binding.bottomNavigationView.setOnApplyWindowInsetsListener { view, insets ->
            view.updatePadding(bottom = 0)
            insets
        }
        binding.bottomNavigationView.selectedItemId = R.id.fitness

        user?.let {
            val username = it.displayName ?: "Unknown User"
            val email = it.email ?: "Unknown Email"
            val profilePictureUri = it.photoUrl?.toString() ?: ""

            // Pass authentication to ViewModel
            profileViewModel.setProfileData(username, email, profilePictureUri)
        }

        mainActivityViewModel.selectedFragmentTag.value?.let { tag ->
            val fragment = when (tag) {
                "GoalFragment" -> {
                    if(mainActivityViewModel.goalView.value == true) GoalFragment()
                    else GoalListFragment()
                }
                "ProfileFragment" -> ProfileFragment()
                else -> {
                    if (mainActivityViewModel.fitnessView.value == true) FitnessFragment()
                    else FitnessAddFragment()
                }
            }
            replaceFragment(fragment)
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.goal -> {
                    if(mainActivityViewModel.goalView.value == true) replaceFragment(GoalFragment())
                    else replaceFragment(GoalListFragment())
                    mainActivityViewModel.setSelectedFragmentTag("GoalFragment")
                }
                R.id.fitness -> {
                    if(mainActivityViewModel.fitnessView.value == true) replaceFragment(FitnessFragment())
                    else replaceFragment(FitnessAddFragment())
                    mainActivityViewModel.setSelectedFragmentTag("FitnessFragment")
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    mainActivityViewModel.setSelectedFragmentTag("ProfileFragment")
                }
                else -> {}
            }
            true
        }
    }
    fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }

    fun redirectToGoal(){
        binding.bottomNavigationView.selectedItemId = R.id.goal
    }
}