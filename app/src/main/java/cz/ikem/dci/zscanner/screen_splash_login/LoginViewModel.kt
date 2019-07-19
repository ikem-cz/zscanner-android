package cz.ikem.dci.zscanner.screen_splash_login

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cz.ikem.dci.zscanner.PREF_LOGGED_IN
import cz.ikem.dci.zscanner.PREF_USERNAME
import cz.ikem.dci.zscanner.SHARED_PREF_KEY

class LoginViewModel(zapplication: Application) : AndroidViewModel(zapplication) {

    private val TAG = LoginViewModel::class.java.simpleName

    private var sharedPreferences: SharedPreferences = zapplication.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

    // state vector: prev login state, current login state, seacat state
    val state: MutableLiveData<Triple<LoginState?, LoginState, SeaCatState>> = MutableLiveData<Triple<LoginState?, LoginState, SeaCatState>>()
            .apply { value = Triple(null, LoginState.LOGGED_OUT, SeaCatState.IDLE) }

    enum class LoginState { LOGGED_OUT, SUBMITTED, LOGGED_IN, LOGIN_FAILED }

    var username: String
        get() {
            throw NotImplementedError() // no need for getter in current context - just write to SharedPreferences and read back in next activities
        }
        set(value) {
            sharedPreferences.edit().putString(PREF_USERNAME, value).apply()
        }

    private var _prevloginstate: LoginState? = null
    private var _loginstate: LoginState = LoginState.LOGGED_OUT
    var loginstate: LoginState
        get() {
            return _loginstate
        }
        set(value) {
            _prevloginstate = _loginstate
            _loginstate = value
            if (value == LoginState.LOGGED_IN) {
                sharedPreferences.edit().putBoolean(PREF_LOGGED_IN, true).apply()

            } else if ((value == LoginState.LOGGED_OUT) || value == LoginState.LOGIN_FAILED) {
                sharedPreferences.edit().putBoolean(PREF_LOGGED_IN, false).apply()
            }
            Log.d(TAG, "Posting state ${_prevloginstate}, ${value}, ${seacatstate}")
            state.postValue(Triple(_prevloginstate, value, seacatstate))
        }

    enum class SeaCatState { IDLE, IP_CONNECTED, ESTABILISHED, READY }

    private var _seacatstate: SeaCatState = SeaCatState.IDLE
    var seacatstate: SeaCatState
        get() {
            return _seacatstate
        }
        set(value) {
            _seacatstate = value
            Log.d(TAG, "Posting state ${_prevloginstate}, ${loginstate}, ${value}")
            state.postValue(Triple(_prevloginstate, loginstate, value))
        }

    init {
        val isloggedIn = sharedPreferences.getBoolean(PREF_LOGGED_IN, false)
        if (isloggedIn) {
            loginstate = LoginState.LOGGED_IN
        }
    }

}
