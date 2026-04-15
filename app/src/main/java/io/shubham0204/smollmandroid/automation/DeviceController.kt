package io.shubham0204.smollmandroid.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DeviceController {
    
    private var service: AccessibilityService? = null
    
    fun setService(accessibilityService: AccessibilityService?) {
        service = accessibilityService
    }
    
    fun isServiceActive(): Boolean = service != null
    
    suspend fun tap(x: Int, y: Int): Boolean {
        val currentService = service ?: return false
        
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }
            
            currentService.dispatchGesture(gesture, callback, null)
        }
    }
    
    suspend fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 300): Boolean {
        val currentService = service ?: return false
        
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            currentService.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }, null)
        }
    }
    
    suspend fun longPress(x: Int, y: Int, duration: Long = 1000): Boolean {
        val currentService = service ?: return false
        
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            currentService.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }, null)
        }
    }
    
    fun typeText(text: String): Boolean {
        val currentService = service ?: return false
        val root = currentService.rootInActiveWindow ?: return false
        
        val focusedNode = findFocusedNode(root)
        return if (focusedNode != null) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            true
        } else {
            false
        }
    }
    
    fun pressBack(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) ?: false
    }
    
    fun pressHome(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) ?: false
    }
    
    fun pressRecents(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) ?: false
    }
    
    fun openNotifications(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
    }
    
    fun openQuickSettings(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) ?: false
    }
    
    fun findText(text: String): AccessibilityNodeInfo? {
        val root = service?.rootInActiveWindow ?: return null
        return findNodeByText(root, text)
    }
    
    fun tapOnText(text: String): Boolean {
        val node = findText(text) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    fun scrollForward(): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        return scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
    }
    
    fun scrollBackward(): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        return scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
    }
    
    fun getAllVisibleText(): List<String> {
        val root = service?.rootInActiveWindow ?: return emptyList()
        val textList = mutableListOf<String>()
        collectAllText(root, textList)
        return textList
    }
    
    fun getAllClickableNodes(): List<AccessibilityNodeInfo> {
        val root = service?.rootInActiveWindow ?: return emptyList()
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectClickableNodes(root, nodes)
        return nodes
    }
    
    private fun findFocusedNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val focused = findFocusedNode(child)
            if (focused != null) return focused
        }
        return null
    }
    
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true && node.isClickable) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByText(child, text)
            if (found != null) return found
        }
        return null
    }
    
    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }
    
    private fun collectAllText(node: AccessibilityNodeInfo, list: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { list.add(it) }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectAllText(it, list) }
        }
    }
    
    private fun collectClickableNodes(node: AccessibilityNodeInfo, list: MutableList<AccessibilityNodeInfo>) {
        if (node.isClickable) list.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectClickableNodes(it, list) }
        }
    }
}
