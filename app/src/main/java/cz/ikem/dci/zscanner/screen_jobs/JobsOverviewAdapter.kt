package cz.ikem.dci.zscanner.screen_jobs

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import cz.ikem.dci.zscanner.PROGRESS_INDICATOR_PAGE_WEIGHT
import cz.ikem.dci.zscanner.PROGRESS_INDICATOR_SUMMARY_WEIGHT
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.SendJob
import kotlinx.android.synthetic.main.job_row.view.*


class JobsOverviewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<JobsOverviewAdapter.ViewHolder>() {

    private val TAG = JobsOverviewCallback::class.java.simpleName
    var items: List<SendJobInfo> = listOf()

    private var mRecyclerView: androidx.recyclerview.widget.RecyclerView? = null

    class ViewHolder(val mItemView: View, var mAnimator: ValueAnimator? = null) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mItemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.job_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.size == 0) {

            val sendJobInfo = items[position]

            holder.mItemView.pat_id_textview.text = sendJobInfo.patid
            holder.mItemView.info_textview.text = sendJobInfo.desc

            holder.mAnimator = null

            val progressBarView = holder.mItemView.progress_bar
            val doneView = holder.mItemView.done_imageview
            val percentView = holder.mItemView.percent_textview


            when (sendJobInfo.status) {
                SendJobInfoStatus.RUNNING -> {
                    progressBarView.visibility = View.VISIBLE
                    progressBarView.alpha = 1f
                    progressBarView.progress = sendJobInfo.percent
                    percentView.visibility = View.VISIBLE
                    percentView.alpha = 1f
                    percentView.text = sendJobInfo.percent.toString()
                    doneView.visibility = View.GONE
                }
                else -> {
                    progressBarView.visibility = View.GONE
                    percentView.visibility = View.GONE
                    doneView.visibility = View.VISIBLE
                    doneView.alpha = 1f
                }
            }

        } else {

            val diff = (payloads[0] as Pair<SendJobInfo, SendJobInfo>)
            val doneImage = holder.mItemView.done_imageview
            val progressbar = holder.mItemView.progress_bar
            val percentText = holder.mItemView.percent_textview

            progressbar.visibility = View.VISIBLE
            percentText.visibility = View.VISIBLE
            doneImage.visibility = View.GONE

            var from = diff.first.percent

            if ((from != 0) && (holder.mAnimator != null)) {
                holder.mAnimator!!.pause()
                from = holder.mAnimator!!.animatedValue as Int
                holder.mAnimator!!.cancel()
            }

            val to = diff.second.percent

            val animator = ValueAnimator.ofInt(from, to)
            animator.addUpdateListener { animation ->
                percentText.text = animation.animatedValue.toString()
                progressbar.progress = animation.animatedValue as Int
            }
            animator.duration = 500
            animator.interpolator = DecelerateInterpolator()
            animator.start()
            holder.mAnimator = animator

            if (to == 100) {
                ObjectAnimator.ofFloat(progressbar, View.ALPHA, 1f, 0f).apply {
                    startDelay = 500
                    duration = 250
                    start()
                }
                ObjectAnimator.ofFloat(percentText, View.ALPHA, 1f, 0f).apply {
                    startDelay = 500
                    duration = 250
                    start()
                }
                doneImage.visibility = View.VISIBLE
                doneImage.alpha = 0f
                ObjectAnimator.ofFloat(doneImage, View.ALPHA, 0f, 1f).apply {
                    startDelay = 750
                    duration = 250
                    start()
                }

            }

        }
    }


    data class SendJobInfo(val instanceId: String, val ts: Long, val patid: String, val desc: String, val percent: Int, val status: SendJobInfoStatus)
    enum class SendJobInfoStatus { RUNNING, DONE }

    private fun makeInfos(sjlist: List<SendJob>): List<SendJobInfo> {
        val infos = sjlist.map { e ->

            val totalVis = e.pageFiles.size * PROGRESS_INDICATOR_PAGE_WEIGHT + 1 * PROGRESS_INDICATOR_SUMMARY_WEIGHT
            val donePages = e.intDoneTasks.filter {
                it.contains("-P-") // set in SendPageWorker
            }
            val doneSummaries = e.intDoneTasks.filter {
                it.contains("-S") // set in SendWimmaryWorker
            }
            val doneVis = donePages.size * PROGRESS_INDICATOR_PAGE_WEIGHT + doneSummaries.size * PROGRESS_INDICATOR_SUMMARY_WEIGHT
            val percent = 100 * doneVis / totalVis
            val status = if (e.intDoneTasks.size == e.intSumTasks) {
                SendJobInfoStatus.DONE
            } else {
                SendJobInfoStatus.RUNNING
            }
            Log.d(TAG, "SendJob ${e.correlationId} done ${e.intDoneTasks.size} of ${e.intSumTasks} (visual ${doneVis} of ${totalVis})")
            SendJobInfo(e.correlationId, e.timestamp, e.intPatDisplay, e.intDocDescr, percent, status)
        }
        return infos
    }


    fun updateSendJobInfos(sjlist: List<SendJob>) {

        val infos = makeInfos(sjlist)
        val oldinfos = items

        val cb = object : DiffUtil.Callback() {
            override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
                return oldinfos[p0].instanceId == infos[p1].instanceId
            }

            override fun getOldListSize(): Int {
                return oldinfos.size
            }

            override fun getNewListSize(): Int {
                return infos.size
            }

            override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
                return oldinfos[p0] == infos[p1]
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                Log.d(TAG, "Making change payload:")
                Log.d(TAG, "   ${oldinfos[oldItemPosition]}")
                Log.d(TAG, "-> ${infos[newItemPosition]}")
                val o = Pair<SendJobInfo, SendJobInfo>(oldinfos[oldItemPosition], infos[newItemPosition])
                return o
            }
        }

        items = infos
        //notifyDataSetChanged()
        val diffResult = DiffUtil.calculateDiff(cb)
        diffResult.dispatchUpdatesTo(this)

        // scroll to top if items added
        if (oldinfos.size < infos.size) {
            if (mRecyclerView != null) {
                mRecyclerView!!.scrollToPosition(0)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        mRecyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }
}
