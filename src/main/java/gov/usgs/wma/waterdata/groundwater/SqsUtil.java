package gov.usgs.wma.waterdata.groundwater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Manager class for SQS queue actions.
 */
@Component
public class SqsUtil {
	private static final String QUEUE_BASE_NAME = "aqts-capture-etl-rdb-log-";
	private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

	@Autowired
	private Properties properties;

	SqsUtil(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Simple helper method to send a message to the etl discrete groundwater rdb
	 * SQS log queue.
	 *
	 * @param mess The message to place in the SQS queue.
	 */
	public void addSQSMessage(String mess) {
		SendMessageRequest send_msg_request = new SendMessageRequest().withQueueUrl(getQueueUrl())
				.withMessageBody(mess);
		sqs.sendMessage(send_msg_request);
	}

	private String getQueueUrl() {
		String queueName = QUEUE_BASE_NAME + properties.getTier();
		String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
		return queueUrl;
	}

}
