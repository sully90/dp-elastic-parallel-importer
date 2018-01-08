package com.github.onsdigital.elastic.importer.models.page.statistics.dataset;


import com.github.onsdigital.elastic.importer.models.page.base.PageType;

/**
 * Created by bren on 01/07/15.
 */
public class ReferenceTables extends DatasetLandingPage {

    private boolean migrated;

    @Override
    public PageType getType() {
        return PageType.reference_tables;
    }

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }
}
