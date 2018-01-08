package com.github.onsdigital.elastic.importer.handlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.onsdigital.elastic.importer.handlertasks.FileReaderTask;
import com.github.onsdigital.elastic.importer.handlertasks.IndexHandlerTask;
import com.github.onsdigital.elastic.importer.models.page.base.Page;
import com.github.onsdigital.elastic.importer.models.page.base.PageType;
import com.github.onsdigital.elasticutils.action.index.SimpleIndexRequestBuilder;
import com.github.onsdigital.elasticutils.client.generic.ElasticSearchClient;
import com.github.onsdigital.elasticutils.client.type.DefaultDocumentTypes;
import com.github.onsdigital.elasticutils.util.JsonUtils;
import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * @author sullid (David Sullivan) on 08/01/2018
 * @project dp-elastic-parallel-importer
 */
public class IndexHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexHandler.class);

    private List<Page> pages = new ArrayList<>();

    @Override
    public Object handleTask(HandlerTask handlerTask) throws Exception {

        IndexHandlerTask task = (IndexHandlerTask) handlerTask;
        int numBulkActions = task.getNumBulkActions();

        // Get the layer responsible for reading files
        FanoutCascadeLayer layer = FanoutCascade.getInstance().getLayerForTask(FileReaderTask.class);

        // Continually check for new futures while the cascade is running
        LOGGER.info(String.format("Shutdown: %s \t Idle: %s", FanoutCascade.getInstance().isShutdown(),
                layer.isIdle()));
        while (!FanoutCascade.getInstance().isShutdown() && !layer.isIdle()) {
            // Try to get values of futures
            for (HandlerTask t : layer.getKeySet()) {
                Future<Object> future = layer.popFuture(t);
                if (future.isDone()) {
                    Object obj = future.get();
                    if (null != obj && obj instanceof Collection<?>) {
                        // Add to the list
                        Collection<?> collection = (Collection<?>) obj;
                        for (Object o : collection) {
                            if (o instanceof Page) {
                                Page p = (Page) obj;
                                this.pages.add(p);
                            }


                        }
                    }
                }
            }

            if (this.pages.size() >= numBulkActions) {
                // Bulk insert
                LOGGER.info(String.format("Indexing %d pages", this.pages.size()));
                for (Page p : this.pages) {
                    PageType pageType = p.getType();
                    String indexName = String.format("%s_%s", "ons", pageType.getFormattedDisplayName());
                    IndexRequest indexRequest = getIndexRequest(indexName, p, task.getSearchClient());
                    task.getSearchClient().addToBulk(indexRequest);
                }

                this.pages.clear();
            }

        }

        return null;
    }

    private static IndexRequest getIndexRequest(String indexName, Page page, ElasticSearchClient<Page> searchClient) {
        String id = page.getUri().toString();

        Optional<byte[]> messageBytes = JsonUtils.convertJsonToBytes(page, JsonInclude.Include.NON_NULL);

        if (messageBytes.isPresent()) {
            SimpleIndexRequestBuilder indexRequestBuilder = searchClient.prepareIndex()
                    .setIndex(indexName)
                    .setType(DefaultDocumentTypes.DOCUMENT.getType())
                    .setId(id)
                    .setSource(messageBytes.get(), XContentType.JSON);

            if (!page.getType().equals(PageType.timeseries)) {
                indexRequestBuilder.setPipeline("opennlp-pipeline");
            }

//            if (page.getType() == PageType.article || page.getType() == PageType.bulletin) {
//                indexRequestBuilder.setPipeline(Pipeline.OPENNLP.getPipeline());
//            }

            return indexRequestBuilder.request();
        }
        return null;
    }
}
