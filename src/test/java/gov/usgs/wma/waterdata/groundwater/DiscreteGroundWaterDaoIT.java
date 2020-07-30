package gov.usgs.wma.waterdata.groundwater;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.StringUtils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_close() throws Exception {
		// SETUP
		out = Mockito.mock(ByteArrayOutputStream.class);
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest);

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);

		// POST SETUP
		dest.close();

		// ASSERT closed
		Mockito.verify(out, Mockito.atLeastOnce()).close();
	}

	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_rowCount_single_California() throws Exception {
		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);

		// POST SETUP
		dest.close();

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

		// POST SETUP
		dest.close();

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

		// POST SETUP
		dest.close();

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

		// POST SETUP
		dest.close();

		// POST SETUP
		LinkedList<String> rdbLines = new LinkedList<>();
		rdbLines.addAll(
				Arrays.stream( out.toString().split("\\n") )
				.collect( toList() ));

		// The rows should be ordered by state first.
		// Texas is after California and these two Texas sites should be last.
		// and the order of the sites should be ordered secondarily.

		// ASSERT state sort
		String siteNoLast = rdbLines.removeLast().split("\\t")[1];
		assertEquals("285634095344401", siteNoLast);
		String siteNoNext = rdbLines.removeLast().split("\\t")[1];
		assertEquals("285634095174701", siteNoNext);
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_dateOrder() throws Exception {
		// SETUP
		final String BAD_DATE = "999999999999";
		states = List.of("California", "Texas");

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);

		// POST SETUP
		dest.close();

		// POST SETUP
		String outlines = out.toString();

		// this is a injection of a bad date order to ensure negative result is caught
		//outlines = outlines.replace("19980604", "20000000");

		Map<String, List<String>> rdbLines = Arrays
				.stream( outlines.split("\\n") )
				.collect( groupingBy(line->line.split("\\t")[1],
						mapping(line->{
							String[] cols =line.split("\\t");
							return cols[2]+(isEmpty(cols[3])?"1200":cols[3]);
						}
						,toList())) );
		// display stream mapped sites with datetimes
		//		rdbLines.keySet()
		//		.stream()
		//		.forEach(site->{
		//			System.out.println();
		//			System.out.println(site);
		//			rdbLines.get(site)
		//			.stream()
		//			.forEachOrdered(System.out::println);
		//		});

		// reduce datetimes list to the max date of each site
		// checks that each date is greater or equal to the next
		// return all 9s if out of order
		Map<String, String> siteDateReduce = rdbLines.entrySet()
				.stream()
				.collect(
						toMap(
								Map.Entry::getKey,
								e->e.getValue()
								.stream()
								.reduce("0", (last, date) -> (date.compareTo(last)>=0)?date:BAD_DATE)
								)
						);
		// display results of analysis of date order
		//		siteDateReduce.entrySet()
		//		.stream()
		//		.forEach(System.out::println);

		// ASSERT date sort
		// The site rows should be ordered by date time.
		// checks all sites for out of order dates by asserting not all 9s
		for (String site : siteDateReduce.keySet()) {
			assertNotEquals(BAD_DATE, siteDateReduce.get(site),
					site + " site dates are out of sort order");
		}
		// I suspected this and was watching for it, assertion exceptions are
		// not detected in stream lambdas. The above loop is in the local scope
		// while streams are another. It hides failures.
		// siteDateReduce.entrySet()
		//		.stream()
		//		.forEach(e->assertNotEquals(BAD_DATE, e.getKey()));
	}
	@DatabaseSetup("classpath:/testData/")
	@Test
	public void testSendDiscreteGroundWater_byLandOrBySea() throws Exception {
		// SETUP
		states = List.of("California", "Texas");

		// ACTION UNDER TEST
		dao.sendDiscreteGroundWater(states, writer);

		// POST SETUP
		dest.close();
		String outlines = out.toString();
		Map<String, List<String>> rdbLines = Arrays
				.stream( outlines.split("\\n") )
				.collect( groupingBy(line->line.split("\\t")[1]) );

		// ASSERT measure direction

		// no below land and S indicator
		String lineA = rdbLines.get("335504116544201").get(0);
		assertTrue( Pattern.compile("^.+\t\t\tS\tNGVD29\t2180\t.+$").matcher(lineA).matches() );
		String lineB = rdbLines.get("335504116544201").get(1);
		assertTrue( Pattern.compile("^.+\t\t\tS\tNGVD29\t2184\t.+$").matcher(lineB).matches() );

		// no above datum and L indicator
		String lineC = rdbLines.get("335504116544201").get(2);
		assertTrue( Pattern.compile("^.+\t246.0\tL\t\t\t.+$").matcher(lineC).matches() );
	}

	@Test
	public void testSendDiscreteGroundWater_handleIOE() throws Exception {
		// SETUP
		Resource mockSQL = Mockito.mock(Resource.class);
		Mockito.when(mockSQL.getInputStream()).thenThrow(new IOException());
		dao.selectQuery = mockSQL;

		// ACTION UNDER TEST
		// ASSERTION
		assertThrows(RuntimeException.class, ()->dao.sendDiscreteGroundWater(states, writer));
	}
}
