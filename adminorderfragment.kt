package com.example.petshop

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminOrdersFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOrderAdapter
    private val orderList = mutableListOf<Order>()
    private lateinit var tvNoOrders: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)

        db = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.recycler_view_admin_orders)
        tvNoOrders = view.findViewById(R.id.tv_no_orders)

        setupRecyclerView()
        fetchOrders()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AdminOrderAdapter(orderList)
        recyclerView.adapter = adapter
    }

    private fun fetchOrders() {
        db.collection("orders").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminOrders", "Listen failed.", e)
                    return@addSnapshotListener
                }

                orderList.clear()
                for (doc in snapshots!!) {
                    val order = doc.toObject(Order::class.java)
                    orderList.add(order)
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }
    }

    private fun checkEmptyState() {
        if (orderList.isEmpty()) {
            tvNoOrders.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoOrders.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
