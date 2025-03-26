import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  resendVerificationEmail,
  verifyEmail,
  verifyEmailWithOTP,
} from "../store/slices/authSlice";
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

  const verifyWithToken = useCallback(
    async (token) => {
      setVerificationMessage("Verifying your email...");
      dispatch(verifyEmail(token));
    },
    [dispatch, setVerificationMessage]
  );

  // Check if we have a token in URL
  useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      verifyWithToken(token);
    } else {
      setShowOtpForm(true);
    }
  }, [searchParams, verifyWithToken]);

  // Handle redirect after successful verification
  useEffect(() => {
    if (verificationSuccess) {
      navigate("/login?verified=true");
    }
  }, [verificationSuccess, navigate]);

  // Handle countdown for resend button
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

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    if (!verificationEmail) {
      // If no email is stored, we can't verify
      setVerificationMessage(
        "Session expired. Please try the link in your email."
      );
      return;
    }

    await dispatch(verifyEmailWithOTP({ email: verificationEmail, otpCode }));
  };

  const handleResendEmail = async () => {
    if (!verificationEmail) {
      setVerificationMessage(
        "Session expired. Please go back to registration."
      );
      return;
    }

    setResendDisabled(true);
    setResendCountdown(60);
    await dispatch(resendVerificationEmail(verificationEmail));

    setVerificationMessage("Verification email resent!");
  };

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">
        Email Verification
      </h2>

      {error && <Alert type="error" message={error} className="mb-4" />}

      {verificationMessage && (
        <Alert type="info" message={verificationMessage} className="mb-4" />
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
              onChange={(e) => setOtpCode(e.target.value)}
              placeholder="Enter 6-digit code"
              required
            />

            <div className="mt-4">
              <Button
                type="submit"
                variant="primary"
                fullWidth
                disabled={loading || !otpCode}
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
