package com.CVproject.wifi_solution

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_fragment__q_r.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        withfi_view_pager.adapter = MainPagerAdapter(supportFragmentManager) // adapter를 사용해 Viewpager와 fragment연결
        withfi_view_pager.offscreenPageLimit = 2 // 뷰 계층 구조의 보관된 페이지, View/Fragment 수를 제어할 수 있다.

        // viewPager 설정
        withfi_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // bottomNavigationView 설정
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.btn_wifiList -> withfi_view_pager.currentItem = 0
                R.id.btn_scan -> withfi_view_pager.currentItem = 1
                R.id.btn_QR -> withfi_view_pager.currentItem = 2
            }
            true
        }

    }

}