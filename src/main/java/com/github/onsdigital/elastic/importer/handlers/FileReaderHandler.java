package com.github.onsdigital.elastic.importer.handlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.elastic.importer.handlertasks.FileReaderTask;
import com.github.onsdigital.elastic.importer.models.page.adhoc.AdHoc;
import com.github.onsdigital.elastic.importer.models.page.base.Page;
import com.github.onsdigital.elastic.importer.models.page.base.PageType;
import com.github.onsdigital.elastic.importer.models.page.census.HomePageCensus;
import com.github.onsdigital.elastic.importer.models.page.compendium.CompendiumChapter;
import com.github.onsdigital.elastic.importer.models.page.compendium.CompendiumData;
import com.github.onsdigital.elastic.importer.models.page.compendium.CompendiumLandingPage;
import com.github.onsdigital.elastic.importer.models.page.home.HomePage;
import com.github.onsdigital.elastic.importer.models.page.release.Release;
import com.github.onsdigital.elastic.importer.models.page.staticpage.StaticArticle;
import com.github.onsdigital.elastic.importer.models.page.staticpage.StaticLandingPage;
import com.github.onsdigital.elastic.importer.models.page.staticpage.StaticPage;
import com.github.onsdigital.elastic.importer.models.page.staticpage.foi.FOI;
import com.github.onsdigital.elastic.importer.models.page.staticpage.qmi.QMI;
import com.github.onsdigital.elastic.importer.models.page.statistics.data.DataSlice;
import com.github.onsdigital.elastic.importer.models.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.elastic.importer.models.page.statistics.dataset.Dataset;
import com.github.onsdigital.elastic.importer.models.page.statistics.dataset.DatasetLandingPage;
import com.github.onsdigital.elastic.importer.models.page.statistics.dataset.TimeSeriesDataset;
import com.github.onsdigital.elastic.importer.models.page.statistics.document.article.Article;
import com.github.onsdigital.elastic.importer.models.page.statistics.document.article.ArticleDownload;
import com.github.onsdigital.elastic.importer.models.page.statistics.document.bulletin.Bulletin;
import com.github.onsdigital.elastic.importer.models.page.taxonomy.ProductPage;
import com.github.onsdigital.elastic.importer.models.page.taxonomy.TaxonomyLandingPage;
import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sullid (David Sullivan) on 08/01/2018
 * @project dp-elastic-parallel-importer
 */
public class FileReaderHandler implements Handler {

    private static final ObjectMapper MAPPER;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReaderHandler.class);

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public Object handleTask(HandlerTask handlerTask) throws Exception {
        FileReaderTask task = (FileReaderTask) handlerTask;
        List<File> files = task.getFiles();

        List<Page> pages = new ArrayList<>();

        try {
            for (File file : files) {
                Page p = fromFile(file);
                if (null != p) {
                    pages.add(p);
                }
            }
            return pages;
        } catch (IOException e) {
            return null;
        }
    }

    private Page asClass(String fileString, Class<? extends Page> returnClass) throws IOException {
        return MAPPER.readValue(fileString, returnClass);
    }

    public Page fromFile(File file) throws IOException {
        String fileString = IOUtils.toString(new FileReader(file));
        Object obj;
        try {
            obj = MAPPER.readValue(fileString, Object.class);
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.warn(String.format("Error constructing object from json in file: %s", file.getAbsolutePath()), e);
            return null;
        }

        if (obj instanceof Map) {
            Map<String, Object> objectMap;
            try {
                objectMap = MAPPER.readValue(fileString, new TypeReference<Map<String, Object>>() {
                });
            } catch (JsonMappingException e) {
                LOGGER.warn(String.format("Error constructing objectMap from json in file: %s", file.getAbsolutePath()), e);
                return null;
            }

            if (objectMap.containsKey("type") && objectMap.get("type") instanceof String) {
                String type = (String) objectMap.get("type");
                type = type.toLowerCase().replaceAll("\\s", "_");

                PageType identifiedPage = PageType.forType(type);
                if (identifiedPage == null) {
                    identifiedPage = PageType.forType(String.format("%s_page", type));
                    if (identifiedPage == null) {
                        LOGGER.info(String.format("Failed to identify page %s, exiting.", type));
                        System.exit(1);
                    }
                    switch (identifiedPage) {
                        case home_page:
                            return asClass(fileString, HomePage.class);
                        case home_page_census:
                            return asClass(fileString, HomePageCensus.class);
                        case taxonomy_landing_page:
                            return asClass(fileString, TaxonomyLandingPage.class);
                        case product_page:
                            return asClass(fileString, ProductPage.class);
                        case bulletin:
                            return asClass(fileString, Bulletin.class);
                        case article:
                            return asClass(fileString, Article.class);
                        case article_download:
                            return asClass(fileString, ArticleDownload.class);
                        case timeseries:
                            return asClass(fileString, TimeSeries.class);
                        case data_slice:
                            return asClass(fileString, DataSlice.class);
                        case compendium_landing_page:
                            return asClass(fileString, CompendiumLandingPage.class);
                        case compendium_chapter:
                            return asClass(fileString, CompendiumChapter.class);
                        case compendium_data:
                            return asClass(fileString, CompendiumData.class);
                        case static_landing_page:
                            return asClass(fileString, StaticLandingPage.class);
                        case static_article:
                            return asClass(fileString, StaticArticle.class);
                        case static_page:
                            return asClass(fileString, StaticPage.class);
                        case static_qmi:
                            return asClass(fileString, QMI.class);
                        case static_foi:
                            return asClass(fileString, FOI.class);
                        case static_adhoc:
                            return asClass(fileString, AdHoc.class);
                        case dataset:
                            return asClass(fileString, Dataset.class);
                        case dataset_landing_page:
                            return asClass(fileString, DatasetLandingPage.class);
                        case timeseries_dataset:
                            return asClass(fileString, TimeSeriesDataset.class);
                        case release:
                            return asClass(fileString, Release.class);
                        case reference_tables:
                        case chart:
                        case table:
                        case image:
                        case visualisation:
                        case equation:
                            return null;
//                        case reference_tables:
//                            return asClass(fileString, ReferenceTables.class);
//                        case chart:
//                            return asClass(fileString, Chart.class);
//                        case table:
//                            return asClass(fileString, Table.class);
//                        case image:
//                            return asClass(fileString, Image.class);
//                        case visualisation:
//                            return asClass(fileString, Visualisation.class);
//                        case equation:
//                            return asClass(fileString, Equation.class);
                        default:
                            LOGGER.warn("Unknown type: " + type);
                    }
                }
            }
        }
        return null;
    }
}
