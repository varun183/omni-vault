import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";

const GOOGLE_DOCS_VIEWABLE_TYPES = [
  // Microsoft Office
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "application/vnd.ms-excel",
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  "application/vnd.ms-powerpoint",
  "application/vnd.openxmlformats-officedocument.presentationml.presentation",
  // OpenOffice
  "application/vnd.oasis.opendocument.text",
  "application/vnd.oasis.opendocument.spreadsheet",
  "application/vnd.oasis.opendocument.presentation",
  // Other formats
  "application/pdf",
  "application/rtf",
  "text/csv",
  "text/plain",
];

// Documents that can be viewed directly in the browser
const BROWSER_VIEWABLE_TYPES = [
  "application/pdf",
  "application/json",
  "text/plain",
  "text/html",
  "text/csv",
  "text/markdown",
  "text/xml",
  "image/svg+xml",
  "image/jpeg",
  "image/png",
  "image/gif",
  "image/webp",
  "audio/mpeg",
  "audio/wav",
  "audio/ogg",
  "video/mp4",
  "video/webm",
  "video/ogg",
];

const documentViewerService = {
  /**
   * Check if a document can be viewed in Google Docs Viewer
   * @param {string} mimeType - The MIME type of the document
   * @returns {boolean} - Whether the document can be viewed in Google Docs Viewer
   */
  canUseGoogleViewer: (mimeType) => {
    if (!mimeType) return false;
    return GOOGLE_DOCS_VIEWABLE_TYPES.includes(mimeType);
  },

  /**
   * Check if a document can be viewed directly in the browser
   * @param {string} mimeType - The MIME type of the document
   * @returns {boolean} - Whether the document can be viewed in the browser
   */
  canViewInBrowser: (mimeType) => {
    if (!mimeType) return false;
    return (
      BROWSER_VIEWABLE_TYPES.includes(mimeType) || mimeType.startsWith("text/")
    );
  },

  /**
   * Get a temporary secure URL for a document
   * @param {string} contentId - The content ID
   * @returns {Promise<string>} - A promise that resolves to a temporary URL
   */
  getTemporaryUrl: async (contentId) => {
    const cacheKey = `temp_url_${contentId}`;

    // Check if we have a cached URL that's still valid
    const cachedUrl = apiCache.get(cacheKey);
    if (cachedUrl) {
      return cachedUrl;
    }

    try {
      // Request a temporary URL from the server
      const response = await axiosInstance.post(
        `/contents/${contentId}/temp-url`
      );
      const tempUrl = response.data.url;

      // Cache the URL with a short TTL (slightly less than server-side expiration)
      // Assuming server gives 15 min, we cache for 14 min
      apiCache.set(cacheKey, tempUrl, 14 * 60 * 1000);

      return tempUrl;
    } catch (error) {
      console.error("Error getting temporary URL:", error);
      throw error;
    }
  },

  /**
   * Get a Google Docs Viewer URL for a document
   * @param {string} documentUrl - The URL of the document to view
   * @returns {string} - The Google Docs Viewer URL
   */
  getGoogleViewerUrl: (documentUrl) => {
    return `https://docs.google.com/viewer?embedded=true&url=${encodeURIComponent(
      documentUrl
    )}`;
  },
};

export default documentViewerService;
