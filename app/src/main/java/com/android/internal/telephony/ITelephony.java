package com.android.internal.telephony;

/**
 * The replacement of original interface of Android
 */
public interface ITelephony {

	boolean endCall();

	void answerRingingCall();

	void silenceRinger();

}