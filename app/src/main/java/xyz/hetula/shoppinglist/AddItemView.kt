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
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_add_item.view.*

class AddItemView : LinearLayoutCompat {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val pad = context?.resources?.getDimension(R.dimen.add_item_padding)?.toInt()!!
        setPadding(pad, pad, pad, pad)
        LayoutInflater.from(context).inflate(R.layout.view_add_item, this, true)
    }

    fun getAmount(): String {
        return txt_amount.text.toString()
    }

    fun getName(): String {
        return txt_name.text.toString()
    }

    fun getPrice(): String {
        return txt_price.text.toString()
    }

    fun reqFocusOnName() {
        txt_name.requestFocus()
    }

    fun setOkListener(listener: () -> Unit) {
        txt_price.setOnKeyListener { _: View, keycode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_UP && keycode == KeyEvent.KEYCODE_ENTER) {
                listener.invoke()
                true
            }
            false
        }
    }
}
