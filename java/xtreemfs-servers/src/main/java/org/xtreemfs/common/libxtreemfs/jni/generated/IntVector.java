/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.xtreemfs.common.libxtreemfs.jni.generated;

public class IntVector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected IntVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(IntVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        xtreemfs_jniJNI.delete_IntVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public static IntVector from(java.util.Collection<Integer> in) {
    IntVector out = new IntVector();
    for (Integer entry : in) {
      out.add(entry);
    }
    return out;
  }

  public Integer[] toArray() {
    int size = (int) this.size();
    Integer[] out = new Integer[size];
    for (int i = 0; i < size; ++i) {
      out[i] = this.get(i);
    }
    return out;
  }

  public IntVector() {
    this(xtreemfs_jniJNI.new_IntVector__SWIG_0(), true);
  }

  public IntVector(long n) {
    this(xtreemfs_jniJNI.new_IntVector__SWIG_1(n), true);
  }

  public long size() {
    return xtreemfs_jniJNI.IntVector_size(swigCPtr, this);
  }

  public long capacity() {
    return xtreemfs_jniJNI.IntVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    xtreemfs_jniJNI.IntVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return xtreemfs_jniJNI.IntVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    xtreemfs_jniJNI.IntVector_clear(swigCPtr, this);
  }

  public void add(int x) {
    xtreemfs_jniJNI.IntVector_add(swigCPtr, this, x);
  }

  public int get(int i) {
    return xtreemfs_jniJNI.IntVector_get(swigCPtr, this, i);
  }

  public void set(int i, int val) {
    xtreemfs_jniJNI.IntVector_set(swigCPtr, this, i, val);
  }

}
