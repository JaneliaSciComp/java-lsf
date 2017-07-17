package org.janelia.cluster.lsf.mock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MockJobManagerTests.class,
  MockLsfTests.class
})
public class MockTestSuite {

}
