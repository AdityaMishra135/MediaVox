package org.skywaves.mediavox.core.views.bottomactionmenu

interface BottomActionMenuCallback {
    fun onItemClicked(item: BottomActionMenuItem) {}
    fun onViewCreated(view: BottomActionMenuView) {}
    fun onViewDestroyed() {}
}
