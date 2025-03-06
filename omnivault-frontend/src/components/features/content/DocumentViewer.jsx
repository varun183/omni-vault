import React, { useState, useEffect } from "react";
import { FiDownload, FiExternalLink } from "react-icons/fi";
import Button from "../../common/Button";
import Spinner from "../../common/Spinner";

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

      // All text-based formats should use the text viewer - expanded list
      if (
        mimeType.startsWith("text/") ||
        mimeType === "application/json" ||
        mimeType === "application/xml" ||
        mimeType === "application/javascript" ||
        mimeType === "application/xhtml+xml"
      )
        return "text";

      if (mimeType === "image/svg+xml") return "iframe";

      // Office documents - we'll fall back to direct download since Google Docs Viewer isn't working
      const officeDocuments = [
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      ];

      if (officeDocuments.includes(mimeType)) return "download";

      // Check file extension as a fallback
      if (filename) {
        const extension = filename.split(".").pop().toLowerCase();
        if (["txt", "json", "xml", "md", "csv", "log"].includes(extension)) {
          return "text";
        }
      }

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
  }, [contentId, mimeType, filename, contentService]);

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
            <iframe
              src={docUrl}
              className="absolute top-0 left-0 w-full h-full border-0 rounded"
              title={filename}
            ></iframe>
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
  let isJson = false;

  // Try to detect and format JSON
  if (
    mimeType === "application/json" ||
    filename?.toLowerCase().endsWith(".json")
  ) {
    try {
      const jsonObj = JSON.parse(text);
      formattedContent = JSON.stringify(jsonObj, null, 2);
      isJson = true;
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

// Helper function to get document type label
function getDocumentTypeLabel(mimeType, filename) {
  // First try to determine by MIME type
  if (mimeType) {
    const mimeTypeMap = {
      "application/pdf": "PDF Document",
      "application/msword": "Word Document",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
        "Word Document",
      "application/vnd.ms-excel": "Excel Spreadsheet",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
        "Excel Spreadsheet",
      "application/vnd.ms-powerpoint": "PowerPoint Presentation",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation":
        "PowerPoint Presentation",
      "application/json": "JSON Document",
      "application/xml": "XML Document",
      "text/plain": "Text Document",
      "text/csv": "CSV Document",
      "text/markdown": "Markdown Document",
      "text/html": "HTML Document",
      "application/zip": "ZIP Archive",
      "application/x-zip-compressed": "ZIP Archive",
      "application/x-rar-compressed": "RAR Archive",
      "application/x-7z-compressed": "7Z Archive",
    };

    if (mimeType in mimeTypeMap) {
      return mimeTypeMap[mimeType];
    }

    if (mimeType.startsWith("text/")) {
      return "Text Document";
    }
  }

  // If we couldn't determine by MIME type, try by file extension
  if (filename) {
    const extension = filename.split(".").pop().toLowerCase();
    const extensionMap = {
      pdf: "PDF Document",
      doc: "Word Document",
      docx: "Word Document",
      xls: "Excel Spreadsheet",
      xlsx: "Excel Spreadsheet",
      ppt: "PowerPoint Presentation",
      pptx: "PowerPoint Presentation",
      txt: "Text Document",
      rtf: "Rich Text Document",
      json: "JSON Document",
      xml: "XML Document",
      md: "Markdown Document",
      csv: "CSV Document",
      html: "HTML Document",
      htm: "HTML Document",
      zip: "ZIP Archive",
      rar: "RAR Archive",
      "7z": "7Z Archive",
    };

    if (extension in extensionMap) {
      return extensionMap[extension];
    }
  }

  return "Document";
}

export default DocumentViewer;
