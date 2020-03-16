package cz.ikem.dci.zscanner.screen_message

import android.content.Context
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.DocumentSubType
import kotlinx.android.synthetic.main.fragment_message_properties.*
import org.json.JSONArray

class CreateMessageSubTypeFragment : Fragment() {

    private lateinit var mViewModel: CreateMessageViewModel

    private var listener: OnCreateMessageViewsInteractionListener? = null

    private val TAG = CreateMessageSubTypeFragment::class.java.simpleName

    val args: CreateMessageSubTypeFragmentArgs by navArgs()


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

        val subtypesJson = args.subtypes

        parseSubTypeJson(subtypesJson) { subtypes, error ->
            if (error != null) {
                //TODO change the text to something more reasonable
                Toast.makeText(context, "Error when parsing subtypes to List<DocumentSubType>", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error when parsing subtypes to List<DocumentSubType>")
                return@parseSubTypeJson
            }

            subtypes?.let { list ->
                context?.let { _context ->
                    createSubTypesAdapter(_context, list)
                }
            }
        }

        document_types_text?.text = getString(R.string.fragment_document_sub_type)

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


    private fun parseSubTypeJson(subtypes: String, completion: (subtypes: List<DocumentSubType>?, error: Error?) -> Unit) {

        //todo try and catch

        val subTypesJsonArray = JSONArray(subtypes)

        val listSubTypes = ArrayList<DocumentSubType>()
        for (subtype in 0 until subTypesJsonArray.length()) {
            val subTypeObject = subTypesJsonArray.getJSONObject(subtype)
            val docSubType = DocumentSubType(subTypeObject.getString("id"), subTypeObject.getString("display"))
            listSubTypes.add(docSubType)
        }
        completion(listSubTypes, null)
    }

    private fun createSubTypesAdapter(context: Context, subtypes: List<DocumentSubType>) {
        val subTypesAdapter = SubTypeAdapter(context)

        subTypesAdapter.onItemSelected = { _subtype ->

            saveSubType(_subtype){
                mViewModel.onProcessEnd { error ->
                    if(error != null){
                    Toast.makeText(context, getString(R.string.error_submitting), Toast.LENGTH_LONG).show()

                        Log.e(TAG, "error while onProcessEnd: ${error.message}")
                        return@onProcessEnd
                    }
                    activity?.finish() //TODO: possibly add some spinner overlay
                }
            }
        }

        document_types_recycler_view.adapter = subTypesAdapter
        document_types_recycler_view.layoutManager = LinearLayoutManager(context)

        subTypesAdapter.submitList(subtypes)
    }

    private fun saveSubType(docSubType: DocumentSubType, completion: (error: Error?) -> Unit) {
        mViewModel.subtype.postValue(docSubType)

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

}
