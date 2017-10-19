// Copyright (c) 2017 Alexander Håkansson
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT
package se.creotec.chscardbalance2.controller

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.ISlidePolicy
import kotlinx.android.synthetic.main.fragment_app_first_run_add_card.*
import se.creotec.chscardbalance2.Constants
import se.creotec.chscardbalance2.GlobalState
import se.creotec.chscardbalance2.R
import se.creotec.chscardbalance2.model.CardData
import se.creotec.chscardbalance2.util.CardNumberMask


class AppFirstRunAddCardFragment : Fragment(), ISlidePolicy {

    private var introParent: AppIntro? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_app_first_run_add_card, container, false)
        card_number_edit.addTextChangedListener(TextEmptyWatcher(clear_button))
        card_number_edit.addTextChangedListener(CardNumberMask())
        // Make max length = total number of digits in card number + spaces between with good formatting
        val filters = Array<InputFilter>(1, { _ -> InputFilter.LengthFilter(Constants.CARD_NUMBER_LENGTH + 3) })
        card_number_edit.filters = filters
        clear_button.setOnClickListener { card_number_edit.text?.clear() }
        card_number_edit.setOnEditorActionListener({view,actionID,_  ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                true
            } else {
                false
            }
        })
        return view
    }

    override fun isPolicyRespected(): Boolean {
        if (isCardNumberValid()) {
            val cardNumber: String = card_number_edit.text.toString().replace(" ", "")
            val cardData = CardData()
            cardData.cardNumber = cardNumber
            val global = activity.application as GlobalState
            global.model.cardData = cardData
            global.saveCardData()
            global.scheduleUpdating()
            return true
        }
        return false
    }

    override fun onUserIllegallyRequestedNextPage() {
        view?.let {
            Toast.makeText(activity, R.string.error_not_valid_number, Toast.LENGTH_LONG).show()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AppIntro) {
            introParent = context
        } else {
            throw RuntimeException("Activity must extend AppIntro")
        }
    }

    override fun onDetach() {
        super.onDetach()
        introParent = null
    }

    private fun isCardNumberValid(): Boolean {
        card_number_edit.let {
            val cardNumber: String = it.text.toString().replace(" ", "")
            if (cardNumber.length == Constants.CARD_NUMBER_LENGTH) {
                return true
            }
        }
        return false
    }

    class TextEmptyWatcher(val clearButton: Button) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (it.isNotEmpty()) {
                    clearButton.visibility = View.VISIBLE
                } else {
                    clearButton.visibility = View.GONE
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}
