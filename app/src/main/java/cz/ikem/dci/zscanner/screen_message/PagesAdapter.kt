package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.Utils
import kotlinx.android.synthetic.main.page_row.view.*
import java.io.File
import java.io.FileOutputStream

class PagesAdapter(private var mActions: PageActionsQueue, val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<PagesAdapter.ViewHolder>() {

    private val TAG = PagesAdapter::class.java.simpleName

    private var mPages: MutableList<PageActionsQueue.Page> = mActions.makePages().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.page_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mPages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bitmap = getBitmap(mPages[position].path)
        //unpackBitmap(mPages[position].path)
        val imageView = holder.mItemView.page_imageview
        imageView.setImageBitmap(bitmap)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.adjustViewBounds = true
    }

    class ViewHolder(val mItemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mItemView)

    fun syncActionsQueue(viewModel: CreateMessageViewModel) {
        val currentPageActions = viewModel.pageActionsQueue
        val diffs = currentPageActions.subtractActionsList(mActions.actionsList())
        if (diffs.count() > 0) {
            for (diff in diffs) {
                when (diff.type) {
                    PageActionsQueue.PageActionType.ADDED -> {
                        if (diff.target < 0) {
                            mPages.add(diff.page)
                            notifyItemInserted(mPages.count() - 1)
                        } else {
                            mPages.add(diff.target, diff.page)
                            notifyItemInserted(diff.target)
                        }
                    }
                    PageActionsQueue.PageActionType.REMOVED -> {
                        val idx = mPages.indexOf(diff.page)
                        mPages.remove(diff.page)
                        notifyItemRemoved(idx)
                    }
                    PageActionsQueue.PageActionType.MOVED -> {
                        val idx = mPages.indexOf(diff.page)
                        val pg = diff.page
                        mPages.remove(diff.page)
                        mPages.add(diff.target, pg)
                        notifyItemMoved(idx, diff.target)
                    }
                }
            }
        }
        mActions = viewModel.pageActionsQueue.clone()
    }

    private fun getBitmap(path: String): Bitmap {

        val digest = Utils.digest(path)

        val directory = context.cacheDir
        val file = File(directory, digest)

        val bitmap: Bitmap

        if (file.exists()) {
            // if cached thumbnail exists, just unpack and return
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = 1
            bmOptions.inPurgeable = true
            bitmap = BitmapFactory.decodeFile(file.absolutePath)
            Log.d(TAG, "Found cached thumb for image ${path}")

        } else {
            // if no cached thumbnail, unpack original image and save thumb in cache
            bitmap = Utils.unpackBitmap(path)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, FileOutputStream(file))
            Log.d(TAG, "Generated thumb for image ${path}")
        }

        return bitmap
    }

}



