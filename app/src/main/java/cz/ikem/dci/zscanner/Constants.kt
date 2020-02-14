package cz.ikem.dci.zscanner

const val REQUEST_CODE_BARCODE = 0xC0DE
const val REQUEST_CODE_PHOTO = 0xF010
const val REQUEST_CODE_PICK_PHOTO = 0x113
const val REQUEST_CODE_PERMISSIONS = 0x5AD

const val KEY_DOC_TYPE = "doctype"
const val KEY_DOC_SUB_TYPE = "docsubtype"
const val KEY_PAT_ID = "patid"
const val KEY_DATE_STRING = "datestring"
const val KEY_CORRELATION_ID = "correlation"
const val KEY_DOCUMENT_NOTE = "doc_description"
const val KEY_NUM_PAGES = "num_pages"
const val KEY_PAGE_NUMBER = "page_number"
const val KEY_PAGE_FILE = "page_file"
const val KEY_NAME = "doc_name"
const val KEY_DIRECTORY = "folder"

const val WORKTAG_SENDING_JOB = "cz.ikem.dci.zscanner.work.sending-job"
const val WORKTAG_REFRESH_DOCUMENT_TYPES = "cz.ikem.dci.zscanner.work.tag-refresh-document-types"
const val WORKTAG_REFRESH_DEPARTMENTS = "cz.ikem.dci.zscanner.work.tag-refresh-departments" //todo should it say ikem?

const val ACTION_LOGIN_OK = "cz.ikem.dci.zscanner.LOGIN_OK"
const val ACTION_LOGIN_FAILED = "cz.ikem.dci.zscanner.LOGIN_FAILED"

const val PROGRESS_INDICATOR_PAGE_WEIGHT = 100
const val PROGRESS_INDICATOR_SUMMARY_WEIGHT = 12

const val MAX_MRUS = 3

const val SHARED_PREF_KEY = "ZSCANNER_SHARED_PREFS"
const val PREF_LOGGED_IN = "LOGGED_IN"
const val PREF_USERNAME = "USERNAME"
const val PREF_FIRST_TIME_PROMPTED = "FIRST_TIME_RUN"
const val PREF_TUTORIAL_NEXT_STEP = "TUTORIAL_NEXT"

const val CREATE_MESSAGE_MODE_KEY = "CreateMessageMode"
const val CREATE_MESSAGE_MODE_PHOTO = "photo"
const val CREATE_MESSAGE_MODE_EXAM = "exam"
const val CREATE_MESSAGE_MODE_DOCUMENT = "doc"

// Konsatnty zasilane Zlatokopem - pri zmene nutno zmenit i na backendu
const val MESSAGE_PHOTO_MODEID = "foto"
const val MESSAGE_EXAM_MODEID = "exam"
const val MESSAGE_DOCUMENT_MODEID = "doc"