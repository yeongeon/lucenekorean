package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.ko.JasoAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.io.IOException;

public class JasoAnalyzerProvider extends AbstractIndexAnalyzerProvider<JasoAnalyzer> {

    private final JasoAnalyzer analyzer;

    @Inject
    public JasoAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, Environment env, @Assisted String name, @Assisted Settings settings) throws IOException {
        super(index, indexSettingsService.getSettings(), name, settings);

        analyzer = new JasoAnalyzer();
    }

    @Override
    public JasoAnalyzer get() {
        return this.analyzer;
    }
}
