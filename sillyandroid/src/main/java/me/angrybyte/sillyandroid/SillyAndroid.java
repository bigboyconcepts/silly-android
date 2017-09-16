package me.angrybyte.sillyandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RawRes;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

/**
 * This is the library basis. It contains methods used to customize and adapt system-provided Android components.
 */
@SuppressWarnings("WeakerAccess")
public final class SillyAndroid {

    private static final String TAG = SillyAndroid.class.getSimpleName();

    /**
     * A wrapper class to shorten the UI configuration queries.
     */
    @SuppressWarnings("unused")
    public static final class UI {

        public static final int PHONE_PORT = 1;
        public static final int PHONE_LAND = 2;
        public static final int TAB_PORT = 3;
        public static final int TAB_LAND = 4;
        public static final int TABLET_PORT = 5;
        public static final int TABLET_LAND = 6;
        public static final int WATCH = 7;
        public static final int TV = 8;

        /**
         * A device type constant set, one of {@link #PHONE_PORT}, {@link #PHONE_LAND}, {@link #TAB_PORT}, {@link #TAB_LAND}, {@link #TABLET_PORT},
         * {@link #TABLET_LAND}, {@link #WATCH}, {@link #TV}.
         */
        @IntDef({PHONE_PORT, PHONE_LAND, TAB_PORT, TAB_LAND, TABLET_PORT, TABLET_LAND, WATCH, TV})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DeviceType {}

        /**
         * Checks if current configuration is detected as a phone.
         */
        public static boolean isPhone(@NonNull final Context context) {
            return UI.getDeviceType(context) == PHONE_PORT || UI.getDeviceType(context) == PHONE_LAND;
        }

        /**
         * Checks if current configuration is detected as a small tablet (tab).
         */
        public static boolean isTab(@NonNull final Context context) {
            return UI.getDeviceType(context) == TAB_PORT || UI.getDeviceType(context) == TAB_LAND;
        }

        /**
         * Checks if current configuration is detected as a tablet.
         */
        public static boolean isTablet(@NonNull final Context context) {
            return UI.getDeviceType(context) == TABLET_PORT || UI.getDeviceType(context) == TABLET_LAND;
        }

        /**
         * Checks if current configuration is detected as a watch.
         */
        public static boolean isWatch(@NonNull final Context context) {
            return UI.getDeviceType(context) == WATCH;
        }

        /**
         * Checks if current configuration is detected as a Television device.
         */
        public static boolean isTelevision(@NonNull final Context context) {
            return UI.getDeviceType(context) == TV;
        }

        /**
         * Checks whether the given activity is in PIP mode (picture-in-picture).
         */
        public static boolean isInPictureInPictureMode(@Nullable final Activity activity) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity != null && activity.isInPictureInPictureMode();
        }

