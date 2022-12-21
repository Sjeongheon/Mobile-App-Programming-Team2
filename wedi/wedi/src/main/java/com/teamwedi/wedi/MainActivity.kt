package com.teamwedi.wedi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.teamwedi.wedi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val REQ_LOGIN = 100
    lateinit var toggle: ActionBarDrawerToggle
    val tabTitles = listOf<String>("날씨", "메인", "옷장")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle("Title")

        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.drawer_opened, R.string.drawer_closed)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        binding.mainDrawerView.setNavigationItemSelectedListener {
            when (it.title) {
                "로그인" -> {
                    val i = Intent(this, AuthActivity::class.java)
                    startActivityForResult(i, REQ_LOGIN)
                }
                "로그아웃" -> {
                    MyApplication.auth.signOut()
                    MyApplication.email = null
                }
            }
            Log.d("kkang", "navigation item click... ${it.title}")
            true
        }

        val tabLayout: TabLayout = binding.tabs

        val viewPager = binding.viewpager
        binding.viewpager.adapter = MyFragmentPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = "${tabTitles[position]}"
        }.attach()

        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_weather)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_main)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_cloth)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_LOGIN -> {

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}