package gov.va.ascent.starter.aws.s3.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.va.ascent.starter.aws.s3.services.S3Service;

@RunWith(MockitoJUnitRunner.class)
public class S3ServiceImplTest {

	private static final String TEST_REGION = "test-region";
	private static final String TEST_BUCKET_NAME = "test-bucket";
	private static final String TEST_TARGET_BUCKET = "test-target-bucket";
	private static final String TEST_DLQ_BUCKET = "test-dlq-bucket";

	@Autowired
	@InjectMocks
	private final S3Service s3Service = new S3ServiceImpl();

	@Mock
	private AmazonS3 mockS3Client;

	@Mock
	S3Object mockS3Object;

	@Mock
	protected TransferManager transferManager;

	@Mock
	private ResourceLoader resourceLoader;

	@Before
	public void setUp() throws Exception {
		final Logger logger = (Logger) LoggerFactory.getLogger(S3ServiceImpl.class);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public void testFields() throws Exception {
		Assert.assertEquals(mockS3Client, FieldUtils.readField(s3Service, "s3client", true));
	}

	@Test
	public void testDownloadFile() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();

		prepareS3Mock(bucketList);
		final ResponseEntity<byte[]> bytesArray = s3Service.downloadFile(TEST_BUCKET_NAME, "TEST-KEY");
		assertNotNull(bytesArray);
	}

	private List<Bucket> prepareBucketList() {
		final List<Bucket> bucketList = new ArrayList<Bucket>();
		bucketList.add(new Bucket(TEST_BUCKET_NAME));
		bucketList.add(new Bucket(TEST_TARGET_BUCKET));
		bucketList.add(new Bucket(TEST_DLQ_BUCKET));
		return bucketList;
	}

	private void prepareS3Mock(final List<Bucket> bucketList) throws Exception {
		Mockito.when(mockS3Client.listBuckets()).thenReturn(bucketList);
		Mockito.when(mockS3Client.getRegionName()).thenReturn(TEST_REGION);
		Mockito.when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
		when(mockS3Object.getObjectContent())
				.thenReturn(new S3ObjectInputStream(new ByteArrayInputStream("testString".getBytes()), null));
	}