        /**
         * Checks whether the given activity is in multi-window (split-screen) mode.
         */
        public static boolean isInMultiWindowMode(@Nullable final Activity activity) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity != null && activity.isInMultiWindowMode();
        }

        /**
         * Gets the current display's size in pixels.
         *
         * @param context Which context to use to check the size
         * @return A new {@link Point} object containing absolute screen width (x) and height (y), in pixels
         */
        @NonNull
        public static Point getScreenSize(@NonNull final Context context) {
            final int x = context.getResources().getDisplayMetrics().widthPixels;
            final int y = context.getResources().getDisplayMetrics().heightPixels;
            return new Point(x, y);
        }

        /**
         * Gets the current display's density expressed in DPI.
         *
         * @param context Which context to use to check the DPI density
         * @return The screen density expressed as dots-per-inch, either {@link DisplayMetrics#DENSITY_LOW},
         * {@link DisplayMetrics#DENSITY_MEDIUM}, or {@link DisplayMetrics#DENSITY_HIGH}
         */
        public static int getDensityDpi(@NonNull final Context context) {
            return context.getResources().getDisplayMetrics().densityDpi;
        }

        /**
         * Checks the device type. Integer values:<br>
         * <ol>
         * <li>Phone, portrait</li>
         * <li>Phone, landscape</li>
         * <li>7" to 9" Tablet (called Tab), portrait</li>
         * <li>7" to 9" Tablet (called Tab), landscape</li>
         * <li>9" or bigger Tablet, portrait</li>
         * <li>9" or bigger Tablet, landscape</li>
         * <li>Watch Wearable</li>
         * <li>Television unit</li>
         * </ol>
         *
         * <b>Note</b>: This relies on Android's resource configuration framework. Use with caution.
         *
         * @param context A context to use to detect device type
         * @return The current device type (integer), one of {@link DeviceType} constants
         */
        @DeviceType
        public static int getDeviceType(@NonNull final Context context) {
            // noinspection WrongConstant
            return context.getResources().getInteger(R.integer.config_device_type);
        }

        /**
         * Tries to get the status bar height for this device. Even if the value returned is larger than 0, that does not mean the status bar is visible.
         *
         * @param context A context to use to find the status bar height
         * @return Height of the status bar on the running device, in pixels
         */
        @IntRange(from = 0)
        public static int getStatusBarHeight(@NonNull final Context context) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        }

        /**
         * Tries to get the navigation bar height for this device. Even if the value returned is larger than 0, that does not mean the nav bar is visible.
         *
         * @param context A context to use to find the navigation bar height
         * @return Height of the navigation bar on the running device, in pixels
         */
        @IntRange(from = 0)
        public static int getNavigationBarHeight(@NonNull final Context context) {
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return context.getResources().getDimensionPixelSize(resourceId);
            }
            return 0;
        }
    }

    /**
     * A listener interface used for observing changes in layout height, i.e. when the software keyboard pops up or hides.
     */
    public interface OnKeyboardChangeListener {

        /**
         * Notifies the listener of a new keyboard pop-up, i.e. when the software keyboard is shown.
         *
         * @param size How high is the keyboard, in pixels
         */
        void onKeyboardShown(@IntRange(from = 1) final int size);

        /**
         * Notifies the listener of an event when the software keyboards hides from the view.
         */
        void onKeyboardHidden();
    }

    /**
     * Making sure that this class' default constructor is private.
     */
    private SillyAndroid() {}

    /**
     * Counts the available intent handlers in the OS for the given intent.
     *
     * @param context Which context to use
     * @param intent  Which intent to check
     * @return A positive number, representing the number of activities that could handle the given intents. A {@code null} intent results
     * in a '{@code 0}' result
     */
    @IntRange(from = 0)
    public static int countIntentHandlers(@NonNull final Context context, @Nullable final Intent intent) {
        if (intent == null) {
            return 0;
        }
        final List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent, 0);
        return activities == null ? 0 : activities.size();
    }

    /**
     * Checks if the OS can handle the given intent.
     *
     * @param context Which context to use
     * @param intent  Which intent to check
     * @return {@code True} if intent is not {@code null} and resolves to at least one activity, {@code false} otherwise
     */
    public static boolean canHandleIntent(@NonNull final Context context, @Nullable final Intent intent) {
        return intent != null && countIntentHandlers(context, intent) > 0;
    }

    /**
     * Returns the root, content view of the given Activity.
     *
     * @param activity   Which activity to look at
     * @param <ViewType> Type of the View you are expecting here
     * @return A View instance, or {@code null} if there is no content view set
     */
    public static <ViewType extends View> ViewType getContentView(@NonNull final Activity activity) {
        // noinspection unchecked
        return (ViewType) activity.findViewById(android.R.id.content);
    }

    /**
     * Returns the root, content view of the given Dialog.
     *
     * @param dialog     Which dialog to look at
     * @param <ViewType> Type of the View you are expecting here
     * @return A View instance, or {@code null} if there is no content view set
     */
    public static <ViewType extends View> ViewType getContentView(@NonNull final Dialog dialog) {
        // noinspection unchecked
        return (ViewType) dialog.findViewById(android.R.id.content);
    }

    /**
     * Does exactly the same thing as calling {@link View#findViewById(int)}, but casts the result to the appropriate View sub-class.
     *
     * @param <ViewType> Which View type to cast the result to
     */
    @Nullable
    public static <ViewType extends View> ViewType findViewById(@NonNull final View container, @IdRes final int viewId) {
        // noinspection unchecked
        return (ViewType) container.findViewById(viewId);
    }

    /**
     * Does exactly the same thing as calling {@link Activity#findViewById(int)}, but casts the result to the appropriate View sub-class.
     *
     * @param <ViewType> Which View type to cast the result to
     */
    @Nullable
    public static <ViewType extends View> ViewType findViewById(@NonNull final Activity activity, @IdRes final int viewId) {
        // noinspection unchecked
        return (ViewType) activity.findViewById(viewId);
    }

    /**
     * Does exactly the same thing as calling {@link Fragment#getView()}.{@link #findViewById(View, int)}, but casts the result to the appropriate View
     * sub-class.
     *
     * @param <ViewType> Which View type to cast the result to
     */
    @Nullable
    public static <ViewType extends View> ViewType findViewById(@NonNull final Fragment fragment, @IdRes final int viewId) {
        // noinspection unchecked
        return fragment.getView() == null ? null : (ViewType) fragment.getView().findViewById(viewId);
    }

    /**
     * Does exactly the same thing as calling {@link Dialog#findViewById(int)}, but casts the result to the appropriate View sub-class.
     *
     * @param <ViewType> Which View type to cast the result to
     */
    @Nullable
    public static <ViewType extends View> ViewType findViewById(@NonNull final Dialog dialog, @IdRes final int viewId) {
        // noinspection unchecked
        return (ViewType) dialog.findViewById(viewId);
    }

    /**
     * Sets the padding to the given View.
     *
     * @param view   Which view to set the padding to, must not be {@code null}
     * @param start  Start padding ('left' for old devices)
     * @param top    Top padding
     * @param end    End padding ('right' for old devices)
     * @param bottom Bottom padding
     */
    public static void setPadding(@NonNull final View view, @Px final int start, @Px final int top, @Px final int end, @Px final int bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setPaddingRelative(start, top, end, bottom);
        } else {
            view.setPadding(start, top, end, bottom);
        }
    }

    /**
     * Sets the top and bottom padding to the given View, keeping the horizontal padding values as the were.
     *
     * @param view    Which view to set the padding to, must not be {@code null}
     * @param padding The vertical padding value
     */
    public static void setPaddingVertical(@NonNull final View view, @Px final int padding) {
        final int paddingStart = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? view.getPaddingStart() : view.getPaddingLeft();
        final int paddingEnd = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? view.getPaddingEnd() : view.getPaddingRight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setPaddingRelative(paddingStart, padding, paddingEnd, padding);
        } else {
            view.setPadding(paddingStart, padding, paddingEnd, padding);
        }
    }

    /**
     * Sets the left/start and right/end padding to the given View, keeping the vertical padding values as the were.
     *
     * @param view    Which view to set the padding to, must not be {@code null}
     * @param padding The horizontal padding value
     */
    public static void setPaddingHorizontal(@NonNull final View view, @Px final int padding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setPaddingRelative(padding, view.getPaddingTop(), padding, view.getPaddingBottom());
        } else {
            view.setPadding(padding, view.getPaddingTop(), padding, view.getPaddingBottom());
        }
    }

    /**
     * Sets the same padding for all sides to the given View.
     *
     * @param view    Which view to set the padding to, must not be {@code null}
     * @param padding The padding value
     */
    public static void setPadding(@NonNull final View view, @Px final int padding) {
        setPadding(view, padding, padding, padding, padding);
    }

    /**
     * Similar to {@link android.text.TextUtils#isEmpty(CharSequence)}, but also trims the String before checking. This means that checking
     * if {@code ' '} or {@code '\n'} are empty returns {@code true}.
     *
     * @param text Which String to test
     * @return {@code False} if the given text contains something other than whitespace, {@code true} otherwise
     */
    public static boolean isEmpty(@Nullable final String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Clamps the given number to the specified range. If number is smaller than {@code minValue}, this method returns the {@code minValue};
     * similar for the {@code maxValue}. If the number is in range, this method returns the original number.
     *
     * @param number   Which number to clamp
     * @param minValue The lowest value in the range
     * @param maxValue The highest value in the range
     * @return A clamped number, as described
     */
    public static int clamp(final int number, final int minValue, final int maxValue) {
        if (number < minValue) {
            return minValue;
        } else if (number > maxValue) {
            return maxValue;
        } else {
            return number;
        }
    }

    /**
     * Tries to dismiss the given {@link PopupMenu}.
     *
     * @param menu Which menu to dismiss
     * @return {@code True} if the given menu is not {@code null} and dismiss was invoked; {@code false} otherwise
     */
    public static boolean dismiss(@Nullable final PopupMenu menu) {
        if (menu != null) {
            menu.dismiss();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to dismiss the given {@link Dialog}.
     *
     * @param dialog Which dialog to dismiss
     * @return {@code True} if the given dialog is not {@code null}, it is currently showing and dismiss was invoked; {@code false}
     * otherwise
     */
    public static boolean dismiss(@Nullable final Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to close the given {@link Closeable} object without crashing.
     *
     * @param closeable Which closeable to close
     * @return {@code True} if the given closeable is not {@code null}, and close was invoked successfully; {@code false} otherwise
     */
    public static boolean close(@Nullable final Closeable closeable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && closeable instanceof Cursor) {
            return close((Cursor) closeable);
        }

        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (Throwable closeError) {
                Log.e(closeable.getClass().getSimpleName(), "Failed to close resource", closeError);
            }
        }
        return false;
    }

    /**
     * Tries to close the given {@link Cursor} object without crashing.
     *
     * @param cursor Which cursor to close
     * @return {@code True} if the given cursor is not {@code null}, not closed, and close was invoked successfully; {@code false} otherwise
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean close(@Nullable final Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            try {
                cursor.close();
                return true;
            } catch (Throwable closeError) {
                Log.e(cursor.getClass().getSimpleName(), "Failed to close resource", closeError);
            }
        }
        return false;
    }

    /**
     * Checks if given raw resource is empty or not.
     *
     * @param context       Which context to use for checking
     * @param rawResourceId The raw resource identifier
     * @return {@code True} if resource is empty, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public static boolean isRawResourceEmpty(@NonNull final Context context, @RawRes final int rawResourceId) {
        InputStream inputStream = null;
        try {
            inputStream = openRawResource(context, rawResourceId);
            return inputStream.available() <= 0;
        } catch (Exception e) {
            Log.e(TAG, "isRawResourceEmpty: FAILED!", e);
            return false;
        } finally {
            close(inputStream);
        }
    }

    /**
     * Reads the raw resource as plain text from the given resource ID.
     *
     * @param context       Which context to use for reading
     * @param rawResourceId The raw resource identifier
     * @return A text representation of the raw resource, never {@code null}
     */
    @NonNull
    @SuppressWarnings("unused")
    public static String readRawResource(@NonNull final Context context, @RawRes final int rawResourceId) {
        InputStream inputStream = null;
        try {
            inputStream = openRawResource(context, rawResourceId);
            byte[] b = new byte[inputStream.available()];
            // noinspection ResultOfMethodCallIgnored - don't care about number of bytes read
            inputStream.read(b);
            return new String(b);
        } catch (Exception e) {
            Log.e(TAG, "readRawResource: FAILED!", e);
            return "";
        } finally {
            close(inputStream);
        }
    }

    /**
     * Opens the input stream to the given raw resource.
     * <b>Note</b>: You need to close the stream manually.
     *
     * @param context       Which context to use for opening
     * @param rawResourceId The raw resource identifier
     * @return A new, open input stream to the requested raw resource
     */
    @NonNull
    public static InputStream openRawResource(@NonNull final Context context, @RawRes final int rawResourceId) {
        return context.getResources().openRawResource(rawResourceId);
    }

    /**
     * Converts a {@link Drawable} into a {@link Bitmap}. Includes an optimization in case the {@link Drawable} in question is already a
     * {@link BitmapDrawable}.
     *
     * @param drawable A Drawable instance to convert
     * @param width    The width of the new Bitmap
     * @param height   The height of the new Bitmap
     * @return A new {@link Bitmap} instance constraint to width and height dimensions supplied, never {@code null}
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Bitmap drawableToBitmap(@NonNull final Drawable drawable, @Px final int width, @Px final int height) {
        final Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return result;
    }

    /**
     * Compares the two {@code null}able objects. For {@link Build.VERSION_CODES#KITKAT} and newer APIs this gets the result from
     * {@link Objects#equals(Object, Object)}; for older APIs this does the same comparison as {@link Objects#equals(Object, Object)}.
     *
     * @param first  The first object to compare, can be {@code null}
     * @param second The second object to compare, can be {@code null}
     * @return {@code True} if objects are equal using {@link Object#equals(Object)}, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public static boolean equal(@Nullable final Object first, @Nullable final Object second) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(first, second);
        } else {
            return (first == second) || (first != null && first.equals(second));
        }
    }

    /**
     * Similarly to {@link java.util.Objects#equals(Object, Object)}, this compares two {@link Drawable}s' constant states.
     *
     * @param a Drawable one
     * @param b Drawable two
     * @return {@code True} if their constant states are equal, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public static boolean drawableEquals(@Nullable final Drawable a, @Nullable final Drawable b) {
        return (a == b) || ((a != null) && (b != null) && (a.getConstantState() != null) && a.getConstantState().equals(b.getConstantState()));
    }

    /**
     * Returns the number of pixels corresponding to the given number of device independent pixels in the given context.
     *
     * @param dips Number of dips to convert
     * @return Resulting number of pixels in the given context
     */
    @Px
    @SuppressWarnings("unused")
    public static int convertDipsToPixels(@NonNull final Context context, final int dips) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics()));
    }

    /**
     * Checks if WiFi is enabled on the device.
     * <b>Note</b>: This does not check if WiFi is connected to a network.
     *
     * @param context Which context to use to check
     * @return {@code True} if WiFi is enabled, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public static boolean isWifiEnabled(@NonNull final Context context) {
        final WifiManager wiFiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wiFiManager != null && wiFiManager.isWifiEnabled();
    }

    /**
     * Checks if WiFi is enabled and connected to a network.
     * <b>Note</b>: This does not check access to the Internet.
     *
     * @param context Which context to use to check
     * @return {@code True} if WiFi is enabled and connected to a network, {@code false} otherwise
     */
    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    public static boolean isWifiConnected(@NonNull final Context context) {
        final WifiManager wiFiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wiFiManager != null && wiFiManager.isWifiEnabled()) {
            // Wi-Fi adapter is ON
            final WifiInfo wifiInfo = wiFiManager.getConnectionInfo();
            return wifiInfo != null && wifiInfo.getNetworkId() != -1;
        } else {
            // Wi-Fi adapter is OFF
            return false;
        }
    }

    /**
     * Checks if device is connected to other, non-WiFi networks.
     * <b>Note</b>: This does not check access to the Internet.
     *
     * @param context Which context to use to check
     * @return {@code True} if there is a non-WiFi network connected, {@code false} if not
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE})
    public static boolean isNonWifiNetworkConnected(@NonNull final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean hasOtherNetwork = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            hasOtherNetwork = true;
        }
        return hasOtherNetwork && !isWifiConnected(context);
    }

    /**
     * Checks if device is connected to any network.
     * <b>Note</b>: This does not check access to the Internet.
     *
     * @param context Which context to use to check
     * @return {@code True} if there is any network connected, {@code false} if not
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE})
    public static boolean isNetworkConnected(@NonNull final Context context) {
        return isWifiConnected(context) || isNonWifiNetworkConnected(context);
    }

    /**
     * Checks if this call is executed on the app's main (UI) thread.
     *
     * @return {@code True} if execution is currently on the main thread, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public static boolean isThisMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * Checks whether the app is currently on the system white-list, i.e. if the OS would allow execution even when in Doze mode.
     * Note that it makes sense to check this only on API 23 (Android 6.0) because the battery optimization API is not available in previous versions.
     * For all pre-Marshmallow APIs, this method will return {@code true}.
     *
     * @param context Which context to use to check
     * @return The value of {@link PowerManager#isIgnoringBatteryOptimizations(String)}
     */
    @SuppressWarnings("unused")
    public static boolean checkDozeModeWhiteList(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
    }

    /**
     * Checks if voice recognition service is present in the device.
     *
     * @param context Which context to use to check
     * @return {@code True} if there is a voice recognition service in the device, {@code false} otherwise
     */
    public static boolean isVoiceInputAvailable(@NonNull final Context context) {
        return canHandleIntent(context, new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    /**
     * Shows a {@link android.widget.Toast} message with its length set to {@link android.widget.Toast#LENGTH_SHORT}.
     *
     * @param context  Which context to use
     * @param stringId The message to show
     */
    public static void toastShort(@NonNull final Context context, @StringRes final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a {@link android.widget.Toast} message with its length set to {@link android.widget.Toast#LENGTH_SHORT}.
     *
     * @param context Which context to use
     * @param string  The message to show
     */
    public static void toastShort(@NonNull final Context context, @NonNull final String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a {@link android.widget.Toast} message with its length set to {@link android.widget.Toast#LENGTH_LONG}.
     *
     * @param context  Which context to use
     * @param stringId The message to show
     */
    public static void toastLong(@NonNull final Context context, @StringRes final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a {@link android.widget.Toast} message with its length set to {@link android.widget.Toast#LENGTH_LONG}.
     *
     * @param context Which context to use
     * @param string  The message to show
     */
    public static void toastLong(@NonNull final Context context, @NonNull final String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    /**
     * Tries to hide the software keyboard using the {@link InputMethodManager}.
     *
     * @return {@code True} if keyboard was properly hidden, {@code false} if something went wrong
     */
    public static boolean hideKeyboard(@NonNull final Activity context) {
        try {
            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            final View layout = SillyAndroid.getContentView(context);
            if (layout == null) {
                return false;
            }
            imm.hideSoftInputFromWindow(layout.getApplicationWindowToken(), 0);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Hiding keyboard failed", e);
            return false;
        }
    }

    /**
     * Creates a new {@link android.view.ViewTreeObserver.OnGlobalLayoutListener} that attempts to calculate the keyboard size.
     * Note that this does not guarantee that a software keyboard is present, it's just a best effort detection attempt.
     * Also note that, although it's not guaranteed to work with all keyboards in all situations, this was tested against top 20 keyboards from the store and
     * had no issues detecting the changes.
     *
     * @param with    What to subscribe with (cannot be {@code null})
     * @param context The Activity context to use (cannot be {@code null})
     * @return A new instance of the layout listener. Note that this won't do anything until you attach the returned instance to the global layout by invoking
     * {@link View#getViewTreeObserver()} and then {@link ViewTreeObserver#addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener)} on the
     * Activity's root layout, passing in the listener instance returned from this method. You also need to manually detach the listener when done listening.
     * The best practice is to do it with the {@link Activity#onStart()} and {@link Activity#onStop()} lifecycle methods.
     */
    @NonNull
    public static ViewTreeObserver.OnGlobalLayoutListener listenToKeyboard(@NonNull final OnKeyboardChangeListener with, @NonNull final Activity context) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isKeyboardVisible;

            @Override
            public void onGlobalLayout() {
                final int statusBarHeight = UI.getStatusBarHeight(context);
                final int navigationBarHeight = UI.getNavigationBarHeight(context);

                // check the display window size for the app layout
                final Rect rect = new Rect();
                context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                // screen height - (user app height + status + nav) -> if non-zero, then there is a soft keyboard
                final ViewGroup layout = SillyAndroid.getContentView(context);
                if (layout == null) {
                    throw new IllegalArgumentException("Passed Activity needs to have its content view set before attaching this listener");
                }
                final int keyboardHeight = layout.getRootView().getHeight() - (statusBarHeight + navigationBarHeight + rect.height());

                if (keyboardHeight <= 0) {
                    if (isKeyboardVisible) {
                        with.onKeyboardHidden();
                        isKeyboardVisible = false;
                    }
                } else {
                    if (!isKeyboardVisible) {
                        with.onKeyboardShown(keyboardHeight);
                        isKeyboardVisible = true;
                    }
                }
            }
        };
    }

}
