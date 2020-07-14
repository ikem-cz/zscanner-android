package cz.ikem.dci.zscanner

const val REQUEST_CODE_BARCODE = 0xC0DE
const val REQUEST_CODE_PHOTO = 0xF010
const val REQUEST_CODE_PICK_PHOTO = 0x113
const val REQUEST_CODE_PERMISSIONS = 0x5AD



const val KEY_CORRELATION_ID = "correlation"
const val KEY_FOLDER_INTERNAL_ID = "folderInternalId"
const val KEY_DOC_TYPE = "documentType"
const val KEY_DOC_SUB_TYPE = "documentSubType"
const val KEY_DEPARTMENT = "department"
const val KEY_NUM_PAGES = "pages"
const val KEY_DATE_STRING = "datetime"


const val KEY_PAGE_FILE = "page"
const val KEY_PAGE_INDEX = "pageIndex"
const val KEY_DOCUMENT_NOTE = "description"

const val KEY_EXTERNAL_ID = "externalId"
const val KEY_INTERNAL_ID = "internalId"
const val KEY_NAME = "name"

const val KEY_TYPE = "type"
const val KEY_SUB_TYPE = "sub-type"

const val KEY_DIRECTORY = "folder"

const val WORKTAG_SENDING_JOB = "cz.ikem.dci.zscanner.work.sending-job"
const val WORKTAG_REFRESH_DOCUMENT_TYPES = "cz.ikem.dci.zscanner.work.tag-refresh-document-types"
const val WORKTAG_REFRESH_DEPARTMENTS = "cz.ikem.dci.zscanner.work.tag-refresh-departments"

const val PROGRESS_INDICATOR_PAGE_WEIGHT = 100
const val PROGRESS_INDICATOR_SUMMARY_WEIGHT = 12

const val MAX_MRUS = 3

const val SHARED_PREF_KEY = "ZSCANNER_SHARED_PREFS"
const val PREF_USERNAME = "USERNAME"
const val PREF_ACCESS_TOKEN = "ACCESS_TOKEN"
const val PREF_FIRST_TIME_PROMPTED = "FIRST_TIME_RUN"
const val PREF_TUTORIAL_NEXT_STEP = "TUTORIAL_NEXT"
const val PREF_LAST_SUCCESSFUL_LOGIN = "PREF_LAST_SUCCESSFUL_LOGIN"

const val BIOMETRIC_KEY_NAME = "zScanner Master Key"
const val SERVER_SIDE_LOGOUT_TIMEOUT = 8*60*1000 // 8h as this is logout time on the server side
const val BROADCAST_ACTION_LOGOUT = "ACTION_LOGOUT"
const val REQUEST_CODE_LOGOUT = 123 // can be whatever, just unique for the app