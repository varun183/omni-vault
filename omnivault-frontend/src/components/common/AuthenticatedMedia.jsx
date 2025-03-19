// src/components/common/AuthenticatedMedia.jsx
import React, { useState, useEffect } from "react";
import Spinner from "./Spinner";
import contentService from "../../services/contentService";
import { apiCache } from "../../utils/apiCache";

/**
 * Component for displaying authenticated media (images, videos, files)
 * with built-in caching and error handling
 */
const AuthenticatedMedia = ({
  contentId,
  type = "image",
  isThumb = false,
  className = "",
  alt = "Content",
  controls = true,
  autoPlay = false,
}) => {
  const [mediaUrl, setMediaUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Create a unique cache key for this media request
    const cacheKey = `media_${contentId}_${isThumb ? "thumb" : "full"}`;

    // Check cache first
    const cachedUrl = apiCache.get(cacheKey);
    if (cachedUrl) {
      setMediaUrl(cachedUrl);
      setLoading(false);
      return;
    }

    const fetchMedia = async () => {
      try {
        setLoading(true);
        const url = isThumb
          ? await contentService.fetchThumbnailWithAuth(contentId)
          : await contentService.fetchFileWithAuth(contentId);

        if (url) {
          setMediaUrl(url);
          // Cache the URL
          apiCache.set(cacheKey, url, 5 * 60 * 1000); // 5 minutes cache
        } else {
          throw new Error("Failed to load media");
        }

        setLoading(false);
      } catch (err) {
        console.error("Error loading media:", err);
        setError("Failed to load media");
        setLoading(false);
      }
    };

    fetchMedia();

    // Clean up object URL when component unmounts
    return () => {
      if (mediaUrl && mediaUrl.startsWith("blob:")) {
        URL.revokeObjectURL(mediaUrl);
      }
    };
  }, [contentId, isThumb, mediaUrl, type]);

  if (loading) {
    return (
      <div
        className={`${className} bg-gray-200 animate-pulse flex items-center justify-center`}
      >
        <Spinner size="md" />
      </div>
    );
  }

  if (error) {
    return (
      <div
        className={`${className} bg-gray-100 flex items-center justify-center text-red-500`}
      >
        {error}
      </div>
    );
  }

  // Render different media types
  switch (type) {
    case "image":
      return <img src={mediaUrl} alt={alt} className={className} />;

    case "video":
      return (
        <video
          src={mediaUrl}
          className={className}
          controls={controls}
          autoPlay={autoPlay}
        />
      );

    case "iframe":
      return (
        <iframe
          src={mediaUrl}
          className={className}
          title={alt}
          frameBorder="0"
        />
      );

    default:
      return (
        <div
          className={`${className} bg-gray-100 flex items-center justify-center text-gray-500`}
        >
          Unsupported media type
        </div>
      );
  }
};

export default AuthenticatedMedia;
