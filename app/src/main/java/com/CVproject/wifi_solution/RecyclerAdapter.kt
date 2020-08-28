package com.CVproject.wifi_solution

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(var items: List<ScanResult>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    // ViewHolder 단위 객체로 View의 데이터를 설정
    class ViewHolder (v: View): RecyclerView.ViewHolder(v){
        var tvWifiName: TextView = v.findViewById(R.id.wifi_name)

        public fun setItem(item: ScanResult){
            tvWifiName.setText(item.SSID)
        }
    }

    // 보여줄 아이템 개수만큼 View를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        var itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.wifi_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size // items의 크기를 구하는 역할

    // 생성된 View에 보여줄 데이터를 설정
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(items.get(position))
    }
}