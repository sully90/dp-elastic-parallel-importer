package com.github.onsdigital.elastic.importer;

import com.github.onsdigital.elastic.importer.handlers.FileReaderHandler;
import com.github.onsdigital.elastic.importer.handlers.IndexHandler;
import com.github.onsdigital.elastic.importer.handlertasks.FileReaderTask;
import com.github.onsdigital.elastic.importer.handlertasks.IndexHandlerTask;
import com.github.onsdigital.elastic.importer.models.page.base.Page;
import com.github.onsdigital.elastic.importer.util.FileScanner;
import com.github.onsdigital.elasticutils.client.Host;
import com.github.onsdigital.elasticutils.client.bulk.configuration.BulkProcessorConfiguration;
import com.github.onsdigital.elasticutils.client.bulk.options.BulkProcessingOptions;
import com.github.onsdigital.elasticutils.client.generic.ElasticSearchClient;
import com.github.onsdigital.elasticutils.client.generic.RestSearchClient;
import com.github.onsdigital.elasticutils.client.generic.TransportSearchClient;
import com.github.onsdigital.elasticutils.client.http.SimpleRestClient;
import com.github.onsdigital.elasticutils.util.ElasticSearchHelper;
import com.github.onsdigital.fanoutcascade.exceptions.PurgingExceptionHandler;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sullid (David Sullivan) on 08/01/2018
 * @project dp-elastic-parallel-importer
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final int numBulkActions = 200;

    public App() {
        // Setup the FanoutCascade

        FanoutCascadeRegistry.getInstance().registerMonitoringThread();
        FanoutCascadeRegistry.getInstance().setExceptionHandler(new PurgingExceptionHandler());

        FanoutCascadeRegistry.getInstance().register(IndexHandlerTask.class, IndexHandler.class, 1);
        FanoutCascadeRegistry.getInstance().register(FileReaderTask.class, FileReaderHandler.class, 8);
    }

    public void start() {

        String zebedeeRoot = System.getenv("zebedee_root");
        String dataDirectory = String.format("%s/zebedee/master/", zebedeeRoot);

        FileScanner fileScanner = new FileScanner(dataDirectory);
        try (ElasticSearchClient<Page> searchClient = getClientTcp(Host.LOCALHOST, numBulkActions)) {

            List<String> fileNames = fileScanner.getFiles();

            FanoutCascadeLayer fileReaderLayer = FanoutCascade.getInstance().getLayerForTask(FileReaderTask.class);
            Iterator<String> it = fileNames.iterator();

            while (it.hasNext()) {
                int count = 100;
                List<String> tmp = new ArrayList<>();
                while (count > 0) {
                    tmp.add(it.next());
                    count--;
                }
                LOGGER.info(String.format("Submitting task with %d files", tmp.size()));
                FileReaderTask fileReaderTask = new FileReaderTask(tmp);
                fileReaderLayer.submit(fileReaderTask);
            }

            IndexHandlerTask indexHandlerTask = new IndexHandlerTask(searchClient, numBulkActions);
            FanoutCascade.getInstance().getLayerForTask(IndexHandlerTask.class).submit(indexHandlerTask);

            searchClient.awaitClose(1, TimeUnit.MINUTES);

            // Shutdown the FanoutCascade
            FanoutCascade.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

    private static ElasticSearchClient<Page> getClientTcp(Host host, int numBulkActions) throws UnknownHostException {
        TransportClient transportClient = ElasticSearchHelper.getTransportClient(host);
        return new TransportSearchClient<>(transportClient, getConfiguration(numBulkActions));
    }

    private static ElasticSearchClient<Page> getClientHttp(Host host, int numBulkActions) throws UnknownHostException {
        SimpleRestClient restClient = ElasticSearchHelper.getRestClient(host);
        return new RestSearchClient<>(restClient, getConfiguration(numBulkActions));
    }

    private static BulkProcessorConfiguration getConfiguration(int numBulkActions) {
        BulkProcessorConfiguration bulkProcessorConfiguration = new BulkProcessorConfiguration(BulkProcessingOptions.builder()
                .setBulkActions(numBulkActions)
//                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
//                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(8)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(1000), 5))
                .build());
        return bulkProcessorConfiguration;
    }

}
