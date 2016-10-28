package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.ko.AnalysisJasoPlugin;
import org.junit.Test;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class JasoAnalyzerTest {

    final String query = "2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.";

    @Test
    public void testJasoAnalyzerNamedAnalyzer() throws Exception {
        System.out.println("####### testJasoAnalyzerNamedAnalyzer #######");

        // http://localhost:9200/test/_analyze?analyzer=jaso&text=2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.&pretty=1
        Index index = new Index("test");
        Settings settings = settingsBuilder()
                .put("path.home", JasoAnalyzerTest.class.getResource("/").getPath()+"tmp")
                .put("index.analysis.analyzer.jaso.type", "jaso_analyzer")
                .put("index.analysis.analyzer.jaso.tokenizer", "jaso_tokenizer")
                .putArray("index.analysis.analyzer.jaso.filter", "jaso_filter", "lowercase")
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisJasoPlugin().onModule(analysisModule);

        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);
        NamedAnalyzer namedAnalyzer = analysisService.analyzer("jaso");

        TokenStream tokenStream = namedAnalyzer.tokenStream(null, query);

        CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttr = tokenStream.addAttribute(TypeAttribute.class);

        try {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                System.out.println(termAtt.toString() + " [" + typeAttr.type() + "]");
            }

            tokenStream.end();
        } finally {
            tokenStream.close();
        }
    }
}