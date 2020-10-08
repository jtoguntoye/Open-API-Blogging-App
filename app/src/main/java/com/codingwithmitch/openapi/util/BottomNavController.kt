package com.codingwithmitch.openapi.util

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.codingwithmitch.openapi.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavController(
    val context: Context,
    @IdRes val containerId: Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangeListener: OnNavigationGraphChanged?,
    val navGraphProvider: NavGraphProvider
) {
    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
     lateinit var navItemChangeListener: OnNavigationItemChanged
    private val navigationBackStack = BackStack.of(appStartDestinationId)

    init {
        if(context is Activity) {
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()): Boolean {

        //Replace fragment representing a navigation item (or selection) of the bottom Nav bar
        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: NavHostFragment.create(navGraphProvider.getNavGraphId(itemId))

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        // Add to backstack
        navigationBackStack.moveLast(itemId)

        //update checked icon
        navItemChangeListener.onItemChanged(itemId)

        //communicate with the activity
        graphChangeListener?.onGraphChanged()

        return true
     }

    fun onBackPressed(){
        val childFragmentManager = fragmentManager.findFragmentById(containerId)!!
            .childFragmentManager
        when{
            childFragmentManager.popBackStackImmediate() -> {
            }

            navigationBackStack.size > 1 -> {
                //Remove last item from backstack
                navigationBackStack.removeLast()

                //Update the container with new fragment
                onNavigationItemSelected()
            }

            //If the stack has only one and its not the navigation home we should
            //ensure that the application always leave from startDestination
            navigationBackStack.last() != appStartDestinationId ->{
                navigationBackStack.removeLast()
                navigationBackStack.add(0, appStartDestinationId)
                onNavigationItemSelected()
            }

            else -> activity.finish()
        }
    }


    //custom back stack to handle navigation between the items of the bottom navigation bar
    private class BackStack: ArrayList<Int>(){

        companion object{
            fun of(vararg elements: Int) :BackStack{
                 val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size - 1)

        fun moveLast(item: Int) {
            remove(item)
            add(item)
        }
    }

    //for setting the checked icon in the bottom nav
    //to be used internally here by the custom navController
    interface OnNavigationItemChanged {
        fun onItemChanged(itemId: Int)
    }

    fun setOnItemNavigationChanged(listener: (ItemId: Int) -> Unit) {
        this.navItemChangeListener = object : OnNavigationItemChanged{
            override fun onItemChanged(itemId: Int) {
              listener.invoke(itemId)
            }
        }
    }
    //get Id of each Navigation graph for each item in the BottomNavigation bar
    //ex R.navigation.nav_blog
    interface NavGraphProvider{
        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    //Execute when navigation Graph changes
    //to be used by the activity
    interface OnNavigationGraphChanged{
        fun onGraphChanged()
    }

    interface OnNavigationReselectedListener{
        fun onReselectNavItem(navController: NavController, fragment: Fragment)
    }
}

//create an extension function for the BottomNavigationView
fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onReselectedListener: BottomNavController.OnNavigationReselectedListener
){
  setOnNavigationItemSelectedListener {
      bottomNavController.onNavigationItemSelected(it.itemId)
  }

  setOnNavigationItemReselectedListener {
      bottomNavController
          .fragmentManager
          .findFragmentById(bottomNavController.containerId)!!
          .childFragmentManager
          .fragments[0]?.let { fragment->
          onReselectedListener.onReselectNavItem(
              bottomNavController.activity.findNavController(bottomNavController.containerId),
              fragment
          )
      }
  }
    bottomNavController.setOnItemNavigationChanged { itemId->
        menu.findItem(itemId).isChecked = true
    }

}