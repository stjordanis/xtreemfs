/*
 * Copyright (c) 2008-2011 by Jan Stender,
 *               Zuse Institute Berlin
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */

package org.xtreemfs.mrc.operations;

import org.xtreemfs.foundation.TimeSync;
import org.xtreemfs.foundation.pbrpc.generatedinterfaces.RPC.POSIXErrno;
import org.xtreemfs.mrc.MRCRequest;
import org.xtreemfs.mrc.MRCRequestDispatcher;
import org.xtreemfs.mrc.UserException;
import org.xtreemfs.mrc.ac.FileAccessManager;
import org.xtreemfs.mrc.database.AtomicDBUpdate;
import org.xtreemfs.mrc.database.StorageManager;
import org.xtreemfs.mrc.database.VolumeManager;
import org.xtreemfs.mrc.utils.MRCHelper;
import org.xtreemfs.mrc.utils.Path;
import org.xtreemfs.mrc.utils.PathResolver;
import org.xtreemfs.pbrpc.generatedinterfaces.MRC.mkdirRequest;
import org.xtreemfs.pbrpc.generatedinterfaces.MRC.timestampResponse;

/**
 * 
 * @author stender
 */
public class CreateDirOperation extends MRCOperation {
    
    public CreateDirOperation(MRCRequestDispatcher master) {
        super(master);
    }
    
    @Override
    public void startRequest(MRCRequest rq) throws Throwable {
        
        final mkdirRequest rqArgs = (mkdirRequest) rq.getRequestArgs();
        
        final VolumeManager vMan = master.getVolumeManager();
        final FileAccessManager faMan = master.getFileAccessManager();
        
        validateContext(rq);
        
        final Path p = new Path(rqArgs.getVolumeName(), rqArgs.getPath());
        
        final StorageManager sMan = vMan.getStorageManagerByName(p.getComp(0));
        final PathResolver res = new PathResolver(sMan, p);
        
        // check if dir == volume
        if (res.getParentDir() == null)
            throw new UserException(POSIXErrno.POSIX_ERROR_EEXIST, "file or directory '" + res.getFileName()
                + "' exists already");
        
        // check whether the path prefix is searchable
        faMan.checkSearchPermission(sMan, res, rq.getDetails().userId, rq.getDetails().superUser, rq
                .getDetails().groupIds);
        
        // check whether the parent directory grants write access
        faMan.checkPermission(FileAccessManager.O_WRONLY, sMan, res.getParentDir(), res.getParentsParentId(),
            rq.getDetails().userId, rq.getDetails().superUser, rq.getDetails().groupIds);
        
        // check whether the file/directory exists already
        res.checkIfFileExistsAlready();
        
        // prepare directory creation in database
        AtomicDBUpdate update = sMan.createAtomicDBUpdate(master, rq);
        
        // get the next free file ID
        long fileId = sMan.getNextFileId();
        
        // atime, ctime, mtime
        int time = (int) (TimeSync.getGlobalTime() / 1000);
        
        // create the metadata object
        sMan.createDir(fileId, res.getParentDirId(), res.getFileName(), time, time, time,
            rq.getDetails().userId, rq.getDetails().groupIds.get(0), rqArgs.getMode(), 0, update);
        
        // set the file ID as the last one
        sMan.setLastFileId(fileId, update);
        
        // update POSIX timestamps of parent directory
        MRCHelper.updateFileTimes(res.getParentsParentId(), res.getParentDir(), false, true, true, sMan,
            time, update);
        
        // set the response
        rq.setResponse(timestampResponse.newBuilder().setTimestampS(time).build());
        
        update.execute();
    }
    
}
