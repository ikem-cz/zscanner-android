package cz.ikem.dci.zscanner.screen_message


import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import kotlinx.android.synthetic.main.fragment_message_pages.*
import kotlinx.android.synthetic.main.fragment_message_pages.view.*


class CreateMessagePagesFragment : androidx.fragment.app.Fragment(), Step {

    private var mListener: OnCreateMessageViewsInteractionListener? = null
    private lateinit var mViewModel: CreateMessageViewModel
    private lateinit var mRecyclerView: androidx.recyclerview.widget.RecyclerView

    private var mSnackbar: Snackbar? = null

    //region Step callbacks

    override fun onSelected() {
        mViewModel.currentStep = ModeDispatcher().stepNumberFor(this)
        /*if (Utils.tutorialNextStep(4, activity)) {
            Handler().postDelayed({
                Utils.makeTooltip("Teď přidáme nějaké obrázky !", pages_recyclerview, Gravity.BOTTOM, activity, showArrow = false, modal = true) {
                    Utils.makeTooltip("Obrázky lze buď načíst z galerie Vašeho fotoaparátu pomocí tohoto tlačítka ...", gallery_fab, Gravity.TOP, activity, showArrow = true, modal = true) {
                        Utils.makeTooltip("... anebo přímo vyfotit", photo_fab, Gravity.START, activity, showArrow = true, modal = true) {
                            Utils.makeTooltip("Nyní vyfoťte nějakou fotografii.", pages_recyclerview, Gravity.BOTTOM, activity, showArrow = false, modal = true) {
                                Utils.tutorialAdvance(activity)
                            }
                        }
                    }
                }
            }, 500)
        }*/
        return
    }

    override fun verifyStep(): VerificationError? {
        if (!mViewModel.containsAtLeastOnePage()) {
            return VerificationError(getString(R.string.err_no_photo))
        }
        return null
    }

    override fun onError(error: VerificationError) {}

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.let{_activity ->
            mViewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_pages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_next_send.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))

        fab_next_send.setOnClickListener {
            mListener?.onProceedButtonPress()
        }

        // initialize recycler view
        mRecyclerView = (view.pages_recyclerview).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
            setEmptyView(view.pages_empty_view)
        }

        val pagesTouchCallback = PagesTouchCallback(mViewModel)
        val itemTouchHelper = PagesItemTouchHelper(pagesTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        context?.let { _context ->
            val adapter = PagesAdapter(mViewModel.pageActions.value!!.clone(), _context)
            mRecyclerView.adapter = adapter

            mViewModel.pageActions.observe(viewLifecycleOwner, Observer<PageActionsQueue> {
                adapter.syncActionsQueue(mViewModel)
                if (mViewModel.containsAtLeastOnePage()) {
                    fab_next_send.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                } else {
                    fab_next_send.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
                }
            })
        }

        mViewModel.undoAction.observe(viewLifecycleOwner, Observer<PageActionsQueue.PageAction> {
            if (mViewModel.undoAction.value != null) {
                mSnackbar = Snackbar.make(view.popup_layout_buttons, "Smazáno.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Zpět") {
                            mViewModel.addPage(mViewModel.undoAction.value?.page?.path, mViewModel.undoAction.value?.target)
                        }
                mSnackbar?.show()
            } else {
                if (mSnackbar != null) {
                    mSnackbar?.dismiss()
                    mSnackbar = null
                }
            }
        })

        // add make photo button listener
        view.photo_layout.setOnClickListener {
            mListener?.onCapturePagePhotoButtonPress()
        }

        view.photo_fab.setOnClickListener {
            mListener?.onCapturePagePhotoButtonPress()
        }

//        view.gallery_layout.setOnClickListener {
//            mListener?.onAttachButtonPress()
//        }
//
//        view.gallery_fab.setOnClickListener {
//            mListener?.onAttachButtonPress()
//        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateMessageViewsInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnCreateMessageViewsInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

}
