package org.xtreemfs.test.common.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.xtreemfs.common.benchmark.BenchmarkUtils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xtreemfs.common.benchmark.*;
import org.xtreemfs.common.libxtreemfs.Client;
import org.xtreemfs.common.libxtreemfs.ClientFactory;
import org.xtreemfs.common.libxtreemfs.Options;
import org.xtreemfs.common.libxtreemfs.Volume;
import org.xtreemfs.dir.DIRClient;
import org.xtreemfs.dir.DIRConfig;
import org.xtreemfs.dir.DIRRequestDispatcher;
import org.xtreemfs.foundation.logging.Logging;
import org.xtreemfs.foundation.pbrpc.client.RPCAuthentication;
import org.xtreemfs.foundation.pbrpc.generatedinterfaces.RPC;
import org.xtreemfs.foundation.util.FSUtils;
import org.xtreemfs.mrc.MRCRequestDispatcher;
import org.xtreemfs.osd.OSD;
import org.xtreemfs.osd.OSDConfig;
import org.xtreemfs.pbrpc.generatedinterfaces.DIRServiceClient;
import org.xtreemfs.test.SetupUtils;
import org.xtreemfs.test.TestEnvironment;

public class ControllerIntegrationTest {

    private static DIRRequestDispatcher dir;
    private static TestEnvironment      testEnv;
    private static DIRConfig            dirConfig;
    private static RPC.UserCredentials  userCredentials;
    private static RPC.Auth             auth           = RPCAuthentication.authNone;
    private static DIRClient            dirClient;
    private static final int            NUMBER_OF_OSDS = 2;
    private static OSDConfig            osdConfigs[];
    private static OSD                  osds[];
    private static MRCRequestDispatcher mrc2;
    private static String               dirAddress;

    private ConfigBuilder               configBuilder;
    private Controller                  controller;
    private Client                      client;

    @BeforeClass
    public static void setUpClass() throws Exception {
        FSUtils.delTree(new java.io.File(SetupUtils.TEST_DIR));
        Logging.start(Logging.LEVEL_WARN, Logging.Category.tool);

        dirConfig = SetupUtils.createDIRConfig();
        osdConfigs = SetupUtils.createMultipleOSDConfigs(NUMBER_OF_OSDS);

        dir = new DIRRequestDispatcher(dirConfig, SetupUtils.createDIRdbsConfig());
        dir.startup();
        dir.waitForStartup();

        testEnv = new TestEnvironment(TestEnvironment.Services.DIR_CLIENT, TestEnvironment.Services.TIME_SYNC,
                TestEnvironment.Services.RPC_CLIENT, TestEnvironment.Services.MRC);
        testEnv.start();

        userCredentials = RPC.UserCredentials.newBuilder().setUsername("test").addGroups("test").build();

        dirClient = new DIRClient(new DIRServiceClient(testEnv.getRpcClient(), null),
                new InetSocketAddress[] { testEnv.getDIRAddress() }, 3, 1000);

        osds = new OSD[NUMBER_OF_OSDS];
        for (int i = 0; i < osds.length; i++) {
            osds[i] = new OSD(osdConfigs[i]);
        }
        dirAddress = testEnv.getDIRAddress().getHostName() + ":" + testEnv.getDIRAddress().getPort();
    }

    @Before
    public void setUp() throws Exception {
        configBuilder = new ConfigBuilder();
        configBuilder.setDirAddress(dirAddress);
        Options options = new Options();
        client = ClientFactory.createClient(dirAddress, userCredentials, null, options);
        client.start();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        for (int i = 0; i < osds.length; i++) {
            if (osds[i] != null) {
                osds[i].shutdown();
            }
        }

        if (mrc2 != null) {
            mrc2.shutdown();
            mrc2 = null;
        }

        testEnv.shutdown();
        dir.shutdown();
        dir.waitForShutdown();
    }

    @Test
    public void testSetupVolumes() throws Exception {
        controller = new Controller(configBuilder.build());

        controller.setupVolumes("TestVolA", "TestVolB", "TestVolC");

        List volumes = Arrays.asList(client.listVolumeNames());
        assertTrue(volumes.contains("TestVolA"));
        assertTrue(volumes.contains("TestVolB"));
        assertTrue(volumes.contains("TestVolC"));

        controller.teardown();
        volumes = Arrays.asList(client.listVolumeNames());
        assertEquals(0, volumes.size());
    }

