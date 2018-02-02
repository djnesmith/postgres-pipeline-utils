import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

import com.ableton.DockerMock
import com.ableton.JenkinsMocks
import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


@SuppressWarnings('ClassName')
class postgresTest extends BasePipelineTest {
  @SuppressWarnings('FieldTypeRequired')
  def postgres

  @Override
  @Before
  void setUp() {
    super.setUp()
    this.postgres = new postgres()

    postgres.with {
      docker = new DockerMock()
      env = [
        BUILD_ID: '1',
        JOB_BASE_NAME: 'TestJob',
      ]

      error = JenkinsMocks.error
      pwd = JenkinsMocks.pwd
      retry = JenkinsMocks.retry
      sh = JenkinsMocks.sh
      writeFile = {}
    }
  }

  @Test
  void withDb() throws Exception {
    String dataDir = JenkinsMocks.pwd(temp: true) + '/1/postgres/data'
    JenkinsMocks.addShMock('id -u', '1000', 0)
    JenkinsMocks.addShMock("mkdir ${dataDir}", '', 0)
    JenkinsMocks.addShMock("pg_isready -h \$DB_PORT_5432_TCP_ADDR", '', 0)

    boolean bodyExecuted = false
    int bodyResult = postgres.withDb('testdb', '9.6') {
      bodyExecuted = true
      return 123
    }

    assertTrue(bodyExecuted)
    assertEquals(123, bodyResult)
  }

  @Test
  void withDbContainerFail() throws Exception {
    String dataDir = JenkinsMocks.pwd(temp: true) + '/1/postgres/data'
    JenkinsMocks.addShMock('id -u', '1000', 0)
    JenkinsMocks.addShMock("mkdir ${dataDir}", '', 0)
    JenkinsMocks.addShMock("pg_isready -h \$DB_PORT_5432_TCP_ADDR", '', 1)

    boolean bodyExecuted = false
    Object bodyResult = null
    boolean exceptionThrown = false
    try {
      bodyResult = postgres.withDb('testdb', '9.6') {
        bodyExecuted = true
        return 123
      }
    } catch (error) {
      exceptionThrown = true
      assertNotNull(error)
    }

    assertTrue(exceptionThrown)
    assertFalse(bodyExecuted)
    assertNull(bodyResult)
  }
}
