package com.kongzue.dialogx.dialogs;

import android.animation.Animator;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.R;
import com.kongzue.dialogx.impl.AnimatorListenerEndCallBack;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback;
import com.kongzue.dialogx.interfaces.DialogXStyle;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnInputDialogButtonClickListener;
import com.kongzue.dialogx.util.views.BlurView;
import com.kongzue.dialogx.util.views.DialogXBaseRelativeLayout;
import com.kongzue.dialogx.util.InputInfo;
import com.kongzue.dialogx.util.views.MaxRelativeLayout;
import com.kongzue.dialogx.util.TextInfo;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/21 17:08
 */
public class MessageDialog extends BaseDialog {
    
    protected MessageDialog me;
    
    private DialogLifecycleCallback<MessageDialog> dialogLifecycleCallback;
    
    public MessageDialog() {
        me = this;
    }
    
    private View dialogView;
    
    protected CharSequence title;
    protected CharSequence message;
    protected CharSequence okText;
    protected CharSequence cancelText;
    protected CharSequence otherText;
    protected String inputText;
    protected String inputHintText;
    
    protected TextInfo titleTextInfo;
    protected TextInfo messageTextInfo;
    protected TextInfo okTextInfo;
    protected TextInfo cancelTextInfo;
    protected TextInfo otherTextInfo;
    protected InputInfo inputInfo;
    
    protected OnDialogButtonClickListener okButtonClickListener;
    protected OnDialogButtonClickListener cancelButtonClickListener;
    protected OnDialogButtonClickListener otherButtonClickListener;
    
    protected int buttonOrientation;
    
    public static MessageDialog build() {
        return new MessageDialog();
    }
    
    public MessageDialog(CharSequence title, CharSequence message, CharSequence okText) {
        this.title = title;
        this.message = message;
        this.okText = okText;
    }
    
    public static MessageDialog show(CharSequence title, CharSequence message, CharSequence okText) {
        MessageDialog messageDialog = new MessageDialog(title, message, okText);
        messageDialog.show();
        return messageDialog;
    }
    
    public MessageDialog(CharSequence title, CharSequence message, CharSequence okText, CharSequence cancelText) {
        this.title = title;
        this.message = message;
        this.okText = okText;
        this.cancelText = cancelText;
    }
    
    public static MessageDialog show(CharSequence title, CharSequence message, CharSequence okText, CharSequence cancelText) {
        MessageDialog messageDialog = new MessageDialog(title, message, okText, cancelText);
        messageDialog.show();
        return messageDialog;
    }
    
    public MessageDialog(CharSequence title, CharSequence message, CharSequence okText, CharSequence cancelText, CharSequence otherText) {
        this.title = title;
        this.message = message;
        this.okText = okText;
        this.cancelText = cancelText;
        this.otherText = otherText;
    }
    
    public static MessageDialog show(CharSequence title, CharSequence message, CharSequence okText, CharSequence cancelText, CharSequence otherText) {
        MessageDialog messageDialog = new MessageDialog(title, message, okText, cancelText, otherText);
        messageDialog.show();
        return messageDialog;
    }
    
    protected DialogImpl dialogImpl;
    
    public void show() {
        int layoutId = R.layout.layout_dialogx_material;
        switch (theme) {
            case LIGHT:
                layoutId = style.layout(true);
                break;
            case DARK:
                layoutId = style.layout(false);
                break;
        }
        dialogView = createView(layoutId);
        dialogImpl = new DialogImpl(dialogView);
        show(dialogView);
    }
    
    public void refreshUI() {
        if (dialogImpl == null) return;
        dialogImpl.refreshView();
    }
    
    class DialogImpl {
        BlurView blurView;
        
        DialogXBaseRelativeLayout boxRoot;
        MaxRelativeLayout bkg;
        TextView txtDialogTitle;
        TextView txtDialogTip;
        RelativeLayout boxCustom;
        EditText txtInput;
        LinearLayout boxButton;
        TextView btnSelectOther;
        View spaceOtherButton;
        TextView btnSelectNegative;
        TextView btnSelectPositive;
        
