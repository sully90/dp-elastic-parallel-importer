package com.github.onsdigital.elastic.importer.handlertasks;

import com.github.onsdigital.elastic.importer.models.page.base.Page;
import com.github.onsdigital.elasticutils.client.generic.ElasticSearchClient;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

/**
 * @author sullid (David Sullivan) on 08/01/2018
 * @project dp-elastic-parallel-importer
 */
public class IndexHandlerTask extends HandlerTask {

    private ElasticSearchClient<Page> searchClient;
    private int numBulkActions;

    public IndexHandlerTask(ElasticSearchClient<Page> searchClient, int numBulkActions) {
        super(IndexHandlerTask.class);
        this.searchClient = searchClient;
        this.numBulkActions = numBulkActions;
    }

    public ElasticSearchClient<Page> getSearchClient() {
        return searchClient;
    }

    public int getNumBulkActions() {
        return numBulkActions;
    }
}