	@Test
	public void testUploadMultiPart() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);

		final MockMultipartFile[] multipartFiles = new MockMultipartFile[2];
		final MockMultipartFile mockFileOne = spy(new MockMultipartFile("data", "filename.txt", "text/plain", "one xml".getBytes()));
		final MockMultipartFile mockFileTwo = new MockMultipartFile("data", "filename.txt", "text/plain", "two xml".getBytes());

		multipartFiles[0] = mockFileOne;
		multipartFiles[1] = mockFileTwo;

		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenReturn(new UploadResult());
		ResponseEntity<List<UploadResult>> response = s3Service.uploadMultiPartFiles(TEST_BUCKET_NAME, multipartFiles);
		assertEquals(200, response.getStatusCodeValue());

		doThrow(new IOException("Testing")).when(mockFileOne).getInputStream();
		try {
			response = s3Service.uploadMultiPartFiles(TEST_BUCKET_NAME, multipartFiles);
			assertTrue(response.getBody().size() == 1);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Exception should not have got thrown to here.");
		}
	}

	@Test
	public void testUploadMultiPartSingle() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		final Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("documentName", "Sample Upload File");
		final MockMultipartFile mockFile = spy(new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes()));
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenReturn(new UploadResult());
		ResponseEntity<UploadResult> response = s3Service.uploadMultiPartFile(TEST_BUCKET_NAME, mockFile, propertyMap);
		assertEquals(200, response.getStatusCodeValue());

		doThrow(new IOException("Testing")).when(mockFile).getInputStream();
		try {
			response = s3Service.uploadMultiPartFile(TEST_BUCKET_NAME, mockFile, propertyMap);
			assertNull(response.getBody());
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Exception should not have got thrown to here.");
		}
	}

	@Test
	public void testUploadByteArray() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		final Map<String, String> propertyMap = new HashMap<>();
		propertyMap.put("documentName", "Sample Upload File");
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenReturn(new UploadResult());
		final ResponseEntity<UploadResult> bytesArray =
				s3Service.uploadByteArray(TEST_BUCKET_NAME, "some xml".getBytes(), "filename.txt", propertyMap);
		assertEquals(200, bytesArray.getStatusCodeValue());
	}

	@Test
	public void testUploadMultiPartSingle_AmazonServiceException() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		final Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("documentName", "Sample Upload File");
		final MockMultipartFile mockFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenThrow(new AmazonServiceException("Error occurred"));
		final ResponseEntity<UploadResult> bytesArray = s3Service.uploadMultiPartFile(TEST_BUCKET_NAME, mockFile, propertyMap);
		assertEquals(200, bytesArray.getStatusCodeValue());
	}

	@Test
	public void testUploadMultiPartSingle_AmazonClientException() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		final Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("documentName", "Sample Upload File");
		final MockMultipartFile mockFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenThrow(new AmazonClientException("Error occurred"));
		final ResponseEntity<UploadResult> bytesArray = s3Service.uploadMultiPartFile(TEST_BUCKET_NAME, mockFile, propertyMap);
		assertEquals(200, bytesArray.getStatusCodeValue());
	}

	@Test
	public void testUploadMultiPartSingle_IOException() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		final Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("documentName", "Sample Upload File");
		final MockMultipartFile mockFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenThrow(new InterruptedException());
		// doThrow(IOException.class).when(upload).waitForUploadResult();
		final ResponseEntity<UploadResult> bytesArray = s3Service.uploadMultiPartFile(TEST_BUCKET_NAME, mockFile, propertyMap);
		assertEquals(200, bytesArray.getStatusCodeValue());
	}

	@Test
	public void testuploadFile() throws Exception {
		final Upload upload = mock(Upload.class);
		when(transferManager.upload(any())).thenReturn(upload);
		when(upload.waitForUploadResult()).thenReturn(new UploadResult());
		final Resource mockResource = mock(Resource.class);
		when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
		ResponseEntity<UploadResult> response = s3Service.uploadFile(TEST_BUCKET_NAME, "testFile.txt", "testFile.txt");
		assertEquals(200, response.getStatusCodeValue());

		when(mockResource.getInputStream()).thenThrow(new IOException("Testing"));
		try {
			response = s3Service.uploadFile(TEST_BUCKET_NAME, "testFile.txt", "testFile.txt");
			assertNull(response.getBody().getBucketName());
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Should not have thrown exception to here.");
		}
	}

	@Test
	public void testCopyFileFromSourceToTargetBucket() throws Exception {
		final List<Bucket> bucketList = prepareBucketList();
		prepareS3Mock(bucketList);
		when(mockS3Client.copyObject(anyObject())).thenReturn(mock(CopyObjectResult.class));
		s3Service.copyFileFromSourceToTargetBucket(TEST_BUCKET_NAME, TEST_TARGET_BUCKET, "testFile.txt");
	}

	@Test
	public void testCopyFileFromSourceToTargetBucket_AmazonServiceException() {
		when(mockS3Client.copyObject(anyObject())).thenThrow(new AmazonServiceException("Error occurred"));
		s3Service.copyFileFromSourceToTargetBucket(TEST_BUCKET_NAME, TEST_TARGET_BUCKET, "testFile.txt");
	}

	@Test
	public void testCopyFileFromSourceToTargetBucket_AmazonClientException() {
		when(mockS3Client.copyObject(anyObject())).thenThrow(new AmazonClientException("Error occurred"));
		s3Service.copyFileFromSourceToTargetBucket(TEST_BUCKET_NAME, TEST_TARGET_BUCKET, "testFile.txt");
	}

	@Test
	public void testMoveMessageToS3() {
		s3Service.moveMessageToS3(TEST_BUCKET_NAME, "key", "messageBody");
	}
}
