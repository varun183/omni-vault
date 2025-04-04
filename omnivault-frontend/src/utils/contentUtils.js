// src/utils/contentUtils.js

/**
 * Utility functions for content type detection, information, and display
 */

/**
 * Determines if a document can be viewed in browser based on MIME type
 * @param {string} mimeType - The MIME type to check
 * @returns {boolean} Whether the content is viewable in browser
 */
export const canViewDocumentInBrowser = (mimeType) => {
  if (!mimeType) return false;

  const viewableTypes = [
    "application/pdf",
    "text/plain",
    "text/html",
    "text/csv",
    "text/markdown",
    "image/svg+xml",
    "application/json",
    "application/xml",
  ];

  return viewableTypes.includes(mimeType) || mimeType.startsWith("text/");
};

/**
 * Gets appropriate icon component for a content type
 * @param {string} contentType - The content type identifier
 * @returns {object} Icon configuration with component and color
 */
export const getContentTypeIcon = (contentType) => {
  switch (contentType?.toUpperCase()) {
    case "TEXT":
      return { name: "FileText", color: "text-blue-500" };
    case "LINK":
      return { name: "Link", color: "text-green-500" };
    case "IMAGE":
      return { name: "Image", color: "text-purple-500" };
    case "VIDEO":
      return { name: "Video", color: "text-red-500" };
    case "DOCUMENT":
      return { name: "File", color: "text-orange-500" };
    default:
      return { name: "File", color: "text-gray-500" };
  }
};

/**
 * Gets a user-friendly name for a content type
 * @param {string} contentType - The content type identifier
 * @returns {string} A readable content type name
 */
export const getContentTypeName = (contentType) => {
  switch (contentType?.toUpperCase()) {
    case "TEXT":
      return "Text Notes";
    case "LINK":
      return "Links";
    case "IMAGE":
      return "Images";
    case "VIDEO":
      return "Videos";
    case "DOCUMENT":
      return "Documents";
    default:
      return "Files";
  }
};

/**
 * Gets a descriptive document type label from MIME type and/or filename
 * @param {string} mimeType - The MIME type of the document
 * @param {string} filename - Optional filename for fallback detection
 * @returns {string} User-friendly document type description
 */
export const getDocumentTypeLabel = (mimeType, filename) => {
  // Check by MIME type first
  if (mimeType) {
    if (mimeType === "application/pdf") return "PDF Document";
    if (
      mimeType === "application/msword" ||
      mimeType ===
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
      return "Word Document";
    if (
      mimeType === "application/vnd.ms-excel" ||
      mimeType ===
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
      return "Excel Spreadsheet";
    if (
      mimeType === "application/vnd.ms-powerpoint" ||
      mimeType ===
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )
      return "PowerPoint Presentation";
    if (mimeType === "application/json") return "JSON Document";
    if (mimeType === "application/xml") return "XML Document";
    if (mimeType === "text/plain") return "Text Document";
    if (mimeType === "text/csv") return "CSV Document";
    if (mimeType === "text/markdown") return "Markdown Document";
    if (mimeType === "text/html") return "HTML Document";
    if (mimeType.startsWith("text/")) return "Text Document";
  }

  // Fall back to extension if MIME type didn't match
  if (filename) {
    const extension = filename.split(".").pop()?.toLowerCase();
    if (extension) {
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
  }

  return "Document";
};

/**
 * Determines if a file is a text-based document that can be rendered as text
 * @param {string} mimeType - The MIME type to check
 * @param {string} filename - Optional filename for fallback detection
 * @returns {boolean} Whether the content should be treated as text
 */
export const isTextBasedDocument = (mimeType, filename) => {
  // Check MIME type first
  if (mimeType) {
    if (mimeType.startsWith("text/")) return true;
    if (mimeType === "application/json") return true;
    if (mimeType === "application/xml") return true;
    if (mimeType === "application/javascript") return true;
    if (mimeType === "application/typescript") return true;
    if (mimeType === "application/x-sh") return true;
    if (mimeType === "application/x-httpd-php") return true;
  }

  // Fall back to filename extension
  if (filename) {
    const extension = filename.split(".").pop()?.toLowerCase();
    return (
      !!extension &&
      [
        "txt",
        "json",
        "xml",
        "md",
        "csv",
        "log",
        "js",
        "ts",
        "jsx",
        "tsx",
        "css",
        "scss",
        "less",
        "html",
        "htm",
        "svg",
        "php",
        "py",
        "rb",
        "java",
        "c",
        "cpp",
        "h",
        "cs",
        "sh",
        "bash",
        "yml",
        "yaml",
        "toml",
        "ini",
        "conf",
        "sql",
      ].includes(extension)
    );
  }

  return false;
};

/**
 * Creates content preview based on content type
 * @param {object} content - The content object
 * @returns {object} Configuration for content preview
 */
export const getContentPreviewConfig = (content) => {
  switch (content.contentType) {
    case "TEXT":
      return {
        type: "text",
        preview: content.textContent?.substring(0, 200) || "",
        hasThumbnail: false,
      };
    case "LINK":
      return {
        type: "link",
        preview: content.url || "",
        hasThumbnail: false,
      };
    case "IMAGE":
      return {
        type: "image",
        preview: null,
        hasThumbnail: !!content.thumbnailPath,
      };
    case "VIDEO":
      return {
        type: "video",
        preview: null,
        hasThumbnail: !!content.thumbnailPath,
      };
    case "DOCUMENT":
      return {
        type: "document",
        preview: getDocumentTypeLabel(
          content.mimeType,
          content.originalFilename
        ),
        hasThumbnail: false,
      };
    default:
      return {
        type: "other",
        preview: content.originalFilename || "",
        hasThumbnail: false,
      };
  }
};
