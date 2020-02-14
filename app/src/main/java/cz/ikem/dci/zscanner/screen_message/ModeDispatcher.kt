package cz.ikem.dci.zscanner.screen_message


import com.stepstone.stepper.Step
import cz.ikem.dci.zscanner.R

class ModeDispatcher {

    val numberOfSteps = 3

    fun instantiateStepAt(position: Int): Step =
           when (position) {
                    0 -> CreateMessagePatientFragment()
                    1 -> CreateMessagePagesFragment()
                    2 -> CreateMessagePropertiesFragment()
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
            }

    fun stepNumberFor(step: Step): Int =

               when (step) {
                    is CreateMessagePatientFragment -> 0
                    is CreateMessagePagesFragment -> 1
                    is CreateMessagePropertiesFragment -> 2
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }


    fun stepTitleAt(position: Int): Int =
             when (position) {
                    0 -> R.string.new_job_patient
                    1 -> R.string.new_job_pages
                    2 -> R.string.new_job_document_properties
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
            }

}