package gov.usgs.wma.waterdata.groundwater;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class,
	TransactionDbUnitTestExecutionListener.class })
@DbUnitConfiguration(
		dataSetLoader=FileSensingDataSetLoader.class)
@AutoConfigureTestDatabase(replace=Replace.NONE)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
@Import({DBTestConfig.class})
@DirtiesContext
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE,
classes={
		DBTestConfig.class,
		DiscreteGroundWaterDao.class})
@ActiveProfiles("it")
public class DiscreteGroundWaterDaoIT {

	@Autowired
	protected DiscreteGroundWaterDao dao;
	protected List<String> states;
	protected RdbWriter writer;
	protected OutputStream out;
	protected Writer dest;
	protected RequestObject request;

	@BeforeEach
	public void beforeEach() {
		request = new RequestObject();
		states = List.of("Maine");
		out = new ByteArrayOutputStream();
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);
	}

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater() {
		// get new data, return list of discrete gw objects
		dao.sendDiscreteGroundWater(states, writer);

		// TODO some assertions
	}

}
