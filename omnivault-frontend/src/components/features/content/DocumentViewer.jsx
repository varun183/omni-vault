import React, { useState, useEffect } from "react";
import { FiDownload, FiExternalLink } from "react-icons/fi";
import Button from "../../common/Button";
import Spinner from "../../common/Spinner";
import {
  getDocumentTypeLabel,
  isTextBasedDocument,
} from "../../../utils/contentUtils";
import AuthenticatedMedia from "../../common/AuthenticatedMedia";

/**
 * An enhanced document viewer component with better format support and fallbacks
 */
const DocumentViewer = ({ contentId, filename, mimeType, contentService }) => {
  const [docUrl, setDocUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewerType, setViewerType] = useState("download");

  useEffect(() => {
    const determineViewerType = () => {
      if (!mimeType) return "download";

      // Directly viewable document types
      if (mimeType === "application/pdf") return "pdf";

      // Text-based documents
      if (isTextBasedDocument(mimeType, filename)) return "text";

      if (mimeType === "image/svg+xml") return "iframe";

      // Office documents - fall back to direct download
      const officeDocuments = [
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      ];

      if (officeDocuments.includes(mimeType)) return "download";

      // Default to download for other types
      return "download";
    };

    const fetchDocument = async () => {
      try {
        setLoading(true);

        // Determine the appropriate viewer
        const viewer = determineViewerType();
        setViewerType(viewer);

        if (viewer === "pdf" || viewer === "iframe" || viewer === "text") {
          const url = await contentService.fetchFileWithAuth(contentId);
          setDocUrl(url);
        }

        setLoading(false);
      } catch (err) {
        console.error("Error loading document:", err);
        setError("Failed to load document");
        setLoading(false);
      }
    };

    fetchDocument();

    return () => {
      // Clean up blob URL when component unmounts
      if (docUrl) {
        URL.revokeObjectURL(docUrl);
      }
    };
  }, [contentId, mimeType, filename, contentService, docUrl]);

  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner size="md" />
      </div>
    );
  }

  if (error) {
    return <div className="text-red-500 py-4 text-center">{error}</div>;
  }

  // Render based on viewer type
  switch (viewerType) {
    case "pdf":
      return (
        <div>
          <div className="flex justify-end mb-4">
            <Button
              onClick={() => contentService.downloadFile(contentId, filename)}
              variant="outline"
              className="flex items-center"
            >
              <FiDownload className="mr-2" />
              Download
            </Button>
          </div>
          <div
            className="relative w-full border rounded"
            style={{ height: "80vh" }}
          >
            <AuthenticatedMedia
              contentId={contentId}
              type="iframe"
              className="absolute top-0 left-0 w-full h-full border-0 rounded"
              alt={filename}
            />
          </div>
        </div>
      );

    case "iframe":
      return (
        <div>
          <div className="flex justify-end mb-4">
            <Button
              onClick={() => contentService.downloadFile(contentId, filename)}
              variant="outline"
              className="flex items-center"
            >
              <FiDownload className="mr-2" />
              Download
            </Button>
          </div>
          <div
            className="relative w-full border rounded"
            style={{ height: "80vh" }}
          >
            <iframe
              src={docUrl}
              className="absolute top-0 left-0 w-full h-full border-0 rounded"
              title={filename}
            ></iframe>
          </div>
        </div>
      );

    case "text":
      return (
        <div>
          <div className="flex justify-end mb-4">
            <Button
              onClick={() => contentService.downloadFile(contentId, filename)}
              variant="outline"
              className="flex items-center"
            >
              <FiDownload className="mr-2" />
              Download
            </Button>
          </div>
          <TextViewer url={docUrl} filename={filename} mimeType={mimeType} />
        </div>
      );

    case "download":
    default:
      return (
        <div className="text-center py-8 bg-gray-50 rounded border">
          <div className="mb-4">
            <p className="text-lg font-medium">
              {getDocumentTypeLabel(mimeType, filename)}
            </p>
            <p className="mt-2 text-gray-500">
              This document type can't be previewed directly in the browser.
            </p>
          </div>
          <Button
            onClick={() => contentService.downloadFile(contentId, filename)}
            className="flex items-center mx-auto"
          >
            <FiDownload className="mr-2" />
            Download File
          </Button>

          {/* Optional: Show open in new tab option for direct access */}
          <div className="mt-4">
            <a
              href={contentService.getFileUrl(contentId)}
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary-600 flex items-center justify-center hover:underline"
            >
              <FiExternalLink className="mr-1" />
              Open in new tab
            </a>
          </div>
        </div>
      );
  }
};

// Improved TextViewer with format detection and optional syntax highlighting
const TextViewer = ({ url, filename, mimeType }) => {
  const [text, setText] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchText = async () => {
      try {
        const response = await fetch(url);
        const text = await response.text();
        setText(text);
        setLoading(false);
      } catch (err) {
        console.error("Error loading text:", err);
        setText("Error loading document content");
        setLoading(false);
      }
    };

    fetchText();
  }, [url]);

  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner size="md" />
      </div>
    );
  }

  // Determine if this is JSON for special formatting
  let formattedContent = text;

  // Try to detect and format JSON
  if (
    isTextBasedDocument(mimeType, filename) &&
    (mimeType === "application/json" ||
      filename?.toLowerCase().endsWith(".json"))
  ) {
    try {
      const jsonObj = JSON.parse(text);
      formattedContent = JSON.stringify(jsonObj, null, 2);
    } catch (e) {
      // Not valid JSON, just use the raw text
      console.warn("File appears to be JSON but couldn't be parsed:", e);
    }
  }

  return (
    <div className="border rounded p-4 bg-gray-50 font-mono whitespace-pre-wrap overflow-x-auto max-h-[70vh] overflow-y-auto">
      {formattedContent}
    </div>
  );
};

export default DocumentViewer;
