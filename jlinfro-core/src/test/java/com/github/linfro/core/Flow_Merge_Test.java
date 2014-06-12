package com.github.linfro.core;

import com.github.linfro.core.common.Disposable;
import com.github.linfro.core.value.TestListener;
import org.junit.Test;

import java.util.Map;

import static com.github.linfro.core.common.ObjectUtil.notNull;
import static com.github.linfro.core.common.ObjectUtil.nvl;
import static com.github.linfro.core.value.TestUtil.assertDisposed;
import static org.junit.Assert.*;

/**
 * @author Dmitry Ermakov
 * @version 2014-06-12
 * @since 1.0.0
 */
public class Flow_Merge_Test {
    @Test
    public void testExampleMerge() throws Exception {
        HasValue<String> a = Values.newHasValue();
        HasValue<Integer> b = Values.newHasValue();
        HasValue<Long> c = Values.newHasValue();

        HasValue<String> res = Values.newHasValue();

        TestListener listener = new TestListener();
        res.addChangeListener(listener);

        Disposable link = a.merge(b, c).map(map -> {
            String aVal = nvl((String) map.get("arg0"), "null");
            Integer bVal = nvl((Integer) map.get("arg1"));
            Long cVal = nvl((Long) map.get("arg2"));

            return aVal + "_" + bVal + "_" + cVal;
        }).flow().force().to(res);

        assertEquals("null_0_0", res.getValue());
        assertEquals(1, listener.getCounter());

        a.setValue("str");
        assertEquals("str_0_0", res.getValue());
        assertEquals(2, listener.getCounter());

        b.setValue(12);
        assertEquals("str_12_0", res.getValue());
        assertEquals(3, listener.getCounter());

        c.setValue(356L);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        link.dispose();

        a.setValue("disposed");
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        b.setValue(-978);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        c.setValue(3L);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());
    }

    @Test
    public void testExampleNamedMerge() throws Exception {
        HasValue<String> a = Values.newHasValue();
        HasValue<Integer> b = Values.newHasValue();
        HasValue<Long> c = Values.newHasValue();

        HasValue<String> res = Values.newHasValue();

        TestListener listener = new TestListener();
        res.addChangeListener(listener);

        Disposable link = a.named("a").merge(b.named("b"), c.named("c")).map(map -> {
            String aVal = nvl((String) map.get("a"), "null");
            Integer bVal = nvl((Integer) map.get("b"));
            Long cVal = nvl((Long) map.get("c"));

            return aVal + "_" + bVal + "_" + cVal;
        }).flow().force().to(res);

        assertEquals("null_0_0", res.getValue());
        assertEquals(1, listener.getCounter());

        a.setValue("str");
        assertEquals("str_0_0", res.getValue());
        assertEquals(2, listener.getCounter());

        b.setValue(12);
        assertEquals("str_12_0", res.getValue());
        assertEquals(3, listener.getCounter());

        c.setValue(356L);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        link.dispose();

        a.setValue("disposed");
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        b.setValue(-978);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());

        c.setValue(3L);
        assertEquals("str_12_356", res.getValue());
        assertEquals(4, listener.getCounter());
    }

    @Test
    public void testDuplicate1() throws Exception {
        HasValue<String> a = Values.newHasValue();
        HasValue<String> b = Values.newHasValue();

        try {
            a.named("test").merge(b.named("test"));
            fail("Merge must throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument key duplicate found: test", e.getMessage());
        }
    }

    @Test
    public void testDuplicate2() throws Exception {
        HasValue<String> a = Values.newHasValue();
        HasValue<String> b = Values.newHasValue();
        HasValue<String> c = Values.newHasValue();

        try {
            a.merge(b.named("test"), c.named("test"));
            fail("Merge must throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument key duplicate found: test", e.getMessage());
        }
    }

    @Test
    public void testDuplicate3() throws Exception {
        HasValue<String> a = Values.newHasValue();
        HasValue<String> b = Values.newHasValue();
        HasValue<String> c = Values.newHasValue();
        HasValue<String> d = Values.newHasValue();

        try {
            a.merge(b.named("test"), c, d.named("test"));
            fail("Merge must throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument key duplicate found: test", e.getMessage());
        }
    }

    @Test
    public void testMetaInfo() throws Exception {
        HasValue<String> a = Values.<String>newHasValue().named("a");
        HasValue<Integer> b = Values.<Integer>newHasValue().named("b");
        HasValue<Long> c = Values.<Long>newHasValue().named("c");

        GetValue<Map<String, Object>> merge = a.merge(b, c);

        assertNull(a.getValue());
        assertNull(b.getValue());
        assertNull(c.getValue());
        forKeys("a", "b", "c").assertMap(merge.getValue(), null, null, null);
        assertEquals("a", merge.getMetaName());

        a.setValue("tst");

        assertEquals("tst", a.getValue());
        assertNull(b.getValue());
        assertNull(c.getValue());
        forKeys("a", "b", "c").assertMap(merge.getValue(), "tst", null, null);
        assertEquals("a", merge.getMetaName());

        b.setValue(12);

        assertEquals("tst", a.getValue());
        assertEquals(new Integer(12), b.getValue());
        assertNull(c.getValue());
        forKeys("a", "b", "c").assertMap(merge.getValue(), "tst", 12, null);
        assertEquals("b", merge.getMetaName());

        c.setValue(512L);

        assertEquals("tst", a.getValue());
        assertEquals(new Integer(12), b.getValue());
        assertEquals(new Long(512L), c.getValue());
        forKeys("a", "b", "c").assertMap(merge.getValue(), "tst", 12, 512L);
        assertEquals("c", merge.getMetaName());

        a.setValue("ddd");

        assertEquals("ddd", a.getValue());
        assertEquals(new Integer(12), b.getValue());
        assertEquals(new Long(512L), c.getValue());
        forKeys("a", "b", "c").assertMap(merge.getValue(), "ddd", 12, 512L);
        assertEquals("a", merge.getMetaName());

        //****** dispose **************************

        assertFalse(a.canDispose());
        assertFalse(b.canDispose());
        assertFalse(c.canDispose());
        assertTrue(merge.canDispose());

        merge.dispose();

        assertDisposed(merge::getValue);
        assertDisposed(merge::getMetaName);
        assertDisposed(merge::isValueValid);

        assertDisposed(a::getValue);
        assertDisposed(a::getMetaName);
        assertDisposed(a::isValueValid);

        assertDisposed(b::getValue);
        assertDisposed(b::getMetaName);
        assertDisposed(b::isValueValid);

        assertDisposed(c::getValue);
        assertDisposed(c::getMetaName);
        assertDisposed(c::isValueValid);
    }

    private static MapHelper forKeys(String... keys) {
        return new MapHelper(keys);
    }

    private static class MapHelper {
        private final String[] keys;

        private MapHelper(String[] keys) {
            this.keys = notNull(keys);
            assertTrue(this.keys.length > 0);
        }

        public void assertMap(Map<String, Object> map, Object... values) {
            assertNotNull(map);
            assertNotNull(values);
            assertTrue(values.length > 0);
            assertEquals(keys.length, values.length);
            assertEquals(keys.length, map.size());

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                assertTrue(map.containsKey(key));
                assertEquals(values[i], map.get(key));
            }
        }
    }
}