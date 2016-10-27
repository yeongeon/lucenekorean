package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class ArirangAnalyzerProvider extends AbstractIndexAnalyzerProvider<KoreanAnalyzer> {

    private final KoreanAnalyzer analyzer;

    @Inject
    public ArirangAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);

        analyzer = new KoreanAnalyzer();
    }

    @Override
    public KoreanAnalyzer get() {
        return this.analyzer;
    }
}