package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.callback

/**
 * Interface for listening to drag progress changes.
 */
fun interface DragListener {
    /**
     * Called when the drag progress changes.
     * 
     * @param progress The current drag progress (0f to 1f).
     */
    fun onDrag(progress: Float)
}

/**
 * Interface for listening to drag state changes.
 */
interface DragStateListener {
    /**
     * Called when the drag starts.
     */
    fun onDragStart()
    
    /**
     * Called when the drag ends.
     * 
     * @param isMenuOpened Whether the menu is opened after the drag ends.
     */
    fun onDragEnd(isMenuOpened: Boolean)
}