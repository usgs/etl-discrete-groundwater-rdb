package gov.usgs.wma.waterdata.groundwater;


import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ServiceException;

public class InvokeAll {
	public static final Logger logger = LoggerFactory.getLogger(InvokeAll.class);

	public ResultObject invoke(Properties properties, Collection<String> folders) {

		try {
			AWSLambda awsLambda = lambdaContext(properties.getRegion());

			int count = 0;
			for (String folder : folders) {
				InvokeRequest invokeRequest = new InvokeRequest()
						.withFunctionName(properties.getArn())
						.withInvocationType(InvocationType.Event)
						.withPayload("{\n"
								+" \"locationFolder\": \""+folder+"\""
								+"}");

				invokeAsync(awsLambda, folder, invokeRequest);
				count++;
			}

			ResultObject result = new ResultObject();
			result.setCount(count);
			result.setMessage("Count is location folder processes submitted.");
			return result;
		} catch (ServiceException e) {
			throw new RuntimeException("Error aquiring AWSLambda client.", e);
		}
	}

	/**
	 * Helper method for test invoke injection
	 * @param awsLambda AWS Lambda context instance
	 * @param forFolder location folder for this invocation
	 * @param invokeRequest response instance from AWS
	 */
	protected void invokeAsync(AWSLambda awsLambda, String forFolder, InvokeRequest invokeRequest) {
		try {
			awsLambda.invoke(invokeRequest);
		} catch (ServiceException e) {
			// do we want to throw, or try the next
			throw new RuntimeException("Error invoking lambda for " + forFolder
					+ " and request " + invokeRequest.toString(), e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Error invoking lambda for " + forFolder
					+ " and request " + invokeRequest.toString(), e);
		}
	}

	/**
	 * Helper method for AWS Lambda context instance injection tests.
	 * @param region the AWS region; typically us-west-2
	 * @return AWS Lambda Context
	 */
	protected AWSLambda lambdaContext(String region) {
		return AWSLambdaClientBuilder.standard().withRegion(region).build();
	}
}
