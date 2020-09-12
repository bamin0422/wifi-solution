<h2>With fi 기술 명세서</h2>

*MainActivity*

- 크게 Fragment_scan, Fragment_now, Fragment_QR로 구성되어 있는 메인 액티비티 클래스이다.

  ```kotlin
  class MainActivity : AppCompatActivity() {
      ...
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
  ```

  

- bottomNavigationView을 통해 아래의 버튼으로 각 프래그먼트로 이동할 수 있다.

  ```kotlin
   bottomNavigationView.setOnNavigationItemSelectedListener {
          when (it.itemId) {
              R.id.btn_wifiList -> withfi_view_pager.currentItem = 0
              R.id.btn_scan -> withfi_view_pager.currentItem = 1
              R.id.btn_QR -> withfi_view_pager.currentItem = 2
          }
          true
      }
  
  }
  ```

  

- 필용한 permission들을 설정했다.

  ```kotlin
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
              ...
          )
          .check()
  }
  ```



*MainPagerAdapter*

- 어댑터를 사용해 Viewpager와 Fragment들을 연결한 FragmentManager을 매개변수로 하는 클래스이다.

  ```kotlin
  class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
  
      override fun getItem(frag_position: Int): Fragment {
          return when(frag_position){
              0 -> Fragment_now()
              1 -> Fragment_scan()
              else -> Fragment_QR()
          }
      }
  ```



*IntroActivity*

- 어플을 실행헀을 때 처음 도입부 부분으로, 어플 마크를 일정 시간동안 띄워주는 액티비티 클래스이다.

  ```kotlin
  class IntroActivity : AppCompatActivity() {
      ...
       private fun startLoading() {
          val handler = Handler()
          handler.postDelayed(Runnable {
              val intent  = Intent(this, MainActivity::class.java)
              startActivity(intent)
              finish()
          },2000)
      }
  }
  ```

  

*Fragment_now*

- 현재 주변 와이파이들을 RecyclerView 형태의 목록으로 보여주는 프래그먼트 클래스이다.

  ```kotlin
  class Fragment_now : Fragment() {
  	...
  	private lateinit var recyclerView: RecyclerView
      ...
  ```



- WifiManager을 통해 주변 와이파이를 스캔한다.

  ```kotlin
  wifiManager = view.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
  wifiManager.setWifiEnabled(true)
  var intentFilter= IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
  view.context.registerReceiver(wifiScanReceiver, intentFilter)
  
  // wifi scan 시작
  var success = wifiManager.startScan()
  ```

  

- 스캔한 결과를 BroadcastReceiver을 통해 판단하고 실행한다.

  ```kotlin
  private val wifiScanReceiver = object: BroadcastReceiver(){
      override fun onReceive(c: Context?, intent: Intent?) { // wifiManager.startscan() 시 발동
  
          var suc = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
  
          if(suc == true){
              scanSuccess()
          }else{
              scanFailure()
          }
  
      }
  }
  ...
  
  // Wifi검색 성공
  private fun scanSuccess() {
      results = wifiManager.scanResults
      mAdapter = RecyclerAdapter(results){scanResult ->
          makeDialog(view, scanResult)
          scanResult.capabilities
      }
      view?.wifi_list?.adapter = mAdapter
      view?.TV_wifiCounter?.setText("총 ${mAdapter.itemCount}개의 wifi가 있습니다.")
  
  }
  ...
   // Wifi검색 실패
      private fun scanFailure() {
          view?.TV_wifiCounter?.setText("wifi 탐색에 실패하였습니다.")
      }
  ```

  

- 목록에서 해당 와이파이를 선택하면 비밀번호를 입력하고 접속할 수 있는 다이얼로그가 띄워진다.

  ```kotlin
  private fun makeDialog(view: View?, wifiSelected: ScanResult) {
      val dlg = WifiDialog(view?.context)
      wifiID = wifiSelected.SSID
      dlg.setOnOKClickedListener { content ->
          wifiPW = content
          wifiManager = view?.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
  
          // wifi 연결
          NetworkConnector(wifiManager, context).connectWifi(wifiID, wifiPW)
      }
      dlg.start(wifiID+"에 연결하시겠습니까?")
  
  }
  ```

  

- 새로고침 버튼을 통해 다시 탐색할 수 있다.

  ```kotlin
  view.reScanWifi.setOnClickListener {
      success = wifiManager.startScan()
      myRef.child("wifiList").setValue(null)
      if(!success) Toast.makeText(view.context.applicationContext, "wifi 스캔에 실패하였습니다.", Toast.LENGTH_SHORT).show()
  }
  ```

  

*WifiDialog*

