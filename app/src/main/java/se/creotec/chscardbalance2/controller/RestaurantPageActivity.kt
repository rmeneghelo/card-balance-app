// Copyright (c) 2017 Alexander Håkansson
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT
package se.creotec.chscardbalance2.controller

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.gson.Gson
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_restaurant_page.*
import se.creotec.chscardbalance2.Constants
import se.creotec.chscardbalance2.R
import se.creotec.chscardbalance2.model.Dish
import se.creotec.chscardbalance2.model.MenuData
import se.creotec.chscardbalance2.model.OnMenuDataChangedListener
import se.creotec.chscardbalance2.model.Restaurant
import se.creotec.chscardbalance2.util.Util

class RestaurantPageActivity : AppCompatActivity(), FoodDishFragment.OnListFragmentInteractionListener, OnMenuDataChangedListener {

    private var restaurant: Restaurant = Restaurant("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_page)
        if (intent.extras == null) {
            finish()
        }

        if (savedInstanceState == null) {
            val restaurantJSON = intent.extras.getString(Constants.INTENT_RESTAURANT_DATA_KEY)
            restaurant = loadRestaurant(restaurantJSON)
        } else {
            val restaurantJSON = savedInstanceState.getSerializable(Constants.INTENT_RESTAURANT_DATA_KEY) as String
            restaurant = loadRestaurant(restaurantJSON)
        }

        setupToolbar()
        setupViewPager(restaurant)

        toolbar_collapsing_layout.title = restaurant.name.toUpperCase()
        toolbar_image.let {
            ImageLoader.getInstance().displayImage(restaurant.imageUrl, it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val restaurantJSON = Gson().toJson(restaurant, Restaurant::class.java)
        outState.putString(Constants.INTENT_RESTAURANT_DATA_KEY, restaurantJSON)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onListFragmentInteraction(item: Dish) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("dish", Util.capitalizeAllWords(item.description))
        clipboard.primaryClip = clipData
        val copied = getString(R.string.dish_copied, item.title)
        parent_view.let { Snackbar.make(it, copied, Snackbar.LENGTH_LONG).show() }
    }

    override fun menuDataChanged(newData: MenuData) {
        Log.i(LOG_TAG, "Menu data was updated")
        //TODO: Update menu list
    }

    private fun loadRestaurant(restaurantJSON: String): Restaurant {
        return Gson().fromJson(restaurantJSON, Restaurant::class.java)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewPager(restaurant: Restaurant) {
        val dishFragment = FoodDishFragment.newInstance(restaurant)
        val aboutFragment = FoodAboutFragment.newInstance(restaurant)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(dishFragment, getString(R.string.restaurant_todays_menu))
        viewPagerAdapter.addFragment(aboutFragment, getString(R.string.restaurant_about))
        restaurant_viewpager.let {
            it.adapter = viewPagerAdapter
            if (restaurant.isClosed()) {
                it.currentItem = POS_ABOUT
            }
            restaurant_tablayout.setupWithViewPager(it)
        }
    }

    companion object {
        private val LOG_TAG = RestaurantPageActivity::class.java.name
        private val POS_TODAYS_MENU = 0
        private val POS_ABOUT = 1
    }
}
