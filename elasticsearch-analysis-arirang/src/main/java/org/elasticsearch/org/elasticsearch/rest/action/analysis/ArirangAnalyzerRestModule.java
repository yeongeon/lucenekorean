package org.elasticsearch.org.elasticsearch.rest.action.analysis;

import org.elasticsearch.common.inject.AbstractModule;

public class ArirangAnalyzerRestModule extends AbstractModule {

    @Override
    protected void configure() {
        // TODO Auto-generated method stub
        bind(ArirangAnalyzerRestAction.class).asEagerSingleton();
    }
}
