// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.vfs.newvfs.persistent;

import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.openapi.vfs.newvfs.persistent.SpecializedFileAttributes.ByteFileAttributeAccessor;
import com.intellij.openapi.vfs.newvfs.persistent.SpecializedFileAttributes.IntFileAttributeAccessor;
import com.intellij.openapi.vfs.newvfs.persistent.SpecializedFileAttributes.LongFileAttributeAccessor;
import com.intellij.openapi.vfs.newvfs.persistent.SpecializedFileAttributes.ShortFileAttributeAccessor;
import com.intellij.openapi.vfs.newvfs.persistent.dev.MappedFileStorageHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.intellij.openapi.vfs.newvfs.persistent.SpecializedFileAttributes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SpecializedFileAttributesTest {
  private static final FileAttribute TEST_LONG_ATTRIBUTE = new FileAttribute("TEST_ATTRIBUTE_LONG", 1, true);
  private static final FileAttribute TEST_INT_ATTRIBUTE = new FileAttribute("TEST_ATTRIBUTE_INT", 1, true);
  private static final FileAttribute TEST_SHORT_ATTRIBUTE = new FileAttribute("TEST_ATTRIBUTE_SHORT", 1, true);
  private static final FileAttribute TEST_BYTE_ATTRIBUTE = new FileAttribute("TEST_ATTRIBUTE_BYTE", 1, true);

  private static final int ENOUGH_VALUES = 1 << 20;

  private FSRecordsImpl vfs;

  private ByteFileAttributeAccessor byteAttributeAccessor;
  private ByteFileAttributeAccessor byteFastAttributeAccessor;

  private ShortFileAttributeAccessor shortAttributeAccessor;
  private ShortFileAttributeAccessor shortFastAttributeAccessor;

  private IntFileAttributeAccessor intAttributeAccessor;
  private IntFileAttributeAccessor intFastAttributeAccessor;

  private LongFileAttributeAccessor longAttributeAccessor;
  private LongFileAttributeAccessor longFastAttributeAccessor;


  @BeforeEach
  public void setup(@TempDir Path tempDir) throws Exception {
    vfs = FSRecordsImpl.connect(tempDir);

    byteAttributeAccessor = specializeAsByte(vfs, TEST_BYTE_ATTRIBUTE);
    byteFastAttributeAccessor = specializeAsFastByte(vfs, TEST_BYTE_ATTRIBUTE);

    shortAttributeAccessor = null;//not implemented yet
    shortFastAttributeAccessor = specializeAsFastShort(vfs, TEST_SHORT_ATTRIBUTE);

    intAttributeAccessor = specializeAsInt(vfs, TEST_INT_ATTRIBUTE);
    intFastAttributeAccessor = specializeAsFastInt(vfs, TEST_INT_ATTRIBUTE);

    longAttributeAccessor = specializeAsLong(vfs, TEST_LONG_ATTRIBUTE);
    longFastAttributeAccessor = null;//not implemented yet
  }

  @AfterEach
  public void tearDown() throws Exception {
    vfs.dispose();
    assertEquals(0,MappedFileStorageHelper.storagesRegistered());
  }

  @ParameterizedTest(name = "fast: {0}")
  @ValueSource(booleans = {true, false})
  public void singleIntValueCouldBeWrittenAndReadBackAsIs(boolean testFastAccessor) throws Exception {
    IntFileAttributeAccessor accessor = testFastAccessor ? intFastAttributeAccessor : intAttributeAccessor;
    int fileId = vfs.createRecord();
    int valueToWrite = 1234;
    accessor.write(fileId, valueToWrite);
    int readBack = accessor.read(fileId, 0);
    assertEquals(valueToWrite, readBack,
                 "Value written must be read back as is");
  }

  @ParameterizedTest(name = "fast: {0}")
  @ValueSource(booleans = {true, false})
  public void manyIntValuesCouldBeWrittenAndReadBackAsIs(boolean testFastAccessor) throws Exception {
    IntFileAttributeAccessor accessor = testFastAccessor ? intFastAttributeAccessor : intAttributeAccessor;
    int fileId = vfs.createRecord();
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < ENOUGH_VALUES; i++) {
      int valueToWrite = rnd.nextInt();
      accessor.write(fileId, valueToWrite);
      int readBack = accessor.read(fileId, 0);
      assertEquals(valueToWrite, readBack,
                   "Value written must be read back as is");
    }
  }


  @ParameterizedTest(name = "fast: {0}")
  @ValueSource(booleans = {true})
  public void singleShortValueCouldBeWrittenAndReadBackAsIs(boolean testFastAccessor) throws Exception {
    ShortFileAttributeAccessor accessor = testFastAccessor ? shortFastAttributeAccessor : shortAttributeAccessor;
    int fileId = vfs.createRecord();
    short valueToWrite = 1234;
    accessor.write(fileId, valueToWrite);
    short readBack = accessor.read(fileId, (short)0);
    assertEquals(valueToWrite, readBack,
                 "Value written must be read back as is");
  }

  @ParameterizedTest(name = "fast: {0}")
  @ValueSource(booleans = {true})
  public void manyShortValuesCouldBeWrittenAndReadBackAsIs(boolean testFastAccessor) throws Exception {
    ShortFileAttributeAccessor accessor = testFastAccessor ? shortFastAttributeAccessor : shortAttributeAccessor;
    int fileId = vfs.createRecord();
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < ENOUGH_VALUES; i++) {
      short valueToWrite = (short)rnd.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
      accessor.write(fileId, valueToWrite);
      short readBack = accessor.read(fileId, (short)0);
      assertEquals(valueToWrite, readBack,
                   "Value written must be read back as is");
    }
  }


  @Test
  public void singleLongValueCouldBeWrittenAndReadBackAsIs() throws Exception {
    int fileId = vfs.createRecord();
    long valueToWrite = 1234;
    longAttributeAccessor.write(fileId, valueToWrite);
    long readBack = longAttributeAccessor.read(fileId, 0);
    assertEquals(valueToWrite, readBack,
                 "Value written must be read back as is");
  }

  @Test
  public void manyLongValuesCouldBeWrittenAndReadBackAsIs() throws Exception {
    int fileId = vfs.createRecord();
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < ENOUGH_VALUES; i++) {
      long valueToWrite = rnd.nextLong();
      longAttributeAccessor.write(fileId, valueToWrite);
      long readBack = longAttributeAccessor.read(fileId, 0);
      assertEquals(valueToWrite, readBack,
                   "Value written must be read back as is");
    }
  }

  @Test
  public void singleByteValueCouldBeWrittenAndReadBackAsIs() throws Exception {
    int fileId = vfs.createRecord();
    byte valueToWrite = 123;
    byteAttributeAccessor.write(fileId, valueToWrite);
    byte readBack = byteAttributeAccessor.read(fileId, (byte)0);
    assertEquals(valueToWrite, readBack,
                 "Value written must be read back as is");
  }

  @ParameterizedTest(name = "fast: {0}")
  @ValueSource(booleans = {true, false})
  public void manyByteValuesCouldBeWrittenAndReadBackAsIs(boolean testFastAccessor) throws Exception {
    ByteFileAttributeAccessor accessor = testFastAccessor ? byteFastAttributeAccessor : byteAttributeAccessor;
    int fileId = vfs.createRecord();
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < ENOUGH_VALUES; i++) {
      byte valueToWrite = (byte)rnd.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
      accessor.write(fileId, valueToWrite);
      byte readBack = accessor.read(fileId, (byte)0);
      assertEquals(valueToWrite, readBack,
                   "Value written must be read back as is");
    }
  }

}