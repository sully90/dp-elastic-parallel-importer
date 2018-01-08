package com.github.onsdigital.elastic.importer.models.partial.markdown;

import com.github.onsdigital.elastic.importer.base.Content;

/**
 * Represents a section in a markdown content - heading as text and body as
 * Markdown.
 *
 * @author david
 */

public class MarkdownSection extends Content {
    private String title;
    private String markdown;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }
}
