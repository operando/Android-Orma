package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class IndexDiffTest {

    SchemaDiffMigration migration;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        migration = new SchemaDiffMigration(getContext());
    }


    @Test
    public void buildDropIndexStatement() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_caseInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create index if not exists index_foo on foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_spaceInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create \n"
                        + "index \n"
                        + "if \n"
                        + "not \n"
                        + "exists \n"
                        + "index_foo \n"
                        + "on \n"
                        + "foo \n"
                        + "(bar)\n"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_omitIfNotExists() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_doubleQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS \"index_foo\" ON \"foo\" (\"bar\")"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

    @Test
    public void buildDropIndexStatement_backQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS `index_foo` ON `foo` (`bar`)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }

}
