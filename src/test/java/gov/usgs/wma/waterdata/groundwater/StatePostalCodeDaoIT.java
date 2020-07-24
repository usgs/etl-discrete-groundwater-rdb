package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
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
@DbUnitConfiguration(dataSetLoader=FileSensingDataSetLoader.class)
@AutoConfigureTestDatabase(replace=Replace.NONE)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
@Import({DBTestConfig.class})
@DirtiesContext
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE,
classes={
		DBTestConfig.class,
		StatePostCodeDao.class})
@ActiveProfiles("it")
public class StatePostalCodeDaoIT {

	@Autowired
	protected StatePostCodeDao dao;

	@BeforeEach
	public void beforeEach() {
	}

	@DatabaseSetup("classpath:/testStateData/")
	@Test
	public void testStatePostalCode() throws Exception {
		String actual = dao.getPostCode("California");

		assertEquals("CA", actual);
	}

	@DatabaseSetup("classpath:/testStateData/")
	@Test
	public void testStatePostalCode_notFound() throws Exception {
		String actual = dao.getPostCode("Unknown State of Things");

		assertEquals("", actual);
	}

	@Test
	public void testPostalCode_handleIOE() throws Exception {
		// SETUP
		Resource mockSQL = Mockito.mock(Resource.class);
		Mockito.when(mockSQL.getInputStream()).thenThrow(new IOException());
		dao.selectQuery = mockSQL;

		// ACTION UNDER TEST
		// ASSERTION
		assertThrows(RuntimeException.class, ()->dao.getPostCode("Bad State of Things"));
	}
}
