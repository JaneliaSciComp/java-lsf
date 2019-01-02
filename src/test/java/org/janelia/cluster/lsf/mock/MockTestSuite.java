package org.janelia.cluster.lsf.mock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MockJobManagerTests.class,
  MockLsfTests.class,
  ParseTests.class,
  LsfUtilsTests.class
})
public class MockTestSuite {

}
