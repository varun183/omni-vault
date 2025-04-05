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

  const { loading, error, verificationSuccess, verificationEmail } =
    useSelector((state) => state.auth);

  const [otpCode, setOtpCode] = useState("");
  const [showOtpForm, setShowOtpForm] = useState(false);
  const [resendDisabled, setResendDisabled] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const [verificationMessage, setVerificationMessage] = useState("");

  // Clean up verification state on component unmount
  useEffect(() => {
    return () => {
      dispatch(clearVerificationState());
    };
  }, [dispatch]);

  // Log page access and initial state
  useEffect(() => {
    logger.info("Email verification page accessed", {
      hasTokenInUrl: !!searchParams.get("token"),
      verificationEmail: verificationEmail || "No email in state",
    });
  }, [searchParams, verificationEmail]);

  // Token verification callback
  const verifyWithToken = useCallback(
    async (token) => {
      try {
        logger.info("Attempting email verification with token", {
          tokenLength: token.length,
        });

        await logger.logAsyncError(
          dispatch(verifyEmail(token)).unwrap(),
          "Email verification with token failed",
          { tokenLength: token.length }
        );

        logger.info("Email verified successfully via token");
      } catch (error) {
        logger.warn("Token verification failed, showing OTP form", {
          error: error.message,
        });
        setShowOtpForm(true);
      }
    },
    [dispatch]
  );

  // Check for token in URL on component mount
  useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      verifyWithToken(token);
    } else {
      // No token, show OTP form
      setShowOtpForm(true);
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
        "Session expired. Please try the link in your email."
      );
      return;
    }

    try {
      logger.info("Attempting OTP verification", {
        emailLength: verificationEmail.length,
      });

      await logger.logAsyncError(
        dispatch(
          verifyEmailWithOTP({
            email: verificationEmail,
            otpCode,
          })
        ).unwrap(),
        "OTP verification failed",
        {
          emailLength: verificationEmail.length,
          otpCodeLength: otpCode.length,
        }
      );

      logger.info("Email verified successfully via OTP");
    } catch (error) {
      // Error is already logged by logAsyncError
      setVerificationMessage("Invalid verification code. Please try again.");
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

      await logger.logAsyncError(
        dispatch(resendVerificationEmail(verificationEmail)).unwrap(),
        "Failed to resend verification email",
        { emailLength: verificationEmail.length }
      );

      logger.info("Verification email resent successfully");
      setVerificationMessage("Verification email resent!");
    } catch (error) {
      // Error is already logged by logAsyncError
      setResendDisabled(false);
      setResendCountdown(0);
    }
  };

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
          We've sent a verification link and code to your email. Please check
          your inbox and spam folder.
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
            />

            <div className="mt-4">
              <Button
                type="submit"
                variant="primary"
                fullWidth
                disabled={loading || !otpCode}
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
      </div>
    </AuthLayout>
  );
};

export default EmailVerificationPage;
