package gov.usgs.wma.waterdata.groundwater;

import java.util.Collection;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ServiceException;

public class InvokeAll {
	final String ARN="arn:aws:lambda:us-west-2:_ACCOUNT_:function:etl-discrete-groundwater-rdb-_TIER_-loadRdb";

	public ResultObject invoke(Properties properties, Collection<String> folders) {

		try {
			AWSLambda awsLambda = lambdaContext();

			String arn = ARN.replace("_ACCOUNT_", properties.getAccount())
					.replace("_TIER_", properties.getTier());

			int count = 0;
			for (String folder : folders) {
				InvokeRequest invokeRequest = new InvokeRequest()
						.withFunctionName(arn)
						.withInvocationType(InvocationType.Event)
						.withPayload("{\n"
								+" \"locationFolder\": \"_folder_\"".replace("_folder", folder)
								+"}");

				invokeAsync(awsLambda, folder, invokeRequest);
				count++;
			}

			ResultObject result = new ResultObject();
			result.setCount(count);
			result.setFilename("N/A, count is location folders processed");
			return result;
		} catch (ServiceException e) {
			throw new RuntimeException("Error aquiring AWSLambda client.", e);
		}
	}

	protected void invokeAsync(AWSLambda awsLambda, String forFolder, InvokeRequest invokeRequest) {
		try {
			awsLambda.invoke(invokeRequest);
		} catch (ServiceException e) {
			// do we want to throw, or try the next
			throw new RuntimeException("Error invoking lambda: "
					+ invokeRequest.getFunctionName() + " for " + forFolder, e);
		}
	}

	protected AWSLambda lambdaContext() {
		AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
				.withCredentials(new ProfileCredentialsProvider())
				.withRegion(Regions.US_WEST_2).build();
		return awsLambda;
	}
}