- Fragment_now에서 발생하는 다이얼로그를 커스텀한 Context를 매개변수로 하는 클래스이다.

  ```kotlin
  class WifiDialog(context: Context?) {
      ...
  	fun start(content: String){
          dlg.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀바 제거
          dlg.setContentView(R.layout.wifi_dialog) // 다이얼로그에 사용할 xml 파일 부르기
          dlg.setCancelable(false) // 다이얼로그 바깥 화면 눌렀을 때 다이얼로그가 닫히지 않도록 함.
  
          // TextView 설정
          lblDesc = dlg.findViewById(R.id.wifi_connect_tv)
          lblDesc.text = content
  
          // EditText 설정
          wifiPW = dlg.findViewById(R.id.wifi_pw)
  
          // ok button 설정
          btnOK = dlg.findViewById(R.id.ok)
          btnOK.setOnClickListener{
              listener.onOkClicked(wifiPW.text.toString())
              dlg.dismiss()
          }
  
          btnCancel = dlg.findViewById(R.id.cancel)
          btnCancel.setOnClickListener {
              dlg.dismiss()
          }
          dlg.show()
      }
  
      fun setOnOKClickedListener(listener: (String) -> Unit){
          this.listener = object : WifiDialogOKClickedListener{
              override fun onOkClicked(content: String) {
                  listener(content)
              }
          }
      }
  
      interface WifiDialogOKClickedListener {
          fun onOkClicked(content: String)
      }
  }
  ```



*Fragment_scan*

- 카메라로 글자를 인식해 비밀번호로 입력하면 와이파이에 접속할 수 있는 프래그먼트 클래스이다.

  ```kotlin
  class Fragment_scan : Fragment(){
      ...
  ```



- Fragment_now에서의 현재 와이파이 목록 데이터를 Fragment_scan으로 가져온다.

  ```kotlin
  var e = object : ValueEventListener{
      override fun onCancelled(error: DatabaseError) {
      }
  
      override fun onDataChange(snapshot: DataSnapshot) {
          wifiList = mutableListOf()
  
          for(data in snapshot.children){
              if(data.key.equals("wifiList")) {
                 for(d in data.children){
                     wifiList.add(d.value.toString())
                 }
              
              }
          }
      }
  }
  myRef.addValueEventListener(e)
  ```



- Fragment_scan 내부에 카메라를 가져와서 버튼을 누르면 사진이 저장된다.

  ```kotlin
  view.imageButton.setOnClickListener {
              takePhoto(view)
      ...
  // imageProxy to bitmap
  fun imageProxyToBitmap(imageProxy: ImageProxy) : Bitmap{
      val buffer = imageProxy.planes[0].buffer
      buffer.rewind()
      val bytes = ByteArray(buffer.remaining())
      buffer.get(bytes)
      val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  
      // Rotate bitmap
      val matrix = Matrix()
      matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
  
      return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height,matrix,true)
  }
      ...
  
  // 카메라 구현
  fun bindCameraUseCase(view: View){
      val rotation = 0
      val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
  
      val cameraProviderFuture = ProcessCameraProvider.getInstance(view.context)
      cameraProviderFuture?.addListener(Runnable {
  
          val cameraProvider = cameraProviderFuture.get()
  
          val preview = Preview.Builder()
              .setTargetRotation(rotation)
              .build()
  
          imageCapture = ImageCapture.Builder()
              .setTargetResolution(Size(960,1280))
              .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
              .setTargetRotation(rotation)
              .build()
  
          cameraProvider.unbindAll()
  
          val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
          preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))
  
      },ContextCompat.getMainExecutor(view.context))
  }
      ...
      
  // 사진 캡처
  fun takePhoto(view: View){
          imageCapture?.takePicture(ContextCompat.getMainExecutor(view.context), object : ImageCapture.OnImageCapturedCallback(){
              override fun onCaptureSuccess(image: ImageProxy) {
                  val bitmap = imageProxyToBitmap(image)
                  textRecognize(bitmap)
                  super.onCaptureSuccess(image)
              }
          })
      }
  ```

  

- 비밀번호가 인식되면 수정할 수 있는 다이얼로그가 띄워진다.

  ```kotlin
  private fun showPasswordPopup() {
   val inflater = view?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
      val view = inflater.inflate(R.layout.password_popup, null)
      var password: TextView = view.findViewById(R.id.editText)
      password.text = lineText
  
      val alertDialog = AlertDialog.Builder(view.context)
          .setTitle("비밀번호 확인")
          .setPositiveButton("확인"){ dialog, which ->
              textView.text = "${password.text}"
          }
          .setNeutralButton("취소",null)
          .create()
  
      alertDialog.setView(view)
      alertDialog.show()
  }
  ```

  

- 최종적으로 비밀번호를 입력하고 버튼을 누르면 알맞은 와이파이에 접속한다.

  ```kotlin
  view.checkbtn.setOnClickListener {
      wifiManager = view.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
  
      for (data in wifiList){
          if (data == null || data=="")continue
          else {
              NetworkConnector(wifiManager, context).connectWifi(data, textView.text.toString())
              if(NetworkConnector(wifiManager, context).isWifiConnected(context)) break
          }
      }
  }
  ```





