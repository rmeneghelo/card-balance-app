// Copyright (c) 2017 Alexander Håkansson
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT
package se.creotec.chscardbalance2.model

import java.util.HashSet

import se.creotec.chscardbalance2.BuildConfig
import se.creotec.chscardbalance2.Constants

class Model : IModel {
    override val quickChargeURL: String
    private val cardDataChangedListeners: MutableSet<OnCardDataChangedListener>
    private val menuDataChangedListeners: MutableSet<OnMenuDataChangedListener>

    override var cardData: CardData = CardData()
        set(value) {
            field = value
            notifyCardDataChangedListeners()
        }
    override var menuData: MenuData = MenuData()
        set(value) {
            field = value
            notifyMenuDataChangedListeners()
        }
    override var cardLastTimeUpdated: Long = -1
        set(value) = if (value >= 0) field = value else field = -1
    override var menuLastTimeUpdated: Long = -1
        set(value) = if (value >= 0) field = value else field = -1
    override var preferredMenuLanguage: String = Constants.PREFS_MENU_LANGUAGE_DEFAULT
        set(value) {
            if (isOKLang(value)) field = value
        }

    init {
        cardDataChangedListeners = HashSet<OnCardDataChangedListener>()
        menuDataChangedListeners = HashSet<OnMenuDataChangedListener>()
        quickChargeURL = BuildConfig.BACKEND_URL + Constants.ENDPOINT_CHARGE
    }

    override fun addCardDataListener(listener: OnCardDataChangedListener) {
        cardDataChangedListeners.add(listener)
    }

    override fun notifyCardDataChangedListeners() {
        for (listener in cardDataChangedListeners) {
            listener.cardDataChanged(this.cardData)
        }
    }

    override fun addMenuDataListener(listener: OnMenuDataChangedListener) {
        menuDataChangedListeners.add(listener)
    }

    override fun notifyMenuDataChangedListeners() {
        for (listener in this.menuDataChangedListeners) {
            listener.menuDataChanged(this.menuData)
        }
    }

    private fun isOKLang(language: String): Boolean {
        when (language) {
            Constants.ENDPOINT_MENU_LANG_EN -> return true
            Constants.ENDPOINT_MENU_LANG_SV -> return true
            else -> return false
        }
    }
}
