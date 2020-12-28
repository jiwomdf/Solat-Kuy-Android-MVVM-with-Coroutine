package com.programmergabut.solatkuy.ui.activitymain

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.base.BaseActivity
import com.programmergabut.solatkuy.data.local.SolatKuyRoom
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.databinding.ActivityMainBinding
import com.programmergabut.solatkuy.databinding.LayoutBottomsheetBygpsBinding
import com.programmergabut.solatkuy.databinding.LayoutBottomsheetBylatitudelongitudeBinding
import com.programmergabut.solatkuy.databinding.LayoutFristopenappBinding
import com.programmergabut.solatkuy.ui.SolatKuyFragmentFactory
import com.programmergabut.solatkuy.util.EnumStatus
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import org.joda.time.LocalDate
import javax.inject.Inject

/*
 * Created by Katili Jiwo Adi Wiyono on 25/03/20.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityViewModel>(
    R.layout.activity_main, MainActivityViewModel::class.java
), View.OnClickListener {

    private lateinit var bottomSheetDialog: Dialog
    private lateinit var firstOpenDialog: Dialog
    private lateinit var bsByGpsBinding: LayoutBottomsheetBygpsBinding
    private lateinit var bsByLatLngBinding: LayoutBottomsheetBylatitudelongitudeBinding
    private lateinit var dialogBinding: LayoutFristopenappBinding

    @Inject
    lateinit var fragmentFactory: SolatKuyFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bottomSheetDialog = BottomSheetDialog(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    override fun setListener() {
        super.setListener()

        inflateBinding()
        dialogBinding.btnByLatitudeLongitude.setOnClickListener(this)
        dialogBinding.btnByGps.setOnClickListener(this)
        bsByGpsBinding.btnProceedByGps.setOnClickListener(this)
        bsByLatLngBinding.btnProceedByLL.setOnClickListener(this)
        observeDb()
        observeErrorMsg()
    }

    override fun onDestroy() {
        setIsHasOpenAnimation(false)
        super.onDestroy()
    }

    private fun inflateBinding() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.layout_fristopenapp, null, false
        )
        bsByLatLngBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.layout_bottomsheet_bylatitudelongitude, null, false
        )
        bsByGpsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.layout_bottomsheet_bygps, null, false
        )
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_by_latitude_longitude -> {
                bottomSheetDialog.setContentView(bsByLatLngBinding.root)
                bottomSheetDialog.show()
            }
            R.id.btn_proceedByLL -> {
                val latitude = bsByLatLngBinding.etLlDialogLatitude.text.toString().trim()
                val longitude = bsByLatLngBinding.etLlDialogLongitude.text.toString().trim()
                insertLocationSettingToDb(latitude, longitude)
            }
            R.id.btn_by_gps -> {
                bottomSheetDialog.setContentView(bsByGpsBinding.root)
                bottomSheetDialog.show()
                getGPSLocation()
            }
            R.id.btn_proceedByGps -> {
                if(bsByGpsBinding.tvGpsDialogLatitude.visibility != View.VISIBLE &&
                    bsByGpsBinding.tvViewLongitude.visibility != View.VISIBLE){
                    startActivity(Intent(ACTION_SETTINGS))
                }
                else{
                    val latitude = bsByGpsBinding.tvGpsDialogLatitude.text.toString().trim()
                    val longitude = bsByGpsBinding.tvGpsDialogLongitude.text.toString().trim()
                    insertLocationSettingToDb(latitude, longitude)
                }
            }
        }
    }

    private fun observeDb(){
        viewModel.msSetting.observe(this, {
            if(it != null){
                if(it.isHasOpenApp)
                    initBottomNav()
                else
                    initDialog()
            }
            else{
                SolatKuyRoom.populateDatabase(getDatabase())
            }
        })
    }

    private fun observeErrorMsg() {
        viewModel.errMsApi1Status.observe(this, {
            val errMsg = viewModel.getErrMsApi1Msg()
            if(errMsg.isEmpty())
                return@observe

            when(it) {
                EnumStatus.SUCCESS -> {
                    Toasty.success(this, errMsg, Toasty.LENGTH_SHORT).show()
                    updateIsHasOpenApp()
                }
                EnumStatus.ERROR -> {
                    Toasty.error(this, errMsg, Toasty.LENGTH_SHORT).show()
                }
                else -> {/* NO-OP */}
            }
        })
    }

    private fun initDialog() {
        firstOpenDialog =  Dialog(this@MainActivity)
        firstOpenDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        firstOpenDialog.setCancelable(false)
        firstOpenDialog.setContentView(dialogBinding.root)
        firstOpenDialog.show()
    }

    private fun initBottomNav() {
        try{
            binding.bottomNavigation.setupWithNavController(binding.navHostFragment.findNavController())
            binding.navHostFragment.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    when(destination.id){
                        R.id.fragmentMain,
                        R.id.fragmentCompass,
                        R.id.quranFragment,
                        R.id.fragmentSetting -> binding.bottomNavigation.visibility = View.VISIBLE
                        else -> binding.bottomNavigation.visibility = View.GONE
                    }
                }
            binding.bottomNavigation.setOnNavigationItemReselectedListener {/* NO-OP */ }
        }
        catch (ex: Exception){
            print(ex.message)
        }
    }

    private fun updateIsHasOpenApp() {
        bottomSheetDialog.dismiss()
        firstOpenDialog.dismiss()
        viewModel.updateIsHasOpenApp(true)
    }

    /* Database Transaction */
    private fun insertLocationSettingToDb(latitude: String, longitude: String) {
        val currDate = LocalDate()
        val data = MsApi1(1,
            latitude,
            longitude,
            "3",
            currDate.monthOfYear.toString(),
            currDate.year.toString())

        viewModel.updateMsApi1(data)
    }

    /* permission */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == LOCATION_PERMISSIONS){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onUpdateLocationListener()
            }
            else{
                bottomSheetDialog.dismiss()
                Toasty.error(this, getString(R.string.permission_is_needed_to_run_the_gps), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionDialog(){
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_needed))
            .setMessage(getString(R.string.permission_is_needed_to_run_the_gps))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.oke)) { _: DialogInterface, _: Int ->
                ActivityCompat.requestPermissions(this, listLocationPermission(), LOCATION_PERMISSIONS)
            }
            .setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int ->
                bottomSheetDialog.dismiss()
            }
            .create()
            .show()
    }

    /* supporting function */
    private fun getGPSLocation(){
        if (isLocationPermissionGranted()) {
            setGpsBottomSheetState()
            onUpdateLocationListener()
        }
        else {
            showPermissionDialog()
            return
        }
    }

    private fun setGpsBottomSheetState() {
        val lm: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) { }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) { }

        if (!gpsEnabled && !networkEnabled)
            setGpsComponentState(EnumStatus.ERROR)
        else
            setGpsComponentState(EnumStatus.LOADING)
    }

    @SuppressLint("MissingPermission")
    private fun onUpdateLocationListener(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 60 * 1000 /* 1 minute */
        mLocationRequest.fastestInterval = 1 * 1000 /* 1 second */

        if (!isLocationPermissionGranted()) {
            showPermissionDialog()
            return
        }

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    setGpsComponentState(EnumStatus.SUCCESS)
                    bsByGpsBinding.tvGpsDialogLatitude.text = location.latitude.toString()
                    bsByGpsBinding.tvGpsDialogLongitude.text= location.longitude.toString()
                }
            }, Looper.myLooper())
    }

    private fun setGpsComponentState(status: EnumStatus){
        when(status){
            EnumStatus.SUCCESS -> {
                bsByGpsBinding.tvViewLatitude.visibility = View.VISIBLE
                bsByGpsBinding.tvViewLongitude.visibility = View.VISIBLE
                bsByGpsBinding.tvGpsDialogLongitude.visibility = View.VISIBLE
                bsByGpsBinding.tvGpsDialogLatitude.visibility = View.VISIBLE
                bsByGpsBinding.ivWarning.visibility = View.INVISIBLE
                bsByGpsBinding.tvWarning.visibility = View.INVISIBLE
                bsByGpsBinding.btnProceedByGps.text = getString(R.string.proceed)

                bsByGpsBinding.btnProceedByGps.visibility = View.VISIBLE
                bsByGpsBinding.btnProceedByGps.text = getString(R.string.proceed)
            }
            EnumStatus.LOADING -> {
                bsByGpsBinding.ivWarning.visibility = View.VISIBLE
                bsByGpsBinding.tvWarning.visibility = View.VISIBLE
                bsByGpsBinding.tvWarning.text = getString(R.string.loading)
                bsByGpsBinding.tvViewLatitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvViewLongitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvGpsDialogLongitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvGpsDialogLatitude.visibility = View.INVISIBLE

                bsByGpsBinding.btnProceedByGps.visibility = View.INVISIBLE
            }
            EnumStatus.ERROR -> {
                bsByGpsBinding.ivWarning.visibility = View.VISIBLE
                bsByGpsBinding.tvWarning.visibility = View.VISIBLE
                bsByGpsBinding.tvWarning.text = getString(R.string.please_enable_your_location)
                bsByGpsBinding.tvGpsDialogLongitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvGpsDialogLatitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvViewLatitude.visibility = View.INVISIBLE
                bsByGpsBinding.tvViewLongitude.visibility = View.INVISIBLE
                bsByGpsBinding.btnProceedByGps.text = getString(R.string.open_setting)

                bsByGpsBinding.btnProceedByGps.visibility = View.VISIBLE
                bsByGpsBinding.btnProceedByGps.text = getString(R.string.open_setting)
            }
        }
    }

    /* VIEW PAGER */
    /* override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.i_prayer_time -> vp2_main.currentItem = 0
            R.id.i_compass -> vp2_main.currentItem = 1
            R.id.i_quran -> vp2_main.currentItem = 2
            R.id.i_info -> vp2_main.currentItem = 3
            R.id.i_setting -> vp2_main.currentItem = 4
        }
        return true
    } */

    /* Animation */
    /* private inner class ZoomOutPageTransformer : ViewPager.PageTransformer, ViewPager2.PageTransformer {

        private val MIN_SCALE = 0.85f
        private val MIN_ALPHA = 0.5f
        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> alpha = 0f
                    position <= 1 -> {
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = MIN_SCALE.coerceAtLeast(1 - kotlin.math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) horzMargin - vertMargin / 2 else horzMargin + vertMargin / 2
                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA + (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> alpha = 0f
                }
            }
        }
    } */


}
