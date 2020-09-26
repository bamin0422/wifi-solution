package com.CVproject.wifi_solution.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.CVproject.wifi_solution.Fragment.Fragment_QR
import com.CVproject.wifi_solution.Fragment.Fragment_now
import com.CVproject.wifi_solution.Fragment.Fragment_scan

class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(frag_position: Int): Fragment {
        return when(frag_position){
            0 -> Fragment_now()
            1 -> Fragment_scan()
            else -> Fragment_QR()
        }
    }
    override fun getCount() = 3 // 전체 페이지 수
}
