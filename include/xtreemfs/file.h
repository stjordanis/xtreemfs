// Copyright 2009 Minor Gordon.
// This source comes from the XtreemFS project. It is licensed under the GPLv2 (see COPYING for terms and conditions).

#ifndef _XTREEMFS_FILE_H_
#define _XTREEMFS_FILE_H_

#include "xtreemfs/osd_proxy.h"


namespace xtreemfs
{
  class MRCProxy;
  class Volume;


  class File : public YIELD::platform::File
  {
  public:
    YIELD_PLATFORM_FILE_PROTOTYPES;
    virtual size_t getpagesize();
    virtual uint64_t get_size();

  private:
    friend class Volume;

    File
    ( 
      yidl::runtime::auto_Object<Volume> parent_volume, 
      const YIELD::platform::Path& path, 
      const org::xtreemfs::interfaces::FileCredentials& file_credentials 
    );

    ~File();

    yidl::runtime::auto_Object<Volume> parent_volume;
    YIELD::platform::Path path;
    org::xtreemfs::interfaces::FileCredentials file_credentials;

    org::xtreemfs::interfaces::OSDWriteResponse latest_osd_write_response;
    std::vector<org::xtreemfs::interfaces::Lock> locks;
    ssize_t selected_file_replica;
  };

  typedef yidl::runtime::auto_Object<File> auto_File;
};

#endif
