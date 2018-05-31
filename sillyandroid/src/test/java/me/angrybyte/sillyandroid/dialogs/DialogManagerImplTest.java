package me.angrybyte.sillyandroid.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import me.angrybyte.sillyandroid.dialogs.DialogManager.DialogManagerCallback;
import me.angrybyte.sillyandroid.dialogs.DialogManager.DialogManagerListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DISCLAIMER: While the best practice may be to have only one 'assert' per test,
 * it's cumbersome to write tests like this and I'm kind of lazy, so.. multiple
 * asserts per test, you're welcome!
 */
public class DialogManagerImplTest {

    private static final int KNOWN_DIALOG = 0xD14_06; // "DIALOG" in numbers :O
    private static final int ANOTHER_DIALOG = 0xAD14_06; // "A DIALOG" in numbers :O
    private static final int INVALID_DIALOG = -1;

    private DialogManager mDialogManager;

    // <editor-fold desc="Setup and Teardown">
    @Before
    public void setUp() {
        final FragmentManager manager = mock(FragmentManager.class);
        mDialogManager = new DialogManagerImpl(manager);
    }

    @After
    public void tearDown() {
        // no real need but it feels weird to have a setUp() only
        mDialogManager = null;
    }
    // </editor-fold>

    // <editor-fold desc="Callback & Listener (getters/setters)">
    @Test
    public void setCallback_getCallback() {
        assertNull(mDialogManager.getCallback());
        final DialogManagerCallback callback = createCallbackMock();
        mDialogManager.setCallback(callback);
        assertEquals(callback, mDialogManager.getCallback());
        mDialogManager.setCallback(null);
        assertNull(mDialogManager.getCallback());
    }

    @Test
    public void setListener_getListener() {
        assertNull(mDialogManager.getListener());
        final DialogManagerListener listener = createListenerMock();
        mDialogManager.setListener(listener);
        assertEquals(listener, mDialogManager.getListener());
        mDialogManager.setListener(null);
        assertNull(mDialogManager.getListener());
    }
    // </editor-fold>

    // <editor-fold desc="Showing dialogs">
    @Test
    public void showDialog_noCallback() {
        mDialogManager.showDialog(KNOWN_DIALOG);
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }

    @Test
    public void showDialog_callbackGivesNull() {
        mDialogManager.setCallback(createCallbackMock());
        mDialogManager.showDialog(KNOWN_DIALOG);
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }

