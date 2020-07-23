package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.LinkedList;

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
		// SETUP
		out = Mockito.mock(ByteArrayOutputStream.class);
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		// ASSERT closed
		Mockito.verify(out, Mockito.atLeastOnce()).close();
	}

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_single_California() throws Exception {
		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		// ASSERT row count
		assertEquals(14, writer.getDataRows());
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_single_Texas() throws Exception {
		// SETUP
		states = List.of("Texas");

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		// ASSERT row count
		assertEquals(2, writer.getDataRows());
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_multiple() throws Exception {
		// SETUP
		states = List.of("California", "Texas");

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		// ASSERT row count
		assertEquals(16, writer.getDataRows());
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_stateOrdered() throws Exception {
		// SETUP
		states = List.of("California", "Texas");

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);
		writer.close();

		// POST SETUP
		LinkedList<String> rdbLines = new LinkedList<>();
		rdbLines.addAll(
				Arrays.stream( out.toString().split("\\n") )
				.collect( Collectors.toList() ));

		// The rows should be ordered by state first.
		// Texas is after California and these two Texas sites should be last.
		// and the order of the sites should be ordered secondarily.

		// ASSERT sort
		String siteNoLast = rdbLines.removeLast().split("\\t")[1];
		assertEquals("285634095344401", siteNoLast);
		String siteNoNext = rdbLines.removeLast().split("\\t")[1];
		assertEquals("285634095174701", siteNoNext);
	}
}
