package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                //todo handle
                Log.e(TAG, "Error when parsing subtypes to List<DocumentSubType>")
                return@parseSubTypeJson
            }

            subtypes?.let { list ->
                context?.let { _context ->
                    createSubTypesAdapter(_context, list)
                }
            }
        }

//TODO extract string
        other_photo_types_text?.text = "SUBTYPE"

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


    private fun parseSubTypeJson (subtypes: String, completion: (subtypes: List<DocumentSubType>?, error: Error?) -> Unit){

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

        subTypesAdapter.onItemSelected = { subtype ->

            mViewModel.type.postValue(subtype.display)

            //TODO
//             send the POST request
            Log.e("DEBUGGING", "send the POST request with subtype")
        }

        document_types_recycler_view.adapter = subTypesAdapter
        document_types_recycler_view.layoutManager = LinearLayoutManager(context)

        subTypesAdapter.submitList(subtypes)
    }





    //    override fun onSelected() {
//        activity?.let { _activity ->
//            val viewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
//            //TODO   viewModel.currentStep = ModeDispatcher().stepNumberFor(this)
//        }
//    }
//
//
//    override fun verifyStep(): VerificationError? {
//        if (mViewModel.type.value!! == "") {
//            return VerificationError(getString(R.string.err_no_entrytype))
//        }
//        return null
//    }
//
//
//    override fun onError(error: VerificationError) {}


}
