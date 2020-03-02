package cz.ikem.dci.zscanner.screen_message


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import kotlinx.android.synthetic.main.fragment_message_pages.*
import kotlinx.android.synthetic.main.fragment_message_pages.view.*


class CreateMessagePagesFragment : androidx.fragment.app.Fragment() {

    private val TAG = CreateMessagePagesFragment::class.java.simpleName

    private var mListener: OnCreateMessageViewsInteractionListener? = null
    private lateinit var mViewModel: CreateMessageViewModel
    private lateinit var mRecyclerView: androidx.recyclerview.widget.RecyclerView

    private var mSnackbar: Snackbar? = null


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


        fab_next_send.isActivated = false

        fab_next_send.setOnClickListener {
            if (!mViewModel.containsAtLeastOnePage()) {
                val errorText = getString(R.string.err_no_photo)
                Log.d(TAG, "step not validated due to $errorText")
                Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_createMessagePagesFragment_to_createMessageTypeFragment)
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

            // set the button color depending on validation
            fab_next_send.backgroundTintList = context?.resources?.getColorStateList(R.color.button_bcg_states, context?.theme)

            mViewModel.pageActions.observe(viewLifecycleOwner, Observer<PageActionsQueue> {
                adapter.syncActionsQueue(mViewModel)
                    view.fab_next_send.isActivated = mViewModel.containsAtLeastOnePage()
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