    @Test
    public void testSequentialBenchmark() throws Exception {
        configBuilder.setNumberOfThreads(2);
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startSequentialWriteBenchmark();
        compareResults("SEQ_WRITE", 2, 10L * MiB_IN_BYTES, 2, results);
        results = controller.startSequentialReadBenchmark();
        compareResults("SEQ_READ", 2, 10L * MiB_IN_BYTES, 2, results);
        controller.teardown();
    }

    @Test
    public void testSequentialBenchmarkSeparatedRuns() throws Exception {
        configBuilder.setNumberOfThreads(2);
        configBuilder.setNoCleanup();
        Config config = configBuilder.build();
        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startSequentialWriteBenchmark();
        compareResults("SEQ_WRITE", 2, 10L * MiB_IN_BYTES, 2, results);
        controller.teardown();

        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        results = controller.startSequentialReadBenchmark();
        compareResults("SEQ_READ", 2, 10L * MiB_IN_BYTES, 2, results);
        controller.teardown();
        deleteVolumes("TestVolA", "TestVolB");
    }

    @Test
    public void testRandomBenchmark() throws Exception {
        configBuilder.setNumberOfThreads(2).setBasefileSizeInBytes(20L * MiB_IN_BYTES)
                .setRandomSizeInBytes(1L * MiB_IN_BYTES);
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startRandomWriteBenchmark();
        compareResults("RAND_WRITE", 2, 1L * MiB_IN_BYTES, 2, results);
        results = controller.startRandomReadBenchmark();
        compareResults("RAND_READ", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();
    }

    @Test
    public void testRandomBenchmarkSeparateRuns() throws Exception {
        configBuilder.setNumberOfThreads(2).setRandomSizeInBytes(1L * MiB_IN_BYTES)
                .setBasefileSizeInBytes(20L * MiB_IN_BYTES).setNoCleanup();
        Config config = configBuilder.build();

        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startRandomWriteBenchmark();
        compareResults("RAND_WRITE", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();

        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        results = controller.startRandomReadBenchmark();
        compareResults("RAND_READ", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();
        deleteVolumes("TestVolA", "TestVolB");
    }

    @Test
    public void testFilebasedBenchmark() throws Exception {
        configBuilder.setNumberOfThreads(2).setRandomSizeInBytes(1L * MiB_IN_BYTES);
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startFilebasedWriteBenchmark();
        compareResults("FILES_WRITE", 2, 1L * MiB_IN_BYTES, 2, results);
        results = controller.startFilebasedReadBenchmark();
        compareResults("FILES_READ", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();
    }

    @Test
    public void testFilebasedBenchmarkSeparateRuns() throws Exception {
        configBuilder.setNumberOfThreads(2).setRandomSizeInBytes(1L * MiB_IN_BYTES).setNoCleanup();
        Config config = configBuilder.build();

        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        Queue<BenchmarkResult> results = controller.startFilebasedWriteBenchmark();
        compareResults("FILES_WRITE", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();

        controller = new Controller(config);
        controller.setupVolumes("TestVolA", "TestVolB");
        results = controller.startFilebasedReadBenchmark();
        compareResults("FILES_READ", 2, 1L * MiB_IN_BYTES, 2, results);
        controller.teardown();
        deleteVolumes("TestVolA", "TestVolB");
    }

    @Test
    public void testConfigUser() throws Exception {
        configBuilder.setUserName("test");
        Volume volume = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        assertEquals("test", volume.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getUserId());
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigGroup() throws Exception {
        configBuilder.setGroup("test");
        Volume volume = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        assertEquals("test", volume.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getGroupId());
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigSeqSize() throws Exception {
        long seqSize = 2L * MiB_IN_BYTES;
        configBuilder.setSequentialSizeInBytes(seqSize);
        Volume volume = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        assertEquals(seqSize, volume.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getSize());
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigBasefileSize() throws Exception {
        long randSize = 1L * MiB_IN_BYTES;
        long basefileSize = 20L * MiB_IN_BYTES;
        configBuilder.setBasefileSizeInBytes(basefileSize).setRandomSizeInBytes(randSize);
        Volume volume = performBenchmark(configBuilder, BenchmarkType.RAND_WRITE);
        assertEquals(basefileSize, volume.getAttr(userCredentials, "benchmarks/basefile").getSize());
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigFilesSize() throws Exception {
        int fileSize = 8 * KiB_IN_BYTES;
        configBuilder.setFilesize(fileSize);
        Volume volume = performBenchmark(configBuilder, BenchmarkType.FILES_WRITE);
        int numberOfFiles = (MiB_IN_BYTES) / (8 * KiB_IN_BYTES);
        for (int i = 0; i < numberOfFiles; i++) {
            long fileSizeActual = volume.getAttr(userCredentials, "benchmarks/randomBenchmark/benchFile" + i).getSize();
            assertEquals(fileSize, fileSizeActual);
        }
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigStripeSize() throws Exception {
        int stripeSize = 64 * KiB_IN_BYTES;
        configBuilder.setStripeSizeInBytes(stripeSize);
        Volume volume = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        assertEquals(stripeSize, volume.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0")
                .getBlksize());
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigStripeWidth() throws Exception {
        configBuilder.setStripeWidth(2);
        Volume volume = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        String locations = volume.getXAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0",
                "xtreemfs.locations");
        assertTrue("Stripe Width not correct", locations.contains("\"width\":2"));
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigOSDSelectionPolicy() throws Exception {
        configBuilder.setOsdSelectionPolicies("1001,3003");
        Volume volumeA = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);
        assertEquals("1001,3003", volumeA.getOSDSelectionPolicy(userCredentials));
        deleteVolumes("TestVolA");
    }

    @Test
    public void testConfigOSDSelectionUUID() throws Exception {
        /* perform benchmark on osd "UUID:localhost:42640" */
        configBuilder.setSelectOsdsByUuid("UUID:localhost:42640");
        Volume volumeA = performBenchmark(configBuilder, BenchmarkType.SEQ_WRITE);

        /* perform benchmark on osd "UUID:localhost:42641" */
        configBuilder = new ConfigBuilder();
        configBuilder.setDirAddress(dirAddress);
        configBuilder.setSelectOsdsByUuid("UUID:localhost:42641").setNoCleanup();
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolB");
        controller.startSequentialWriteBenchmark();
        controller.teardown();
        Volume volumeB = client.openVolume("TestVolB", null, new Options());

        /* assert, that the benchmark files were created on the correct osd */
        assertEquals("1002", volumeA.getOSDSelectionPolicy(userCredentials));
        assertEquals("UUID:localhost:42640",
                volumeA.getSuitableOSDs(userCredentials, "benchmarks/sequentialBenchmark/benchFile0", 1).get(0));
        assertEquals("1002", volumeB.getOSDSelectionPolicy(userCredentials));
        assertEquals("UUID:localhost:42641",
                volumeB.getSuitableOSDs(userCredentials, "benchmarks/sequentialBenchmark/benchFile0", 1).get(0));
        deleteVolumes("TestVolA", "TestVolB");
    }

    /* The NoCleanup option is testet implicitly in all the above Config tests */

    @Test
    public void testConfigNoCleanupVolumes() throws Exception {
        configBuilder.setNoCleanupOfVolumes();
        configBuilder.setNumberOfThreads(3);
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA", "TestVolB", "TestVolC");
        controller.startSequentialWriteBenchmark();

        Volume volumeA = client.openVolume("TestVolA", null, new Options());
        Volume volumeB = client.openVolume("TestVolB", null, new Options());
        Volume volumeC = client.openVolume("TestVolC", null, new Options());

        /* the benchFiles are still there after the benchmark */
        long seqSize = 10L * BenchmarkUtils.MiB_IN_BYTES;
        assertEquals(seqSize, volumeA.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getSize());
        assertEquals(seqSize, volumeB.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getSize());
        assertEquals(seqSize, volumeC.getAttr(userCredentials, "benchmarks/sequentialBenchmark/benchFile0").getSize());

        controller.teardown();

        /*
         * after the teardown (which includes the deletion of the benchmark volumes and files), only the volumes are
         * present
         */
        assertEquals(0, Integer.valueOf(volumeA.getXAttr(userCredentials, "", "xtreemfs.num_files")));
        assertEquals(0, Integer.valueOf(volumeB.getXAttr(userCredentials, "", "xtreemfs.num_files")));
        assertEquals(0, Integer.valueOf(volumeC.getXAttr(userCredentials, "", "xtreemfs.num_files")));
        assertEquals(0, Integer.valueOf(volumeA.getXAttr(userCredentials, "", "xtreemfs.used_space")));
        assertEquals(0, Integer.valueOf(volumeB.getXAttr(userCredentials, "", "xtreemfs.used_space")));
        assertEquals(0, Integer.valueOf(volumeC.getXAttr(userCredentials, "", "xtreemfs.used_space")));
    }

    @Test
    public void testConfigNoCleanupBasefile() throws Exception {
        long basefileSize = 30L * BenchmarkUtils.MiB_IN_BYTES;
        long randSize = BenchmarkUtils.MiB_IN_BYTES;
        configBuilder.setRandomSizeInBytes(randSize).setNoCleanupOfBasefile().setBasefileSizeInBytes(basefileSize)
                .setNoCleanupOfVolumes();
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA");
        controller.startRandomWriteBenchmark();

        /* the filebased benchmark is used to show, that really files are (created and) deleted, except the basefile */
        controller.startFilebasedWriteBenchmark();

        Volume volume = client.openVolume("TestVolA", null, new Options());

        /* number of files from filebased benchmark + basefile */
        int numberOfFiles = (int) (randSize / (4 * BenchmarkUtils.KiB_IN_BYTES)) + 1;
        assertEquals(numberOfFiles, Integer.valueOf(volume.getXAttr(userCredentials, "", "xtreemfs.num_files")));
        assertEquals(basefileSize + randSize,
                Integer.valueOf(volume.getXAttr(userCredentials, "", "xtreemfs.used_space")));

        controller.teardown();

        /*
         * after the teardown (which includes the deletion of the benchmark volumes and files), only the basefile is
         * still present
         */
        assertEquals(basefileSize, volume.getAttr(userCredentials, "benchmarks/basefile").getSize());
        assertEquals(1, Integer.valueOf(volume.getXAttr(userCredentials, "", "xtreemfs.num_files")));
        assertEquals(basefileSize, Integer.valueOf(volume.getXAttr(userCredentials, "", "xtreemfs.used_space")));
    }

    private void compareResults(String type, int threads, long size, int numberOfResults, Queue<BenchmarkResult> results) {
        int resultCounter = 0;
        for (BenchmarkResult result : results) {
            resultCounter++;
            String benchmarkType = result.getBenchmarkType().toString();
            assertEquals(type, benchmarkType);
            assertEquals(threads, result.getNumberOfReadersOrWriters());
            assertEquals(size, result.getDataRequestedInBytes());
            assertEquals(size, result.getByteCount());
        }
        assertEquals(numberOfResults, resultCounter);
    }

    private Volume performBenchmark(ConfigBuilder configBuilder, BenchmarkType type) throws Exception {
        configBuilder.setNoCleanup();
        controller = new Controller(configBuilder.build());
        controller.setupVolumes("TestVolA");
        switch (type) {
        case SEQ_WRITE:
            controller.startSequentialWriteBenchmark();
            break;
        case RAND_WRITE:
            controller.startRandomWriteBenchmark();
            break;
        case FILES_WRITE:
            controller.startFilebasedWriteBenchmark();
            break;
        }
        controller.teardown();

        Volume volume = client.openVolume("TestVolA", null, new Options());
        return volume;
    }

    private void deleteVolumes(String... volumeNames) throws IOException {
        for (String volumeName : volumeNames) {
            client.deleteVolume(auth, userCredentials, volumeName);
        }
    }

    private void printResults(Queue<BenchmarkResult> results) {
        System.err.println("Type\t\t\tThreads\t\tTime\tSpeed\tRequested\t\tCount");
        for (BenchmarkResult res : results) {
            System.err.println(res.getBenchmarkType() + "\t\t" + res.getNumberOfReadersOrWriters() + "\t\t\t"
                    + res.getTimeInSec() + "\t" + res.getSpeedInMiBPerSec() + "\t" + res.getDataRequestedInBytes()
                    + "\t\t" + res.getByteCount());
        }
    }

}
