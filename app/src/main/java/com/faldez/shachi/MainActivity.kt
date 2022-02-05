package com.faldez.shachi

import android.animation.Animator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.faldez.shachi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    lateinit var navController: NavController
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        navController = navFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isShow = when (destination.id) {
                R.id.savedFragment -> true
                R.id.browseFragment -> true
                R.id.browseSavedFragment -> true
                R.id.moreFragment -> true
                R.id.favoriteFragment -> true
                else -> false
            }
            if (isShow) {
                showBottomNavigation()

            } else {
                hideBottomNavigation()
            }
        }
        setTheme()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.popBackStack()
    }

    fun setTheme() {
        val theme = sharedPreferences.getString("theme", "follow_system")

        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        Log.d("MainActivity", "$theme $mode")
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        Log.d("MoreFragment", pref.key)
        when (pref.key) {
            "settings" -> {
                navController.navigate(R.id.action_more_to_settings)
                return true
            }
            "servers" -> {
                navController.navigate(R.id.action_global_to_servers)
                return true
            }
            "blacklisted_tags" -> {
                navController.navigate(R.id.action_global_to_blacklisted_tags)
                return true
            }
            "oss_notices" -> {
                navController.navigate(R.id.action_more_to_oss)
                return true
            }
        }
        return false
    }

    private fun hideBottomNavigation() {
        if (binding.bottomNavigationView.isVisible) {
            val constraint = findViewById<ConstraintLayout>(R.id.mainLayout)
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraint)
            constraintSet.connect(R.id.navFragment,
                ConstraintSet.BOTTOM,
                R.id.mainLayout,
                ConstraintSet.BOTTOM)
            constraintSet.applyTo(constraint)
            binding.bottomNavigationView.animate()
                .translationY(binding.bottomNavigationView.height.toFloat())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {}

                    override fun onAnimationEnd(p0: Animator?) {
                        binding.bottomNavigationView.visibility = View.GONE
                    }

                    override fun onAnimationCancel(p0: Animator?) {}

                    override fun onAnimationRepeat(p0: Animator?) {}
                })
        }
    }

    private fun showBottomNavigation() {
        if (!binding.bottomNavigationView.isVisible) {
            val constraint = findViewById<ConstraintLayout>(R.id.mainLayout)
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraint)
            constraintSet.connect(R.id.navFragment,
                ConstraintSet.BOTTOM,
                R.id.bottomNavigationView,
                ConstraintSet.TOP)
            constraintSet.applyTo(constraint)
            binding.bottomNavigationView.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(p0: Animator?) {}

                    override fun onAnimationCancel(p0: Animator?) {}

                    override fun onAnimationRepeat(p0: Animator?) {}
                })
        }
    }
}
@GlideModule
class MyGlideModule : AppGlideModule()