*Fragment_QR*

- 현재 접속한 와이파이의 정보를 QR코드로 나타내 공유할 수 있는 프래그먼트 클래스이다.

  ```kotlin
  class Fragment_QR : Fragment(){
      ...
  ```



- Fragment_now에서 현재 와이파이의 데이터를 Fragment_QR로 가져온다.

  ```kotlin
  var e = object : ValueEventListener {
      override fun onCancelled(error: DatabaseError) {
      }
  
      override fun onDataChange(snapshot: DataSnapshot) {
          for(data in snapshot.children){
              if(data.key.equals("curWifi")) {
                  if(data.child("ssid").key.equals("ssid")){
                      wifiID = data.child("ssid").value as String
                  }
                  if(data.child("password").key.equals("password")){
                      wifiPW = data.child("password").value as String
                  }
              }
          }
      }
  }
  myRef.addValueEventListener(e)
  ```



- 버튼을 누르면 해당 와이파이의 정보가 갖춰진 QR코드를 띄운다.

  ```kotlin
  view.btn_qrmaker.setOnClickListener {
          if (wifiID == "" || wifiPW == "-"){
              wifi_name_QR.setText("wifi를 다시 연결해 주십시오.")
          }
          else {
              currentWifi = CurWifi(wifiID, wifiPW, "WPA")
              QR_image.setImageBitmap(QRmaker(currentWifi.toString()).makeQRBitmap())
              wifi_name_QR.setText(currentWifi?.ssid)
          }
  
  }
  ```



*QRMaker*

- QR 코드를 생성하는 String형을 매개변수로 하는 클래스이다.

  ```kotlin
  class QRmaker(currentWifi: String) {
      ...
   
       fun makeQRBitmap(): Bitmap {
          val multiFormatWriter = MultiFormatWriter()
          var bitmatrix: BitMatrix = multiFormatWriter.encode(currentwifi, BarcodeFormat.QR_CODE, 700, 700)
          val barcodeEncoder = BarcodeEncoder()
          val bitmap: Bitmap = barcodeEncoder.createBitmap(bitmatrix)
  
          return bitmap
      }
  
      init {
          currentwifi = currentWifi
      }
  ```



*CurWifi*

- 현재 연결된 와이파이 정보를 담은 String 형의 ssid, pw, securityType의 매개변수로 하는 클래스이다.

  ```kotlin
  class CurWifi (ssid: String, password: String, securityType: String){
      public var ssid = ssid
      public var password = password
      public var securityType = securityType
  
      override fun toString(): String {
          return "WIFI:T:"+securityType+";S:"+ssid+";P:"+password+";;"
      }
  }
  ```



*NetworkConnector*

- Fragment_scan에서 받은 와이파이 목록 데이터에 연결하기 위한 어댑터로 WifiManager와 Context를 매개변수로 하는 클래스이다.

  ```kotlin
  class NetworkConnector(private val wifiManager: WifiManager, private val context: Context?) {
      ...
  
  ```



- WifiNetworkSuggestion을 활용해 list에 네트워크 suggestion을 추가, 삭제한다.

  ```
  fun connectWifi(ssid: String, pw: String){
      when{
          // WifiNetworkSuggestion 버전 29이상일 때만 사용
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
  
              // 네트워크 Suggestion 삭제
              wifiManager.removeNetworkSuggestions(
                  listOf(
                	...
                  )
              )
  
              // 네트워크 Suggestion 추가
              var status = wifiManager.addNetworkSuggestions(
                  listOf(
                   ...
                  )
              )
              if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS){
                  myRef.child("curWifi").child("ssid").setValue(ssid)
                  myRef.child("curWifi").child("password").setValue(pw)
                  myRef.child("curWifi").child("securityType").setValue("WPA")
              }
  
          }
  
          else -> {
              val wifiConfig = WifiConfiguration().apply {
                  SSID = String.format("\"%s\"", ssid)
                  preSharedKey = String.format("\"%s\"", pw)
              }
              with(wifiManager) {
                  val netId = addNetwork(wifiConfig)
                  disconnect()
                  enableNetwork(netId, true)
              }
              if(isWifiConnected(context)){
                  myRef.child("curWifi").child("ssid").setValue(ssid)
                  myRef.child("curWifi").child("password").setValue(pw)
                  myRef.child("curWifi").child("securityType").setValue("WPA")
              }
          }
      }
  }
  ```



- 와이파이가 연결됐는지 확인한다.

  ```kotlin
  public fun isWifiConnected(context: Context?): Boolean {
  
      var result = false
      val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
      if (capabilities != null){
          if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
          {
              result = true
          }
          else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
              result = false
          }
      }
      return result
  }
  ```

  