package com.github.onsdigital.elastic.importer.models.page.base;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.onsdigital.elastic.importer.base.Content;
import com.github.onsdigital.elastic.importer.models.partial.Link;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bren on 10/06/15.
 * <p>
 * This is the generic content object that that has common properties of all page types on the website
 */
public abstract class Page extends Content {

    protected PageType type;

    private URI uri;

    private PageDescription description;

    private List<Link> topics;

    private List<Object> breadcrumb;

    private List<Object> correction;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Set<String>> entities;

    public Page() {
        this.type = getType();
    }

    public abstract PageType getType();

    public PageDescription getDescription() {
        return description;
    }

    public void setDescription(PageDescription description) {
        this.description = description;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public List<Link> getTopics() {
        return topics;
    }

    public void setTopics(List<Link> topics) {
        this.topics = topics;
    }

    public List<Object> getBreadcrumb() {
        return breadcrumb;
    }

    public List<Object> getCorrection() {
        return correction;
    }

    public Map<String, Set<String>> getEntities() {
        return entities;
    }
}
