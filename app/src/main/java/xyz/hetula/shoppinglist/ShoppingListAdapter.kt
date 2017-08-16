/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package xyz.hetula.shoppinglist

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class ShoppingListAdapter : RecyclerView.Adapter<ShoppingListAdapter.CartViewHolder>() {
    private val mPendingCart = ArrayList<CartItem>()
    private val mOnCart = ArrayList<CartItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        return CartViewHolder(inflater.inflate(R.layout.list_item_cart_item, parent, false))
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        if (showHeader(holder.txtItem, position)) {
            holder.btnAddCart.setImageDrawable(null)
            holder.txtItem.gravity = Gravity.CENTER
            holder.txtItem.setTypeface(null, Typeface.BOLD)
            return
        }
        holder.txtItem.setTypeface(null, Typeface.NORMAL)
        holder.txtItem.gravity = Gravity.CENTER_VERTICAL or Gravity.START

        val onCart: Boolean
        val item: CartItem

        if (position >= mPendingCart.size + 2) {
            item = mOnCart[position - mPendingCart.size - 2]
            onCart = true
        } else {
            item = mPendingCart[position - 1]
            onCart = false
        }

        val context = holder.itemView.context
        val name = if (item.price <= 0) {
            context.getString(R.string.cart_item_name_no_price,
                    item.count,
                    item.name)
        } else {
            context.getString(R.string.cart_item_name,
                    item.count,
                    item.name,
                    String.format(Locale.getDefault(), "%.2f", item.price))
        }


        holder.txtItem.text = name
        holder.btnAddCart.setImageResource(
                if (onCart)
                    R.drawable.ic_remove_shopping_cart
                else
                    R.drawable.ic_add_shopping_cart)

        holder.btnAddCart.isEnabled = true
        holder.btnAddCart.setOnClickListener {
            holder.btnAddCart.isEnabled = false
            if (onCart) {
                val pos = holder.adapterPosition - mPendingCart.size - 1
                val cartItem = mOnCart.removeAt(pos - 1)
                mPendingCart.add(cartItem)
                notifyItemMoved(holder.adapterPosition, mPendingCart.size)
                notifyItemChanged(mPendingCart.size)
            } else {
                val pos = holder.adapterPosition
                val cartItem = mPendingCart.removeAt(pos - 1)
                mOnCart.add(0, cartItem)
                notifyItemMoved(pos, mPendingCart.size + 2)
                notifyItemChanged(mPendingCart.size + 2)
            }
        }
    }

    override fun getItemCount(): Int {
        return mPendingCart.size + mOnCart.size + 2
    }

    fun saveData(context: Context) {
        val gson = Gson()
        val shopList = ShoppingListData(mPendingCart, mOnCart)
        val data = gson.toJson(shopList)

        val filename = "shoppinglist.json"
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(data)
            it.flush()
        }
        Log.d("ShoppingListAdapt", "Saved database!")
    }

    fun clearCart() {
        mPendingCart.clear()
        mOnCart.clear()
        notifyDataSetChanged()
    }

    fun addCartItem(item: CartItem) {
        mPendingCart.add(0, item)
        notifyItemInserted(1)
        notifyItemChanged(0)
    }

    fun addCartItems(shoppingListData: ShoppingListData) {
        mPendingCart.addAll(shoppingListData.pendingList)
        mOnCart.addAll(shoppingListData.cartList)
        notifyDataSetChanged()
    }

    private fun showHeader(txtItem: TextView, position: Int): Boolean {
        if (position == 0) {
            txtItem.setText(R.string.cart_list_pending)
            return true
        } else if (mPendingCart.size + 1 == position) {
            txtItem.setText(R.string.cart_list_in_cart)
            return true
        }
        return false
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtItem = itemView.findViewById<TextView>(R.id.text_cart_item)!!
        val btnAddCart = itemView.findViewById<ImageButton>(R.id.btn_cart_item)!!
    }

    data class ShoppingListData(val pendingList: ArrayList<CartItem> = ArrayList(),
                                val cartList: ArrayList<CartItem> = ArrayList())
}


