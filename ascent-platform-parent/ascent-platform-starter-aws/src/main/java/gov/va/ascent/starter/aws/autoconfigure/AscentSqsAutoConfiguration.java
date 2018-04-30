package gov.va.ascent.starter.aws.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import gov.va.ascent.starter.aws.server.AscentEmbeddedAwsLocalstackApplication;
import gov.va.ascent.starter.aws.sqs.config.AbstractSqsConfiguration;
import gov.va.ascent.starter.aws.sqs.config.SqsProperties;
import gov.va.ascent.starter.aws.sqs.config.StandardSqsConfiguration;
import gov.va.ascent.starter.aws.sqs.services.SqsService;
import gov.va.ascent.starter.aws.sqs.services.impl.SqsServiceImpl;
/**
 * Created by akulkarni on 2/1/18.
 */

@Configuration
@EnableConfigurationProperties(SqsProperties.class)
@Import({AbstractSqsConfiguration.class, StandardSqsConfiguration.class})
public class AscentSqsAutoConfiguration {
	
	@Autowired(required=false)
	AscentEmbeddedAwsLocalstackApplication ascentEmbeddedAwsLocalstack;
	
	@Bean
	@ConditionalOnMissingBean
	public SqsService sqsService(){
		return new SqsServiceImpl();
	}
	
}

