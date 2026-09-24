package com.anjar.portfolio.service;

import com.anjar.portfolio.config.EmailProperties;
import com.anjar.portfolio.entity.Message;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailProperties emailProperties;

    @Async
    public void sendContactNotification(Message message) {
        if (!emailProperties.isEnabled()) {
            log.info("Email disabled, skip sending for message from {}", message.getEmail());
            return;
        }

        try {
            Resend resend = new Resend(emailProperties.getApiKey());

            String htmlBody = buildEmailTemplate(message);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(emailProperties.getFromName() + " <" + emailProperties.getFromEmail() + ">")
                    .to(emailProperties.getToEmail())
                    .replyTo(message.getEmail())
                    .subject("📬 New Contact: " + message.getSubject())
                    .html(htmlBody)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("✅ Email sent successfully. ID: {}", response.getId());

        } catch (ResendException e) {
            log.error("❌ Failed to send email (Resend error): {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email: {}", e.getMessage(), e);
        }
    }

    private String buildEmailTemplate(Message message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>New Contact Message</title>
            </head>
            <body style="margin:0; padding:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif; background:#f8fafc;">
              <div style="max-width:600px; margin:0 auto; padding:32px 16px;">

                <!-- Header -->
                <div style="background:linear-gradient(135deg,#3b82f6,#8b5cf6); border-radius:20px 20px 0 0; padding:32px; text-align:center;">
                  <div style="width:64px; height:64px; border-radius:16px; background:rgba(255,255,255,0.2); display:inline-flex; align-items:center; justify-content:center; margin-bottom:16px; font-size:32px;">
                    📬
                  </div>
                  <h1 style="color:white; margin:0; font-size:24px; font-weight:800; letter-spacing:-0.02em;">
                    New Contact Message
                  </h1>
                  <p style="color:rgba(255,255,255,0.9); margin:8px 0 0; font-size:14px;">
                    Someone just contacted you from your portfolio
                  </p>
                </div>

                <!-- Body -->
                <div style="background:white; padding:32px; border-radius:0 0 20px 20px; box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                  <!-- Sender Info -->
                  <table style="width:100%%; border-collapse:collapse; margin-bottom:24px;">
                    <tr>
                      <td style="padding:12px 0; border-bottom:1px solid #f1f5f9;">
                        <span style="font-size:12px; font-weight:700; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;">From</span>
                        <p style="margin:4px 0 0; font-size:15px; color:#0f172a; font-weight:600;">%s</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:12px 0; border-bottom:1px solid #f1f5f9;">
                        <span style="font-size:12px; font-weight:700; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;">Email</span>
                        <p style="margin:4px 0 0; font-size:15px;">
                          <a href="mailto:%s" style="color:#3b82f6; text-decoration:none; font-weight:600;">%s</a>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:12px 0; border-bottom:1px solid #f1f5f9;">
                        <span style="font-size:12px; font-weight:700; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;">Subject</span>
                        <p style="margin:4px 0 0; font-size:15px; color:#0f172a; font-weight:600;">%s</p>
                      </td>
                    </tr>
                  </table>

                  <!-- Message -->
                  <div style="margin-bottom:24px;">
                    <span style="font-size:12px; font-weight:700; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;">Message</span>
                    <div style="margin-top:8px; padding:20px; background:#f8fafc; border-left:4px solid #3b82f6; border-radius:0 12px 12px 0; font-size:15px; line-height:1.7; color:#334155; white-space:pre-wrap;">%s</div>
                  </div>

                  <!-- Actions -->
                  <div style="text-align:center; padding-top:16px; border-top:1px solid #f1f5f9;">
                    <a href="mailto:%s?subject=Re: %s" style="display:inline-block; padding:14px 32px; background:linear-gradient(135deg,#3b82f6,#8b5cf6); color:white; text-decoration:none; border-radius:12px; font-size:15px; font-weight:700;">
                      ↩️ Reply Now
                    </a>
                  </div>

                </div>

                <!-- Footer -->
                <div style="text-align:center; padding:24px 0; color:#94a3b8; font-size:12px;">
                  <p style="margin:0;">Sent from your portfolio contact form</p>
                  <p style="margin:4px 0 0;">%s</p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                message.getName(),
                message.getEmail(),
                message.getEmail(),
                message.getSubject() != null ? message.getSubject() : "(No subject)",
                message.getMessage(),
                message.getEmail(),
                message.getSubject() != null ? message.getSubject() : "",
                timestamp
        );
    }
}