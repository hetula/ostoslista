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

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.io.File
import java.io.FileNotFoundException

class ShoppingListFragment : Fragment() {
    private val mAdapter = ShoppingListAdapter()

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.shopping_list.layoutManager = LinearLayoutManager(context)
        view.shopping_list.adapter = mAdapter
        LoadTask(mAdapter).execute(context)
        return view
    }

    override fun onPause() {
        super.onPause()
        mAdapter.saveData(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_clear_shoplist -> {
                removeAll()
                true
            }
            R.id.action_add_item -> {
                addItem()
                true
            }
            R.id.action_save_list -> {
                mAdapter.saveData(context)
                Snackbar.make(view!!, R.string.shopping_list_saved, Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun removeAll() {
        AlertDialog.Builder(context)
                .setTitle(R.string.clear_all_items_title)
                .setMessage(R.string.clear_all_items_message)
                .setNegativeButton(android.R.string.cancel) { d: DialogInterface, _: Int ->
                    d.dismiss()
                }
                .setPositiveButton(R.string.remove_items) { d: DialogInterface, _: Int ->
                    mAdapter.clearCart()
                    d.dismiss()
                }
                .create()
                .show()
    }

    private fun addItem() {
        val addView = AddItemView(context)
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val dlg = AlertDialog.Builder(context)
                .setTitle(R.string.item_new_title)
                .setView(addView)
                .setNegativeButton(android.R.string.cancel) { d: DialogInterface, _: Int ->
                    d.dismiss()
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                .setPositiveButton(R.string.add_item) { d: DialogInterface, _: Int ->
                    val err = add(addView.getAmount(), addView.getName(), addView.getPrice())
                    if (err != null) {
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    } else {
                        d.dismiss()
                    }
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                .create()
        addView.setOkListener {
            val err = add(addView.getAmount(), addView.getName(), addView.getPrice())
            if (err != null) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            } else {
                dlg.dismiss()
            }
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
        dlg.show()
        addView.reqFocusOnName()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun add(amount: String, name: String, price: String): String? {
        return try {
            if (name.isBlank()) {
                return context.getString(R.string.empty_name)
            }
            val a = Integer.parseInt(amount)
            val p = if (price.isNotBlank()) java.lang.Double.parseDouble(price) else 0.0
            val item = CartItem(a, name, p)
            mAdapter.addCartItem(item)
            null
        } catch (e: Throwable) {
            e.message
        }
    }

    internal class LoadTask(private val adapter: ShoppingListAdapter) :
            AsyncTask<Context, Void, ShoppingListAdapter.ShoppingListData>() {

        override fun doInBackground(vararg p0: Context?): ShoppingListAdapter.ShoppingListData? {
            val context = p0[0]!!
            val gson = Gson()

            val filename = "shoppinglist.json"
            return try {
                val data = File(context.filesDir, filename).readText()
                gson.fromJson<ShoppingListAdapter.ShoppingListData>(data,
                        ShoppingListAdapter.ShoppingListData::class.java)
            } catch (e: FileNotFoundException) {
                Log.e("LoadTask", "No database found", e)
                null
            }
        }

        override fun onPostExecute(result: ShoppingListAdapter.ShoppingListData?) {
            if (result == null) {
                Log.e("LoadTask", "NULL read from database!")
            } else {
                adapter.addCartItems(result)
            }
        }
    }

}
