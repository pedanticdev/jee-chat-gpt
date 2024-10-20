package fish.payara;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

@ApplicationScoped
public class S3BucketManager {
    public static final String DEFAULT_REGION = "default";
    public static final String S3_SCHEME = "s3x://";
    private static final Logger LOG = Logger.getLogger(S3BucketManager.class.getName());

    @Inject
    @ConfigProperty(name = "S3_BUCKET_ACCESS_KEY")
    String accessKey;

    @Inject
    @ConfigProperty(name = "S3_BUCKET_SECRET_KEY")
    String secretKey;

    @Inject
    @ConfigProperty(name = "s3.endpoint-override")
    String contaboUrl;

    @Inject
    @ConfigProperty(name = "s3.new-embedding-files.bucket")
    String newEmbeddingFileBucket;

    @Inject
    @ConfigProperty(name = "s3.embedded-files.bucket")
    String embeddedFileBucket;

    S3Client s3client;

    @PostConstruct
    void init() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        s3client =
                S3Client.builder()
                        .region(Region.of(DEFAULT_REGION))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                        .endpointOverride(URI.create(contaboUrl))
                        .serviceConfiguration(
                                S3Configuration.builder().pathStyleAccessEnabled(true).build())
                        .build();
    }

    public List<String> listNewObjects() {
        return listObjectKeys(newEmbeddingFileBucket);
    }

    public List<String> listEmbeddedObjects() {
        return listObjectKeys(embeddedFileBucket);
    }

    private List<String> listObjectKeys(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();

        List<String> keys = new ArrayList<>();
        ListObjectsV2Response response;
        do {
            response = s3client.listObjectsV2(request);
            for (S3Object s3Object : response.contents()) {
                keys.add(s3Object.key());
            }

            request =
                    ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .continuationToken(response.nextContinuationToken())
                            .build();

        } while (Boolean.TRUE.equals(response.isTruncated()));

        return keys;
    }

    private GetObjectRequest buildGetRequest(String objectKey) {
        return GetObjectRequest.builder().bucket(newEmbeddingFileBucket).key(objectKey).build();
    }

    public List<Path> getNewFilePaths() {
        List<Path> newFilePaths = new ArrayList<>();
        List<String> listedObjectKeys = listNewObjects();
        listedObjectKeys.forEach(
                s -> {
                    URI uri = URI.create(S3_SCHEME + newEmbeddingFileBucket + "/" + s);
                    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                        newFilePaths.add(fileSystem.getPath(uri.getPath()));
                    } catch (IOException e) {
                        // Handle exceptions
                    }
                });

        return newFilePaths;
    }

    public Path getNewFilePath(String objectKey) {
        // URI uri = URI
        // .create(S3_SCHEME + accessKey + ":" + secretKey + "@" + "usc1.contabostorage.com" + "/" +
        // newEmbeddingFileBucket + "/" + objectKey);
        // log.log(Level.INFO, "Attempting to mount filesystem at {0}", uri);
        // try {
        // return Paths.get(newEmbeddingFileBucket, objectKey);
        // } catch (Exception e) {
        //
        // }
        //
        // return null;
        String fileExtension = ".tmp";
        int lastDotIndex = objectKey.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = objectKey.substring(lastDotIndex);
        }
        try {
            ResponseBytes<GetObjectResponse> responseStream =
                    s3client.getObjectAsBytes(buildGetRequest(objectKey));
            Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), fileExtension);
            Files.deleteIfExists(tempFile);
            Files.copy(responseStream.asInputStream(), tempFile);
            return tempFile;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An error occurred reading S3 object to file ", e);

            return null;
        }
    }

    public void moveEmbeddedFiles(List<String> objectKeys) {
        for (String objectKey : objectKeys) {
            try {
                CopyObjectRequest copyRequest =
                        CopyObjectRequest.builder()
                                .copySource(newEmbeddingFileBucket + "/" + objectKey)
                                .destinationBucket(embeddedFileBucket)
                                .destinationKey(objectKey)
                                .build();

                s3client.copyObject(copyRequest);

                DeleteObjectRequest deleteRequest =
                        DeleteObjectRequest.builder()
                                .bucket(newEmbeddingFileBucket)
                                .key(objectKey)
                                .build();

                s3client.deleteObject(deleteRequest);

                System.out.println("Moved object: " + objectKey);
            } catch (S3Exception e) {
                System.err.println("Error moving object: " + objectKey + ". " + e.getMessage());
            }
        }
    }
}
