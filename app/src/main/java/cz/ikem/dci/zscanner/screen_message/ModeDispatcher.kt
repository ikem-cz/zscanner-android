package cz.ikem.dci.zscanner.screen_message


import com.stepstone.stepper.Step
import cz.ikem.dci.zscanner.MESSAGE_DOCUMENT_MODEID
import cz.ikem.dci.zscanner.MESSAGE_EXAM_MODEID
import cz.ikem.dci.zscanner.MESSAGE_PHOTO_MODEID
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.screen_message.CreateMessageMode.*

class ModeDispatcher(val mode: CreateMessageMode) {

    constructor(modeId: String) : this(
            when (modeId) {
                MESSAGE_DOCUMENT_MODEID -> DOCUMENT
                MESSAGE_EXAM_MODEID -> EXAM
                MESSAGE_PHOTO_MODEID -> PHOTO
                else -> throw AssertionError("Expectation failed")
            })

    val numberOfSteps: Int
        get() = when (mode) {
            DOCUMENT, EXAM -> 3
            PHOTO -> 2
        }

    fun instantiateStepAt(position: Int): Step =
            when (mode) {
                DOCUMENT, EXAM -> when (position) {
                    0 -> CreateMessagePatientFragment()
                    1 -> CreateMessagePropertiesFragment()
                    2 -> CreateMessagePagesFragment()
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
                PHOTO -> when (position) {
                    0 -> CreateMessagePatientFragment()
                    1 -> CreateMessagePagesFragment()
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
            }

    fun stepNumberFor(step: Step): Int =
            when (mode) {
                DOCUMENT, EXAM -> when (step) {
                    is CreateMessagePatientFragment -> 0
                    is CreateMessagePropertiesFragment -> 1
                    is CreateMessagePagesFragment -> 2
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
                PHOTO -> when (step) {
                    is CreateMessagePatientFragment -> 0
                    is CreateMessagePagesFragment -> 1
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
            }

    fun stepTitleAt(position: Int): Int =
            when (mode) {
                DOCUMENT, EXAM -> when (position) {
                    0 -> R.string.new_job_patient
                    1 -> R.string.new_job_document_properties
                    2 -> R.string.new_job_pages
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
                PHOTO -> when (position) {
                    0 -> R.string.new_job_patient
                    1 -> R.string.new_job_pages
                    else -> {
                        throw AssertionError("Expectation failed")
                    }
                }
            }

    val modeId: String
        get() = when (mode) {
            DOCUMENT -> MESSAGE_DOCUMENT_MODEID
            EXAM -> MESSAGE_EXAM_MODEID
            PHOTO -> MESSAGE_PHOTO_MODEID
        }

    val modeNameResource: Int
        get() = when (mode) {
            DOCUMENT -> R.string.mode_document_name
            EXAM -> R.string.mode_exam_name
            PHOTO -> R.string.mode_photo_name
        }
}