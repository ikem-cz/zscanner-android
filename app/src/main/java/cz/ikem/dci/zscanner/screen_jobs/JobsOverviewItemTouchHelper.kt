package cz.ikem.dci.zscanner.screen_jobs

import androidx.recyclerview.widget.ItemTouchHelper

class JobsOverviewItemTouchHelper(jobsTouchCallback: JobsOverviewCallback) : ItemTouchHelper(jobsTouchCallback)