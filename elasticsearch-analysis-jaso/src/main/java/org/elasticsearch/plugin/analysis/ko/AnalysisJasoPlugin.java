package org.elasticsearch.plugin.analysis.ko;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.JasoAnalysisBinderProcessor;
import org.elasticsearch.plugins.Plugin;

public class AnalysisJasoPlugin extends Plugin {

    @Override
    public String name() {
        return "analysis-jaso";
    }

    @Override
    public String description() {
        return "Jaso Analyzer";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new JasoAnalysisBinderProcessor());
    }
}