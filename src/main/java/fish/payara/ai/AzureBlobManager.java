package fish.payara.ai;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobItem;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.azure.storage.blob.AzureBlobStorageDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Log
public class AzureBlobManager implements EmbeddingDocumentLoader {
    @ConfigProperty(name = "BLOB_SAS_TOKEN")
    @Inject
    String blobSasToken;

    @ConfigProperty(name = "BLOB_SAS_URL")
    @Inject
    String blobSasUrl;

    @ConfigProperty(name = "azure_blob_container")
    @Inject
    String azureBlobContainer;

    @Inject
    @ConfigProperty(name = "s3.embedded-files.bucket")
    String embeddedFileBucket;

    AzureBlobStorageDocumentLoader documentLoader;
    BlobServiceClient blobServiceClient;

    @PostConstruct
    void init() {
        blobServiceClient =
                new BlobServiceClientBuilder()
                        .endpoint(blobSasUrl)
                        .sasToken(blobSasToken)
                        .buildClient();
        documentLoader = new AzureBlobStorageDocumentLoader(blobServiceClient);
    }

    @Override
    public List<Document> loadDocuments() {
        return documentLoader.loadDocuments(azureBlobContainer, new ApachePdfBoxDocumentParser());
    }

    @Override
    public Document loadDocument(String documentKey) {
        return documentLoader.loadDocument(
                azureBlobContainer, documentKey, new ApachePdfBoxDocumentParser());
    }

    @Override
    public void moveEmbeddedDocument(List<String> documentKeys) {
        BlobContainerClient blobContainerClient =
                blobServiceClient.getBlobContainerClient(azureBlobContainer);
        for (String documentKey : documentKeys) {
            BlobClient sourceBlob = blobContainerClient.getBlobClient(documentKey);

            BlobClient targetBlob =
                    blobContainerClient.getBlobClient(embeddedFileBucket + "/" + documentKey);
            SyncPoller<BlobCopyInfo, Void> blobCopyInfoVoidSyncPoller =
                    targetBlob.beginCopy(sourceBlob.getBlobUrl(), Duration.ofSeconds(2));
            PollResponse<BlobCopyInfo> pollResponse = blobCopyInfoVoidSyncPoller.poll();
            log.log(Level.INFO, "Copy identifier: %s%n", pollResponse.getValue().getCopyId());
            sourceBlob.delete();
        }
    }

    @Override
    public List<String> listObjects() {
        return blobServiceClient.getBlobContainerClient(azureBlobContainer).listBlobs().stream()
                .map(BlobItem::getName)
                .toList();
    }
}
