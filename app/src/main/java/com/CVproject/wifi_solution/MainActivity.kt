package com.CVproject.wifi_solution

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_fragment__q_r.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        var myRef = FirebaseDatabase.getInstance().reference
        myRef.child("wifiList").setValue(null)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this){} // 모바일 광고 SDK 초기화


       setPermission() // 권한 췍

        withfi_view_pager.adapter =
            MainPagerAdapter(supportFragmentManager) // adapter를 사용해 Viewpager와 fragment연결
        withfi_view_pager.offscreenPageLimit = 2 // 뷰 계층 구조의 보관된 페이지, View/Fragment 화수를 제어할 수 있다.

        // viewPager 설정
        withfi_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // bottomNavigationView 설정
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.btn_wifiList -> withfi_view_pager.currentItem = 0
                R.id.btn_scan -> withfi_view_pager.currentItem = 1
                R.id.btn_QR -> withfi_view_pager.currentItem = 2
            }
            true
        }

    }

    private fun setPermission() {
        val permission = object : PermissionListener { //  테드 퍼미션에서 권한
            override fun onPermissionGranted() { // 허용된 경우
                Toast.makeText(this@MainActivity, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // 거부된 경우
                Toast.makeText(this@MainActivity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.ACCESS_NETWORK_STATE
            )
            .check()
    }


}