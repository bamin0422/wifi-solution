## With fi 기술 명세서





*NetworkConnerctor*

- Fragment_now와 Fragment_Scan에서 접속을 시도할 때 파이어베이스를 이용해 Realtime Database를 이용해 wifi리스트를 가져와 WifiSuggestion을 이용해 와이파이에 접속할 수 있게 구현한 WifiMangager, Context를 매개변수로 담은 클래스이다.

  ```kotlin
  class NetworkConnector(private val wifiManager: WifiManager, private val context: Context?) {
      ...
  // ssid와 비밀번호가 있는 객체들을 받을 때 Suggestion list에 넣거나 삭제
  fun connectWifi(ssid: String, pw: String){
      when{
          // WifiNetworkSuggestion 버전 29이상일 때만 사용
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
  
              // list 형태의 네트워크 Suggestion 삭제
              wifiManager.removeNetworkSuggestions(
                  listOf(
                    ...
                  )
              )
  
              // list 형태의 네트워크 Suggestion 추가
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
  
  // 연결 되었는지 확인
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

  

*QRmaker*

- Fragment_QR에서 QR코드를 생성하는 String을 매개변수로 담는 클래스이다.

  ```kotlin
  class QRmaker(currentWifi: String) {
  ...
  ```

  

- QR코드를 생성하기 위해 Zxing 라이브러리를 사용하였다.

  ```kotlin
  fun makeQRBitmap(): Bitmap {
      val multiFormatWriter = MultiFormatWriter()
      var bitmatrix: BitMatrix = multiFormatWriter.encode(currentwifi, BarcodeFormat.QR_CODE, 700, 700)
      val barcodeEncoder = BarcodeEncoder()
      val bitmap: Bitmap = barcodeEncoder.createBitmap(bitmatrix)
  
      return bitmap
  }
  ```



*WifiDialog*

- Fragment_now에서 해당 와이파이를 터치하면 연결할지 여부를 묻는 다이얼로그를 생성하는 Context를 매개변수로 담는 클래스이다.

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
  
  ```



*RecyclerAdapter*

- Fragment_now에서 RecyclerView을 만들기 위한 어댑터 역할을 하는 ScanResult형의 리스트와 ScanResult로 입력을 받아 Unit형으로 반환하는 프로포티를 매개변수로 담는 클래스이다.

  ```kotlin
  class RecyclerAdapter(var items: List<ScanResult>, val itemClick: (ScanResult) -> Unit) :
      RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
          ...
      // ViewHolder 단위 객체로 View의 데이터를 설정
      class ViewHolder (v: View, itemClick: (ScanResult) -> Unit): RecyclerView.ViewHolder(v){
  
          var tvWifiName: TextView = v.findViewById(R.id.wifi_name)
          var myRef:DatabaseReference = FirebaseDatabase.getInstance().reference
          var view = v
          var itemclick = itemClick
  
          fun setItem(item: ScanResult) {
              tvWifiName.setText(item.SSID)
              myRef.child("wifiList").push().setValue(item.SSID)
              view.setOnClickListener {
                  itemclick(item)
              }
          }
      }
  
  
      // 보여줄 아이템 개수만큼 View를 생성
      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
          var itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.wifi_list_item, parent, false)
          return ViewHolder(itemView, itemClick)
      }
  
      override fun getItemCount(): Int = items.size // items의 크기를 구하는 역할
  
      // 생성된 View에 보여줄 데이터를 설정
      override fun onBindViewHolder(holder: ViewHolder, position: Int) {
          holder.setItem(items.get(position))
      }
  }
  ```

  

*CurWifi*

- 현재 와이파이 정보를 담아주는 역할을 하는 String 형의 정보들을 매개변수로 담는 클래스이다.

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



*Fragment_QR*

- 버튼을 누르면  QR Maker에서 생성된 연결된 와이파이의 정보를 가져 QR코드를 통해 공유할 수 있게 하는 프래그먼트이다.

  ```kotlin
  class Fragment_QR : Fragment(){
      ...
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

- Fragment_now에서 현재 연결된 와이파이 정보를 가져온다.

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
  ```

  

*Fragment_Scan*

- 카메라로 글자를 인식하고 그 결과를 보여주는 프래그먼트이다.

  ```kotlin
  class Fragment_scan : Fragment(){
  	...
  ```

  

- 카메라를 앱 내부에서 나타나도록 구현했다.

  ```kotlin
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
  ```

  

- 글자를 인식하기 위해 파이어베이스를 사용하였다. (OCR)

  ```kotlin
  fun takePhoto(view: View){
      imageCapture?.takePicture(ContextCompat.getMainExecutor(view.context), object : ImageCapture.OnImageCapturedCallback(){
          override fun onCaptureSuccess(image: ImageProxy) {
              val bitmap = imageProxyToBitmap(image)
              textRecognize(bitmap)
              super.onCaptureSuccess(image)
          }
      })
  }
  
  fun textRecognize(bitmap: Bitmap){
      FirebaseVision.getInstance().onDeviceTextRecognizer.processImage(FirebaseVisionImage.fromBitmap(bitmap))
          .addOnSuccessListener {firebaseVisionText ->
              for(block in firebaseVisionText.textBlocks){
  
                  lineText = block.text
                  view?.textView?.setText(lineText)
                  showPasswordPopup()
                  break
  
              }
          }
  }
  ```

  

- 카메라에 얻고자 하는 글자가 나온 상태서 버튼을 누르면 해당 글자가 비밀번호가 맞는지 확인하기 위한 다이얼로그를 구현하였다.

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



- 다이얼로그에서 수정 후 확인을 누르면 화면에 해당 비밀번호가 나오고 확인 버튼을 누르면 해당 와이파이와 접속할 수 있다.

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

  

*Fragment_now*

- 현재 와이파이 목록을 나타내는 프래그먼트이다.

  ```kotlin
  class Fragment_now : Fragment() {
  	...
  ```

  

- 와이파이 목록을 RecyclerAdapter클래스에서 recyclerView을 이용했다.

  ```kotlin
  private lateinit var recyclerView: RecyclerView
  ```

  

- WifiManager을 이용해 와이파이를 찾아  BroadcastReceiver을 통해 목록에 저장한다.

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

  

- 목록에서 해당 와이파이를 터치하면 비밀번호를 입력할 수 있게끔 다이얼로그도 구현했다.

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

  

- 그 외에도 새로고침 버튼을 누르면 주변 와이파이 목록이 다시 설정될 수 있게 구현했다.

  ```kotlin
  view.reScanWifi.setOnClickListener {
      success = wifiManager.startScan()
      myRef.child("wifiList").setValue(null)
      if(!success) Toast.makeText(view.context.applicationContext, "wifi 스캔에 실패하였습니다.", Toast.LENGTH_SHORT).show()
  }
  ```



*IntroActivity*

- 처음 어플을 실행했을 때 handler을 통해 정해진 시간동안 인트로 화면을 띄우고, 시작할 수 있게 하였다.

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
  ```



*MainPageAdapter*

- Viewpager와 fragment를 연결하는 어댑터 역할을 하는 FragmentManager을 매개변수로 담는 클래스이다.

  ```kotlin
  class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
  	...
  
      override fun getItem(frag_position: Int): Fragment {
          return when(frag_position){
              0 -> Fragment_now()
              1 -> Fragment_scan()
              else -> Fragment_QR()
          }
      }
  ```

  

*MainActivity*

- 전체적인 액티비티를 담당하는 곳으로 이 안에는 글자를 인식할 수 있는 scan 프래그먼트, 현재 와이파이 목록을 나타내주는 now 프래그먼트, 현재 와이파이를 QR코드로 공유할 수 있는 QR 프래그먼트로 구성되어 있다.

  ```kotlin
  class MainActivity : AppCompatActivity() {
      ...
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
  ```

  

- 각 프래그먼트로 이동할 수 있는 NavigationView를 설정했다.

  ```kotlin
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
  ```

  

- 스캔, 와이파이 등을 이용하기 위한 권한들을  설정했다.

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
  ```