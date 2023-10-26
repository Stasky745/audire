package com.alexmercerind.audire.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.alexmercerind.audire.R
import com.alexmercerind.audire.databinding.FragmentIdentifyBinding
import com.alexmercerind.audire.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IdentifyFragment : Fragment() {
    private var _binding: FragmentIdentifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentifyFragmentViewModel by viewModels()

    private lateinit var idleFloatingActionButtonObjectAnimator: ObjectAnimator
    private lateinit var visibilityRecordFloatingActionButtonObjectAnimator: ObjectAnimator

    private lateinit var visibilityStopButtonObjectAnimator: ObjectAnimator

    private lateinit var visibilityWaveViewObjectAnimator: ObjectAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIdentifyBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.recordFloatingActionButton.setOnClickListener {
            // Request Manifest.permission.RECORD_AUDIO.
            requestRecordAudioPermission()

            if (checkRecordAudioPermission()) {
                viewModel.start()

                hideRecordFloatingActionButton()
            } else {
                // Manifest.permission.RECORD_AUDIO is not available.
                showRecordAudioPermissionNotAvailableDialog()
            }
        }

        binding.stopButton.setOnClickListener {
            // Stop the recording.
            viewModel.stop()

            showRecordFloatingActionButton()
        }

        // https://stackoverflow.com/a/70718428/12825435
        viewModel.seconds.observe(viewLifecycleOwner) {
            binding.stopButton.text = String.format("00:%02d", it)
        }
        viewModel.data.observe(viewLifecycleOwner) {
            showRecordFloatingActionButton()

            // TODO: Handle audio data.
        }

        idleFloatingActionButtonObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.recordFloatingActionButton,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F),
        ).apply {
            duration = 2000L
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        visibilityRecordFloatingActionButtonObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.recordFloatingActionButton,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5F, 1.0F),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5F, 1.0F),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.0F, 1.0F),
        ).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
        }

        visibilityStopButtonObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.stopButton,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5F, 1.0F),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5F, 1.0F),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.0F, 1.0F),
        ).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
        }

        visibilityWaveViewObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.waveView,
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.0F, 1.0F),
        ).apply {
            duration = 500L
            interpolator = AccelerateDecelerateInterpolator()
        }

        if (viewModel.recording) {
            binding.recordFloatingActionButton.scaleX = 0.5F
            binding.recordFloatingActionButton.scaleY = 0.5F
            binding.recordFloatingActionButton.alpha = 0.0F
            binding.stopButton.scaleX = 1.0F
            binding.stopButton.scaleY = 1.0F
            binding.stopButton.alpha = 1.0F
            binding.waveView.alpha = 1.0F
            idleFloatingActionButtonObjectAnimator.cancel()
        } else {
            binding.recordFloatingActionButton.scaleX = 1.0F
            binding.recordFloatingActionButton.scaleY = 1.0F
            binding.recordFloatingActionButton.alpha = 1.0F
            binding.stopButton.scaleX = 0.5F
            binding.stopButton.scaleY = 0.5F
            binding.stopButton.alpha = 0.0F
            binding.waveView.alpha = 0.0F
            idleFloatingActionButtonObjectAnimator.start()
            idleFloatingActionButtonObjectAnimator.start()
        }

        return view
    }

    private fun showRecordFloatingActionButton() {
        idleFloatingActionButtonObjectAnimator.start()
        visibilityRecordFloatingActionButtonObjectAnimator.start()
        visibilityStopButtonObjectAnimator.reverse()
        visibilityWaveViewObjectAnimator.reverse()
    }

    private fun hideRecordFloatingActionButton() {
        idleFloatingActionButtonObjectAnimator.cancel()
        visibilityRecordFloatingActionButtonObjectAnimator.reverse()
        visibilityStopButtonObjectAnimator.start()
        visibilityWaveViewObjectAnimator.start()
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 0
        )
    }

    private fun checkRecordAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRecordAudioPermissionNotAvailableDialog() {
        MaterialAlertDialogBuilder(
            requireActivity(), R.style.Base_Theme_Audire_MaterialAlertDialog
        ).setTitle(R.string.identify_record_permission_not_available_title)
            .setMessage(R.string.identify_record_permission_not_available_message)
            .setPositiveButton(
                R.string.ok
            ) { dialog, _ -> dialog?.dismiss() }.create().show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        viewModel.stop()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 0) {
            Log.d(Constants.LOG_TAG, permissions.toString())
            Log.d(Constants.LOG_TAG, grantResults.toString())
        }
    }

}
