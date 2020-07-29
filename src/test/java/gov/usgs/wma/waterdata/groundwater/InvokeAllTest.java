package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;

class InvokeAllTest {

	Properties properties;
	List<String> folders;
	AWSLambda mockAws;

	int callCount;
	String arn;

	@BeforeEach
	public void before() {

		properties = new Properties();
		properties.setAccount("JUNIT-ACCOUNT");
		properties.setRegion("JUNIT-WEST");
		properties.setBucket("JUNIT-S3");
		properties.setTier("JUNIT-TEST"); // TEST, QA, and PROD-INTERNAL

		folders = Lists.list("California", "Wisconsin");
		callCount = 0;
		mockAws = Mockito.mock(AWSLambda.class);
	}

	@Test
	void testHappyPath() {
		// SETUP
		InvokeAll invoker = new InvokeAll() {
			@Override
			protected void invokeAsync(AWSLambda awsLambda, String forFolder, InvokeRequest invokeRequest) {
				callCount++;
				arn = invokeRequest.getFunctionName();
			}
			@Override
			protected AWSLambda lambdaContext() {
				return mockAws;
			}
		};

		// ACTION UNDER TEST
		ResultObject result = invoker.invoke(properties, folders);

		// ASSERTIONS
		// ensure calls per folder
		assertEquals(folders.size(), result.getCount());
		assertEquals(folders.size(), callCount);

		// ensure arn value injection
		assertTrue(arn.contains(properties.getAccount()));
		assertTrue(arn.contains(properties.getTier()));
		assertFalse(arn.contains("_ACCOUNT_"));
		assertFalse(arn.contains("_TIER_"));
		assertFalse(arn.contains("_"));

		//		Mockito.verify(mockAws, Mockito.atLeast(2)).invoke(Mockito.any());
	}

}
