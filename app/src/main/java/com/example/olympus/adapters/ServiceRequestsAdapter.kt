package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceRequestsAdapter(
    private var requests: List<ServiceRequest>,
    private val onAccept: (ServiceRequest) -> Unit,
    private val onReject: (ServiceRequest) -> Unit
) : RecyclerView.Adapter<ServiceRequestsAdapter.ServiceRequestViewHolder>() {

    inner class ServiceRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvRequestUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvRequestUserEmail)
        val tvStatus: TextView = view.findViewById(R.id.tvRequestStatus)
        val btnAccept: Button = view.findViewById(R.id.btnAcceptRequest)
        val btnReject: Button = view.findViewById(R.id.btnRejectRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_request, parent, false)
        return ServiceRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceRequestViewHolder, position: Int) {
        val request = requests[position]
        holder.tvUserName.text = request.userName
        holder.tvUserEmail.text = request.userEmail
        holder.tvStatus.text = holder.itemView.context.getString(
            R.string.service_request_card_action_with_role,
            request.requestedRole
        )
        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<ServiceRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