        public DialogImpl(View convertView) {
            boxRoot = convertView.findViewById(R.id.box_root);
            bkg = convertView.findViewById(R.id.bkg);
            txtDialogTitle = convertView.findViewById(R.id.txt_dialog_title);
            txtDialogTip = convertView.findViewById(R.id.txt_dialog_tip);
            boxCustom = convertView.findViewById(R.id.box_custom);
            txtInput = convertView.findViewById(R.id.txt_input);
            boxButton = convertView.findViewById(R.id.box_button);
            btnSelectOther = convertView.findViewById(R.id.btn_selectOther);
            spaceOtherButton = convertView.findViewById(R.id.space_other_button);
            btnSelectNegative = convertView.findViewById(R.id.btn_selectNegative);
            btnSelectPositive = convertView.findViewById(R.id.btn_selectPositive);
            init();
            refreshView();
        }
        
        private void init() {
            txtDialogTitle.getPaint().setFakeBoldText(true);
            btnSelectNegative.getPaint().setFakeBoldText(true);
            btnSelectPositive.getPaint().setFakeBoldText(true);
            btnSelectOther.getPaint().setFakeBoldText(true);
            
            boxRoot.setOnLifecycleCallBack(new DialogXBaseRelativeLayout.OnLifecycleCallBack() {
                @Override
                public void onShow() {
                    isShow = true;
                    boxRoot.setAlpha(0f);
                    int enterAnimResId = style.enterAnimResId() == 0 ? R.anim.anim_dialogx_default_enter : style.enterAnimResId();
                    Animation enterAnim = AnimationUtils.loadAnimation(getContext(), enterAnimResId);
                    enterAnim.setInterpolator(new DecelerateInterpolator());
                    bkg.startAnimation(enterAnim);
                    
                    boxRoot.animate().setDuration(enterAnim.getDuration()).alpha(1f).setInterpolator(new DecelerateInterpolator()).setDuration(300).setListener(null);
                    
                    getDialogLifecycleCallback().onShow(me);
                    
                    if (style.blurSettings() != null && style.blurSettings().blurBackground()) {
                        bkg.post(new Runnable() {
                            @Override
                            public void run() {
                                int blurFrontColor = getResources().getColor(style.blurSettings().blurForwardColorRes(isLightTheme()));
                                blurView = new BlurView(bkg.getContext(), null);
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(bkg.getWidth(), bkg.getHeight());
                                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                                blurView.setOverlayColor(blurFrontColor);
                                blurView.setTag("blurView");
                                blurView.setRadiusPx(style.blurSettings().blurBackgroundRoundRadiusPx());
                                bkg.addView(blurView, 0, params);
                            }
                        });
                    }
                    
                    if (autoShowInputKeyboard) {
                        txtInput.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                txtInput.requestFocus();
                                txtInput.setFocusableInTouchMode(true);
                                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(txtInput, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                                txtInput.setSelection(txtInput.getText().length());
                            }
                        }, 300);
                    }
                }
                
                @Override
                public void onDismiss() {
                    isShow = false;
                    getDialogLifecycleCallback().onDismiss(me);
                }
            });
        }
        
        private void refreshView() {
            bkg.setMaxWidth(DialogX.dialogMaxWidth);
            if (me instanceof InputDialog) {
                txtInput.setVisibility(View.VISIBLE);
            } else {
                txtInput.setVisibility(View.GONE);
            }
            boxRoot.setClickable(true);
            
            showText(txtDialogTitle, title);
            showText(txtDialogTip, message);
            showText(btnSelectPositive, okText);
            showText(btnSelectNegative, cancelText);
            showText(btnSelectOther, otherText);
            showText(txtInput, inputText);
            if (spaceOtherButton != null) {
                if (otherText == null) {
                    spaceOtherButton.setVisibility(View.GONE);
                } else {
                    spaceOtherButton.setVisibility(View.VISIBLE);
                }
            }
            
            useTextInfo(txtDialogTitle, titleTextInfo);
            useTextInfo(txtDialogTip, messageTextInfo);
            useTextInfo(btnSelectPositive, okTextInfo);
            useTextInfo(btnSelectNegative, cancelTextInfo);
            useTextInfo(btnSelectOther, otherTextInfo);
            if (inputInfo != null) {
                if (inputInfo.getMAX_LENGTH() != -1)
                    txtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputInfo.getMAX_LENGTH())});
                int inputType = InputType.TYPE_CLASS_TEXT | inputInfo.getInputType();
                if (inputInfo.isMultipleLines()) {
                    inputType = inputType | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
                }
                txtInput.setInputType(inputType);
                if (inputInfo.getTextInfo() != null)
                    useTextInfo(txtInput, inputInfo.getTextInfo());
                
                if (inputInfo.isSelectAllText()) {
                    txtInput.post(new Runnable() {
                        @Override
                        public void run() {
                            txtInput.selectAll();
                        }
                    });
                }
            }
            
            int visibleButtonCount = 0;
            if (!isNull(okText)) {
                visibleButtonCount = visibleButtonCount + 1;
            }
            if (!isNull(cancelText)) {
                visibleButtonCount = visibleButtonCount + 1;
            }
            if (!isNull(otherText)) {
                visibleButtonCount = visibleButtonCount + 1;
            }
            
            boxButton.setOrientation(buttonOrientation);
            if (buttonOrientation == LinearLayout.VERTICAL) {
                //纵向
                if (style.verticalButtonOrder() != null && style.verticalButtonOrder().length != 0) {
                    boxButton.removeAllViews();
                    for (int buttonType : style.verticalButtonOrder()) {
                        switch (buttonType) {
                            case DialogXStyle.BUTTON_OK:
                                boxButton.addView(btnSelectPositive);
                                if (style.overrideVerticalButtonRes() != null) {
                                    btnSelectPositive.setBackgroundResource(
                                            style.overrideVerticalButtonRes().overrideVerticalOkButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.BUTTON_OTHER:
                                boxButton.addView(btnSelectOther);
                                if (style.overrideVerticalButtonRes() != null) {
                                    btnSelectOther.setBackgroundResource(
                                            style.overrideVerticalButtonRes().overrideVerticalOtherButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.BUTTON_CANCEL:
                                boxButton.addView(btnSelectNegative);
                                if (style.overrideVerticalButtonRes() != null) {
                                    btnSelectNegative.setBackgroundResource(
                                            style.overrideVerticalButtonRes().overrideVerticalCancelButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.SPACE:
                                Space space = new Space(getContext());
                                LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                spaceLp.weight = 1;
                                boxButton.addView(space, spaceLp);
                                break;
                            case DialogXStyle.SPLIT:
                                View splitView = new View(getContext());
                                splitView.setBackgroundColor(getResources().getColor(style.splitColorRes(isLightTheme())));
                                LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, style.splitWidthPx());
                                boxButton.addView(splitView, viewLp);
                                break;
                        }
                    }
                }
            } else {
                //横向
                if (style.horizontalButtonOrder() != null && style.horizontalButtonOrder().length != 0) {
                    boxButton.removeAllViews();
                    for (int buttonType : style.horizontalButtonOrder()) {
                        switch (buttonType) {
                            case DialogXStyle.BUTTON_OK:
                                boxButton.addView(btnSelectPositive);
                                if (style.overrideHorizontalButtonRes() != null) {
                                    btnSelectPositive.setBackgroundResource(
                                            style.overrideHorizontalButtonRes().overrideHorizontalOkButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.BUTTON_OTHER:
                                boxButton.addView(btnSelectOther);
                                if (style.overrideHorizontalButtonRes() != null) {
                                    btnSelectOther.setBackgroundResource(
                                            style.overrideHorizontalButtonRes().overrideHorizontalOtherButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.BUTTON_CANCEL:
                                boxButton.addView(btnSelectNegative);
                                if (style.overrideHorizontalButtonRes() != null) {
                                    btnSelectNegative.setBackgroundResource(
                                            style.overrideHorizontalButtonRes().overrideHorizontalCancelButtonBackgroundRes(visibleButtonCount, isLightTheme())
                                    );
                                }
                                break;
                            case DialogXStyle.SPACE:
                                if (boxButton.getChildCount() >= 1) {
                                    if (boxButton.getChildAt(boxButton.getChildCount() - 1).getVisibility() == View.GONE) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                                Space space = new Space(getContext());
                                LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                spaceLp.weight = 1;
                                boxButton.addView(space, spaceLp);
                                break;
                            case DialogXStyle.SPLIT:
                                if (boxButton.getChildCount() >= 1) {
                                    if (boxButton.getChildAt(boxButton.getChildCount() - 1).getVisibility() == View.GONE) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                                View splitView = new View(getContext());
                                splitView.setBackgroundColor(getResources().getColor(style.splitColorRes(isLightTheme())));
                                LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(style.splitWidthPx(), ViewGroup.LayoutParams.MATCH_PARENT);
                                boxButton.addView(splitView, viewLp);
                                break;
                        }
                    }
                }
            }
            
            //Events
            if (cancelable) {
                boxRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doDismiss(v);
                    }
                });
            } else {
                boxRoot.setOnClickListener(null);
            }
            boxRoot.setOnBackPressedListener(new OnBackPressedListener() {
                @Override
                public boolean onBackPressed() {
                    if (cancelable) {
                        dismiss();
                    }
                    return false;
                }
            });
            btnSelectPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (okButtonClickListener != null) {
                        if (okButtonClickListener instanceof OnInputDialogButtonClickListener) {
                            String s = txtInput == null ? "" : txtInput.getText().toString();
                            if (!((OnInputDialogButtonClickListener) okButtonClickListener).onClick(me, v, s)) {
                                doDismiss(v);
                            }
                        } else {
                            if (!okButtonClickListener.onClick(me, v)) {
                                doDismiss(v);
                            }
                        }
                    } else {
                        doDismiss(v);
                    }
                }
            });
            btnSelectNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cancelButtonClickListener != null) {
                        if (cancelButtonClickListener instanceof OnInputDialogButtonClickListener) {
                            String s = txtInput == null ? "" : txtInput.getText().toString();
                            if (!((OnInputDialogButtonClickListener) cancelButtonClickListener).onClick(me, v, s)) {
                                doDismiss(v);
                            }
                        } else {
                            if (!cancelButtonClickListener.onClick(me, v)) {
                                doDismiss(v);
                            }
                        }
                    } else {
                        doDismiss(v);
                    }
                }
            });
            btnSelectOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (otherButtonClickListener != null) {
                        if (otherButtonClickListener instanceof OnInputDialogButtonClickListener) {
                            String s = txtInput == null ? "" : txtInput.getText().toString();
                            if (!((OnInputDialogButtonClickListener) otherButtonClickListener).onClick(me, v, s)) {
                                doDismiss(v);
                            }
                        } else {
                            if (!otherButtonClickListener.onClick(me, v)) {
                                doDismiss(v);
                            }
                        }
                    } else {
                        doDismiss(v);
                    }
                }
            });
            
        }
        
        public void doDismiss(View v) {
            if (v != null) v.setEnabled(false);
            
            int exitAnimResId = style.exitAnimResId() == 0 ? R.anim.anim_dialogx_default_exit : style.exitAnimResId();
            Animation enterAnim = AnimationUtils.loadAnimation(getContext(), exitAnimResId);
            enterAnim.setInterpolator(new AccelerateInterpolator());
            bkg.startAnimation(enterAnim);
            
            boxRoot.animate().setDuration(300).alpha(0f).setInterpolator(new AccelerateInterpolator()).setDuration(enterAnim.getDuration()).setListener(new AnimatorListenerEndCallBack() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dismiss(dialogView);
                }
            });
        }
    }
    
    public void dismiss() {
        if (dialogImpl == null) return;
        dialogImpl.doDismiss(null);
    }
    
    public DialogLifecycleCallback<MessageDialog> getDialogLifecycleCallback() {
        return dialogLifecycleCallback == null ? new DialogLifecycleCallback<MessageDialog>() {
        } : dialogLifecycleCallback;
    }
    
    public MessageDialog setDialogLifecycleCallback(DialogLifecycleCallback<MessageDialog> dialogLifecycleCallback) {
        this.dialogLifecycleCallback = dialogLifecycleCallback;
        return this;
    }
    
    public MessageDialog setStyle(DialogXStyle style) {
        this.style = style;
        return this;
    }
    
    public MessageDialog setTheme(DialogX.THEME theme) {
        this.theme = theme;
        return this;
    }
    
    public CharSequence getOkButton() {
        return okText;
    }
    
    public MessageDialog setOkButton(CharSequence okText) {
        this.okText = okText;
        refreshUI();
        return this;
    }
    
    public MessageDialog setOkButton(OnDialogButtonClickListener okButtonClickListener) {
        this.okButtonClickListener = okButtonClickListener;
        refreshUI();
        return this;
    }
    
    public MessageDialog setOkButton(CharSequence okText, OnDialogButtonClickListener okButtonClickListener) {
        this.okText = okText;
        this.okButtonClickListener = okButtonClickListener;
        refreshUI();
        return this;
    }
    
    public CharSequence getCancelButton() {
        return cancelText;
    }
    
    public MessageDialog setCancelButton(CharSequence cancelText) {
        this.cancelText = cancelText;
        refreshUI();
        return this;
    }
    
    public MessageDialog setCancelButton(OnDialogButtonClickListener cancelButtonClickListener) {
        this.cancelButtonClickListener = cancelButtonClickListener;
        refreshUI();
        return this;
    }
    
    public MessageDialog setCancelButton(CharSequence cancelText, OnDialogButtonClickListener cancelButtonClickListener) {
        this.cancelText = cancelText;
        this.cancelButtonClickListener = cancelButtonClickListener;
        refreshUI();
        return this;
    }
    
    public CharSequence getOtherButton() {
        return otherText;
    }
    
    public MessageDialog setOtherButton(CharSequence otherText) {
        this.otherText = otherText;
        refreshUI();
        return this;
    }
    
    public MessageDialog setOtherButton(OnDialogButtonClickListener otherButtonClickListener) {
        this.otherButtonClickListener = otherButtonClickListener;
        refreshUI();
        return this;
    }
    
    public MessageDialog setOtherButton(CharSequence otherText, OnDialogButtonClickListener otherButtonClickListener) {
        this.otherText = otherText;
        this.otherButtonClickListener = otherButtonClickListener;
        refreshUI();
        return this;
    }
    
    public OnDialogButtonClickListener getOkButtonClickListener() {
        return okButtonClickListener;
    }
    
    public MessageDialog setOkButtonClickListener(OnDialogButtonClickListener okButtonClickListener) {
        this.okButtonClickListener = okButtonClickListener;
        refreshUI();
        return this;
    }
    
    public OnDialogButtonClickListener getCancelButtonClickListener() {
        return cancelButtonClickListener;
    }
    
    public MessageDialog setCancelButtonClickListener(OnDialogButtonClickListener cancelButtonClickListener) {
        this.cancelButtonClickListener = cancelButtonClickListener;
        refreshUI();
        return this;
    }
    
    public OnDialogButtonClickListener getOtherButtonClickListener() {
        return otherButtonClickListener;
    }
    
    public MessageDialog setOtherButtonClickListener(OnDialogButtonClickListener otherButtonClickListener) {
        this.otherButtonClickListener = otherButtonClickListener;
        refreshUI();
        return this;
    }
    
    public CharSequence getTitle() {
        return title;
    }
    
    public MessageDialog setTitle(CharSequence title) {
        this.title = title;
        refreshUI();
        return this;
    }
    
    public CharSequence getMessage() {
        return message;
    }
    
    public MessageDialog setMessage(CharSequence message) {
        this.message = message;
        refreshUI();
        return this;
    }
    
    public TextInfo getTitleTextInfo() {
        return titleTextInfo;
    }
    
    public MessageDialog setTitleTextInfo(TextInfo titleTextInfo) {
        this.titleTextInfo = titleTextInfo;
        refreshUI();
        return this;
    }
    
    public TextInfo getMessageTextInfo() {
        return messageTextInfo;
    }
    
    public MessageDialog setMessageTextInfo(TextInfo messageTextInfo) {
        this.messageTextInfo = messageTextInfo;
        refreshUI();
        return this;
    }
    
    public TextInfo getOkTextInfo() {
        return okTextInfo;
    }
    
    public MessageDialog setOkTextInfo(TextInfo okTextInfo) {
        this.okTextInfo = okTextInfo;
        refreshUI();
        return this;
    }
    
    public TextInfo getCancelTextInfo() {
        return cancelTextInfo;
    }
    
    public MessageDialog setCancelTextInfo(TextInfo cancelTextInfo) {
        this.cancelTextInfo = cancelTextInfo;
        refreshUI();
        return this;
    }
    
    public TextInfo getOtherTextInfo() {
        return otherTextInfo;
    }
    
    public MessageDialog setOtherTextInfo(TextInfo otherTextInfo) {
        this.otherTextInfo = otherTextInfo;
        refreshUI();
        return this;
    }
    
    public int getButtonOrientation() {
        return buttonOrientation;
    }
    
    public MessageDialog setButtonOrientation(int buttonOrientation) {
        this.buttonOrientation = buttonOrientation;
        refreshUI();
        return this;
    }
    
    public boolean isCancelable() {
        return cancelable;
    }
    
    public MessageDialog setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        refreshUI();
        return this;
    }
    
    public OnBackPressedListener getOnBackPressedListener() {
        return onBackPressedListener;
    }
    
    public MessageDialog setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
        refreshUI();
        return this;
    }
}