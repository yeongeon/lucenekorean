package org.elasticsearch.plugin.analysis.arirang;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Map;

import static java.util.Collections.singletonMap;


/**
 *
 * https://github.com/usemodj/elasticsearch-analysis-korean/blob/master/src/main/java/org/elasticsearch/plugins/analysis/kr/AnalysisKoreanPlugin.java
 *
 */
public class AnalysisArirangPlugin extends Plugin implements AnalysisPlugin {

    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        return singletonMap("kr_filter", ArirangFilterFactory::new);
    }


    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        return singletonMap("kr_tokenizer", ArirangTokenizerFactory::new);
    }


    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return singletonMap("kr_analyzer", ArirangAnalyzerProvider::new);
    }

}