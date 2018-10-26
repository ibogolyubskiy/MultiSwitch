package com.multiswitch.app;

import com.multiswitch.MultiSwitch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(RobolectricTestRunner.class)
public class ApplicationTest {

    private MultiSwitch multiswitch;

    @Before
    public void setUp() {
        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
        multiswitch = activity.getSwitchMultiButton();
    }

    @Test
    public void testGetSelectedButtons() {
        Integer in[] = new Integer[]{0, 2};
        StringBuilder sb = new StringBuilder();
        sb.append("in: ");

        for (Integer i : in) {
            multiswitch.setTabSelected(i);
            sb.append(i).append(" ");
        }

        sb.append("out: ");

        Integer out[] = multiswitch.getSelectedTabs();

        for (Integer i : out) {
            sb.append(i).append(" ");
        }

        assertArrayEquals(sb.toString(), in, out);
    }

    @Test
    public void testSelectAndUnselectButtons() {
        Integer in[] = new Integer[]{0, 1, 2, 3};

        for (Integer i : in) {
            multiswitch.setTabSelected(i);
        }

        Integer out[] = multiswitch.getSelectedTabs();

        assertArrayEquals(in, out);

        multiswitch.clearSelected();

        assertArrayEquals(multiswitch.getSelectedTabs(), new Integer[]{});

        for (Integer i : in) {
            if (i % 2 == 1)
                multiswitch.setTabSelected(i);
        }

        assertArrayEquals(multiswitch.getSelectedTabs(), new Integer[]{1, 3});
    }

    @Test
    public void testGetButtonStatus() {
        multiswitch.clearSelected();

        Integer in[] = new Integer[]{0, 3};
        for (Integer i : in) {
            multiswitch.setTabSelected(i);
        }
        assertTrue(multiswitch.getState(0));
        assertTrue(multiswitch.getState(3));
        assertFalse(multiswitch.getState(1));
        assertFalse(multiswitch.getState(2));

        multiswitch.clearSelected();

        assertFalse(multiswitch.getState(0));
        assertFalse(multiswitch.getState(3));

        multiswitch.setTabSelected(1);
        multiswitch.setTabSelected(2);

        assertTrue(multiswitch.getState(1));
        assertTrue(multiswitch.getState(2));
    }
}