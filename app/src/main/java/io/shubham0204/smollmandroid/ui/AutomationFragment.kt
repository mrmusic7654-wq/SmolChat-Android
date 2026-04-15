package io.shubham0204.smollmandroid.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.automation.AuraAccessibilityService
import io.shubham0204.smollmandroid.automation.DeviceController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutomationFragment : Fragment() {
    
    private lateinit var statusText: TextView
    private lateinit var activeText: TextView
    private lateinit var enableButton: Button
    private lateinit var testHomeButton: Button
    private lateinit var testBackButton: Button
    private lateinit var testTapButton: Button
    private lateinit var testSwipeButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_automation, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        statusText = view.findViewById(R.id.status_text)
        activeText = view.findViewById(R.id.active_text)
        enableButton = view.findViewById(R.id.enable_button)
        testHomeButton = view.findViewById(R.id.test_home)
        testBackButton = view.findViewById(R.id.test_back)
        testTapButton = view.findViewById(R.id.test_tap)
        testSwipeButton = view.findViewById(R.id.test_swipe)
        
        updateStatus()
        
        enableButton.setOnClickListener {
            AuraAccessibilityService.openAccessibilitySettings(requireContext())
        }
        
        testHomeButton.setOnClickListener {
            val success = DeviceController.pressHome()
            showToast("Home: $success")
        }
        
        testBackButton.setOnClickListener {
            val success = DeviceController.pressBack()
            showToast("Back: $success")
        }
        
        testTapButton.setOnClickListener {
            lifecycleScope.launch {
                showToast("Tapping center in 2s...")
                delay(2000)
                val success = DeviceController.tap(540, 960)
                showToast("Tap: $success")
            }
        }
        
        testSwipeButton.setOnClickListener {
            lifecycleScope.launch {
                showToast("Swiping up in 2s...")
                delay(2000)
                val success = DeviceController.swipe(540, 1500, 540, 500)
                showToast("Swipe: $success")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateStatus()
    }
    
    private fun updateStatus() {
        val isEnabled = AuraAccessibilityService.isAccessibilityServiceEnabled(requireContext())
        val isActive = DeviceController.isServiceActive()
        
        statusText.text = if (isEnabled) "Enabled ✅" else "Disabled ❌"
        activeText.text = if (isActive) "Active ✅" else "Inactive ❌"
        
        testHomeButton.isEnabled = isActive
        testBackButton.isEnabled = isActive
        testTapButton.isEnabled = isActive
        testSwipeButton.isEnabled = isActive
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
