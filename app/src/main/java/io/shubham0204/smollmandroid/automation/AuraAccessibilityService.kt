package io.shubham0204.smollmandroid.automation

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import io.shubham0204.smollmandroid.automation.DeviceController

class AuraAccessibilityService : AccessibilityService() {
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        DeviceController.setService(this)
        android.util.Log.d("AuraAccessibility", "Service connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Events will be processed in Phase 2
    }
    
    override fun onInterrupt() {
        DeviceController.setService(null)
        android.util.Log.d("AuraAccessibility", "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        DeviceController.setService(null)
        android.util.Log.d("AuraAccessibility", "Service destroyed")
    }
    
    companion object {
        fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
            val expectedComponentName = "${context.packageName}/io.shubham0204.smollmandroid.automation.AuraAccessibilityService"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return enabledServices?.contains(expectedComponentName) == true
        }
        
        fun openAccessibilitySettings(context: android.content.Context) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
