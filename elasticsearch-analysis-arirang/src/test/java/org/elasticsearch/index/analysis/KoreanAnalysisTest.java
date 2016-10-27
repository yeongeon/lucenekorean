package org.elasticsearch.index.analysis;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import org.apache.lucene.util.TestUtil;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

import static org.apache.lucene.util.LuceneTestCase.createTempDir;
import static org.elasticsearch.common.settings.Settings.Builder.EMPTY_SETTINGS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class KoreanAnalysisTest {

    private static Random random;

    @BeforeClass
    public static void beforeClass(){
        random = random();
    }

    @AfterClass
    public static void afterClass(){
        // --
    }

    public static Random random() {
        return RandomizedContext.current().getRandom();
    }

    public Environment newEnvironment() throws IOException {
        return newEnvironment(Settings.EMPTY);
    }

    public Environment newEnvironment(Settings settings) throws IOException {
        Settings build = Settings.builder()
                .put(settings)
                .put(Environment.PATH_HOME_SETTING.getKey(), KoreanAnalysisTest.class.getResource("/").getPath())
                .putArray(Environment.PATH_DATA_SETTING.getKey(), tmpPaths()).build();
        return new Environment(build);
    }


    public String[] tmpPaths() {
        final int numPaths = TestUtil.nextInt(random, 1, 3);
        final String[] absPaths = {KoreanAnalysisTest.class.getResource("/").getPath()};
//        for (int i = 0; i < numPaths; i++) {
//            absPaths[i] = createTempDir().toAbsolutePath().toString();
//        }
//        absPaths[0] = KoreanAnalysisTest.class.getResource("/").getPath();
        return absPaths;
    }

    @Test
    public void testRepositoryResolution() throws IOException {
        Environment environment = newEnvironment();
        assertThat(environment.resolveRepoFile("/test/repos/repo1"), nullValue());
        assertThat(environment.resolveRepoFile("test/repos/repo1"), nullValue());
        environment = newEnvironment(Settings.builder().putArray(Environment.PATH_REPO_SETTING.getKey(), "/test/repos", "/another/repos", "/test/repos/../other").build());
        assertThat(environment.resolveRepoFile("/test/repos/repo1"), notNullValue());
        assertThat(environment.resolveRepoFile("test/repos/repo1"), notNullValue());
        assertThat(environment.resolveRepoFile("/another/repos/repo1"), notNullValue());
        assertThat(environment.resolveRepoFile("/test/repos/../repo1"), nullValue());
        assertThat(environment.resolveRepoFile("/test/repos/../repos/repo1"), notNullValue());
        assertThat(environment.resolveRepoFile("/somethingeles/repos/repo1"), nullValue());
        assertThat(environment.resolveRepoFile("/test/other/repo"), notNullValue());


        assertThat(environment.resolveRepoURL(new URL("file:///test/repos/repo1")), notNullValue());
        assertThat(environment.resolveRepoURL(new URL("file:/test/repos/repo1")), notNullValue());
        assertThat(environment.resolveRepoURL(new URL("file://test/repos/repo1")), nullValue());
        assertThat(environment.resolveRepoURL(new URL("file:///test/repos/../repo1")), nullValue());
        assertThat(environment.resolveRepoURL(new URL("http://localhost/test/")), nullValue());

        assertThat(environment.resolveRepoURL(new URL("jar:file:///test/repos/repo1!/repo/")), notNullValue());
        assertThat(environment.resolveRepoURL(new URL("jar:file:/test/repos/repo1!/repo/")), notNullValue());
        assertThat(environment.resolveRepoURL(new URL("jar:file:///test/repos/repo1!/repo/")).toString(), endsWith("repo1!/repo/"));
        assertThat(environment.resolveRepoURL(new URL("jar:file:///test/repos/../repo1!/repo/")), nullValue());
        assertThat(environment.resolveRepoURL(new URL("jar:http://localhost/test/../repo1?blah!/repo/")), nullValue());
    }

}
