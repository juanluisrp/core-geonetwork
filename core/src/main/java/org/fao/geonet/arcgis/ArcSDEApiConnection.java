/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.arcgis;

import com.esri.sde.sdk.client.*;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.Log;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapter for ArcSDE connections.
 * <p>
 * See http://edndoc.esri.com/arcsde/9.3/api/japi/japi.htm for (very little) information about the
 * ArcSDE Java API.
 *
 * @author heikki doeleman
 */
public class ArcSDEApiConnection implements ArcSDEConnection{
    private static final String METADATA_TABLE = "GDB_ITEMS";
    private static final String METADATA_COLUMN = "documentation";

    private SeConnection seConnection;

    /**
     * Opens a connection to the specified ArcSDE server.
     */
    public ArcSDEApiConnection(String server, int instance, String database, String username, String password) {
        try {
            seConnection = new SeConnection(server, instance, database, username, password);
            Log.info(ARCSDE_LOG_MODULE_NAME, "Connected to ArcSDE using the Java API");
            seConnection.setConcurrency(SeConnection.SE_LOCK_POLICY);
        } catch (SeException x) {
            SeError error = x.getSeError();
            String description = error.getExtError() + " " + error.getExtErrMsg() + " " + error.getErrDesc();
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error connecting to ArcSDE (via API): " + description, x);
            throw new ExceptionInInitializerError(x);
        }
    }


    public void close() {
        try {
            seConnection.close();
        } catch (SeException x) {
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error closing the ArcSDE connection (via API)", x);
            // TODO handle exception
        }
    }

    /**
     * Closes the connection to ArcSDE server in case users of this class neglect to do so.
     */
    protected void finalize() throws Throwable {
        try {
            seConnection.close();
        } catch (Throwable x) {
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error closing the ArcSDE connection (via API) "
                + "in the finalize method", x);
            throw x;
        } finally {
            super.finalize();
        }
    }

    @Override
    public List<String> retrieveMetadata(AtomicBoolean cancelMonitor) throws Exception {
        Log.info(ARCSDE_LOG_MODULE_NAME, "Start retrieve metadata");
        List<String> results = new ArrayList<String>();
        try {
            // query table containing XML metadata
            SeSqlConstruct sqlConstruct = new SeSqlConstruct();
            String[] tables = {METADATA_TABLE};
            sqlConstruct.setTables(tables);
            String[] propertyNames = {METADATA_COLUMN};
            SeQuery query = new SeQuery(seConnection);
            query.prepareQuery(propertyNames, sqlConstruct);
            query.execute();

            // it is not documented in the ArcSDE API how you know there are no more rows to fetch!
            // I'm assuming: query.fetch returns null (empiric tests indicate this assumption is correct).
            boolean allRowsFetched = false;
            while (!allRowsFetched) {
                if (cancelMonitor.get()) {
                    return Collections.emptyList();
                }
                SeRow row = query.fetch();
                if (row != null) {
                    ByteArrayInputStream bytes = row.getBlob(0);
                    byte[] buff = new byte[bytes.available()];
                    bytes.read(buff);
                    String document = new String(buff, Constants.ENCODING);
                    if (document.contains(ISO_METADATA_IDENTIFIER)) {
                        Log.info(ARCSDE_LOG_MODULE_NAME, "ISO metadata found");
                        results.add(document);
                    }
                } else {
                    allRowsFetched = true;
                }
            }
            query.close();
            Log.info(ARCSDE_LOG_MODULE_NAME, "Finished retrieving metadata, found: #" + results.size()
                + " metadata records");
            return results;
        } catch (SeException x) {
            SeError error = x.getSeError();
            String description = error.getExtError() + " " + error.getExtErrMsg() + " " + error.getErrDesc();
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error retrieving the metadata from "
                + "ArcSDE connection (via API):" + description, x);
            throw new Exception(x);
        }
    }
}
