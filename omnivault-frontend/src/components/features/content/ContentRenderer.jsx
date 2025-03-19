import React from "react";
import { FiDownload, FiExternalLink } from "react-icons/fi";
import Button from "../../common/Button";
import AuthenticatedMedia from "../../common/AuthenticatedMedia";
import DocumentViewer from "./DocumentViewer";
import contentService from "../../../services/contentService";

/**
 * A universal component for rendering different content types
 */
const ContentRenderer = ({ content }) => {
  if (!content) {
    return <div className="text-center py-4">No content to display</div>;
  }

  switch (content.contentType) {
    case "TEXT":
      return <TextContentRenderer content={content} />;
    case "LINK":
      return <LinkContentRenderer content={content} />;
    case "IMAGE":
      return <ImageContentRenderer content={content} />;
    case "VIDEO":
      return <VideoContentRenderer content={content} />;
    case "DOCUMENT":
      return <DocumentContentRenderer content={content} />;
    default:
      return <GenericContentRenderer content={content} />;
  }
};

const TextContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4">
      <div className="prose prose-blue max-w-none">
        {content.textContent.split("\n").map((paragraph, i) => (
          <p key={i}>{paragraph}</p>
        ))}
      </div>
    </div>
  );
};

const LinkContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4">
      <a
        href={content.url}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center text-primary-600 hover:text-primary-700"
      >
        <FiExternalLink className="mr-1" />
        {content.url}
      </a>
      {content.previewImagePath && (
        <div className="mt-4">
          <AuthenticatedMedia
            contentId={content.id}
            type="image"
            alt="Link preview"
            className="max-w-full rounded"
          />
        </div>
      )}
    </div>
  );
};

const ImageContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4">
      <AuthenticatedMedia
        contentId={content.id}
        type="image"
        alt={content.title}
        className="max-w-full rounded"
      />
      <div className="mt-4 flex justify-end">
        <Button
          onClick={() =>
            contentService.downloadFile(
              content.id,
              content.originalFilename || "image.jpg"
            )
          }
          variant="outline"
          className="flex items-center"
          size="sm"
        >
          <FiDownload className="mr-2" />
          Download Original
        </Button>
      </div>
    </div>
  );
};

const VideoContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4">
      <AuthenticatedMedia
        contentId={content.id}
        type="video"
        alt={content.title}
        className="max-w-full rounded w-full"
        controls={true}
      />
      <div className="mt-4 flex justify-end">
        <Button
          onClick={() =>
            contentService.downloadFile(
              content.id,
              content.originalFilename || "video.mp4"
            )
          }
          variant="outline"
          className="flex items-center"
          size="sm"
        >
          <FiDownload className="mr-2" />
          Download Original
        </Button>
      </div>
    </div>
  );
};

const DocumentContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4">
      <DocumentViewer
        contentId={content.id}
        filename={content.originalFilename}
        mimeType={content.mimeType}
        contentService={contentService}
      />
    </div>
  );
};

const GenericContentRenderer = ({ content }) => {
  return (
    <div className="bg-white rounded-lg shadow p-6 mt-4 text-center">
      <p className="mb-4">This file type may not be viewable in the browser.</p>
      <Button
        onClick={() =>
          contentService.downloadFile(
            content.id,
            content.originalFilename || "file"
          )
        }
        className="flex items-center mx-auto"
      >
        <FiDownload className="mr-2" />
        Download File
      </Button>
    </div>
  );
};

export default ContentRenderer;
