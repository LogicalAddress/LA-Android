package ng.com.nhub.paygis.etc;

import junit.framework.TestCase;

import de.greenrobot.event.EventBus;

/**
 * Created by retnan on 1/3/16.
 */
public class ServerMessageEventTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        EventBus.getDefault().register(this);
        ServerMessageEvent serverMessageEvent = new ServerMessageEvent("Hello");
        EventBus.getDefault().post(serverMessageEvent);
    }

    public void onEvent(ServerMessageEvent event){
        assertEquals("hello", event.message);
    }

    public void tearDown() throws Exception {
        EventBus.getDefault().unregister(this);
    }
}