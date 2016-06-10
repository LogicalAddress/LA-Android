package ng.com.nhub.paygis;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.media.ToneGenerator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.parse.ParseUser;

import ng.com.nhub.paygis.dialpad.DialpadImageButton;
import ng.com.nhub.paygis.dialpad.UnicodeDialerKeyListener;
import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.ParseLib;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DialPadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialPadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialPadFragment extends Fragment implements
        DialpadImageButton.OnPressedListener,
        View.OnClickListener,
        View.OnLongClickListener,
        View.OnKeyListener,
        TextWatcher {

    /** The length of DTMF tones in milliseconds */
    private static final int TONE_LENGTH_MS = 150;
    private static final int TONE_LENGTH_INFINITE = -1;

    /** The DTMF tone volume relative to other sounds in the stream */
    private static final int TONE_RELATIVE_VOLUME = 80;

    /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys */
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF;


    private View mDelete;
    private ToneGenerator mToneGenerator;
    private final Object mToneGeneratorLock = new Object();

    // determines if we want to playback local DTMF tones.
    private boolean mDTMFToneEnabled;


    /**
     * View (usually FrameLayout) containing mDigits field. This can be null, in which mDigits
     * isn't enclosed by the container.
     */
    private View mDigitsContainer;
    private EditText mDigits;

    /**
     * Remembers the number of dialpad buttons which are pressed at this moment.
     * If it becomes 0, meaning no buttons are pressed, we'll call
     * {@link ToneGenerator#stopTone()}; the method shouldn't be called unless the last key is
     * released.
     */
    private int mDialpadPressCount;

    private boolean mWasEmptyBeforeTextChange;

    private View mDialButtonContainer;
    private View mDialButton;

    private View mSpacer;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DialPadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DialPadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DialPadFragment newInstance(String param1, String param2) {
        DialPadFragment fragment = new DialPadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //TODO: Pass PrincipalActivity Context so we can call setRequest....
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void setupKeypad(View fragmentView) {
        int[] buttonIds = new int[] { R.id.one, R.id.two, R.id.three, R.id.four, R.id.five,
                R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.zero, R.id.star, R.id.pound};
        for (int id : buttonIds) {
            ((DialpadImageButton) fragmentView.findViewById(id)).setOnPressedListener(this);
        }
        // Long-pressing one button will initiate Voicemail.
        fragmentView.findViewById(R.id.one).setOnLongClickListener(this);
        // Long-pressing zero button will enter '+' instead.
        fragmentView.findViewById(R.id.zero).setOnLongClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_dialpad, container, false);

        // Load up the resources for the text field.
        Resources r = getResources();

        mDigitsContainer = fragmentView.findViewById(R.id.digits_container);
        mDigits = (EditText) fragmentView.findViewById(R.id.digits);
        mDigits.setKeyListener(UnicodeDialerKeyListener.INSTANCE);
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);
        mDigits.setOnLongClickListener(this);
        mDigits.addTextChangedListener(this);

        // PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), mDigits);
        // Check for the presence of the keypad
        View oneButton = fragmentView.findViewById(R.id.one);
        if (oneButton != null) {
            setupKeypad(fragmentView);
        }

        LinearLayout dialPadFragmentLayout = (LinearLayout) fragmentView.findViewById(
                R.id.dialPadFragmentLayout
        );

        YoYo.with(Techniques.BounceInUp)
                .duration(400)
                .playOn(dialPadFragmentLayout);


//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        int minCellSize = (int) (56 * dm.density); // 56dip == minimum size of menu buttons
//        int cellCount = dm.widthPixels / minCellSize;
//        int fakeMenuItemWidth = dm.widthPixels / cellCount;
//        mDialButtonContainer = fragmentView.findViewById(R.id.dialButtonContainer);
//        // If in portrait, add padding to the dial button since we need space for the
//        // search and menu/overflow buttons.
//        if (mDialButtonContainer != null && !OrientationUtil.isLandscape(this.getActivity())) {
//            mDialButtonContainer.setPadding(
//                    fakeMenuItemWidth, mDialButtonContainer.getPaddingTop(),
//                    fakeMenuItemWidth, mDialButtonContainer.getPaddingBottom());
//        }

        mDialButton = fragmentView.findViewById(R.id.dialButton);
        mDialButton.setOnClickListener(this);
        mDialButton.setOnLongClickListener(this);

        mDelete = fragmentView.findViewById(R.id.deleteButton);
        if (mDelete != null) {
            mDelete.setOnClickListener(this);
            mDelete.setOnLongClickListener(this);
        }

        mSpacer = fragmentView.findViewById(R.id.spacer);
        mSpacer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDigitsEmpty()) {
                    if (getActivity() != null) {
                        return ((HostInterface) getActivity()).onDialpadSpacerTouchWithEmptyQuery();
                    }
                    return true;
                }
                return false;
            }
        });

        TextView tvTitle = (TextView) fragmentView.findViewById(R.id.title);
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getString("handle") != null) {
            tvTitle.setText(currentUser.getString("handle"));
        } else {
            //Fail Safe
            Log.e("RETNAN", "Investigate - DialPadFragment");
            ParseLib.parseSignUpLogin(AppData.getCurrentUser());
        }
        RelativeLayout mockContainer = (RelativeLayout) fragmentView.findViewById(R.id.mock_container);
        EditText btnWritePost = (EditText) fragmentView.findViewById(R.id.description);
        btnWritePost.setInputType(InputType.TYPE_NULL);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(btnWritePost.getWindowToken(), 0);
        btnWritePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if(currentUser.getString("handle") == "Anonymous" ||
                            currentUser.getString("handle").equals("Anonymous")){
                        mListener.openSetUserNameDialog();
                    }else{
                        mListener.onClickOnNewPost();
                    }
                }else{
                    //onAttach is not called earlier
                    if (getActivity() != null) {
                        if(currentUser.getString("handle") == "Anonymous" ||
                                currentUser.getString("handle").equals("Anonymous")){
                            ((OnFragmentInteractionListener) getActivity()).openSetUserNameDialog();
                        }else{
                            ((OnFragmentInteractionListener) getActivity()).onClickOnNewPost();
                        }
                    }
                }
            }
        });

        return fragmentView;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void dial(String address) {
        if (mListener != null) {
            mListener.onFragmentInteraction(Uri.parse(address));
        }else{
            //onAttach is not called earlier
            if (getActivity() != null) {
                ((OnFragmentInteractionListener) getActivity()).onFragmentInteraction(Uri.parse(address));
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Plays the specified tone for TONE_LENGTH_MS milliseconds.
     */
    private void playTone(int tone) {
        playTone(tone, TONE_LENGTH_MS);
    }

    /**
     * Play the specified tone for the specified milliseconds
     *
     * The tone is played locally, using the audio stream for phone calls.
     * Tones are played only if the "Audible touch tones" user preference
     * is checked, and are NOT played if the device is in silent mode.
     *
     * The tone length can be -1, meaning "keep playing the tone." If the caller does so, it should
     * call stopTone() afterward.
     *
     * @param tone a tone code from {@link ToneGenerator}
     * @param durationMs tone length.
     */
    private void playTone(int tone, int durationMs) {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
            return;
        }
        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        AudioManager audioManager =
                (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
                || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                FileLog.e("tmessages", "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, durationMs);
        }
    }


    private void keyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                playTone(ToneGenerator.TONE_DTMF_1, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_2:
                playTone(ToneGenerator.TONE_DTMF_2, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_3:
                playTone(ToneGenerator.TONE_DTMF_3, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_4:
                playTone(ToneGenerator.TONE_DTMF_4, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_5:
                playTone(ToneGenerator.TONE_DTMF_5, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_6:
                playTone(ToneGenerator.TONE_DTMF_6, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_7:
                playTone(ToneGenerator.TONE_DTMF_7, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_8:
                playTone(ToneGenerator.TONE_DTMF_8, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_9:
                playTone(ToneGenerator.TONE_DTMF_9, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_0:
                playTone(ToneGenerator.TONE_DTMF_0, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_POUND:
                playTone(ToneGenerator.TONE_DTMF_P, TONE_LENGTH_INFINITE);
                break;
            case KeyEvent.KEYCODE_STAR:
                playTone(ToneGenerator.TONE_DTMF_S, TONE_LENGTH_INFINITE);
                break;
            default:
                break;
        }
//        mHaptic.vibrate();
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);
        // If the cursor is at the end of the text we hide it.
        final int length = mDigits.length();
        if (length == mDigits.getSelectionStart() && length == mDigits.getSelectionEnd()) {
            mDigits.setCursorVisible(false);
        }
    }

    /**
     * Stop the tone if it is played.
     */
    private void stopTone() {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                FileLog.e("tmessages", "stopTone: mToneGenerator == null");
                return;
            }
            mToneGenerator.stopTone();
        }
    }

    //DialpadImageButton.OnPressedListener
    /**
     * When a key is pressed, we start playing DTMF tone, do vibration, and enter the digit
     * immediately. When a key is released, we stop the tone. Note that the "key press" event will
     * be delivered by the system with certain amount of delay, it won't be synced with user's
     * actual "touch-down" behavior.
     */
    @Override
    public void onPressed(View view, boolean pressed) {

        FileLog.e("tmessages", "onPressed(). view: " + view + ", pressed: " + pressed);

        if (pressed) {
            switch (view.getId()) {
                case R.id.one: {
                    keyPressed(KeyEvent.KEYCODE_1);
                    break;
                }
                case R.id.two: {
                    keyPressed(KeyEvent.KEYCODE_2);
                    break;
                }
                case R.id.three: {
                    keyPressed(KeyEvent.KEYCODE_3);
                    break;
                }
                case R.id.four: {
                    keyPressed(KeyEvent.KEYCODE_4);
                    break;
                }
                case R.id.five: {
                    keyPressed(KeyEvent.KEYCODE_5);
                    break;
                }
                case R.id.six: {
                    keyPressed(KeyEvent.KEYCODE_6);
                    break;
                }
                case R.id.seven: {
                    keyPressed(KeyEvent.KEYCODE_7);
                    break;
                }
                case R.id.eight: {
                    keyPressed(KeyEvent.KEYCODE_8);
                    break;
                }
                case R.id.nine: {
                    keyPressed(KeyEvent.KEYCODE_9);
                    break;
                }
                case R.id.zero: {
                    keyPressed(KeyEvent.KEYCODE_0);
                    break;
                }
                case R.id.pound: {
                    keyPressed(KeyEvent.KEYCODE_POUND);
                    break;
                }
                case R.id.star: {
                    keyPressed(KeyEvent.KEYCODE_STAR);
                    break;
                }
                default: {
                    FileLog.e("tmessages", "Unexpected onTouch(ACTION_DOWN) event from: " + view);
                    break;
                }
            }
            mDialpadPressCount++;
        } else {
            view.jumpDrawablesToCurrentState();
            mDialpadPressCount--;
            if (mDialpadPressCount < 0) {
                // e.g.
                // - when the user action is detected as horizontal swipe, at which only
                //   "up" event is thrown.
                // - when the user long-press '0' button, at which dialpad will decrease this count
                //   while it still gets press-up event here.
                FileLog.e("tmessages", "mKeyPressCount become negative.");
                stopTone();
                mDialpadPressCount = 0;
            } else if (mDialpadPressCount == 0) {
                stopTone();
            }
        }
    }

    /**
     * In most cases, when the dial button is pressed, there is a
     * number in digits area. Pack it in the intent, start the
     * outgoing call broadcast as a separate task and finish this
     * activity.
     *
     * When there is no digit and the phone is CDMA and off hook,
     * we're sending a blank flash for CDMA. CDMA networks use Flash
     * messages when special processing needs to be done, mainly for
     * 3-way or call waiting scenarios. Presumably, here we're in a
     * special 3-way scenario where the network needs a blank flash
     * before being able to add the new participant.  (This is not the
     * case with all 3-way calls, just certain CDMA infrastructures.)
     *
     * Otherwise, there is no digit, display the last dialed
     * number. Don't finish since the user may want to edit it. The
     * user needs to press the dial button again, to dial it (general
     * case described above).
     */
    public void dialButtonPressed() {
        //TODO - Uncomment this later

        if (isDigitsEmpty()) { // No number entered.
            handleDialButtonClickWithEmptyDigits();
        } else {
            final String number = mDigits.getText().toString();
            dial(number);
            mDigits.getText().clear();
        }

//        if (isDigitsEmpty()) { // No number entered.
//            handleDialButtonClickWithEmptyDigits();
//        } else {
//            final String number = mDigits.getText().toString();
//            // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
//            // test equipment.
//            // TODO: clean it up.
//            if (number != null
//                    && !TextUtils.isEmpty(mProhibitedPhoneNumberRegexp)
//                    && number.matches(mProhibitedPhoneNumberRegexp)
//                    && (SystemProperties.getInt("persist.radio.otaspdial", 0) != 1)) {
//                Log.i(TAG, "The phone number is prohibited explicitly by a rule.");
//                if (getActivity() != null) {
//                    DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
//                            R.string.dialog_phone_call_prohibited_message);
//                    dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
//                }
//                // Clear the digits just in case.
//                mDigits.getText().clear();
//            } else {
//                final Intent intent = CallUtil.getCallIntent(number,
//                        (getActivity() instanceof DialtactsActivity ?
//                                ((DialtactsActivity) getActivity()).getCallOrigin() : null));
//                startActivity(intent);
//                mClearDigitsOnStop = true;
//                getActivity().finish();
//            }
//        }


    }

    /**
     * @return true if the widget with the phone number digits is empty.
     */
    private boolean isDigitsEmpty() {
        return mDigits.length() == 0;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.deleteButton: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return;
            }
            case R.id.dialButton: {
//                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys
                dialButtonPressed();
                return;
            }
            case R.id.digits: {
                if (!isDigitsEmpty()) {
                    mDigits.setCursorVisible(true);
                }
                return;
            }
            default: {
                FileLog.e("tmessages", "Unexpected onClick() event from: " + view);
                return;
            }
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {

        switch (view.getId()) {
            case R.id.digits:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    dialButtonPressed();
                    return true;
                }
                break;
        }
        return false;
    }

    private void handleDialButtonClickWithEmptyDigits() {
        //TODO: empty digit string
//        if (phoneIsCdma() && phoneIsOffhook()) {
//            // This is really CDMA specific. On GSM is it possible
//            // to be off hook and wanted to add a 3rd party using
//            // the redial feature.
//            startActivity(newFlashIntent());
//        } else {
//            if (!TextUtils.isEmpty(mLastNumberDialed)) {
//                // Recall the last number dialed.
//                mDigits.setText(mLastNumberDialed);
//                // ...and move the cursor to the end of the digits string,
//                // so you'll be able to delete digits using the Delete
//                // button (just as if you had typed the number manually.)
//                //
//                // Note we use mDigits.getText().length() here, not
//                // mLastNumberDialed.length(), since the EditText widget now
//                // contains a *formatted* version of mLastNumberDialed (due to
//                // mTextWatcher) and its length may have changed.
//                mDigits.setSelection(mDigits.getText().length());
//            } else {
//                // There's no "last number dialed" or the
//                // background query is still running. There's
//                // nothing useful for the Dial button to do in
//                // this case.  Note: with a soft dial button, this
//                // can never happens since the dial button is
//                // disabled under these conditons.
//                playTone(ToneGenerator.TONE_PROP_NACK);
//            }
//        }
    }

    @Override
    public boolean onLongClick(View view) {
        final Editable digits = mDigits.getText();
        final int id = view.getId();
        switch (id) {
            case R.id.deleteButton: {
                digits.clear();
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                return true;
            }
            case R.id.one: {
//                // '1' may be already entered since we rely on onTouch() event for numeric buttons.
//                // Just for safety we also check if the digits field is empty or not.
//                if (isDigitsEmpty() || TextUtils.equals(mDigits.getText(), "1")) {
//                    // We'll try to initiate voicemail and thus we want to remove irrelevant string.
//                    removePreviousDigitIfPossible();
//                    if (isVoicemailAvailable()) {
//                        callVoicemail();
//                    } else if (getActivity() != null) {
//                        // Voicemail is unavailable maybe because Airplane mode is turned on.
//                        // Check the current status and show the most appropriate error message.
//                        final boolean isAirplaneModeOn =
//                                Settings.System.getInt(getActivity().getContentResolver(),
//                                        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
//                        if (isAirplaneModeOn) {
//                            DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
//                                    R.string.dialog_voicemail_airplane_mode_message);
//                            dialogFragment.show(getFragmentManager(),
//                                    "voicemail_request_during_airplane_mode");
//                        } else {
//                            DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
//                                    R.string.dialog_voicemail_not_ready_message);
//                            dialogFragment.show(getFragmentManager(), "voicemail_not_ready");
//                        }
//                    }
//                    return true;
//                }
                return false;
            }
            case R.id.zero: {
//                // Remove tentative input ('0') done by onTouch().
//                removePreviousDigitIfPossible();
//                keyPressed(KeyEvent.KEYCODE_PLUS);
//                // Stop tone immediately and decrease the press count, so that possible subsequent
//                // dial button presses won't honor the 0 click any more.
//                // Note: this *will* make mDialpadPressCount negative when the 0 key is released,
//                // which should be handled appropriately.
//                stopTone();
//                if (mDialpadPressCount > 0) mDialpadPressCount--;
                return true;
            }
            case R.id.digits: {
                // Right now EditText does not show the "paste" option when cursor is not visible.
                // To show that, make the cursor visible, and return false, letting the EditText
                // show the option by itself.
                mDigits.setCursorVisible(true);
                return false;
            }
            case R.id.dialButton: {
                if (isDigitsEmpty()) {
                    handleDialButtonClickWithEmptyDigits();
                    // This event should be consumed so that onClick() won't do the exactly same
                    // thing.
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        mWasEmptyBeforeTextChange = TextUtils.isEmpty(s);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable input) {
        if (mWasEmptyBeforeTextChange != TextUtils.isEmpty(input)) {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
        // DTMF Tones do not need to be played here any longer -
        // the DTMF dialer handles that functionality now.
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri address);
        void onClickOnNewPost();
        void openSetUserNameDialog();
    }

    public interface HostInterface {
        /**
         * Notifies the parent activity that the space above the dialpad has been tapped with
         * no query in the dialpad present. In most situations this will cause the dialpad to
         * be dismissed, unless there happens to be content showing.
         */
        boolean onDialpadSpacerTouchWithEmptyQuery();
    }

    @Override
    public void onResume() {
        super.onResume();

        //final StopWatch stopWatch = StopWatch.start("Dialpad.onResume");

        // Query the last dialed number. Do it first because hitting
        // the DB is 'slow'. This call is asynchronous.
        // queryLastOutgoingCall();

        //stopWatch.lap("qloc");

        final ContentResolver contentResolver = getActivity().getContentResolver();

        // retrieve the DTMF tone play back setting.
        mDTMFToneEnabled = Settings.System.getInt(contentResolver,
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;

        // retrieve dialpad autocomplete setting
//        mSmartDialEnabled = Settings.Secure.getInt(contentResolver,
//                Settings.Secure.DIALPAD_AUTOCOMPLETE, 0) == 1;

        // stopWatch.lap("dtwd");

        // Retrieve the haptic feedback setting.
        // mHaptic.checkSystemSetting();

        // stopWatch.lap("hptc");

        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                } catch (RuntimeException e) {
                    Log.w("tmessages", "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
        // stopWatch.lap("tg");
        // Prevent unnecessary confusion. Reset the press count anyway.
        mDialpadPressCount = 0;

        // Initialize smart dialing state. This has to be done before anything is filled in before
        // the dialpad edittext to prevent entries from being loaded from a null cache.
        // initializeSmartDialingState();

//        Activity parent = getActivity();
//        if (parent instanceof DialtactsActivity) {
//            // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
//            // digits in the dialer field.
//            fillDigitsIfNecessary(parent.getIntent());
//        }

        // stopWatch.lap("fdin");

        // While we're in the foreground, listen for phone state changes,
        // purely so that we can take down the "dialpad chooser" if the
        // phone becomes idle while the chooser UI is visible.
//        TelephonyManager telephonyManager =
//                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        // stopWatch.lap("tm");

        // Potentially show hint text in the mDigits field when the user
        // hasn't typed any digits yet.  (If there's already an active call,
        // this hint text will remind the user that he's about to add a new
        // call.)
        //
        // TODO: consider adding better UI for the case where *both* lines
        // are currently in use.  (Right now we let the user try to add
        // another call, but that call is guaranteed to fail.  Perhaps the
        // entire dialer UI should be disabled instead.)
//        if (phoneIsInUse()) {
//            final SpannableString hint = new SpannableString(
//                    getActivity().getString(R.string.dialerDialpadHintText));
//            hint.setSpan(new RelativeSizeSpan(0.8f), 0, hint.length(), 0);
//            mDigits.setHint(hint);
//        } else {
//            // Common case; no hint necessary.
//            mDigits.setHint(null);
//
//            // Also, a sanity-check: the "dialpad chooser" UI should NEVER
//            // be visible if the phone is idle!
//            showDialpadChooser(false);
//        }

        // mFirstLaunch = false;

        // stopWatch.lap("hnt");

        // updateDialAndDeleteButtonEnabledState();

        // stopWatch.lap("bes");

        // stopWatch.stopAndLog(TAG, 50);
    }

    @Override
    public void onPause() {
        super.onPause();

//        // Stop listening for phone state changes.
//        TelephonyManager telephonyManager =
//                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        // Make sure we don't leave this activity with a tone still playing.
        stopTone();
        // Just in case reset the counter too.
        mDialpadPressCount = 0;

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
        // TODO: I wonder if we should not check if the AsyncTask that
        // lookup the last dialed number has completed.
//        mLastNumberDialed = EMPTY_NUMBER;  // Since we are going to query again, free stale number.
//
//        SpecialCharSequenceMgr.cleanup();
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mClearDigitsOnStop) {
//            mClearDigitsOnStop = false;
//            mDigits.getText().clear();
//        }
    }
}
