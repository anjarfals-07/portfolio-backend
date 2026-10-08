package com.anjar.portfolio.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-email:}")
    private String fromEmail;

    @Value("${app.mail.from-name:Portfolio}")
    private String fromName;

    @Value("${app.mail.admin-email:}")
    private String adminEmail;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ============================================================
    // GENERIC
    // ============================================================
    @Async
    public void sendNewMessageEmail(
            String to,
            String subject,
            String templateName,
            Map<String, String> vars) {

        if (!enabled) {
            log.info("📧 [DISABLED] {} skipped (to: {})", subject, to);
            return;
        }

        try {
            String html = loadTemplate(templateName, vars);
            sendEmail(to, subject, html);
            log.info("✅ Email sent to {} (subject: {})", to, subject);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ============================================================
    // WELCOME
    // ============================================================
    @Async
    public void sendWelcomeEmail(String toEmail, String username, String slug) {
        if (!enabled) {
            log.info("📧 [DISABLED] Welcome email to {} skipped", toEmail);
            return;
        }

        try {
            Map<String, String> vars = Map.of(
                    "username", username,
                    "slug", slug,
                    "portfolioUrl", frontendUrl + "/" + slug,
                    "loginUrl", frontendUrl + "/login"
            );

            String html = loadTemplate("welcome.html", vars);
            sendEmail(toEmail, "🎉 Selamat Datang di Portfolio!", html);
            log.info("✅ Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed welcome email: {}", e.getMessage());
        }
    }

    // ============================================================
    // APPROVED
    // ============================================================
    @Async
    public void sendApprovedEmail(String toEmail, String username, String slug) {
        if (!enabled) {
            log.info("📧 [DISABLED] Approved email to {} skipped", toEmail);
            return;
        }

        try {
            Map<String, String> vars = Map.of(
                    "username", username,
                    "slug", slug,
                    "loginUrl", frontendUrl + "/login",
                    "portfolioUrl", frontendUrl + "/" + slug
            );

            String html = loadTemplate("approved.html", vars);
            sendEmail(toEmail, "✅ Akun Kamu Di-approve!", html);
            log.info("✅ Approved email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed approved email: {}", e.getMessage());
        }
    }

    // ============================================================
    // REJECTED
    // ============================================================
    @Async
    public void sendRejectedEmail(String toEmail, String username, String reason) {
        if (!enabled) {
            log.info("📧 [DISABLED] Rejected email to {} skipped", toEmail);
            return;
        }

        try {
            Map<String, String> vars = Map.of(
                    "username", username,
                    "reason", reason != null ? reason : "Tidak memenuhi syarat",
                    "registerUrl", frontendUrl + "/register"
            );

            String html = loadTemplate("rejected.html", vars);
            sendEmail(toEmail, "❌ Update Status Akun Kamu", html);
            log.info("✅ Rejected email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed rejected email: {}", e.getMessage());
        }
    }

    // ============================================================
    // NEW USER NOTIF TO ADMIN
    // ============================================================
    @Async
    public void sendNewUserNotifToAdmin(String username, String email, String slug) {
        if (!enabled || adminEmail == null || adminEmail.isBlank()) {
            log.info("📧 [DISABLED] New user notif to admin skipped");
            return;
        }

        try {
            Map<String, String> vars = Map.of(
                    "username", username,
                    "email", email,
                    "slug", slug,
                    "adminUrl", frontendUrl + "/admin/users"
            );

            String html = loadTemplate("new-user.html", vars);
            sendEmail(adminEmail, "👤 User Baru: " + username, html);
            log.info("✅ New user notif sent to admin ({})", adminEmail);
        } catch (Exception e) {
            log.error("❌ Failed new user notif: {}", e.getMessage());
        }
    }

    // ============================================================
    // RESET PASSWORD
    // ============================================================
    @Async
    public void sendResetPasswordEmail(String toEmail, String username, String resetToken) {
        if (!enabled) {
            log.info("📧 [DISABLED] Reset password email to {} skipped", toEmail);
            return;
        }

        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            Map<String, String> vars = Map.of(
                    "username", username,
                    "resetUrl", resetUrl
            );

            String html = loadTemplate("reset-password.html", vars);
            sendEmail(toEmail, "🔐 Reset Password — Portfolio", html);
            log.info("✅ Reset password email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed reset password email: {}", e.getMessage());
        }
    }

    // ============================================================
    // PAYMENT EMAILS  ⭐ FIXED (mailEnabled → enabled)
    // ============================================================

    @Async
    public void sendPaymentApprovedEmail(
            String toEmail,
            String username,
            String referenceId,
            String nextStepUrl
    ) {
        if (!enabled) {
            log.info("📧 [DISABLED] Payment approved email to {} skipped", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Pembayaran Berhasil — " + referenceId);

            String html = buildPaymentApprovedHtml(username, referenceId, nextStepUrl);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("✅ Payment approved email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send payment approved email: {}", e.getMessage());
        }
    }

    @Async
    public void sendPaymentRejectedEmail(
            String toEmail,
            String username,
            String referenceId,
            String reason,
            String retryUrl
    ) {
        if (!enabled) {
            log.info("📧 [DISABLED] Payment rejected email to {} skipped", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Bukti Bayar Ditolak — " + referenceId);

            String html = buildPaymentRejectedHtml(
                    username, referenceId, reason, retryUrl);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("✅ Payment rejected email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send payment rejected email: {}", e.getMessage());
        }
    }

    @Async
    public void sendAdminPaymentNotification(
            String referenceId,
            String username,
            String methodLabel
    ) {
        if (!enabled || adminEmail == null || adminEmail.isBlank()) {
            log.info("📧 [DISABLED] Admin payment notification skipped");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("💳 Bukti Bayar Baru — " + referenceId);

            String html = buildAdminPaymentNotificationHtml(
                    referenceId, username, methodLabel);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("✅ Admin payment notification sent");
        } catch (Exception e) {
            log.error("❌ Failed to send admin notification: {}", e.getMessage());
        }
    }

    // ============================================================
    // HTML TEMPLATES
    // ============================================================

    private String buildPaymentApprovedHtml(
            String username, String referenceId, String nextStepUrl) {
        return """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background: #f6f8fb; padding: 24px;">
          <div style="max-width: 560px; margin: 0 auto; background: #fff;
                      border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb;">
            <h2 style="color: #10b981;">✅ Pembayaran Diterima!</h2>
            <p>Hai <strong>%s</strong>,</p>
            <p>Pembayaran kamu dengan reference <code>%s</code> sudah kami
               konfirmasi.</p>
            <p>Selanjutnya, akun kamu akan diproses oleh admin. Kamu akan
               mendapat email lagi setelah akun aktif.</p>
            <p style="margin-top: 24px;">
              <a href="%s" style="display: inline-block; background: #3b82f6;
                 color: #fff; padding: 12px 24px; border-radius: 8px;
                 text-decoration: none; font-weight: 600;">Cek Status</a>
            </p>
            <p style="margin-top: 32px; color: #64748b; font-size: 13px;">
              Terima kasih,<br/>Portfolio Team
            </p>
          </div>
        </body>
        </html>
        """.formatted(username, referenceId, nextStepUrl);
    }

    private String buildPaymentRejectedHtml(
            String username, String referenceId, String reason, String retryUrl) {
        return """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background: #f6f8fb; padding: 24px;">
          <div style="max-width: 560px; margin: 0 auto; background: #fff;
                      border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb;">
            <h2 style="color: #ef4444;">❌ Bukti Bayar Ditolak</h2>
            <p>Hai <strong>%s</strong>,</p>
            <p>Bukti bayar kamu dengan reference <code>%s</code> ditolak.</p>
            <p><strong>Alasan:</strong></p>
            <blockquote style="border-left: 3px solid #ef4444; padding-left: 12px;
                       color: #64748b;">%s</blockquote>
            <p>Silakan upload ulang bukti yang benar.</p>
            <p style="margin-top: 24px;">
              <a href="%s" style="display: inline-block; background: #3b82f6;
                 color: #fff; padding: 12px 24px; border-radius: 8px;
                 text-decoration: none; font-weight: 600;">Upload Ulang</a>
            </p>
            <p style="margin-top: 32px; color: #64748b; font-size: 13px;">
              Terima kasih,<br/>Portfolio Team
            </p>
          </div>
        </body>
        </html>
        """.formatted(username, referenceId, reason, retryUrl);
    }

    private String buildAdminPaymentNotificationHtml(
            String referenceId, String username, String methodLabel) {
        return """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background: #f6f8fb; padding: 24px;">
          <div style="max-width: 560px; margin: 0 auto; background: #fff;
                      border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb;">
            <h2 style="color: #3b82f6;">💳 Bukti Bayar Baru</h2>
            <p>Ada bukti bayar baru yang perlu diverifikasi:</p>
            <ul>
              <li><strong>Reference:</strong> %s</li>
              <li><strong>User:</strong> %s</li>
              <li><strong>Method:</strong> %s</li>
            </ul>
            <p>Segera cek dashboard admin untuk verifikasi.</p>
          </div>
        </body>
        </html>
        """.formatted(referenceId, username, methodLabel);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================
    private void sendEmail(String to, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
        log.debug("📧 Email sent: to={}, subject={}", to, subject);
    }

    private String loadTemplate(String templateName, Map<String, String> vars)
            throws IOException {
        ClassPathResource resource = new ClassPathResource("template/email/" + templateName);
        String html = StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );

        for (Map.Entry<String, String> entry : vars.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return html;
    }
}