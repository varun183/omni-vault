import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  resendVerificationEmail,
  verifyEmail,
  verifyEmailWithOTP,
  clearVerificationState,
} from "../store/slices/authSlice";
import logger from "../services/loggerService";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import Alert from "../components/common/Alert";

const EmailVerificationPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Retrieve verification email from multiple sources
  const getVerificationEmail = () => {
    const stateEmail = localStorage.getItem("verification_email");
    const email = stateEmail || searchParams.get("email");
    return email;
  };

  const [verificationEmail] = useState(getVerificationEmail());
  const { loading, error, verificationSuccess } = useSelector(
    (state) => state.auth
  );

  const [otpCode, setOtpCode] = useState("");
  const [showOtpForm, setShowOtpForm] = useState(true);
  const [resendDisabled, setResendDisabled] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const [verificationMessage, setVerificationMessage] = useState("");

  // Redirect if no verification email is available
  useEffect(() => {
    if (!verificationEmail) {
      logger.warn("No verification email found, redirecting to register");
      navigate("/register");
    }
  }, [verificationEmail, navigate]);

  // Clean up verification state on component unmount
  useEffect(() => {
    return () => {
      dispatch(clearVerificationState());
    };
  }, [dispatch]);

  // Token verification callback
  const verifyWithToken = useCallback(
    async (token) => {
      try {
        logger.info("Attempting email verification with token", {
          tokenLength: token.length,
        });

        await dispatch(verifyEmail(token)).unwrap();

        logger.info("Email verified successfully via token");

        // Handle window closing for verification link
        if (window.opener) {
          try {
            // Close the original window (verification page)
            window.opener.location = "/login?verified=true";
            window.close();
          } catch (err) {
            // Fallback if direct closing fails
            logger.warn("Could not close opener window", err);
            window.opener.focus();
          }
        }

        // Fallback navigation if opener methods fail
        navigate("/login?verified=true");
      } catch (error) {
        logger.warn("Token verification failed, showing OTP form", {
          error: error.message,
        });
        setShowOtpForm(true);
      }
    },
    [dispatch, navigate]
  );

  // Check for token in URL on component mount
  useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      verifyWithToken(token);
    }
  }, [searchParams, verifyWithToken]);

  // Handle successful verification
  useEffect(() => {
    if (verificationSuccess) {
      logger.info("Email verification successful, redirecting to login");
      navigate("/login?verified=true");
    }
  }, [verificationSuccess, navigate]);

  // Resend email countdown
  useEffect(() => {
    let intervalId;
    if (resendCountdown > 0) {
      intervalId = setInterval(() => {
        setResendCountdown((prev) => prev - 1);
      }, 1000);
    } else {
      setResendDisabled(false);
    }

    return () => clearInterval(intervalId);
  }, [resendCountdown]);

  // OTP Submission Handler
  const handleOtpSubmit = async (e) => {
    e.preventDefault();

    if (!verificationEmail) {
      logger.warn("OTP verification attempted without email", {
        errorType: "MissingVerificationEmail",
      });

      setVerificationMessage(
        "Session expired. Please go back to registration."
      );
      return;
    }

    try {
      logger.info("Attempting OTP verification", {
        emailLength: verificationEmail.length,
      });

      // Dispatch OTP verification
      await dispatch(
        verifyEmailWithOTP({
          email: verificationEmail,
          otpCode,
        })
      ).unwrap();

      // Clear local storage and navigate to login
      localStorage.removeItem("verification_email");
      navigate("/login?verified=true");

      logger.info("Email verified successfully via OTP");
    } catch (error) {
      // Log and display error
      logger.error("OTP verification failed", error);
      setVerificationMessage(
        error.message || "Invalid verification code. Please try again."
      );
    }
  };

  // Resend Email Handler
  const handleResendEmail = async () => {
    if (!verificationEmail) {
      logger.warn("Resend email attempt with no verification email", {
        errorType: "MissingVerificationEmail",
      });

      setVerificationMessage(
        "Session expired. Please go back to registration."
      );
      return;
    }

    try {
      logger.info("Attempting to resend verification email", {
        emailLength: verificationEmail.length,
      });

      setResendDisabled(true);
      setResendCountdown(60);

      // Dispatch resend verification email
      await dispatch(resendVerificationEmail(verificationEmail)).unwrap();

      logger.info("Verification email resent successfully");
      setVerificationMessage("Verification email resent!");
    } catch (error) {
      // Log and display error
      logger.error("Failed to resend verification email", error);
      setVerificationMessage(
        error.message ||
          "Failed to resend verification email. Please try again."
      );
      setResendDisabled(false);
      setResendCountdown(0);
    }
  };

  // If no email found, don't render the page
  if (!verificationEmail) {
    return null;
  }

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">
        Email Verification
      </h2>

      {error && (
        <Alert
          type="error"
          message={error}
          className="mb-4"
          onClose={() => {
            logger.info("Error alert dismissed");
            dispatch(clearVerificationState());
          }}
        />
      )}

      {verificationMessage && (
        <Alert
          type="info"
          message={verificationMessage}
          className="mb-4"
          onClose={() => {
            logger.info("Verification message dismissed");
            setVerificationMessage("");
          }}
        />
      )}

      <div className="text-center mb-6">
        <p className="text-gray-600 mb-4">
          We've sent a verification link and code to{" "}
          <span className="font-semibold">{verificationEmail}</span>. Please
          check your inbox and spam folder.
        </p>

        {showOtpForm && (
          <form onSubmit={handleOtpSubmit} className="mt-6">
            <Input
              label="Verification Code"
              id="otpCode"
              value={otpCode}
              onChange={(e) => {
                // Log OTP input interaction
                logger.debug("OTP code input changed", {
                  inputLength: e.target.value.length,
                });
                setOtpCode(e.target.value);
              }}
              placeholder="Enter 6-digit code"
              required
              maxLength={6}
            />

            <div className="mt-4">
              <Button
                type="submit"
                variant="primary"
                fullWidth
                disabled={loading || !otpCode || otpCode.length !== 6}
                onClick={() => logger.info("OTP verification initiated")}
              >
                {loading ? <Spinner size="sm" className="mr-2" /> : null}
                Verify Email
              </Button>
            </div>
          </form>
        )}

        <div className="mt-6">
          <Button
            type="button"
            variant="secondary"
            onClick={handleResendEmail}
            disabled={loading || resendDisabled}
          >
            {resendDisabled
              ? `Resend Email (${resendCountdown}s)`
              : "Resend Verification Email"}
          </Button>
        </div>

        <div className="mt-4 text-sm text-gray-600">
          <p>
            Didn't receive the email?{" "}
            <button
              type="button"
              className="text-primary-600 hover:underline"
              onClick={() => navigate("/register")}
            >
              Go back to registration
            </button>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
};

export default EmailVerificationPage;
