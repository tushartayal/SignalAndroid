package org.thoughtcrime.securesms.nickname;

import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.thoughtcrime.securesms.NicknameHandler;
import org.thoughtcrime.securesms.NicknameMocks;
import org.thoughtcrime.securesms.database.DatabaseFactory;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NicknameHandlerTests extends NicknameMocks {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupRecipientObject();
        super.setupStaticRecipientDatabase();
        super.setupSetNicknameMethod();
    }

    @Test
    public void testSetNicknameNewNickname() {
        super.setupSetNicknameMethod();

        NicknameHandler handler = new NicknameHandler(super.context);
        boolean res = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "test");

        assertTrue(res);
        verify(recipientDatabase).setNickname(recipient,"test");
    }

    @Test
    public void testSetNicknameSameOldName() {
        super.setupSetNicknameMethod();

        NicknameHandler handler = new NicknameHandler(super.context);
        boolean resNewNickname = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "test");

        boolean resSameNickname = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "test");

        assertTrue(resNewNickname);
        assertTrue(resSameNickname);
        verify(recipientDatabase, times(2)).setNickname(recipient,"test");
    }

    @Test
    public void testSetNicknameSameOldAndThenNew() {
        super.setupSetNicknameMethod();

        NicknameHandler handler = new NicknameHandler(super.context);
        boolean resNewNickname = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "test");

        boolean resSameNickname = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "test");

        boolean resNewNicknameNewInput = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "new test");

        boolean resNewNicknameSameNewInput = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .setNickname(recipient, "new test");

        assertTrue(resNewNickname);
        assertTrue(resSameNickname);

        assertTrue(resNewNicknameNewInput);
        assertTrue(resNewNicknameSameNewInput);

        verify(recipientDatabase, times(2)).setNickname(recipient,"test");
        verify(recipientDatabase, times(2)).setNickname(recipient,"new test");
    }

    @Test
    public void removeNickname() {
        super.setupSetNicknameMethod();

        NicknameHandler handler = new NicknameHandler(super.context);
        boolean resRemoveNickname = handler.setRecipientDatabase(DatabaseFactory.getRecipientDatabase(super.context))
                .removeNickname(recipient);

        assertTrue(resRemoveNickname);
        verify(recipientDatabase, times(1)).setNickname(recipient, null);


    }
}
