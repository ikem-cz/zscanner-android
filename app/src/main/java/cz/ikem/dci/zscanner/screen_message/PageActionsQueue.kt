package cz.ikem.dci.zscanner.screen_message

import android.util.Log

class PageActionsQueue() {

    private val TAG = PageActionsQueue::class.java.simpleName

    constructor(actionsList: MutableList<PageAction>, lastListSteps: Int, lastList: MutableList<Page>) : this() {
        mActionsList = actionsList
        mCachedPageListActions = lastListSteps
        mCachedPageList = lastList
    }

    data class Page(val path: String, var note: String)
    enum class PageActionType { ADDED, REMOVED, MOVED }
    data class PageAction(val page: Page, val type: PageActionType, val target: Int = -1)

    private var mActionsList: MutableList<PageAction> = ArrayList()
    private var mCachedPageList: MutableList<Page> = ArrayList()
    private var mCachedPageListActions: Int = 0

    fun add(action: PageAction) {
        mActionsList.add(action)
    }

    fun actionsList(): List<PageAction> {
        return mActionsList.toList()
    }

    fun makePages(): List<Page> {
        Log.v(TAG, "Made page list from ${mCachedPageListActions} cached, ${mActionsList.count()} total")
        while (mCachedPageListActions < mActionsList.count()) {
            val action = mActionsList[mCachedPageListActions]
            applyAction(action, mCachedPageList)
            mCachedPageListActions += 1
        }
        return mCachedPageList
    }

    private fun applyAction(action: PageAction, targetList: MutableList<Page>) {
        when (action.type) {
            PageActionType.ADDED -> {
                if (action.target < 0) {
                    targetList.add(action.page)
                } else {
                    targetList.add(action.target, action.page)
                }
            }
            PageActionType.REMOVED -> {
                targetList.remove(action.page)
            }
            PageActionType.MOVED -> {
                val oldIndex = targetList.indexOf(action.page)
                val page = targetList[oldIndex]
                targetList.removeAt(oldIndex)
                targetList.add(action.target, page)
            }
        }
    }

    fun clone(): PageActionsQueue {
        return PageActionsQueue(
                mutableListOf<PageAction>().apply { addAll(mActionsList) },
                mCachedPageListActions,
                mutableListOf<Page>().apply { addAll(mCachedPageList) })
    }

    fun subtractActionsList(subtrahend: List<PageAction>): List<PageAction> {
        return mActionsList.subList(subtrahend.count(), mActionsList.count())
    }

}