package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter

class CreateMessageStepAdapter(fm: androidx.fragment.app.FragmentManager, ctx: Context, private val mode: CreateMessageMode) : AbstractFragmentStepAdapter(fm, ctx) {

    override fun createStep(position: Int): Step {
        return ModeDispatcher(mode).instantiateStepAt(position)
    }

    override fun getCount(): Int {
        return ModeDispatcher(mode).numberOfSteps
    }

}