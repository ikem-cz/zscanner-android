package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.DocumentType
import kotlinx.android.synthetic.main.fragment_message_properties.*



class CreateMessageTypeFragment : Fragment() {

    private val TAG = CreateMessageTypeFragment::class.java.simpleName

    private lateinit var mViewModel: CreateMessageViewModel
    private var listener: OnCreateMessageViewsInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.let { _activity ->
            mViewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)

        }
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_properties, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createDocumentTypesAdapter(this.requireContext())

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateMessageViewsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCreateMessageViewsInteractionListener")
        }
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun createDocumentTypesAdapter(context: Context) {
        val typesAdapter = TypesAdapter(context)

        typesAdapter.onItemSelected = { docType ->
            //val isSubTypeNull = (type.subtypeId == null)

            saveType(docType) {
                if (docType.subtype == null) {
                    mViewModel.subtype.postValue(null)

                    mViewModel.onProcessEnd{ error ->
                        if(error != null){
                            Toast.makeText(context, getString(R.string.error_submitting), Toast.LENGTH_LONG).show()

                            Log.e(TAG, "error while onProcessEnd: ${error.message}")
                            return@onProcessEnd
                        }
                        Log.e("DEBUGGING", "send the POST request")
                        activity?.finish() //TODO: possibly add some spinner overlay
                    }

                } else {
                    val action = CreateMessageTypeFragmentDirections.actionCreateMessageTypeFragmentToCreateMessageSubTypeFragment(docType.subtype)
                    findNavController().navigate(action)
                }
            }
        }

        document_types_recycler_view.adapter = typesAdapter
        document_types_recycler_view.layoutManager = LinearLayoutManager(context)

        // display possible types
        mViewModel.storedTypes.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list: List<DocumentType>? ->
            typesAdapter.submitList(list)
        })
    }

    private fun saveType(docType: DocumentType, completion: (error: Error?) -> Unit) {
        mViewModel.type.postValue(docType)

        doUntilFalse(500) {
            if (mViewModel.type.value !== null) {
                completion(null)
                false
            } else {
                true
            }
        }
    }

    /**
     * Executes given [closure] periodically until it returns false
     *
     * @param period Milliseconds of how often this should happen
     * @param closure A closure that will be executed. If it returns false, it won't be executed again
     *
     * */
    private fun doUntilFalse(period: Long, closure: () -> Boolean) {
        val origLooper = Looper.myLooper() ?: Looper.getMainLooper()
        val handler = Handler(origLooper)
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (closure()) {
                    handler.postDelayed(this, period)
                }
            }
        }, 0)
    }

    companion object {
        const val EXTRA_DEPARTMENT = "extra_department"
    }
}
