package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
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
import org.elasticsearch.index.settings.IndexSettingsService;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.arirang.AnalysisArirangPlugin;
import org.junit.Test;

import java.io.StringReader;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class ArirangAnalyzerTest {

    public String query = "2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.";
    //public String query = "자동문이 ㄴ 자동완성되었다";

    @Test
    public void testArirangAnalyzerNamedAnalyzer() throws Exception {
        System.out.println("####### testArirangAnalyzerNamedAnalyzer #######");

        // http://localhost:9200/test/_analyze?analyzer=arirang&text=2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.&pretty=1
        Index index = new Index("test");
        Settings settings = settingsBuilder()
                .put("path.home", ArirangAnalyzerTest.class.getResource("/").getPath()+"tmp")
                .put("index.analysis.analyzer.arirang.type", "arirang_analyzer")
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisArirangPlugin().onModule(analysisModule);

        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);
        NamedAnalyzer namedAnalyzer = analysisService.analyzer("arirang");

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

    @Test
    public void testArirangAnalyzerTokenFilter() throws Exception {
        System.out.println("####### testArirangAnalyzerTokenFilter #######");

        // http://localhost:9200/test/_analyze?tokenizer=arirang_tokenizer&filters=arirang_filter&text=2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.&pretty=1
        Index index = new Index("test");
        Settings settings = settingsBuilder()
                .put("path.home", ArirangAnalyzerTest.class.getResource("/").getPath()+"tmp")
                .put("index.analysis.analyzer.arirang.type", "custom")
                .put("index.analysis.analyzer.arirang.tokenizer", "arirang_tokenizer")
                .put("index.analysis.analyzer.arirang.filter", "arirang_filter")
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisArirangPlugin().onModule(analysisModule);

        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);

        IndexSettingsService indexSettingsService = new IndexSettingsService(index, settings);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("arirang_filter");
        Tokenizer tokenizer = (new ArirangTokenizerFactory(index,indexSettingsService,null,settings)).create();

        tokenizer.setReader(new StringReader(query));
        TokenStream tokenStream = tokenFilter.create(tokenizer);

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

    @Test
    public void testArirangCustomAnalyzer() throws Exception {
        System.out.println("####### testArirangCustomAnalyzer #######");

        // http://localhost:9200/test/_analyze?tokenizer=arirang_tokenizer&filters=arirang_filter,lowercase&text=2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.&pretty=1
        Index index = new Index("test");
        Settings settings = settingsBuilder()
                .put("path.home", ArirangAnalyzerTest.class.getResource("/").getPath()+"tmp")
                .put("index.analysis.analyzer.arirang.type", "custom")
                .put("index.analysis.analyzer.arirang.tokenizer", "arirang_tokenizer")
                .putArray("index.analysis.analyzer.arirang.filter", "arirang_filter", "lowercase")
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();

        IndexSettingsService indexSettingsService = new IndexSettingsService(index, settings);

        ArirangTokenizerFactory arirangTokenizerFactory = new ArirangTokenizerFactory(index, indexSettingsService, null, settings);
        Tokenizer tokenizer = arirangTokenizerFactory.create();
        ArirangTokenFilterFactory arirangTokenFilterFactory = new ArirangTokenFilterFactory(index, indexSettingsService, null, settings);
        LowerCaseTokenFilterFactory lowerCaseTokenFilterFactory = new LowerCaseTokenFilterFactory(index, indexSettingsService, null, settings);
        TokenStream tokenStream;

        tokenizer.setReader(new StringReader(query));

        tokenStream = arirangTokenFilterFactory.create(tokenizer);
        tokenStream = lowerCaseTokenFilterFactory.create(tokenStream);

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

    @Test
    public void testKoreanTokenizer() throws Exception {
        System.out.println("####### testKoreanTokenizer #######");

        // http://localhost:9200/test/_analyze?tokenizer=arirang_tokenizer&filters=arirang_filter&text=2015년 11월 2일 추가 : 만일 Elasticsearch 2.0 이후 버전을 사용중이라면, Elasticsearch 2.0 에서의 인덱싱에 대한 성능 고려 사항 블로그 포스트를 확인하세요.&pretty=1
        KoreanTokenizer koreanTokenizer = new KoreanTokenizer();  // tokenizer 적용.
        koreanTokenizer.setReader(new StringReader(query));

        TokenStream tokenStream = new KoreanFilter(koreanTokenizer); // filter 적용.

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