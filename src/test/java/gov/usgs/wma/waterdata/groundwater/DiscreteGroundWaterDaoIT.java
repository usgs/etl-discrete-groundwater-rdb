package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
		DiscreteGroundWaterDao.class})
@ActiveProfiles("it")
public class DiscreteGroundWaterDaoIT {

	@Autowired
	protected DiscreteGroundWaterDao dao;
	protected List<String> states;
	protected RdbWriter writer;
	protected OutputStream out;
	protected Writer dest;

	@BeforeEach
	public void beforeEach() {
		states = List.of("California");
		out = new ByteArrayOutputStream();
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);
	}

	/*


	TODO BuildRdbFile closes the out not the DAO

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater() throws Exception {

		out = Mockito.mock(ByteArrayOutputStream.class);
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);

		// get new data, return list of discrete gw objects
		dao.sendDiscreteGroundWater(states, writer);

		Mockito.verify(out, Mockito.atLeastOnce()).close();
	}


	TODO also the DAO does not write the header
	assertEquals(4, writer.getHeaderRows());

	 */
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_close() throws Exception {
		out = Mockito.mock(ByteArrayOutputStream.class);
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);

		// get new data, return list of discrete gw objects
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		Mockito.verify(out, Mockito.atLeastOnce()).close();
	}

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_single_California() throws Exception {
		//get new data, return list of discrete gw objects
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		assertEquals(14, writer.getDataRows());
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_single_Texas() throws Exception {
		//get new data, return list of discrete gw objects
		states = List.of("Texas");
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		assertEquals(2, writer.getDataRows());
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_multiple() throws Exception {
		//get new data, return list of discrete gw objects
		states = List.of("California", "Texas");
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		assertEquals(16, writer.getDataRows());
	}
}
