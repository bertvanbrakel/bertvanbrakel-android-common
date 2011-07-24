package com.bertvanbrakel.android.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.bertvanbrakel.android.lang.Logger;

public class LoggerTest {

    /**
     * Ensure log names don't get too long
     */
    @Test
    public void test_loggername_truncation(){
        assertEquals( "", new Logger("").getLogName());
        assertEquals( "a", new Logger("a").getLogName());
        assertEquals( "a.b.c", new Logger("a.b.c").getLogName());

        assertEquals( "somepackage.SomeClass", new Logger("com.acme.someproject.somepackage.SomeClass").getLogName());
        assertEquals( "acme.foo.pkg.SomeClass", new Logger("com.acme.foo.pkg.SomeClass").getLogName());

        assertEquals( "LongNameWithNotDotsInIt", new Logger("SomeReallyLongNameWithNotDotsInIt").getLogName());
    }
}
