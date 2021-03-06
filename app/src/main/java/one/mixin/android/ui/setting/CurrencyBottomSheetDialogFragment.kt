package one.mixin.android.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_currency_bottom_sheet.view.*
import kotlinx.android.synthetic.main.item_currency.view.*
import kotlinx.android.synthetic.main.view_wallet_transfer_type_bottom.view.close_iv
import kotlinx.android.synthetic.main.view_wallet_transfer_type_bottom.view.search_et
import one.mixin.android.R
import one.mixin.android.extension.toast
import one.mixin.android.ui.common.MixinBottomSheetDialogFragment
import one.mixin.android.vo.Fiats
import one.mixin.android.widget.BottomSheet
import one.mixin.android.widget.SearchView

class CurrencyBottomSheetDialogFragment : MixinBottomSheetDialogFragment() {
    companion object {
        const val TAG = "CurrencyBottomSheetDialogFragment"

        fun newInstance() = CurrencyBottomSheetDialogFragment()
    }

    var callback: Callback? = null

    private val currencyAdapter = CurrencyAdapter()
    private val currencies = arrayListOf<Currency>()

    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.fragment_currency_bottom_sheet, null)
        (dialog as BottomSheet).setCustomView(contentView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contentView.close_iv.setOnClickListener { dismiss() }
        contentView.search_et.listener = object : SearchView.OnSearchViewListener {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun onSearch() {
            }
        }
        currencyAdapter.checkedName = Fiats.currency
        currencyAdapter.currencyListener = object : OnCurrencyListener {
            override fun onClick(currency: Currency) {
                Fiats.currency = currency.name
                Fiats.currencySymbol = currency.symbol
                callback?.onCurrencyClick(currency)
                toast(R.string.save_success)
                dismiss()
            }
        }
        contentView.currency_rv.adapter = currencyAdapter
        setListData()
    }

    private fun setListData() {
        val names = resources.getStringArray(R.array.currency_names)
        val symbols = resources.getStringArray(R.array.currency_symbols)
        val flagArray = resources.obtainTypedArray(R.array.currency_flags)
        val flags = arrayListOf<Int>()
        for (i in 0 until names.size) {
            flags.add(flagArray.getResourceId(i, 0))
        }
        flagArray.recycle()

        if (flags.size != names.size || flags.size != symbols.size || names.size != symbols.size) {
            return
        }
        currencies.clear()
        for (i in 0 until flags.size) {
            currencies.add(Currency(names[i], symbols[i], flags[i]))
        }
        currencyAdapter.submitList(currencies)
    }

    private fun filter(s: String) {
        currencyAdapter.submitList(if (s.isNotBlank()) {
            currencies.filter {
                it.name.contains(s, true)
            }
        } else currencies)
    }

    interface Callback {
        fun onCurrencyClick(currency: Currency)
    }
}

class CurrencyAdapter : ListAdapter<Currency, CurrencyHolder>(Currency.DIFF_CALLBACK) {
    var currencyListener: OnCurrencyListener? = null
    var checkedName: String = "USD"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CurrencyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false))

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, checkedName, currencyListener) }
    }
}

class CurrencyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(currency: Currency, checkedName: String, listener: OnCurrencyListener?) {
        if (currency.name == checkedName) {
            itemView.check_iv.isVisible = true
        } else {
            itemView.check_iv.isInvisible = true
        }
        itemView.flag_iv.setImageResource(currency.flag)
        itemView.name.text = itemView.context.getString(R.string.wallet_setting_currency_desc, currency.name, currency.symbol)
        itemView.setOnClickListener { listener?.onClick(currency) }
    }
}

interface OnCurrencyListener {
    fun onClick(currency: Currency)
}

data class Currency(
    val name: String,
    val symbol: String,
    val flag: Int
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Currency>() {
            override fun areItemsTheSame(oldItem: Currency, newItem: Currency) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: Currency, newItem: Currency) =
                oldItem == newItem
        }
    }
}
