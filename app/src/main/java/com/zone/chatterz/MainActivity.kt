package com.zone.chatterz

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.zone.chatterz.mainFragment.ChatActivity
import com.zone.chatterz.mainFragment.ProfileActivity
import com.zone.chatterz.mainFragment.SearchActivity
import com.zone.chatterz.mainFragment.StatusActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerToggle : ActionBarDrawerToggle

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setNavigationInitially()

        bottomNavigationBar.setOnNavigationItemSelectedListener { menuItem ->
            bottomNavigationBar.menu.getItem(0).setIcon(R.drawable.chats_light_icon)
            bottomNavigationBar.menu.getItem(1).setIcon(R.drawable.search_light_icon)
            bottomNavigationBar.menu.getItem(2).setIcon(R.drawable.icon_status)
            bottomNavigationBar.menu.getItem(3).setIcon(R.drawable.proifle_light_icon)
            when(menuItem.itemId){

                R.id.chats ->{
                    val chatFragment = ChatActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main,chatFragment)
                        .addToBackStack(null).commit()
                    menuItem.setIcon(R.drawable.chat_dark_icon)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.search_bottombar ->{
                    val searchActivity = SearchActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main,searchActivity)
                        .addToBackStack(null).commit()
                    menuItem.setIcon(R.drawable.search_dark_icon)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.status_bottombar ->{
                    val statusActivity = StatusActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main,statusActivity)
                        .addToBackStack(null).commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.profile_bottombar ->{
                    val profileActivity = ProfileActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_main,profileActivity)
                        .addToBackStack(null).commit()
                    menuItem.setIcon(R.drawable.profile_dark_icon)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

    }
    private fun setNavigationInitially(){
        bottomNavigationBar.menu.getItem(0).setIcon(R.drawable.chats_light_icon)
        val chatsFragment = ChatActivity()
        supportFragmentManager.beginTransaction()
            .add(R.id.container_main,chatsFragment)
            .addToBackStack(null).commit()
        bottomNavigationBar.menu.getItem(1).setIcon(R.drawable.chat_dark_icon)
    }
}


