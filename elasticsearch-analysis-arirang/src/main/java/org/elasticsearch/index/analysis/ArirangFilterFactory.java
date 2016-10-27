package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class ArirangFilterFactory extends AbstractTokenFilterFactory {
    private final String name;
    private boolean bigrammable = true;
    private boolean hasOrigin = true;

    public ArirangFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.name = settings.get("name", "kr_filter");
    }

    @Override
    public TokenStream create(TokenStream tokenstream) {
        return new KoreanFilter(tokenstream, bigrammable, hasOrigin);
    }

    public void setBigrammable(boolean bool) {
        this.bigrammable = bool;
    }

    public void setHasOrigin(boolean bool) {
        this.hasOrigin = bool;
    }

}
