package com.kongzue.dialogx.util.views;

import static android.view.WindowInsetsAnimation.Callback.DISPATCH_MODE_STOP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.R;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnSafeInsetsChangeListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/22 13:53
 */
public class DialogXBaseRelativeLayout extends RelativeLayout {
    
    private OnSafeInsetsChangeListener onSafeInsetsChangeListener;
    private BaseDialog parentDialog;
    private boolean autoUnsafePlacePadding = true;
    private boolean focusable = true;
    private boolean interceptBack = true;
    
    private OnLifecycleCallBack onLifecycleCallBack;
    private PrivateBackPressedListener onBackPressedListener;
    
    public DialogXBaseRelativeLayout(Context context) {
        super(context);
        init(null);
    }
    
    public DialogXBaseRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DialogXBaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private boolean isInited = false;
    
    private void init(AttributeSet attrs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForceDarkAllowed(false);
        }
        if (!isInited) {
            if (attrs != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DialogXBaseRelativeLayout);
                focusable = a.getBoolean(R.styleable.DialogXBaseRelativeLayout_baseFocusable, true);
                autoUnsafePlacePadding = a.getBoolean(R.styleable.DialogXBaseRelativeLayout_autoSafeArea, true);
                interceptBack = a.getBoolean(R.styleable.DialogXBaseRelativeLayout_interceptBack, true);
                a.recycle();
                isInited = true;
            }
            if (focusable) {
                setFocusable(true);
                setFocusableInTouchMode(true);
                requestFocus();
            }
            setBkgAlpha(0f);
            if (parentDialog != null && parentDialog.getDialogImplMode() != DialogX.IMPL_MODE.VIEW) {
                setFitsSystemWindows(true);
            }
        }
    }
    
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (DialogX.useActivityLayoutTranslationNavigationBar || parentDialog.getDialogImplMode() != DialogX.IMPL_MODE.VIEW) {
            paddingView(insets.left, insets.top, insets.right, insets.bottom);
        }
        return super.fitSystemWindows(insets);
    }
    
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (DialogX.useActivityLayoutTranslationNavigationBar || parentDialog.getDialogImplMode() != DialogX.IMPL_MODE.VIEW) {
                paddingView(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
        }
        return super.dispatchApplyWindowInsets(insets);
    }
    
    public void paddingView(WindowInsets insets) {
        if (insets == null) {
            if (BaseDialog.publicWindowInsets() != null) {
                insets = BaseDialog.publicWindowInsets();
            } else {
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paddingView(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isAttachedToWindow() && event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK && interceptBack) {
            if (onBackPressedListener != null) {
                return onBackPressedListener.onBackPressed();
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.onTouchEvent(event);
    }
    
    private ViewTreeObserver.OnGlobalLayoutListener decorViewLayoutListener;
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            final ViewParent parent = getParent();
            if (parent instanceof View) {
                ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));
            }
            ViewCompat.requestApplyInsets(this);
            
            if (BaseDialog.getTopActivity() == null) return;
    
            View decorView = (View) getParent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setWindowInsetsAnimationCallback(decorView, new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
    
                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        paddingView(insets.toWindowInsets());
                        return insets;
                    }
                });
            }else{
                decorView.getViewTreeObserver().addOnGlobalLayoutListener(decorViewLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            paddingView(getRootWindowInsets());
                        } else {
                            if (BaseDialog.getTopActivity() == null) return;
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            ((Activity) BaseDialog.getTopActivity()).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
                            Rect rect = new Rect();
                            View decorView = (View) getParent();
                            decorView.getWindowVisibleDisplayFrame(rect);
                            paddingView(rect.left, rect.top, displayMetrics.widthPixels - rect.right, displayMetrics.heightPixels - rect.bottom);
                        }
                    }
                });
                decorViewLayoutListener.onGlobalLayout();
            }
            
            if (onLifecycleCallBack != null) {
                onLifecycleCallBack.onShow();
            }
            isLightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO;
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        View decorView = (View) getParent();
        if (decorViewLayoutListener != null && decorView != null) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(decorViewLayoutListener);
        }
        if (onLifecycleCallBack != null) {
            onLifecycleCallBack.onDismiss();
        }
        onSafeInsetsChangeListener = null;
        super.onDetachedFromWindow();
    }
    
    @Override
    public boolean performClick() {
        if (!isEnabled()) return false;
        return super.performClick();
    }
    
    @Override
    public boolean callOnClick() {
        if (!isEnabled()) return false;
        return super.callOnClick();
    }
    
    public DialogXBaseRelativeLayout setOnLifecycleCallBack(OnLifecycleCallBack onLifecycleCallBack) {
        this.onLifecycleCallBack = onLifecycleCallBack;
        return this;
    }
    
    public float getSafeHeight() {
        return getMeasuredHeight() - unsafePlace.bottom - unsafePlace.top;
    }
    
    private WeakReference<View> requestFocusView;
    
    public void bindFocusView(View view) {
        requestFocusView = new WeakReference<>(view);
    }
    
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (direction == View.FOCUS_DOWN && requestFocusView != null && requestFocusView.get() != null) {
            return requestFocusView.get().requestFocus();
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }
    
    public abstract static class OnLifecycleCallBack {
        public void onShow() {
        }
        
        public abstract void onDismiss();
    }
    
    protected Rect unsafePlace = new Rect();
    
    private void paddingView(int left, int top, int right, int bottom) {
        unsafePlace = new Rect(left, top, right, bottom);
        if (onSafeInsetsChangeListener != null) onSafeInsetsChangeListener.onChange(unsafePlace);
        MaxRelativeLayout bkgView = findViewById(R.id.bkg);
        if (bkgView != null && bkgView.getLayoutParams() instanceof LayoutParams) {
            LayoutParams bkgLp = (LayoutParams) bkgView.getLayoutParams();
            if (bkgLp.getRules()[ALIGN_PARENT_BOTTOM] == RelativeLayout.TRUE && isAutoUnsafePlacePadding()) {
                bkgView.setPadding(0, 0, 0, bottom);
                bkgView.setNavBarHeight(bottom);
                setPadding(extraPadding[0] + left, extraPadding[1] + top, extraPadding[2] + right, extraPadding[3]);
                return;
            }
        }
        if (isAutoUnsafePlacePadding())
            setPadding(extraPadding[0] + left, extraPadding[1] + top, extraPadding[2] + right, extraPadding[3] + bottom);
    }
    
    public DialogXBaseRelativeLayout setOnBackPressedListener(PrivateBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
        return this;
    }
    
    public OnSafeInsetsChangeListener getOnSafeInsetsChangeListener() {
        return onSafeInsetsChangeListener;
    }
    
    public DialogXBaseRelativeLayout setOnSafeInsetsChangeListener(OnSafeInsetsChangeListener onSafeInsetsChangeListener) {
        this.onSafeInsetsChangeListener = onSafeInsetsChangeListener;
        return this;
    }
    
    public boolean isAutoUnsafePlacePadding() {
        return autoUnsafePlacePadding;
    }
    
    public Rect getUnsafePlace() {
        return unsafePlace;
    }
    
    public DialogXBaseRelativeLayout setAutoUnsafePlacePadding(boolean autoUnsafePlacePadding) {
        this.autoUnsafePlacePadding = autoUnsafePlacePadding;
        if (!autoUnsafePlacePadding) {
            setPadding(extraPadding[0], extraPadding[1], extraPadding[2], extraPadding[3]);
        }
        return this;
    }
    
    public BaseDialog getParentDialog() {
        return parentDialog;
    }
    
    public DialogXBaseRelativeLayout setParentDialog(BaseDialog parentDialog) {
        this.parentDialog = parentDialog;
        if (parentDialog.getDialogImplMode() != DialogX.IMPL_MODE.VIEW) {
            setFitsSystemWindows(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                paddingView(getRootWindowInsets());
            }
        }
        return this;
    }
    
    boolean isLightMode = true;
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean newLightStatus = ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO);
        if (isLightMode != newLightStatus && DialogX.globalTheme == DialogX.THEME.AUTO) {
            getParentDialog().restartDialog();
        }
    }
    
    float nowBkgAlphaValue;
    
    public DialogXBaseRelativeLayout setBkgAlpha(float alpha) {
        nowBkgAlphaValue = alpha;
        if (getBackground() != null) getBackground().mutate().setAlpha((int) (alpha * 255));
        return this;
    }
    
    @Override
    public void setBackground(Drawable background) {
        background.setAlpha((int) (nowBkgAlphaValue * 255));
        super.setBackground(background);
    }
    
    @Override
    public void setBackgroundColor(int color) {
        setBackground(new ColorDrawable(color));
    }
    
    public boolean isBaseFocusable() {
        return focusable;
    }
    
    public boolean isInterceptBack() {
        return interceptBack;
    }
    
    public DialogXBaseRelativeLayout setInterceptBack(boolean interceptBack) {
        this.interceptBack = interceptBack;
        return this;
    }
    
    @Override
    public void setVisibility(int visibility) {
        if (visibility == GONE && getAlpha() == 0f) {
            setAlpha(0.01f);
        }
        super.setVisibility(visibility);
    }
    
    public interface PrivateBackPressedListener {
        boolean onBackPressed();
    }
    
    int[] extraPadding = new int[4];
    
    public void setRootPadding(int left, int top, int right, int bottom) {
        extraPadding[0] = left;
        extraPadding[1] = top;
        extraPadding[2] = right;
        extraPadding[3] = bottom;
    }
    
    public int getRootPaddingLeft() {
        return extraPadding[0];
    }
    
    public int getRootPaddingTop() {
        return extraPadding[1];
    }
    
    public int getRootPaddingRight() {
        return extraPadding[2];
    }
    
    public int getRootPaddingBottom() {
        return extraPadding[3];
    }
    
    public int getUseAreaWidth() {
        return getWidth() - getRootPaddingRight();
    }
    
    public int getUseAreaHeight() {
        return getHeight() - getRootPaddingBottom();
    }
}
