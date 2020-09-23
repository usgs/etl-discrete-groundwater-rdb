package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class,
	TransactionDbUnitTestExecutionListener.class })
@DbUnitConfiguration(databaseConnection={"observation","transform"},dataSetLoader=FileSensingDataSetLoader.class)
@AutoConfigureTestDatabase(replace=Replace.NONE)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
@Import({DBTestConfig.class})
@DirtiesContext
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE,
classes={
		DBTestConfig.class,
		AqToNwisParmDao.class})
@ActiveProfiles("it")
public class AqToNwisParmDaoIT {

	@Autowired
	protected AqToNwisParmDao dao;

	@DatabaseSetup(connection="transform",
			value="classpath:/testDataTransform/")
	@Test
	public void testStatePostalCode() {
		List<Parameter> parameters = dao.getParameters();
        assertEquals(18, parameters.size());

	}

	@Test
	public void testPostalCode_handleIOE() throws Exception {
		// SETUP
		Resource mockSQL = Mockito.mock(Resource.class);
		Mockito.when(mockSQL.getInputStream()).thenThrow(new IOException());
		dao.selectQuery = mockSQL;

		// ACTION UNDER TEST
		// ASSERTION
		// TODO fix this
		assertThrows(RuntimeException.class, ()->dao.getParameters());
	}
}
