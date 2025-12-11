package com.example.petshop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOrderAdapter
    private val orderList = mutableListOf<Order>()
    private lateinit var tvNoOrders: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recycler_view_order_history)
        tvNoOrders = findViewById(R.id.tv_no_orders_history)
        progressBar = findViewById(R.id.order_history_progress_bar)

        setupRecyclerView()
        fetchUserOrders()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminOrderAdapter(orderList)
        recyclerView.adapter = adapter
    }

    private fun fetchUserOrders() {
        showLoading(true)
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showLoading(false)
            checkEmptyState()
            return
        }

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                showLoading(false)
                if (e != null) {
                    Log.w("OrderHistoryActivity", "Listen failed.", e)
                    checkEmptyState()
                    return@addSnapshotListener
                }

                orderList.clear()
                snapshots?.mapNotNullTo(orderList) { it.toObject(Order::class.java) }
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
    
    private fun showLoading(isLoading: Boolean){
        progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if(isLoading) View.GONE else View.VISIBLE
    }
}
