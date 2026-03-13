package com.firefly.experience.lending.core.application.documents.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to upload and attach a document to a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentCommand {

    private UUID applicationId;
    private String fileName;
    private String documentType;
    private byte[] content;
}
