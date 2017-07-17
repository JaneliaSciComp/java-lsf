package org.janelia.cluster.lsf;

import org.janelia.cluster.lsf.mock.MockJobManagerTests;
import org.janelia.cluster.lsf.mock.MockLsfTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MockJobManagerTests.class,
  MockLsfTests.class,
  LsfTests.class
})
public class FullTestSuite {

}
