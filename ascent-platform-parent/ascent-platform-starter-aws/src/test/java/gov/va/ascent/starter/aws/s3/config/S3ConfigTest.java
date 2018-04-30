package gov.va.ascent.starter.aws.s3.config;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.va.ascent.framework.config.AscentCommonSpringProfiles;

@RunWith(MockitoJUnitRunner.class)
public class S3ConfigTest {
	
	private static final String TEST_AWS_ID = "test-id";
	private static final String TEST_AWS_KEY = "test-key";
	private static final String TEST_AWS_REGION = "us-east-1";
	private static final String TEST_END_POINT = "http://localhost:4572/evsstestbucket/";

    @InjectMocks
	S3Config s3Config = new S3Config();
	
    @Mock
    private Environment environment;
    
	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(s3Config, "awsId", TEST_AWS_ID);
		ReflectionTestUtils.setField(s3Config, "awsKey", TEST_AWS_KEY);
		ReflectionTestUtils.setField(s3Config, "region", TEST_AWS_REGION);
		ReflectionTestUtils.setField(s3Config, "endpoint", TEST_END_POINT);
		
        final Logger logger = (Logger) LoggerFactory.getLogger(S3Config.class);
        logger.setLevel(Level.DEBUG);
	}
	
	@Test
	public void testFields() throws Exception {
		Assert.assertEquals(TEST_AWS_ID, FieldUtils.readField(s3Config, "awsId", true));
		Assert.assertEquals(TEST_AWS_KEY, FieldUtils.readField(s3Config, "awsKey", true));
		Assert.assertEquals(TEST_AWS_REGION, FieldUtils.readField(s3Config, "region", true));
	}
	
	@Test
	public void testS3Client() throws Exception {
        String[] profiles = { AscentCommonSpringProfiles.PROFILE_EMBEDDED_AWS };
		when(environment.getActiveProfiles()).thenReturn(profiles);
		AmazonS3 amazonS3 = s3Config.s3client();
		assertNotNull(amazonS3);
	}
	
	@Test
	public void testS3Client_localIntProfile() throws Exception {
        String[] profiles = { AscentCommonSpringProfiles.PROFILE_ENV_LOCAL_INT };
		when(environment.getActiveProfiles()).thenReturn(profiles);
		AmazonS3 amazonS3 = s3Config.s3client();
		assertNotNull(amazonS3);
	}
	
	@Test
	public void testS3TransferManager() throws Exception {
        String[] profiles = { AscentCommonSpringProfiles.PROFILE_EMBEDDED_AWS };
		when(environment.getActiveProfiles()).thenReturn(profiles);
		TransferManager amazonS3TransferManager = s3Config.transferManager();
		assertNotNull(amazonS3TransferManager);
	}
}