    @Test
    public void showDialog_invalidId() {
        final DialogManagerCallback callback = createCallbackMock();
        // just return anything non-null for a known dialog (we're targeting the unknown ID here)
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), any())).thenReturn(mock(Dialog.class));
        mDialogManager.setCallback(callback);
        mDialogManager.showDialog(INVALID_DIALOG);
        assertFalse(mDialogManager.isDialogShowing(INVALID_DIALOG));
    }

    @Test
    public void showDialog_happy_noBundle() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog dialogMock = createDialogMock();
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isNull())).thenReturn(dialogMock);
        mDialogManager.setCallback(callback);
        mDialogManager.showDialog(KNOWN_DIALOG);
        // verify callback was invoked
        verify(dialogMock).setOnShowListener(isNotNull());
        verify(dialogMock).setOnDismissListener(isNotNull());
        // finally verify dialog was shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }

    @Test
    public void showDialog_happy_withBundle() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog dialogMock = createDialogMock();
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isA(Bundle.class))).thenReturn(dialogMock);
        mDialogManager.setCallback(callback);
        mDialogManager.showDialog(KNOWN_DIALOG, new Bundle());
        // verify callback was invoked
        verify(dialogMock).setOnShowListener(isNotNull());
        verify(dialogMock).setOnDismissListener(isNotNull());
        // finally verify dialog was shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }
    // </editor-fold>

    // <editor-fold desc="Showing dialog fragments"> @formatter:off
    @Test public void showDialogFragment_noCallback() { /* TODO ideas on how to do this cleanly? */ }
    @Test public void showDialogFragment_callbackGivesNull() { /* TODO ideas on how to do this cleanly? */ }
    @Test public void showDialogFragment_invalidId() { /* TODO ideas on how to do this cleanly? */ }
    @Test public void showDialogFragment_happy_noBundle() { /* TODO ideas on how to do this cleanly? */ }
    @Test public void showDialogFragment_happy_withBundle() { /* TODO ideas on how to do this cleanly? */ }
    // </editor-fold>  @formatter:on

    // <editor-fold desc="Dialog visibility">
    @Test
    public void isDialogShowing() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog dialogMock = createDialogMock();
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isNull())).thenReturn(dialogMock);
        mDialogManager.setCallback(callback);
        mDialogManager.showDialog(KNOWN_DIALOG);
        // check if dialog was shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        dialogMock.hide();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }

    @Test
    public void dismissDialog() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog dialogMock = createDialogMock();
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isNull())).thenReturn(dialogMock);
        mDialogManager.setCallback(callback);
        mDialogManager.showDialog(KNOWN_DIALOG);
        // check if dialog was shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        dialogMock.dismiss();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
    }

    @Test
    public void hideAll_unhideAll_dismissAll() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog[] dialogMocks = new Dialog[]{createDialogMock(), createDialogMock()};
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isNull())).thenReturn(dialogMocks[0]);
        when(callback.onCreateDialog(eq(ANOTHER_DIALOG), isNull())).thenReturn(dialogMocks[0]);
        mDialogManager.setCallback(callback);
        // show both dialogs
        mDialogManager.showDialog(KNOWN_DIALOG);
        mDialogManager.showDialog(ANOTHER_DIALOG);
        // check if dialogs were shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertTrue(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // now hide them
        mDialogManager.hideAll();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertFalse(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // unhide them
        mDialogManager.unhideAll();
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertTrue(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // and finally dismiss them
        mDialogManager.dismissAll();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertFalse(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
    }
    // </editor-fold>

    // <editor-fold desc="State saving & restoring">
    @Test
    public void saveState_dialogsOnly() { }

    @Test
    public void restoreState_dialogsOnly() { }
    // </editor-fold>

    // <editor-fold desc="Memory management">
    @Test
    public void recreateAll() {
        // setup the callback
        final DialogManagerCallback callback = createCallbackMock();
        final Dialog[] dialogMocks = new Dialog[]{createDialogMock(), createDialogMock()};
        when(callback.onCreateDialog(eq(KNOWN_DIALOG), isNull())).thenReturn(dialogMocks[0]);
        when(callback.onCreateDialog(eq(ANOTHER_DIALOG), isNull())).thenReturn(dialogMocks[0]);
        mDialogManager.setCallback(callback);
        // show both dialogs
        mDialogManager.showDialog(KNOWN_DIALOG);
        mDialogManager.showDialog(ANOTHER_DIALOG);
        // check if dialogs were shown
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertTrue(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // now hide them to set states to 'hidden'
        mDialogManager.hideAll();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertFalse(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // recreate them (this will reset the 'hidden' state to false)
        mDialogManager.recreateAll(true);
        assertTrue(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertTrue(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
        // and finally dismiss them
        mDialogManager.dismissAll();
        assertFalse(mDialogManager.isDialogShowing(KNOWN_DIALOG));
        assertFalse(mDialogManager.isDialogShowing(ANOTHER_DIALOG));
    }

    @Test
    public void dispose() {
        mDialogManager.setCallback(createCallbackMock());
        mDialogManager.setListener(createListenerMock());
        mDialogManager.dispose();
        assertNull(mDialogManager.getCallback());
        assertNull(mDialogManager.getListener());
    }
    // </editor-fold>

    /* Private helpers */

    @NonNull
    private DialogManagerListener createListenerMock() {
        return mock(DialogManagerListener.class);
    }

    @NonNull
    private DialogManagerCallback createCallbackMock() {
        return mock(DialogManagerCallback.class);
    }

    @NonNull
    private Dialog createDialogMock() {
        final Dialog dialogMock = mock(Dialog.class);
        when(dialogMock.getContext()).thenReturn(mock(Context.class));

        final AtomicBoolean showing = new AtomicBoolean(false);
        // mock showing
        doAnswer(invocation -> {
            showing.set(true);
            return null;
        }).when(dialogMock).show();
        // mock hiding
        doAnswer(invocation -> {
            showing.set(false);
            return null;
        }).when(dialogMock).hide();
        // mock dismissal
        doAnswer(invocation -> {
            showing.set(false);
            return null;
        }).when(dialogMock).dismiss();
        // mock the 'isShowing' check
        when(dialogMock.isShowing()).thenAnswer(invocation -> showing.get());

        return dialogMock;
    }

}