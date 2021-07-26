package com.ojomono.ionce.ui.roll.group

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.isNotEmpty
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.QR_CODE
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentQRCodeScannerDialogBinding
import com.ojomono.ionce.ui.dialogs.AlertDialog
import com.ojomono.ionce.utils.StringResource
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.ui.bases.FullScreenDialogFragment
import com.ojomono.ionce.utils.proxies.QRCodeGenerator

class QRCodeScannerDialogFragment : FullScreenDialogFragment() {

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // the fragment initialization parameters
        private const val ARG_REQUEST_KEY = "request-key"
        private const val ARG_BUNDLE_KEY = "bundle-key"

        // Fragment tags
        const val FT_PERMISSIONS = "permissions"

        /**
         * Use this factory method to create a new instance of this fragment
         */
        fun newInstance(requestKey: String, bundleKey: String) =
            QRCodeScannerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REQUEST_KEY, requestKey)
                    putString(ARG_BUNDLE_KEY, bundleKey)
                }
            }
    }

    /************/
    /** Fields **/
    /************/

    private lateinit var requestKey: String
    private lateinit var bundleKey: String
    private lateinit var binding: FragmentQRCodeScannerDialogBinding
    private var cameraSource: CameraSource? = null

    // TODO move to generic permission handling when made
    // Register the permissions callback
    private var actionWaitingForPermission: (() -> Unit)? = null    // Action to run when granted
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) actionWaitingForPermission?.invoke()
            else handlePermissionDenied()
            actionWaitingForPermission = null   // Anyway, action is not waiting anymore
        }

    private val processor = object : Detector.Processor<Barcode> {

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            detections?.apply {
                if (detectedItems.isNotEmpty()) {
                    val qr = detectedItems.valueAt(0)
                    handleDetections(qr.rawValue)
                }
            }
        }

        override fun release() {}
    }

    private val callback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            // Ideally, you should check the condition somewhere
            // before inflating the layout which contains the SurfaceView
            if (isPlayServicesAvailable(requireActivity()))
                checkPermissionAndInvoke {
                    // Checking again to avoid lint error...
                    if (context?.let {
                            ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
                        } == PackageManager.PERMISSION_GRANTED
                    ) cameraSource?.start(holder)
                }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource?.stop()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the arguments
        arguments?.let {
            requestKey = requireNotNull(it.getString(ARG_REQUEST_KEY))
            bundleKey = requireNotNull(it.getString(ARG_BUNDLE_KEY))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set the data binding
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_q_r_code_scanner_dialog,
                container,
                false
            )
        binding.lifecycleOwner = viewLifecycleOwner

        // Set the action bar
        setActionBar(binding.toolbar, StringResource(R.string.q_r_code_scanner_title))

        // Set camera view
        setupCameraView()
        binding.surfaceView.holder.addCallback(callback)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    /***********************/
    /** private methods **/
    /***********************/

    private fun setupCameraView() = checkPermissionAndInvoke {
        BarcodeDetector.Builder(requireContext()).setBarcodeFormats(QR_CODE).build()
            .apply {
                setProcessor(processor)
                if (!isOperational) {
                    Log.d(TAG, "Native QR detector dependencies not available!")
                } else
                    cameraSource = CameraSource.Builder(requireContext(), this)
                        .setAutoFocusEnabled(true)
                        .setFacing(CameraSource.CAMERA_FACING_BACK).build()
            }
    }

    // TODO move to generic permission handling when made
    private fun checkPermissionAndInvoke(func: () -> Unit) {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED -> func.invoke()
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                    showRequestPermissionRationale()
                else -> {
                    actionWaitingForPermission = func   // Store function to run when granted
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    // TODO move to generic permission handling when made
    private fun showRequestPermissionRationale() =
        binding.mainLayout.showSnackbar(
            binding.root,
            getString(R.string.group_roll_permission_rationale_message),
            Snackbar.LENGTH_INDEFINITE,
            getString(R.string.dialog_ok)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    // TODO move to generic permission handling when made
    private fun handlePermissionDenied() =
        AlertDialog(
            message = StringResource(getString(R.string.group_roll_permission_denied_message)),
            withNegative = false,
            okButtonText = StringResource(getString(R.string.dialog_ok)),
            onPositive = ::dismiss
        ).show(parentFragmentManager, FT_PERMISSIONS)

    // TODO move to generic permission handling when made
    private fun View.showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }

    /**
     * Helper method to check if Google Play Services are up to-date on the phone
     */
    private fun isPlayServicesAvailable(activity: Activity): Boolean {
        val code = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(activity.applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, code)?.show()
            return false
        }
        return true
    }

    /**
     * The method that actually handles the detected QR-code
     */
    private fun handleDetections(value: String) {
        if (value.startsWith(QRCodeGenerator.QR_CODE_PREFIX)) {
            setFragmentResult(
                requestKey,
                bundleOf(bundleKey to value.removePrefix(QRCodeGenerator.QR_CODE_PREFIX))
            )
            dismiss()
        }
    }
}