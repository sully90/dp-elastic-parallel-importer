package com.github.onsdigital.elastic.importer.models.page.staticpage.base;



import com.github.onsdigital.elastic.importer.models.partial.Link;

import java.net.URI;

/**
 * Created by bren on 30/06/15.
 *
 * References to static pages, holds a short version of summary of referred page seperately.
 */
public class StaticPageSection extends Link {

    private StaticPageSection() {
        // For Jackson
    }

    private String summary;

    public StaticPageSection(URI uri) {
        super(uri);
    }

    public StaticPageSection(URI uri, Integer index) {
        super(uri, index);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
