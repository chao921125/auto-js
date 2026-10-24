package org.autojs.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.autojs.app.ui.EditorFragment
import org.autojs.app.ui.LogFragment
import org.autojs.app.ui.ScriptListFragment
import org.autojs.app.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scripts -> switchFragment(ScriptListFragment())
                R.id.nav_editor -> switchFragment(EditorFragment())
                R.id.nav_log -> switchFragment(LogFragment())
                R.id.nav_settings -> switchFragment(SettingsFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_scripts
        }
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}
