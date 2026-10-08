package com.anjar.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.UUID;

@Slf4j
@Service
public class DomainVerificationService {

    /**
     * Target CNAME yang harus di-set user.
     * Contoh: app.platform.com
     */
    @Value("${app.custom-domain.cname-target:app.platform.com}")
    private String cnameTarget;

    /**
     * IP server kita (opsional, untuk verifikasi A record).
     */
    @Value("${app.custom-domain.server-ip:}")
    private String serverIp;

    // ============================================================
    // CNAME / A RECORD VERIFICATION
    // ============================================================

    /**
     * Cek apakah domain sudah menunjuk ke server kita (via CNAME atau A record).
     */
    public boolean verifyCname(String domain) {
        if (domain == null || domain.isBlank()) return false;

        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            for (InetAddress addr : addresses) {
                String ip = addr.getHostAddress();
                log.debug("🌐 DNS: {} → {}", domain, ip);

                if (serverIp != null && !serverIp.isBlank() && serverIp.equals(ip)) {
                    return true;
                }
            }

            // Fallback: cek CNAME record
            return verifyCnameRecord(domain);

        } catch (UnknownHostException e) {
            log.warn("⚠️ Domain tidak resolve: {}", domain);
            return false;
        }
    }

    private boolean verifyCnameRecord(String domain) {
        try {
            DirContext ctx = createDnsContext();

            // Cek CNAME
            Attributes attrs = ctx.getAttributes(domain, new String[]{"CNAME"});
            Attribute cname = attrs.get("CNAME");

            if (cname != null) {
                String value = cname.get().toString().toLowerCase();
                log.debug("🔗 CNAME: {} → {}", domain, value);

                if (value.contains(cnameTarget.toLowerCase())) {
                    return true;
                }
            }

            // Fallback: cek A record
            Attributes aAttrs = ctx.getAttributes(domain, new String[]{"A"});
            Attribute a = aAttrs.get("A");
            if (a != null) {
                String value = a.get().toString();
                log.debug("🔗 A: {} → {}", domain, value);

                if (serverIp != null && !serverIp.isBlank() && value.contains(serverIp)) {
                    return true;
                }
            }

            return false;

        } catch (NamingException e) {
            log.warn("⚠️ DNS lookup gagal untuk {}: {}", domain, e.getMessage());
            return false;
        }
    }

    // ============================================================
    // TXT RECORD VERIFICATION
    // ============================================================

    /**
     * Cek TXT record untuk token verifikasi.
     *
     * User harus set TXT record:
     *   Name:  _platform-verify.badru.com
     *   Value: platform-verify=abc123xyz
     */
    public boolean verifyTxtRecord(String domain, String token) {
        if (domain == null || domain.isBlank()) return false;
        if (token == null || token.isBlank()) return false;

        try {
            DirContext ctx = createDnsContext();

            String txtRecordName = "_platform-verify." + domain;
            Attributes attrs = ctx.getAttributes(txtRecordName, new String[]{"TXT"});
            Attribute txt = attrs.get("TXT");

            if (txt != null) {
                String value = txt.get().toString();
                log.debug("📝 TXT: {} → {}", txtRecordName, value);

                String expected = "platform-verify=" + token;
                return value.contains(expected);
            }

            return false;

        } catch (NamingException e) {
            log.warn("⚠️ TXT lookup gagal untuk {}: {}", domain, e.getMessage());
            return false;
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private DirContext createDnsContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "5000");
        env.put("com.sun.jndi.dns.timeout.retries", "2");
        return new InitialDirContext(env);
    }

    /**
     * Generate token verifikasi random.
     */
    public String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCnameTarget() {
        return cnameTarget;
    }

    public String getTxtRecordName(String domain) {
        return "_platform-verify." + domain;
    }

    public String getTxtRecordValue(String token) {
        return token != null ? "platform-verify=" + token : "";
    }
}