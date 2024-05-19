package org.skywaves.mediavox.adapters

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.skywaves.mediavox.core.views.MySquareImageView
import org.skywaves.mediavox.databinding.DirectoryItemGridRoundedCornersBinding
import org.skywaves.mediavox.databinding.DirectoryItemGridSquareBinding
import org.skywaves.mediavox.databinding.DirectoryItemListBinding

interface DirectoryItemBinding {
    val root: ViewGroup
    val dirThumbnail: MySquareImageView
    val dirPath: TextView
    val dirCheck: ImageView
    val dirHolder: ViewGroup
    val photoCnt: TextView
    val dirSize: TextView
    val dirName: TextView
    val dirLock: ImageView
    val dirPin: ImageView
    val dirLocation: ImageView
    val dirDragHandle: ImageView
    val dirDragHandleWrapper: ViewGroup?
}

class ListDirectoryItemBinding(val binding: DirectoryItemListBinding) : DirectoryItemBinding {
    override val root: ViewGroup = binding.root
    override val dirThumbnail: MySquareImageView = binding.dirThumbnail
    override val dirPath: TextView = binding.dirPath
    override val dirCheck: ImageView = binding.dirCheck
    override val dirHolder: ViewGroup = binding.dirHolder
    override val photoCnt: TextView = binding.photoCnt
    override val dirSize: TextView = binding.dirSize
    override val dirName: TextView = binding.dirName
    override val dirLock: ImageView = binding.dirLock
    override val dirPin: ImageView = binding.dirPin
    override val dirLocation: ImageView = binding.dirLocation
    override val dirDragHandle: ImageView = binding.dirDragHandle
    override val dirDragHandleWrapper: ViewGroup? = null
}

fun DirectoryItemListBinding.toItemBinding() = ListDirectoryItemBinding(this)

class GridDirectoryItemRoundedCornersBinding(val binding: DirectoryItemGridRoundedCornersBinding) : DirectoryItemBinding {
    override val root: ViewGroup = binding.root
    override val dirThumbnail: MySquareImageView = binding.dirThumbnail
    override val dirPath: TextView = binding.dirPath
    override val dirCheck: ImageView = binding.dirCheck
    override val dirHolder: ViewGroup = binding.dirHolder
    override val photoCnt: TextView = binding.photoCnt
    override val dirSize: TextView = binding.dirSize
    override val dirName: TextView = binding.dirName
    override val dirLock: ImageView = binding.dirLock
    override val dirPin: ImageView = binding.dirPin
    override val dirLocation: ImageView = binding.dirLocation
    override val dirDragHandle: ImageView = binding.dirDragHandle
    override val dirDragHandleWrapper: ViewGroup = binding.dirDragHandleWrapper
}

fun DirectoryItemGridRoundedCornersBinding.toItemBinding() = GridDirectoryItemRoundedCornersBinding(this)
