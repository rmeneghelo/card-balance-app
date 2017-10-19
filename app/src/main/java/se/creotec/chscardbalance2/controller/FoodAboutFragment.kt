// Copyright (c) 2017 Alexander Håkansson
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT
package se.creotec.chscardbalance2.controller

import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_restaurant_about.*
import org.w3c.dom.Text
import se.creotec.chscardbalance2.R
import se.creotec.chscardbalance2.model.OpenHour
import se.creotec.chscardbalance2.model.Restaurant
import se.creotec.chscardbalance2.util.Util
import java.util.*

class FoodAboutFragment : Fragment() {

    private var restaurant: Restaurant = Restaurant("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            restaurant = Gson().fromJson(it.getString(ARG_RESTAURANT), Restaurant::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restaurant_about, container, false)

        restaurant_rating_bar.rating = restaurant.rating
        setOpenHours(restaurant_about_open_now, restaurant_about_open_hours)
        restaurant_visit_website.setOnClickListener {
            val webIntent = CustomTabsIntent.Builder()
                    .setToolbarColor(activity.getColor(R.color.color_primary))
                    .build()
            webIntent.launchUrl(context, Uri.parse(restaurant.websiteUrl))
        }

        if (restaurant.averagePrice != 0) {
            restaurant_about_avg_price.text = getString(R.string.restaurant_about_avg_price, restaurant.averagePrice)
        } else {
            restaurant_about_avg_price.text = getString(R.string.restaurant_about_avg_no_price)
        }

        restaurant_about_campus.text = getString(R.string.restaurant_about_campus, restaurant.campus)

        return view
    }

    override fun onResume() {
        super.onResume()
        setOpenHours(restaurant_about_open_now, restaurant_about_open_hours)
    }

    private fun setOpenHours(openNow: TextView?, openHours: TextView?) {
        if (restaurant.dishes.isEmpty()) {
            showClosed(openNow, openHours, false)
        } else {
            val c = Calendar.getInstance()
            c.time = Date()
            restaurant.openHours.forEach { oh ->
                if (oh.dayOfWeek == c.get(Calendar.DAY_OF_WEEK)) {
                    if (Util.isBetweenHours(oh.startHour, oh.endHour)) {
                        openNow?.text = getString(R.string.restaurant_about_open_now)
                        openNow?.setTextColor(ContextCompat.getColor(activity, R.color.color_success))
                        val startFormatted = DateUtils.formatDateTime(activity, OpenHour.toUnixTimeStamp(oh.startHour), DateUtils.FORMAT_SHOW_TIME)
                        val endFormatted = DateUtils.formatDateTime(activity, OpenHour.toUnixTimeStamp(oh.endHour), DateUtils.FORMAT_SHOW_TIME)
                        openHours?.text = getString(R.string.restaurant_about_hours_range, startFormatted, endFormatted)
                        return
                    } else {
                        showClosed(openNow, openHours, true, before = OpenHour.isBefore(oh.startHour), openHour = oh)
                        return
                    }
                }
            }
            openNow?.text = getString(R.string.restaurant_about_open_today)
            openNow?.setTextColor(ContextCompat.getColor(activity, R.color.color_success))
            openHours?.text = ""
        }
    }

    private fun showClosed(openNow: TextView?, openHours: TextView?, now: Boolean, before: Boolean = false, openHour: OpenHour? = null) {
        if (now) {
            openNow?.text = getString(R.string.restaurant_about_closed_now)
            if (before && openHour != null) {
                val startFormatted = DateUtils.formatDateTime(activity, OpenHour.toUnixTimeStamp(openHour.startHour), DateUtils.FORMAT_SHOW_TIME)
                openHours?.text = getString(R.string.restaurant_about_opens_at, startFormatted)
            } else {
                openHours?.text = ""
            }
        } else {
            openNow?.text = getString(R.string.restaurant_about_closed_today)
            openHours?.text = ""
        }
        openNow?.setTextColor(ContextCompat.getColor(activity, R.color.color_fail))
    }

    companion object {
        private val ARG_RESTAURANT = "about_restaurant"
        fun newInstance(restaurant: Restaurant): FoodAboutFragment {
            val fragment = FoodAboutFragment()
            val args = Bundle()
            val restaurantJSON = Gson().toJson(restaurant, Restaurant::class.java)
            args.putString(ARG_RESTAURANT, restaurantJSON)
            fragment.arguments = args
            return fragment
        }
    }
}
