/*
 * Copyright (c) 2008-2011 by Paul Seiferth, Zuse Institute Berlin
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */
package org.xtreemfs.common.libxtreemfs;

import org.xtreemfs.foundation.SSLOptions;
import org.xtreemfs.foundation.VersionManagement;

/**
 * Represents all possible options for libxtreemfs.
 */
public class Options {

    enum XtreemFSServiceType {
        kDIR, kMRC
    };

    // Optimizations.
    /**
     * Maximum number of entries of the StatCache. Default: 100000
     */
    private int    metadataCacheSize     = 100000;

    /**
     * Time to live for MetadataCache entries. Default: 120
     */
    private long    metadataCacheTTLs     = 120;

    /**
     * Maximum number of pending bytes (of async writes) per file. TODO: Reenable async writes when retry
     * support is completed.
     */
    private int     maxWriteahead         = 0;

    /**
     * Maximum number of pending async write requests per file. Default: 10
     */
    private int     maxWriteaheadRequests = 10;

    /**
     * Number of retrieved entries per readdir request. Default: 1024
     */
    private int     readdirChunkSize      = 1024;

    // Error Handling options.
    /**
     * How often shall a failed operation get retried? Default: 40
     */
    private int     maxTries              = 40;
    /**
     * How often shall a failed read operation get retried? Default: 40
     */
    private int     maxReadTries          = 40;
    /**
     * How often shall a failed write operation get retried? Default: 40
     */
    private int     maxWriteTries         = 40;
    /**
     * How long to wait after a failed request? Default: 15
     */
    private int     retryDelay_s          = 15;

    /**
     * Stops retrying to execute a synchronous request if this signal was send to the thread responsible for
     * the execution of the request. Default: 0
     */
    private int     interruptSignal       = 0;

    /**
     * Maximum time until a connection attempt will be aborted. Default: 60
     */
    private int     connectTimeout_s      = 60;
    /**
     * Maximum time until a request will be aborted and the response returned. Default:
     */
    private int     requestTimeout_s      = 30;

    /**
     * The RPC Client closes connections after "linger_timeout_s" time of inactivity. Default: 600
     */
    private int     lingerTimeout_s       = 600;

    public int getMetadataCacheSize() {
        return metadataCacheSize;
    }
    
    public void setMetadataCacheSize(int metadataCacheSize) {
        this.metadataCacheSize = metadataCacheSize;
    }

    public long getMetadataCacheTTLs() {
        return metadataCacheTTLs;
    }

    public int getInterruptSignal() {
        return interruptSignal;
    }

    public int getConnectTimeout_s() {
        return connectTimeout_s;
    }

    public int getRequestTimeout_s() {
        return requestTimeout_s;
    }

    public int getLingerTimeout_s() {
        return lingerTimeout_s;
    }

    // SSL options.
    private String  sslPemCertPath                    = "";
    private String  sslPemPath                        = "";
    private String  sslPemKeyPass                     = "";
    private String  sslPKCS2Path                      = "";
    private String  sslPKCS12Pass                     = "";

    // Grid Support options.
    /**
     * True, if the XtreemFS Grid-SSL Mode (only SSL handshake, no encryption of data itself) shall be used.
     * Default: false
     */
    private boolean gridSSL                           = false;
    /**
     * True if the Globus user mapping shall be used. Default: false
     * */
    private boolean gridAuthModeGlobus                = false;
    /**
     * True if the Unicore user mapping shall be used. Default: false
     * */
    private boolean gridAuthModeUnicore               = false;
    /**
     * Location of the gridmap file. Default: ""
     */
    private String  gridGridmapLocation               = "";
    /**
     * Default Location of the Globus gridmap file. Default: "/etc/grid-security/grid-mapfile"
     */
    private String  gridGridmapLocationDefaultGlobus  = "/etc/grid-security/grid-mapfile";
    /**
     * Default Location of the Unicore gridmap file. Default: "/etc/grid-security/d-grid_uudb"
     */
    private String  gridGridmapLocationDefaultUnicore = "/etc/grid-security/d-grid_uudb";
    /**
     * Periodic interval after which the gridmap file will be reloaded. Default: 60
     */
    private int     gridGridmapReloadInterval_m       = 60;                               // 60 minutes = 1
                                                                                           // hour

    // Advanced XtreemFS options
    /**
     * Interval for periodic file size updates in seconds. Default: 60
     */
    private int     periodicFileSizeUpdatesIntervalS  = 60;

    /**
     * Interval for periodic xcap renewal in seconds. Default: 60
     */
    private int     periodicXcapRenewalIntervalS      = 60;

    protected int getPeriodicXcapRenewalIntervalS() {
        return periodicXcapRenewalIntervalS;
    }

    protected int getPeriodicFileSizeUpdatesIntervalS() {
        return periodicFileSizeUpdatesIntervalS;
    }

    /**
     * Returns the version string and prepends "component".
     */
    String showVersion(String component) {
        return component + VersionManagement.RELEASE_VERSION;
    }

    /**
     * Return the version.
     * 
     */
    String getVersion() {
        return VersionManagement.RELEASE_VERSION;
    }

    /**
     * Creates a new SSLOptions object based on the value of the members: - sslPem_path - sslPemCertPath -
     * sslPemKeyPass - sslPkcs12Path - sslPkcs12Pass - gridSsl || protocol.
     * 
     */
    public SSLOptions generateSSLOptions() {
        SSLOptions sslOptions = null;
        if (sslEnabled()) {
            // TODO: Find out how to create SSLOptions object.
            // sslOptions = new SSLOptions(new FileInputStream(new File(sslPemPath)),
            // sslPemKeyPass, sslPemCertPath,
            // new FileInputStream(new File(sslPKCS2Path)),
            // sslPKCS12Pass, new String(), gridSSL || protocol == Schemes.SCHEME_PBRPCG );
        }
        return null;
    }

    /** Extract volume name and dir service address from dir_volume_url. */
    protected void parseURL(XtreemFSServiceType service_type) {
        // TODO: Implement!
    }

    public boolean sslEnabled() {
        return !sslPemCertPath.isEmpty() || !sslPKCS2Path.isEmpty();
    }

    public int getMaxTries() {
        return maxTries;
    }

    protected int getMaxWriteTries() {
        return maxWriteTries;
    }

    public int getRetryDelay_s() {
        return retryDelay_s;
    }

    public int getMaxWriteahead() {
        return maxWriteahead;
    }

    protected int getMaxWriteaheadRequests() {
        return maxWriteaheadRequests;
    }

    public int getReaddirChunkSize() {
        return readdirChunkSize;
    }
    
    public void setPeriodicFileSizeUpdatesIntervalS(int periodicFileSizeUpdatesIntervalS) {
        this.periodicFileSizeUpdatesIntervalS = periodicFileSizeUpdatesIntervalS;
    }
    
    public void setMaxWriteAhead(int maxWriteAhead) {
        this.maxWriteahead = maxWriteAhead; 
    }

    public void setMaxReadTries(int maxReadTries) {
        this.maxReadTries = maxReadTries;
    }

    public int getMaxReadTries() {
        return maxReadTries;
    }
}