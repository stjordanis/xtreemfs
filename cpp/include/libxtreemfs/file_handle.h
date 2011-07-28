/*
 * Copyright (c) 2011 by Michael Berlin, Zuse Institute Berlin
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */

#ifndef CPP_INCLUDE_LIBXTREEMFS_FILE_HANDLE_H_
#define CPP_INCLUDE_LIBXTREEMFS_FILE_HANDLE_H_

namespace xtreemfs {

namespace pbrpc {
class Lock;
class Stat;
class UserCredentials;
}  // namespace pbrpc

class FileHandle {
 public:
  virtual ~FileHandle() {}

  /** Read from a file 'count' bytes starting at 'offset' into 'buf'.
   *
   * @param user_credentials    Name and Groups of the user.
   * @param buf[out]            Buffer to be filled with read data.
   * @param count               Number of requested bytes.
   * @param offset              Offset in bytes.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   *
   * @return    Number of bytes read.
   */
  virtual int Read(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      char *buf,
      size_t count,
      off_t offset) = 0;

  /** Write to a file 'count' bytes at file offset 'offset' from 'buf'.
   *
   * @param user_credentials    Name and Groups of the user.
   * @param buf[in]             Buffer which contains data to be written.
   * @param count               Number of bytes to be written from buf.
   * @param offset              Offset in bytes.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   *
   * @return    Number of bytes written.
   */
  virtual int Write(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      const char *buf,
      size_t count,
      off_t offset) = 0;

  /** Flushes pending writes and file size updates (corresponds to a fsync()
   *  system call).
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   */
  virtual void Flush() = 0;

  /** Truncates the file to "new_file_size_ bytes".
   *
   * @param user_credentials    Name and Groups of the user.
   * @param off_t               New size of the file.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   **/
  virtual void Truncate(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      off_t new_file_size) = 0;

  /** Retrieve the attributes of this file and writes the result in "stat".
   *
   * @param user_credentials    Name and Groups of the user.
   * @param stat                Pointer to Stat which will be overwritten.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   */
  virtual void GetAttr(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      xtreemfs::pbrpc::Stat* stat) = 0;

  /** Sets a lock on the specified file region and returns the resulting Lock
   *  object.
   *
   * If the acquisition of the lock fails, PosixErrorException will be thrown
   * and posix_errno() will return POSIX_ERROR_EAGAIN.
   *
   * @param user_credentials    Name and Groups of the user.
   * @param process_id      ID of the process to which the lock belongs.
   * @param offset          Start of the region to be locked in the file.
   * @param length          Length of the region.
   * @param exclusive       shared/read lock (false) or write/exclusive (true)?
   * @param wait_for_lock   if true, blocks until lock acquired.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   *
   * @remark Ownership is transferred to the caller.
   */
  virtual xtreemfs::pbrpc::Lock* AcquireLock(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      int process_id,
      boost::uint64_t offset,
      boost::uint64_t length,
      bool exclusive,
      bool wait_for_lock) = 0;

  /** Checks if the requested lock does not result in conflicts. If true, the
   *  returned Lock object contains the requested 'process_id' in 'client_pid',
   *  otherwise the Lock object is a copy of the conflicting lock.
   *
   * @param user_credentials    Name and Groups of the user.
   * @param process_id      ID of the process to which the lock belongs.
   * @param offset          Start of the region to be locked in the file.
   * @param length          Length of the region.
   * @param exclusive       shared/read lock (false) or write/exclusive (true)?
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   *
   * @remark Ownership is transferred to the caller.
   */
  virtual xtreemfs::pbrpc::Lock* CheckLock(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      int process_id,
      boost::uint64_t offset,
      boost::uint64_t length,
      bool exclusive) = 0;

  /** Releases "lock".
   *
   * @param user_credentials    Name and Groups of the user.
   * @param process_id      ID of the process to which the lock belongs.
   * @param offset          Start of the region to be locked in the file.
   * @param length          Length of the region.
   * @param exclusive       shared/read lock (false) or write/exclusive (true)?
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   */
  virtual void ReleaseLock(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      int process_id,
      boost::uint64_t offset,
      boost::uint64_t length,
      bool exclusive) = 0;

  /** Releases "lock" (parameters given in Lock object).
   *
   * @param user_credentials    Name and Groups of the user.
   * @param lock    Lock to be released.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   */
  virtual void ReleaseLock(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      const xtreemfs::pbrpc::Lock& lock) = 0;

  /** Triggers the replication of the replica on the OSD with the UUID
   *  "osd_uuid" if the replica is a full replica (and not a partial one).
   *
   * The Replica had to be added beforehand and "osd_uuid" has to be included
   * in the XlocSet of the file.
   *
   * @param user_credentials    Name and Groups of the user.
   * @param osd_uuid    UUID of the OSD where the replica is located.
   *
   * @throws AddressToUUIDNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   * @throws UUIDNotInXlocSetException
   */
  virtual void PingReplica(
      const xtreemfs::pbrpc::UserCredentials& user_credentials,
      const std::string& osd_uuid) = 0;

  /** Closes the open file handle (flushing any pending data).
   *
   * @throws AddressToUUIDNotFoundException
   * @throws FileInfoNotFoundException
   * @throws FileHandleNotFoundException
   * @throws IOException
   * @throws PosixErrorException
   * @throws UnknownAddressSchemeException
   */
  virtual void Close() = 0;
};

}  // namespace xtreemfs


#endif  // CPP_INCLUDE_LIBXTREEMFS_FILE_HANDLE_H_
